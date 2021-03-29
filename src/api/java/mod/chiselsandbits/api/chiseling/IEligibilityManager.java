package mod.chiselsandbits.api.chiseling;

import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IItemProvider;
import org.jetbrains.annotations.NotNull;

/**
 * An object which can manage the eligibility of chiseling of blocks, blockstates,
 * or itemstacks (which contain blocks eligible) for chiseling.
 */
public interface IEligibilityManager
{

    static IEligibilityManager getInstance() {
        return IChiselAndBitsAPI.getInstance().getEligibilityManager();
    }

    default boolean canBeChiseled(@NotNull final BlockState state) {
        return this.canBeChiseled(state.getBlock());
    }

    boolean canBeChiseled(@NotNull final Block state);

    default boolean canBeChiseled(@NotNull final IItemProvider provider)
    {
        final Item target = provider.asItem();
        if (target instanceof BlockItem)
            return this.canBeChiseled(((BlockItem) target).getBlock());

        return false;
    }
}
