package mod.chiselsandbits.api.item.chiseled;

import com.mojang.math.Vector4f;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import static mod.chiselsandbits.api.util.ColorUtils.NOT_FITTING_PATTERN_PLACEMENT_COLOR;
import static mod.chiselsandbits.api.util.ColorUtils.SUCCESSFUL_PATTERN_PLACEMENT_COLOR;

/**
 * Represents items which represent a broken chiseled block.
 */
public interface IChiseledBlockItem extends IMultiStateItem, IWireframeProvidingItem, IWithModeItem<IModificationOperation>
{
    @Override
    default VoxelShape getWireFrame(
      final ItemStack stack, final Player player, final BlockHitResult rayTraceResult)
    {
        return IVoxelShapeManager.getInstance().get(
          createItemStack(stack),
          CollisionType.NONE_AIR
        );
    }

    @Override
    default Vector4f getWireFrameColor(ItemStack heldStack, Player playerEntity, BlockHitResult blockRayTraceResult)
    {
        return canPlace(heldStack, playerEntity, blockRayTraceResult) ?
                 SUCCESSFUL_PATTERN_PLACEMENT_COLOR :
                 NOT_FITTING_PATTERN_PLACEMENT_COLOR;
    }

    @Override
    default Vec3 getTargetedBlockPos(ItemStack heldStack, Player playerEntity, BlockHitResult blockRayTraceResult)
    {
        return !playerEntity.isCrouching() ?
                 Vec3.atLowerCornerOf(blockRayTraceResult.getBlockPos().offset(blockRayTraceResult.getDirection().getNormal()))
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
    boolean canPlace(ItemStack heldStack, Player playerEntity, BlockHitResult blockRayTraceResult);
}
