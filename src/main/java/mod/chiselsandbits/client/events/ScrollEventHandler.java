package mod.chiselsandbits.client.events;

import com.google.common.collect.Lists;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.network.packets.HeldToolModeChangedPacket;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ScrollEventHandler
{

    @SubscribeEvent
    public static void onScroll(final InputEvent.MouseScrollEvent event) {
        if (Minecraft.getInstance().player == null || !Minecraft.getInstance().player.isCrouching())
            return;

        final ItemStack stack = ItemStackUtils.getModeItemStackFromPlayer(Minecraft.getInstance().player);
        if (stack.isEmpty())
            return;

        if (!(stack.getItem() instanceof IWithModeItem))
            return;

        final IWithModeItem<?> toolModeItem = (IWithModeItem<?>) stack.getItem();
        final List<?> modes = Lists.newArrayList(toolModeItem.getPossibleModes());
        int workingIndex = modes.indexOf(toolModeItem.getMode(stack));

        if (event.getScrollDelta() < 0) {
            workingIndex++;
        }
        else
        {
            workingIndex--;
        }

        if (workingIndex < 0)
            workingIndex = modes.size() + workingIndex;
        else if (workingIndex >= modes.size())
            workingIndex -= modes.size();

        ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new HeldToolModeChangedPacket(workingIndex));
        event.setCanceled(true);
    }
}
