package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class ChiseledBlockItem extends BlockItem implements IMultiStateItem
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
            final LevelChunkSection legacyLoadedChunkSection = LegacyLoadManager.getInstance().attemptLegacyBlockEntityLoad(
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
    public InteractionResult place(@NotNull final BlockPlaceContext context)
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

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, @Nullable final Level worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpBitBag, tooltip);
    }
}
