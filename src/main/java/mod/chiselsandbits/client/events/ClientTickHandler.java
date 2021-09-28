package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.item.MagnifyingGlassItem;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
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
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().level != null)
        {
            if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof MagnifyingGlassItem
                  || Minecraft.getInstance().player.getOffhandItem().getItem() instanceof MagnifyingGlassItem)
            {
                if (Minecraft.getInstance().gui != null)
                {
                    Minecraft.getInstance().gui.toolHighlightTimer = 40;
                }
            }

            if (!ItemStackUtils.getPatternItemStackFromPlayer(Minecraft.getInstance().player).isEmpty()) {
                if (Minecraft.getInstance().gui != null && Minecraft.getInstance().gui.toolHighlightTimer == 0)
                {
                    final ItemStack stack = ItemStackUtils.getPatternItemStackFromPlayer(Minecraft.getInstance().player);
                    if (stack.getOrCreateTag().contains("highlight") && stack.getOrCreateTag().contains("highlightStartTime")) {
                        final long startTime = stack.getOrCreateTag().getLong("highlightStartTime");
                        if (Minecraft.getInstance().level.getGameTime() + 2 < startTime || startTime + 40 < Minecraft.getInstance().level.getGameTime())
                        {
                            stack.getOrCreateTag().remove("highlight");
                            stack.getOrCreateTag().remove("highlightStartTime");
                        }
                    }
                }
            }
        }
    }
}
