package mod.chiselsandbits.client.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.item.chiseled.IChiseledBlockItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.client.render.ChiseledBlockGhostRenderer;
import mod.chiselsandbits.client.render.ChiseledBlockWireframeRenderer;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.ONE_THOUSANDS;

public class MultiStateBlockPreviewRenderHandler
{

    public static void renderMultiStateBlockPreview(final PoseStack poseStack)
    {
        final HitResult rayTraceResult = Minecraft.getInstance().hitResult;
        if (!(rayTraceResult instanceof final BlockHitResult blockRayTraceResult) || blockRayTraceResult.getType() == HitResult.Type.MISS)
            return;

        final Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;

        final ItemStack heldStack = ItemStackUtils.getMultiStateItemStackFromPlayer(playerEntity);
        if (!(heldStack.getItem() instanceof IWireframeProvidingItem frameProvider))
            return;

        Vec3 targetedRenderPos = frameProvider.getTargetedBlockPos(heldStack, playerEntity, blockRayTraceResult).add(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS);
        // Snap to bit grid
        final float bitSize = StateEntrySize.current().getSizePerBit();
        targetedRenderPos = targetedRenderPos.subtract(
                targetedRenderPos.x % bitSize + (targetedRenderPos.x < 0 ? bitSize : 0),
                targetedRenderPos.y % bitSize + (targetedRenderPos.y < 0 ? bitSize : 0),
                targetedRenderPos.z % bitSize + (targetedRenderPos.z < 0 ? bitSize : 0)
        );
        if (false)
            renderWireFrame(poseStack, playerEntity, heldStack, frameProvider, blockRayTraceResult, targetedRenderPos);
        else
            renderGhost(poseStack, playerEntity, heldStack, blockRayTraceResult, targetedRenderPos);
    }

    private static void renderWireFrame(final PoseStack poseStack, final Player playerEntity, final ItemStack heldStack, final IWireframeProvidingItem wireframeItem, final BlockHitResult blockRayTraceResult, final Vec3 targetedRenderPos)
    {
        final VoxelShape wireFrame = wireframeItem.getWireFrame(heldStack, playerEntity, blockRayTraceResult);
        final Vec3 color = wireframeItem.getWireFrameColor(heldStack, playerEntity, blockRayTraceResult);

        ChiseledBlockWireframeRenderer.getInstance().renderShape(
                poseStack,
                wireFrame,
                targetedRenderPos,
                color
        );
    }

    private static void renderGhost(final PoseStack poseStack, final Player playerEntity, final ItemStack heldStack, final BlockHitResult blockRayTraceResult, final Vec3 targetedRenderPos)
    {
        final boolean canPlace;
        final boolean isPattern;
        final ItemStack renderStack;
        if (heldStack.getItem() instanceof IPatternItem patternItem)
        {
            final IMultiStateItemStack multiSate = patternItem.createItemStack(heldStack);
            final BlockPlaceContext context = new BlockPlaceContext(
              playerEntity,
              playerEntity.getMainHandItem() == heldStack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
              heldStack,
              blockRayTraceResult
            );
            canPlace = patternItem.getMode(heldStack).performPlacement(multiSate.createSnapshot(), context, true).isSuccess();
            isPattern = true;
            renderStack = multiSate.toBlockStack();
            if (!(renderStack.getItem() instanceof IChiseledBlockItem))
                return;
        }
        else if (heldStack.getItem() instanceof final IChiseledBlockItem chiseledBlockItem)
        {
            canPlace = chiseledBlockItem.canPlace(heldStack, playerEntity, blockRayTraceResult);
            isPattern = false;
            renderStack = heldStack;
        }
        else
            return;

        ChiseledBlockGhostRenderer.getInstance().renderGhost(
          poseStack,
          renderStack,
          targetedRenderPos,
          isPattern,
          canPlace
        );
    }
}
