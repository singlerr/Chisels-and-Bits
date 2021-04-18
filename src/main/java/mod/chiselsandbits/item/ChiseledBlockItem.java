package mod.chiselsandbits.item;

import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IAreaMutator;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.item.multistate.ChiseledBlockMultiStateItemStack;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import org.jetbrains.annotations.NotNull;

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
        return new ChiseledBlockMultiStateItemStack(stack);
    }

    @NotNull
    @Override
    public ActionResultType tryPlace(@NotNull final BlockItemUseContext context)
    {
        final IAreaAccessor source = this.createItemStack(context.getItem());
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
}
