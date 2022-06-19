package mod.chiselsandbits.item;

import com.google.common.base.Suppliers;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.chiseled.IChiseledBlockItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.placement.PlacementResult;
import mod.chiselsandbits.api.util.BlockStateUtils;
import mod.chiselsandbits.api.util.HelpTextUtils;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.multistate.snapshot.SimpleSnapshot;
import mod.chiselsandbits.platforms.core.util.constants.NbtConstants;
import mod.chiselsandbits.registrars.ModModificationOperation;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class ChiseledBlockItem extends BlockItem implements IChiseledBlockItem
{

    private static final Supplier<ItemStack> DEFAULT_INSTANCE = Suppliers.memoize(() -> {
        final Random random = new Random();

        final int blockStateCount = (StateEntrySize.current().getBitsPerBlockSide() / 4) *
                                      (StateEntrySize.current().getBitsPerBlockSide() / 4) *
                                      (StateEntrySize.current().getBitsPerBlockSide() / 4);
        final List<BlockInformation> blockInformation = new ArrayList<>(blockStateCount);
        for (int i = 0; i < blockStateCount; i++)
        {
            blockInformation.add(BlockStateUtils.getRandomSupportedInformation(random));
        }

        final SimpleSnapshot results = new SimpleSnapshot(BlockInformation.AIR);

        results.mutableStream()
          .forEach(stateEntryInfo -> {
              final Vec3 pos = stateEntryInfo.getStartPoint()
                                 .multiply(StateEntrySize.current().getBitsPerBlockSideScalingVector());

              final Vec3 indexPos = pos.multiply(1 / 4d, 1/4d, 1/4d);
              final Vec3i index = new Vec3i(indexPos.x(), indexPos.y(), indexPos.z());

              final int size = StateEntrySize.current().getBitsPerBlockSide() / 4;
              final int blockInformationIndex = index.getX() + (index.getY() * size) + (index.getZ() * size * size);
              final BlockInformation info = blockInformation.get(blockInformationIndex);
              stateEntryInfo.overrideState(info);
          });

        final ItemStack stack = results.toItemStack().toBlockStack();
        stack.getOrCreateTag().putBoolean(NbtConstants.DEFAULT_INSTANCE_INDICATOR, true);
        return stack;
    });

    public ChiseledBlockItem(final Block blockIn, final Properties builder)
    {
        super(blockIn, builder);
    }

    @Override
    public @NotNull Component getName(final @NotNull ItemStack stack)
    {
        if (stack.getOrCreateTag().contains(NbtConstants.DEFAULT_INSTANCE_INDICATOR) &&
              stack.getOrCreateTag().getBoolean(NbtConstants.DEFAULT_INSTANCE_INDICATOR)) {
            return LocalStrings.DefaultChiseledBlockItemName.getText();
        }

        return super.getName(stack);
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
        return new SingleBlockMultiStateItemStack(stack);
    }

    @NotNull
    @Override
    public InteractionResult place(@NotNull final BlockPlaceContext context)
    {
        final IAreaAccessor source = this.createItemStack(context.getItemInHand());
        final IWorldAreaMutator areaMutator = context.getPlayer().isCrouching() ?
                                                IMutatorFactory.getInstance().covering(
                                                  context.getLevel(),
                                                  context.getClickLocation(),
                                                  context.getClickLocation().add(1d, 1d, 1d)
                                                )
                                                :
                                                  IMutatorFactory.getInstance().in(context.getLevel(), context.getClickedPos());
        final IMultiStateSnapshot attemptTarget = areaMutator.createSnapshot();

        final boolean noCollisions = source.stream().sequential()
          .allMatch(stateEntryInfo -> {
              try
              {
                  attemptTarget.setInAreaTarget(
                    stateEntryInfo.getBlockInformation(),
                    stateEntryInfo.getStartPoint());

                  return true;
              }
              catch (SpaceOccupiedException exception)
              {
                  return false;
              }
          });

        if (noCollisions)
        {
            try (IBatchMutation ignored = areaMutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(context.getPlayer())))
            {
                source.stream().forEach(
                  stateEntryInfo -> {
                      try
                      {
                          areaMutator.setInAreaTarget(
                            stateEntryInfo.getBlockInformation(),
                            stateEntryInfo.getStartPoint());
                      }
                      catch (SpaceOccupiedException ignored1)
                      {
                      }
                  }
                );
            }


            if (context.getPlayer() == null || !context.getPlayer().isCreative()) {
                context.getItemInHand().shrink(1);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final Level worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        HelpTextUtils.build(LocalStrings.HelpBitBag, tooltip);
    }

    @Override
    public PlacementResult getPlacementResult(final ItemStack heldStack, final Player playerEntity, final BlockHitResult blockRayTraceResult)
    {
        final IAreaAccessor source = this.createItemStack(heldStack);
        final Vec3 target = getTargetedPosition(heldStack, playerEntity, blockRayTraceResult);
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().covering(
          playerEntity.level,
          target,
          target.add(1,1,1));
        final IMultiStateSnapshot attemptTarget = areaMutator.createSnapshot();

        final boolean noSpaceOccupied = source.stream()
                .allMatch(stateEntryInfo -> {
                    try {
                        attemptTarget.setInAreaTarget(
                                stateEntryInfo.getBlockInformation(),
                                stateEntryInfo.getStartPoint());

                        return true;
                    } catch (SpaceOccupiedException exception) {
                        return false;
                    }
                });
        return noSpaceOccupied
                ? PlacementResult.success()
                : PlacementResult.failure(
                        IClientConfiguration::getNotFittingPatternPlacementColor,
                        LocalStrings.PatternPlacementNotAnAirBlock.getText());
    }

    @Override
    public @NotNull ItemStack getDefaultInstance()
    {
        return DEFAULT_INSTANCE.get();
    }

    @Override
    public @NotNull IModificationOperation getMode(final ItemStack stack)
    {
        return ModModificationOperation.ROTATE_AROUND_X.get();
    }

    @Override
    public void setMode(final ItemStack stack, final IModificationOperation mode)
    {
        final IMultiStateItemStack multiStateItemStack = this.createItemStack(stack);
        mode.apply(multiStateItemStack);
    }

    @Override
    public @NotNull Collection<IModificationOperation> getPossibleModes()
    {
        return ModModificationOperation.REGISTRY_SUPPLIER.get().getValues();
    }

    @Override
    public boolean requiresUpdateOnClosure()
    {
        return false;
    }
}
