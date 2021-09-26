package mod.chiselsandbits.api.item.bit;

import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.item.click.IRightClickControllingItem;
import mod.chiselsandbits.api.item.withhighlight.IWithHighlightItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

/**
 * Represents an item which is a single bit.
 */
public interface IBitItem extends IRightClickControllingItem, IWithHighlightItem, IWithModeItem<IChiselMode>
{

    /**
     * Returns the blockstate which is contained in a stack with the
     * given bit item.
     *
     * @param stack The stack which contains this bit item.
     *
     * @return The blockstate contained in this bit item.
     */
    BlockState getBitState(final ItemStack stack);

    /**
     * Invoked when a merge operation of a bit inside a bitbag is beginning during a shift-click interaction
     * in the bit bag UI.
     */
    void onMergeOperationWithBagBeginning();

    /**
     * Invoked when a merge operation of a bit inside a bitbag is ending during a shift-click interaction
     * in the bit bag UI.
     */
    void onMergeOperationWithBagEnding();
}
