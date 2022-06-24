package mod.chiselsandbits.client.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.client.render.SlotOverlayRenderManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class SlotOverlayRenderHandler
{
    public static void renderSlotOverlays(final PoseStack poseStack) {
        Player player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator())
            return;

        int centerOfScreen = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2;

        for(int slotIndex = 0; slotIndex < 9; ++slotIndex) {
            int xOffset = centerOfScreen - 90 + slotIndex * 20 + 2;
            int yOffSet = Minecraft.getInstance().getWindow().getGuiScaledHeight() - 16 - 3;

            SlotOverlayRenderManager.getInstance().renderSlot(xOffset, yOffSet, poseStack, player.getInventory().items.get(slotIndex));
        }
    }
}
