package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.item.withhighlight.IWithHighlightItem;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.render.ChiseledBlockWireframeRenderer;
import mod.chiselsandbits.client.render.MeasurementRenderer;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WorldRenderLastHandler
{

    @SubscribeEvent
    public static void renderCustomWorldHighlight(final RenderWorldLastEvent event)
    {
        final PlayerEntity playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;

        final ItemStack heldStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (heldStack.isEmpty())
            return;

        final Item heldItem = heldStack.getItem();
        if (!(heldItem instanceof IWithHighlightItem))
            return;

        final IWithHighlightItem withHighlightItem = (IWithHighlightItem) heldItem;
        if (withHighlightItem.shouldDrawDefaultHighlight(playerEntity))
            return;

        withHighlightItem.renderHighlight(
          playerEntity,
          event.getContext(),
          event.getMatrixStack(),
          event.getPartialTicks(),
          event.getProjectionMatrix(),
          event.getFinishTimeNano()
        );
    }

    @SubscribeEvent
    public static void renderMeasurements(final RenderWorldLastEvent event)
    {
        MeasurementRenderer.getInstance().renderMeasurements(event);
    }

    @SubscribeEvent
    public static void renderMultiStateBlockPreview(final RenderWorldLastEvent event)
    {
        final PlayerEntity playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;

        final ItemStack heldStack = ItemStackUtils.getMultiStateItemStackFromPlayer(playerEntity);
        if (heldStack.isEmpty())
            return;

        final Item heldItem = heldStack.getItem();
        if (!(heldItem instanceof IWireframeProvidingItem))
            return;

        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK)
            return;

        if (!(rayTraceResult instanceof BlockRayTraceResult))
            return;

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final IWireframeProvidingItem wireframeItem = (IWireframeProvidingItem) heldItem;

        final VoxelShape wireFrame = wireframeItem.getWireFrame(heldStack, playerEntity, blockRayTraceResult);
        final Vector3d color = wireframeItem.getWireFrameColor(heldStack, playerEntity, blockRayTraceResult);
        final BlockPos targetedRenderPos = wireframeItem.getTargetedBlockPos(heldStack, playerEntity, blockRayTraceResult);

        ChiseledBlockWireframeRenderer.getInstance().renderShape(
          event.getMatrixStack(),
          wireFrame,
          targetedRenderPos,
          color
        );
    }
}
