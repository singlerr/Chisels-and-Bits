package mod.chiselsandbits.api.pattern.placement;

import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.snapshot.IMultiStateSnapshot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.registries.IForgeRegistryEntry;

/**
 * Presents a way placing a pattern in the world.
 */
public interface IPatternPlacementType extends IForgeRegistryEntry<IPatternPlacementType>, IToolMode<IToolModeGroup>
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
      final PlayerEntity player,
      final Vector3d targetedPoint,
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
      BlockItemUseContext context,
      final boolean simulate);

    /**
     * Invoked to determine where the targeted position of the placement type is.
     *
     * @param heldStack The stack that the player is holding.
     * @param playerEntity The player which is holding the itemstack.
     * @param blockRayTraceResult The block ray trace result in the current context.
     * @return The position of the potential placement.
     */
    Vector3d getTargetedPosition(ItemStack heldStack, PlayerEntity playerEntity, BlockRayTraceResult blockRayTraceResult);
}
