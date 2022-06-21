package mod.chiselsandbits.api.pattern.placement;

import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistryEntry;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Presents a way placing a pattern in the world.
 */
public interface IPatternPlacementType extends IChiselsAndBitsRegistryEntry, IToolMode<IToolModeGroup>
{

    /**
     * Used to build a voxelshape for rendering in the preview.
     *
     * @param sourceSnapshot The snapshot stored in the pattern for placement.
     * @param player The player performing the action.
     * @param targetedPoint The point targeted by the player.
     * @param hitFace The face hit by the player.
     * @return The VoxelShape used to render the wireframe.
     */
    VoxelShape buildVoxelShapeForWireframe(final IMultiStateSnapshot sourceSnapshot,
      final Player player,
      final Vec3 targetedPoint,
      final Direction hitFace);

    /**
     * Invoked to perform the actual placement of the pattern in the world.
     *
     * @param source The snapshot stored in the pattern for placement.
     * @param context The use context of the pattern item on a block.
     * @param simulate Indicates if the placement operation is a simulation or not.
     *
     * @return The action result which influences the further processing of the click interaction.
     */
    PlacementResult performPlacement(
      IMultiStateSnapshot source,
      BlockPlaceContext context,
      final boolean simulate);

    /**
     * Invoked to determine where the targeted position of the placement type is.
     *
     * @param heldStack The stack that the player is holding.
     * @param player The player which is holding the itemstack.
     * @param blockRayTraceResult The block ray trace result in the current context.
     * @return The position of the potential placement.
     */
    Vec3 getTargetedPosition(ItemStack heldStack, Player player, BlockHitResult blockRayTraceResult);

    /**
     * Invoked to determine whether this mode modifies existing blocks, or only edits air blocks.
     */
    default boolean editsBlocks()
    {
        return true;
    }
}
