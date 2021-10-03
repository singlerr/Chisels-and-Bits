package mod.chiselsandbits.api.block.bitbag;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;

/**
 * Represents a block which can accept a bit bags interaction.
 */
public interface IBitBagAcceptingBlock
{
    /**
     * Invoked when a bit bag interaction is performed against a given block.
     * @param bitBagStack The bit bag stack.
     * @param player The player performing the action.
     * @param blockRayTraceResult The ray trace result.
     */
    void onBitBagInteraction(
      final ItemStack bitBagStack,
      final PlayerEntity player,
      final BlockRayTraceResult blockRayTraceResult
    );
}
