package mod.chiselsandbits.api.item.wireframe;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

/**
 * Represents an item which can provide a wireframe for different purposes,
 * including rendering a preview.
 */
public interface IWireframeProvidingItem
{
    /**
     * Provides access to the wire frame of the item.
     *
     * @param stack The stack to get the wire frame from.
     * @param player The player to get the wire frame for.
     * @param rayTraceResult The ray trace result of the current context.
     * @return The VoxelShape for the wire frame.
     */
    VoxelShape getWireFrame(
      final ItemStack stack,
      final PlayerEntity player,
      final BlockRayTraceResult rayTraceResult
    );

    /**
     * The color to render the wireframe in.
     *
     * @param heldStack The stack to get the wire frame color for.
     * @param playerEntity The entity that is rendering the wire frame color.
     * @param blockRayTraceResult The block ray trace result for the current context.
     * @return An RGB (XYZ) Vector with the color.
     */
    Vector3d getWireFrameColor(
      ItemStack heldStack,
      PlayerEntity playerEntity,
      BlockRayTraceResult blockRayTraceResult
    );

    /**
     * Returns the position the wire frame should be rendered at.
     *
     * @param heldStack The stack to get the position for.
     * @param playerEntity The entity that is rendering the wire frame.
     * @param blockRayTraceResult The block ray trace result for the current context.
     * @return The position to render the wire frame.
     */
    Vector3d getTargetedBlockPos(
      ItemStack heldStack,
      PlayerEntity playerEntity,
      BlockRayTraceResult blockRayTraceResult
    );
}
