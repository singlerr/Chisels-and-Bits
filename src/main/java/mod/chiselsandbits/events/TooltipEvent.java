package mod.chiselsandbits.events;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.core.ChiselsAndBits;
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

@Mod.EventBusSubscriber(modid = ChiselsAndBits.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TooltipEvent
{

    @SubscribeEvent
    public static void onItemTooltip(final ItemTooltipEvent event)
    {
        if (Minecraft.getInstance().player != null && ChiselsAndBits.getConfig().getCommon().enableHelp.get())
            if (Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof MagnifyingGlassItem
                  || Minecraft.getInstance().player.getHeldItemOffhand().getItem() instanceof MagnifyingGlassItem)
                if (event.getItemStack().getItem() instanceof BlockItem) {
                    final BlockItem blockItem = (BlockItem) event.getItemStack().getItem();
                    final Block block = blockItem.getBlock();
                    final BlockState blockState = block.getDefaultState();

                    final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(blockState);

                    event.getToolTip().add(
                        result.canBeChiseled() || result.isAlreadyChiseled() ?
                          result.getReason().mergeStyle(TextFormatting.GREEN) :
                          result.getReason().mergeStyle(TextFormatting.RED)
                    );
                }
    }
}
