package mod.chiselsandbits.item;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SealingNotSupportedException;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.pattern.IMultiUsePatternItem;
import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.multistate.snapshot.EmptySnapshot;
import mod.chiselsandbits.network.packets.TileEntityUpdatedPacket;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SingleUsePatternItem extends Item implements IPatternItem
{

    public SingleUsePatternItem(final Properties builder)
    {
        super(builder);
    }

    /**
     * Creates an itemstack aware context wrapper that gives access to the multistate information contained within the given itemstack.
     *
     * @param stack The stack to get an {@link IMultiStateItemStack} for.
     * @return The {@link IMultiStateItemStack} that represents the data in the given itemstack.
     */
    @NotNull
    @Override
    public IMultiStateItemStack createItemStack(final ItemStack stack)
    {
        //Take care of an empty pattern.
        //Generally the case when this is a stack from the creative menu.
        if (stack.getOrCreateTag().isEmpty()) {
            return EmptySnapshot.Stack.INSTANCE;
        }

        return new SingleBlockMultiStateItemStack(stack);
    }

    @Override
    @NotNull
    public ActionResultType onItemUse(@NotNull ItemUseContext context) {
        final IMultiStateItemStack contents = createItemStack(context.getItem());
        if (contents.getStatistics().isEmpty()) {
            if (context.getPlayer() == null)
                return ActionResultType.FAIL;

            if (!context.getPlayer().isCreative())
                return ActionResultType.FAIL;

            if (!context.getPlayer().isCrouching())
                return ActionResultType.FAIL;

            final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(context.getWorld(), context.getPos());
            final ItemStack snapshotPatternStack = areaMutator.createSnapshot().toItemStack().toPatternStack();
            context.getItem().setTag(snapshotPatternStack.getOrCreateTag().copy());
            return ActionResultType.SUCCESS;
        }


        return this.tryPlace(new BlockItemUseContext(context));
    }

    @NotNull
    public ActionResultType tryPlace(@NotNull final BlockItemUseContext context)
    {
        if (context.getPlayer() == null)
            return ActionResultType.FAIL;

        final IAreaAccessor source = this.createItemStack(context.getItem());
        final IMultiStateSnapshot sourceSnapshot = source.createSnapshot();
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(context.getWorld(), context.getPos());
        final IMultiStateSnapshot attemptTarget = areaMutator.createSnapshot();

        final boolean noCollisions = source.stream().sequential()
                .allMatch(stateEntryInfo -> {
                    try
                    {
                        attemptTarget.setInAreaTarget(
                          stateEntryInfo.getState(),
                          stateEntryInfo.getStartPoint()
                        );

                        return true;
                    }
                    catch (SpaceOccupiedException exception)
                    {
                        return false;
                    }
                });

        final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(context.getPlayer());
        final boolean hasRequiredBits = context.getPlayer().isCreative() || sourceSnapshot.getStatics().getStateCounts().entrySet().stream()
          .allMatch(e -> playerBitInventory.canExtract(e.getKey(), e.getValue()));

        if (noCollisions && hasRequiredBits) {
            try (IBatchMutation ignored = areaMutator.batch()) {
                source.stream().sequential().forEach(
                  stateEntryInfo -> {
                      try
                      {
                          areaMutator.setInAreaTarget(
                            stateEntryInfo.getState(),
                            stateEntryInfo.getStartPoint()
                          );
                      }
                      catch (SpaceOccupiedException ignored1)
                      {
                      }
                  }
                );
            }

            if (!context.getPlayer().isCreative()) {
                sourceSnapshot.getStatics().getStateCounts().forEach(playerBitInventory::extract);
            }

            final TileEntity tileEntityCandidate = context.getWorld().getTileEntity(context.getPos());
            if (tileEntityCandidate instanceof IMultiStateBlockEntity) {
                IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntityCandidate;
                final Direction placementDirection = context.getPlayer() == null ? Direction.NORTH : context.getPlayer().getHorizontalFacing().getOpposite();
                final int horizontalIndex = placementDirection.getHorizontalIndex();

                int rotationCount = horizontalIndex - 4;
                if (rotationCount < 0) {
                    rotationCount += 4;
                }

                multiStateBlockEntity.rotate(Direction.Axis.Y, rotationCount);

                if (!context.getWorld().isRemote()) {
                    context.getWorld().notifyBlockUpdate(context.getPos(), Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), Constants.BlockFlags.DEFAULT);

                    ChiselsAndBits.getInstance().getNetworkChannel().sendToTrackingChunk(
                      new TileEntityUpdatedPacket(tileEntityCandidate),
                      context.getWorld().getChunkAt(context.getPos())
                    );
                }
            }

            return determineSuccessResult(context);
        }

        return ActionResultType.FAIL;
    }

    protected ActionResultType determineSuccessResult(final BlockItemUseContext context) {
        if (context.getPlayer() != null && context.getPlayer().isCreative())
        {
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.CONSUME;
    }

    @Override
    public @NotNull ItemStack seal(@NotNull final ItemStack source) throws SealingNotSupportedException
    {
        if (source.getItem() == this)
        {
            if (!(source.getItem() instanceof IMultiUsePatternItem))
            {
                final ItemStack seal = new ItemStack(ModItems.MULTI_USE_PATTERN_ITEM.get());
                seal.setTag(source.getOrCreateTag().copy());
                return seal;
            }

            throw new SealingNotSupportedException();
        }

        return source;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(
      final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        if ((Minecraft.getInstance().getMainWindow() != null && Screen.hasShiftDown())) {
            tooltip.add(new StringTextComponent("        "));
            tooltip.add(new StringTextComponent("        "));
        }

        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpSimplePattern, tooltip);
    }
}
