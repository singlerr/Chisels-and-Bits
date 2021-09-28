package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.chiseled.IChiseledBlockItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.NbtConstants;
import mod.chiselsandbits.item.multistate.SingleBlockMultiStateItemStack;
import mod.chiselsandbits.legacy.LegacyLoadManager;
import mod.chiselsandbits.utils.MultiStateSnapshotUtils;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import net.minecraft.item.Item.Properties;

public class ChiseledBlockItem extends BlockItem implements IChiseledBlockItem
{

    public ChiseledBlockItem(final Block blockIn, final Properties builder)
    {
        super(blockIn, builder);
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
        if (stack.getOrCreateTag().contains(NbtConstants.BLOCK_ENTITY_DATA)){
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

    @NotNull
    @Override
    public ActionResultType place(@NotNull final BlockItemUseContext context)
    {
        final IAreaAccessor source = this.createItemStack(context.getItemInHand());
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(context.getLevel(), context.getClickedPos());
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

        if (noCollisions) {
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

            return ActionResultType.SUCCESS;
        }

        return ActionResultType.FAIL;
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
        final IWorldAreaMutator areaMutator = IMutatorFactory.getInstance().in(playerEntity.level, getTargetedBlockPos(
          heldStack, playerEntity, blockRayTraceResult));
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
}
