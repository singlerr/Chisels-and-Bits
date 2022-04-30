package mod.chiselsandbits.client.logic;

import mod.chiselsandbits.api.item.pattern.IPatternItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

public class PatternContentInTooltipHandler
{
    public static void doRenderContent(final ItemStack stack, final int mouseX, final int mouseY)
    {
        if (!(stack.getItem() instanceof final IPatternItem patternItem))
            return;

        if (!(Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown()))
            return;

        final ItemStack renderTarget = patternItem.createItemStack(stack).toBlockStack();

        final float zLevel = Minecraft.getInstance().getItemRenderer().blitOffset;
        Minecraft.getInstance().getItemRenderer().blitOffset = 400;
        Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(renderTarget, mouseX + 4, mouseY + Minecraft.getInstance().font.lineHeight * 2 + 4);
        Minecraft.getInstance().getItemRenderer().blitOffset = zLevel;
    }
}
