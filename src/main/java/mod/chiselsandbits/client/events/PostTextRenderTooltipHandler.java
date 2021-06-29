package mod.chiselsandbits.client.events;

import mod.chiselsandbits.api.item.pattern.IPatternItem;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PostTextRenderTooltipHandler
{
    @SubscribeEvent
    public static void onRenderTooltipPostText(final RenderTooltipEvent.PostText event)
    {
        if (!(event.getStack().getItem() instanceof IPatternItem))
            return;

        if (!Screen.hasShiftDown())
            return;

        final IPatternItem patternItem = (IPatternItem) event.getStack().getItem();
        final ItemStack renderTarget = patternItem.createItemStack(event.getStack()).toBlockStack();

        final float zLevel = Minecraft.getInstance().getItemRenderer().zLevel;
        Minecraft.getInstance().getItemRenderer().zLevel = 400;
        Minecraft.getInstance().getItemRenderer().renderItemAndEffectIntoGUI(renderTarget, event.getX() + 4, event.getY() + Minecraft.getInstance().fontRenderer.FONT_HEIGHT + 4);
        Minecraft.getInstance().getItemRenderer().zLevel = zLevel;
    }
}
