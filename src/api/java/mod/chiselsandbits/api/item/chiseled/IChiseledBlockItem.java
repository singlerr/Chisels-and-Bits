package mod.chiselsandbits.api.item.chiseled;

import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import static mod.chiselsandbits.api.util.ColorUtils.NOT_FITTING_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.ColorUtils.SUCCESSFUL_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.StateEntryPredicates.NOT_AIR;

/**
 * Represents items which represent a broken chiseled block.
 */
public interface IChiseledBlockItem extends IMultiStateItem, IWireframeProvidingItem
{
    @Override
    default VoxelShape getWireFrame(
      final ItemStack stack, final PlayerEntity player, final BlockRayTraceResult rayTraceResult)
    {
        return IVoxelShapeManager.getInstance().get(
          createItemStack(stack),
          accessor -> NOT_AIR
        );
    }

    @Override
    default Vector3d getWireFrameColor(ItemStack heldStack, PlayerEntity playerEntity, BlockRayTraceResult blockRayTraceResult)
    {
        return canPlace(heldStack, playerEntity, blockRayTraceResult) ?
                 SUCCESSFUL_PATTERN_PLACEMENT_COLOR :
                                                      NOT_FITTING_PATTERN_PLACEMENT_COLOR;
    }

    @Override
    default Vector3d getTargetedBlockPos(ItemStack heldStack, PlayerEntity playerEntity, BlockRayTraceResult blockRayTraceResult)
    {
        return !playerEntity.isCrouching() ?
                 Vector3d.atLowerCornerOf(blockRayTraceResult.getBlockPos().offset(blockRayTraceResult.getDirection().getNormal()))
                 :
                   blockRayTraceResult.getLocation();
    }

    /**
     * Indicates if the stacks block can be placed at the position targeted by the player.
     *
     * @param heldStack           The stack with the broken block.
     * @param playerEntity        The player in question.
     * @param blockRayTraceResult The block ray trace result for the player in the current context.
     * @return True when the block in the stack can be placed, false when not.
     */
    boolean canPlace(ItemStack heldStack, PlayerEntity playerEntity, BlockRayTraceResult blockRayTraceResult);
}
