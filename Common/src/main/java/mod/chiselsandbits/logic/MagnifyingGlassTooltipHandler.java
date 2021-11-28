package mod.chiselsandbits.logic;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.ICommonConfiguration;
import mod.chiselsandbits.item.MagnifyingGlassItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MagnifyingGlassTooltipHandler
{

    public static void onItemTooltip(final ItemStack itemStack, final List<Component> toolTips)
    {
        if (Minecraft.getInstance().player != null && ICommonConfiguration.getInstance().getEnableHelp().get())
            if (Minecraft.getInstance().player.getMainHandItem().getItem() instanceof MagnifyingGlassItem
                  || Minecraft.getInstance().player.getOffhandItem().getItem() instanceof MagnifyingGlassItem)
                if (itemStack.getItem() instanceof final BlockItem blockItem) {
                    final Block block = blockItem.getBlock();
                    final BlockState blockState = block.defaultBlockState();

                    final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(blockState);

                    toolTips.add(
                        result.canBeChiseled() || result.isAlreadyChiseled() ?
                          result.getReason().withStyle(ChatFormatting.GREEN) :
                          result.getReason().withStyle(ChatFormatting.RED)
                    );
                }
    }
}
