package mod.chiselsandbits.api.chiseling;

import mod.chiselsandbits.api.IChiselAndBitsAPI;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.util.IItemProvider;

import java.util.Optional;

/**
 * Manages converting none chiseled objects into chiseled variants and back.
 */
public interface IConversionManager
{
    /**
     * The instance of the manager.
     * @return The manager.
     */
    static IConversionManager getInstance() {
        return IChiselAndBitsAPI.getInstance().getConversionManager();
    }

    default Optional<Block> getChiseledVariantOf(final BlockState blockState) {
        return getChiseledVariantOf(blockState.getBlock());
    }

    Optional<Block> getChiseledVariantOf(final Block block);

    default Optional<Block> getChiseledVariantOf(final IItemProvider provider) {
        final Item targetItem = provider.asItem();
        if (targetItem instanceof BlockItem)
            return getChiseledVariantOf(((BlockItem) targetItem).getBlock());

        return Optional.empty();
    }
}
