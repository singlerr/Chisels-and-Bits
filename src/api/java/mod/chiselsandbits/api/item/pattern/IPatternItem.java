package mod.chiselsandbits.api.item.pattern;

import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.pattern.placement.IPatternPlacementType;
import mod.chiselsandbits.api.pattern.placement.PlacementResult;
import mod.chiselsandbits.api.sealing.ISupportsSealing;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;

import static mod.chiselsandbits.api.util.ColorUtils.SUCCESSFUL_PATTERN_PLACEMENT_COLOR;

/**
 * Represents an item that can be a pattern
 */
public interface IPatternItem extends IMultiStateItem, ISupportsSealing, IWithModeItem<IPatternPlacementType>, IWireframeProvidingItem
{
    @Override
    default VoxelShape getWireFrame(
      final ItemStack stack, final PlayerEntity player, final BlockRayTraceResult rayTraceResult) {
        return getMode(stack).buildVoxelShapeForWireframe(
          createItemStack(stack).createSnapshot(),
          player,
          rayTraceResult.getLocation(),
          rayTraceResult.getDirection()
        );
    }

    @Override
    default Vector3d getWireFrameColor(ItemStack heldStack, PlayerEntity playerEntity, BlockRayTraceResult blockRayTraceResult) {
        final PlacementResult result = this.getMode(heldStack).performPlacement(
          createItemStack(heldStack).createSnapshot(),
          new BlockItemUseContext(
            playerEntity,
                playerEntity.getMainHandItem() == heldStack ?
                    Hand.MAIN_HAND : Hand.OFF_HAND,
            heldStack,
            blockRayTraceResult
            ),
          true
        );

        return result.isSuccess() ? SUCCESSFUL_PATTERN_PLACEMENT_COLOR : result.getFailureColor();
    }

    @Override
    default Vector3d getTargetedBlockPos(ItemStack heldStack, PlayerEntity playerEntity, BlockRayTraceResult blockRayTraceResult) {
        return getMode(heldStack).getTargetedPosition(heldStack, playerEntity, blockRayTraceResult);
    }
}
