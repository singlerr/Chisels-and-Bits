package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.item.MagnifyingGlassItem;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientTickHandler
{

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void handleClientTickForMagnifyingGlass(final TickEvent.ClientTickEvent event)
    {
        if (Minecraft.getInstance().player != null)
            if (Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof MagnifyingGlassItem
                  || Minecraft.getInstance().player.getHeldItemOffhand().getItem() instanceof MagnifyingGlassItem)
                if (Minecraft.getInstance().ingameGUI != null)
                    Minecraft.getInstance().ingameGUI.remainingHighlightTicks = 40;
    }
}
