package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.item.withhighlight.IWithHighlightItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class WorldRenderLastHandler
{

    @SubscribeEvent
    public static void onRenderWorldLast(final RenderWorldLastEvent event)
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
}
