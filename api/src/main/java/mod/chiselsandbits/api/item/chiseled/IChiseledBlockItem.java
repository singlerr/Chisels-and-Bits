package mod.chiselsandbits.api.item.chiseled;

import com.mojang.math.Vector4f;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.placement.IPlacementPreviewProvidingItem;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.modification.operation.IModificationOperation;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Represents items which represent a broken chiseled block.
 */
public interface IChiseledBlockItem extends IMultiStateItem, IPlacementPreviewProvidingItem, IWithModeItem<IModificationOperation>
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
        return getPlacementResult(heldStack, playerEntity, blockRayTraceResult).getColor();
    }

    @Override
    default Vec3 getTargetedPosition(ItemStack heldStack, Player playerEntity, BlockHitResult blockRayTraceResult)
    {
        return !playerEntity.isCrouching() ?
                 Vec3.atLowerCornerOf(blockRayTraceResult.getBlockPos().offset(blockRayTraceResult.getDirection().getNormal()))
                 :
                   blockRayTraceResult.getLocation();
    }

    @Override
    default boolean overridesBits(ItemStack heldStack)
    {
        return false;
    }

    @Override
    default boolean ignoreDepth(ItemStack heldStack)
    {
        return overridesBits(heldStack);
    }
}
