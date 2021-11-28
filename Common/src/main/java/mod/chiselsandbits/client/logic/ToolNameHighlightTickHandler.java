package mod.chiselsandbits.client.logic;

import mod.chiselsandbits.item.MagnifyingGlassItem;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class ToolNameHighlightTickHandler
{

    public static void handleClientTickForMagnifyingGlass()
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
