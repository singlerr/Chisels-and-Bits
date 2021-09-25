package mod.chiselsandbits.events;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.item.MagnifyingGlassItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent
{

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event)
    {
        if (Minecraft.getInstance().player != null && Configuration.getInstance().getCommon().enableHelp.get())
            if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof MagnifyingGlassItem
                  || Minecraft.getInstance().player.getOffhandItem().getItem() instanceof MagnifyingGlassItem)
                if (event.getItemStack().getItem() instanceof BlockItem) {
                    final BlockItem blockItem = (BlockItem) event.getItemStack().getItem();
                    final Block block = blockItem.getBlock();
                    final BlockState blockState = block.defaultBlockState();

                    final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(blockState);

                    event.getToolTip().add(
                        result.canBeChiseled() || result.isAlreadyChiseled() ?
                          result.getReason().withStyle(TextFormatting.GREEN) :
                          result.getReason().withStyle(TextFormatting.RED)
                    );
                }
    }
}
