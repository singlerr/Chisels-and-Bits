package mod.chiselsandbits.events;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.items.ItemMagnifyingGlass;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.item.BlockItem;
import net.minecraft.util.text.StringTextComponent;
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
            if (Minecraft.getInstance().player.getHeldItemMainhand().getItem() instanceof ItemMagnifyingGlass || Minecraft.getInstance().player.getHeldItemOffhand().getItem() instanceof ItemMagnifyingGlass)
                if (event.getItemStack().getItem() instanceof BlockItem) {
                    final BlockItem blockItem = (BlockItem) event.getItemStack().getItem();
                    final Block block = blockItem.getBlock();
                    final BlockState blockState = block.getDefaultState();
                    final BlockBitInfo.SupportsAnalysisResult result = BlockBitInfo.doSupportAnalysis(blockState);

                    event.getToolTip().add(
                        new StringTextComponent(
                          result.isSupported() ?
                            TextFormatting.GREEN + result.getSupportedReason().getLocal() + TextFormatting.RESET :
                                                                                                                   TextFormatting.RED + result.getUnsupportedReason().getLocal() + TextFormatting.RESET
                        )
                    );
                }
    }
}
