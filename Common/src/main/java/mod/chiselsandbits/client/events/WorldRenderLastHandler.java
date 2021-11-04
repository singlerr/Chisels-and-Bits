package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.item.wireframe.IWireframeProvidingItem;
import mod.chiselsandbits.api.item.withhighlight.IWithHighlightItem;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.render.ChiseledBlockWireframeRenderer;
import mod.chiselsandbits.client.render.MeasurementRenderer;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
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
        final Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;

        final ItemStack heldStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (heldStack.isEmpty())
            return;

        final Item heldItem = heldStack.getItem();
        if (!(heldItem instanceof final IWithHighlightItem withHighlightItem))
            return;

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
          event.getMatrixStack(),
          wireFrame,
          targetedRenderPos,
          color
        );
    }
}
