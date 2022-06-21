package mod.chiselsandbits.client.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector4f;
import mod.chiselsandbits.api.item.chiseled.IChiseledBlockItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.client.render.ChiseledBlockGhostRenderer;
import mod.chiselsandbits.client.render.ChiseledBlockWireframeRenderer;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
        if (!(heldStack.getItem() instanceof IWireframeProvidingItem wireframeItem))
            return;

        Vec3 targetedRenderPos = wireframeItem.getTargetedPosition(heldStack, playerEntity, blockRayTraceResult).add(ONE_THOUSANDS, ONE_THOUSANDS, ONE_THOUSANDS);
        // Snap to bit grid
        final float bitSize = StateEntrySize.current().getSizePerBit();
        targetedRenderPos = targetedRenderPos.subtract(
                targetedRenderPos.x % bitSize + (targetedRenderPos.x < 0 ? bitSize : 0),
                targetedRenderPos.y % bitSize + (targetedRenderPos.y < 0 ? bitSize : 0),
                targetedRenderPos.z % bitSize + (targetedRenderPos.z < 0 ? bitSize : 0)
        );
        final Vector4f color = wireframeItem.getWireFrameColor(heldStack, playerEntity, blockRayTraceResult);
        if (!renderGhost(poseStack, heldStack, wireframeItem, targetedRenderPos, color))
            renderWireFrame(poseStack, playerEntity, heldStack, wireframeItem, blockRayTraceResult, targetedRenderPos, color);
    }

    private static void renderWireFrame(final PoseStack poseStack, final Player playerEntity, final ItemStack heldStack, final IWireframeProvidingItem wireframeItem, final BlockHitResult blockRayTraceResult, final Vec3 targetedRenderPos, Vector4f color)
    {
        final VoxelShape wireFrame = wireframeItem.getWireFrame(heldStack, playerEntity, blockRayTraceResult);

        ChiseledBlockWireframeRenderer.getInstance().renderShape(
                poseStack,
                wireFrame,
                targetedRenderPos,
                color
        );
    }

    private static boolean renderGhost(final PoseStack poseStack, final ItemStack heldStack, IWireframeProvidingItem wireframeItem, final Vec3 targetedRenderPos, Vector4f color)
    {
        final Item item = heldStack.getItem();
        final boolean isPattern = item instanceof IPatternItem;
        final ItemStack renderStack;
        if (isPattern)
        {
            final IMultiStateItemStack multiSate = ((IPatternItem) item).createItemStack(heldStack);
            renderStack = multiSate.toBlockStack();
            if (renderStack.isEmpty())
                return false;
        }
        else if (item instanceof IChiseledBlockItem)
            renderStack = heldStack;
        else
            return false;

        ChiseledBlockGhostRenderer.getInstance().renderGhost(
          poseStack,
          renderStack,
          targetedRenderPos,
          wireframeItem.ignoreDepth(renderStack),
          color
        );
        return true;
    }
}
