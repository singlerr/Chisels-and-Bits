package mod.chiselsandbits.item;

import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.chiseled.IChiseledBlockItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.legacy.LegacyLoadManager;
import mod.chiselsandbits.registrars.ModModificationOperation;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class ChiseledBlockItem extends BlockItem implements IChiseledBlockItem
{

    public ChiseledBlockItem(final Block blockIn, final Properties builder)
    {
        super(blockIn, builder);
    }

    @NotNull
    @Override
    public ActionResultType place(@NotNull final BlockItemUseContext context)
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

        if (noCollisions)
        {
            try (IBatchMutation ignored = areaMutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(context.getPlayer())))
            {
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


            if (context.getPlayer() == null || !context.getPlayer().abilities.instabuild) {
                context.getItemInHand().shrink(1);
            }
            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
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
        if (stack.getOrCreateTag().contains(NbtConstants.BLOCK_ENTITY_DATA))
        {
            final ChunkSection legacyLoadedChunkSection = LegacyLoadManager.getInstance().attemptLegacyBlockEntityLoad(
              stack.getOrCreateTag().getCompound(NbtConstants.BLOCK_ENTITY_DATA)
            );

            final IMultiStateSnapshot snapshot = MultiStateSnapshotUtils.createFromSection(legacyLoadedChunkSection);
            final IMultiStateItemStack multiStateItemStack = snapshot.toItemStack();

            final ItemStack tempStack = multiStateItemStack.toBlockStack();
            stack.setTag(tempStack.getTag());
        }

        return new SingleBlockMultiStateItemStack(stack);
    }

    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpBitBag, tooltip);
    }

    @Override
    public boolean canPlace(final ItemStack heldStack, final PlayerEntity playerEntity, final BlockRayTraceResult blockRayTraceResult)
    {
        final IAreaAccessor source = this.createItemStack(heldStack);
        final Vector3d target = getTargetedBlockPos(heldStack, playerEntity, blockRayTraceResult);
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().covering(
          playerEntity.level,
          target,
          target.add(1,1,1));
        final IMultiStateSnapshot attemptTarget = areaMutator.createSnapshot();

        final boolean noCollision = source.stream()
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

        return noCollision;
    }

    @Override
    public @NotNull IModificationOperation getMode(final ItemStack stack)
    {
        return ModModificationOperation.ROTATE_AROUND_X.get();
    }

    @Override
    public void setMode(final ItemStack stack, final IModificationOperation mode)
    {
        if (mode == null)
            return;

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
