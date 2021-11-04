package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.item.withhighlight.IWithHighlightItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.DrawSelectionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DrawHighlightHandler
{

    @SubscribeEvent
    public static void onDrawHighlight(final DrawSelectionEvent.HighlightBlock event)
    {
        final Player playerEntity = Minecraft.getInstance().player;
        if (playerEntity == null)
            return;

        final ItemStack heldStack = ItemStackUtils.getHighlightItemStackFromPlayer(playerEntity);
        if (heldStack.isEmpty())
            return;

        final Item stackItem = heldStack.getItem();
        if (!(stackItem instanceof IWithHighlightItem))
            return;

        if (((IWithHighlightItem) stackItem).shouldDrawDefaultHighlight(playerEntity))
            return;

        event.setCanceled(true);
    }
}
