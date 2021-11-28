package mod.chiselsandbits.client.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.util.RayTracingUtils;
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

public class MultiStateBlockPreviewRenderHandler
{

    public static void renderMultiStateBlockPreview(final PoseStack poseStack)
    {
        final Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;

        final ItemStack heldStack = ItemStackUtils.getMultiStateItemStackFromPlayer(playerEntity);
        if (heldStack.isEmpty())
            return;

        final Item heldItem = heldStack.getItem();
        if (!(heldItem instanceof final IWireframeProvidingItem wireframeItem))
            return;

        final HitResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != HitResult.Type.BLOCK)
            return;

        if (!(rayTraceResult instanceof final BlockHitResult blockRayTraceResult))
            return;

        final VoxelShape wireFrame = wireframeItem.getWireFrame(heldStack, playerEntity, blockRayTraceResult);
        final Vec3 color = wireframeItem.getWireFrameColor(heldStack, playerEntity, blockRayTraceResult);
        final Vec3 targetedRenderPos = wireframeItem.getTargetedBlockPos(heldStack, playerEntity, blockRayTraceResult);

        ChiseledBlockWireframeRenderer.getInstance().renderShape(
          poseStack,
          wireFrame,
          targetedRenderPos,
          color
        );
    }
}
