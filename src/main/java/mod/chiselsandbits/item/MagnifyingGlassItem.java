package mod.chiselsandbits.item;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class MagnifyingGlassItem extends Item
{

    public MagnifyingGlassItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public Component getHighlightTip(final ItemStack item, final Component displayName)
    {
        if (Minecraft.getInstance().level == null)
        {
            return displayName;
        }

        if (Minecraft.getInstance().hitResult == null)
        {
            return displayName;
        }

        if (Minecraft.getInstance().hitResult.getType() != HitResult.Type.BLOCK)
        {
            return displayName;
        }

        final BlockHitResult rayTraceResult = (BlockHitResult) Minecraft.getInstance().hitResult;
        final BlockState state = Minecraft.getInstance().level.getBlockState(rayTraceResult.getBlockPos());

        final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(state);
        return result.isAlreadyChiseled() || result.canBeChiseled() ?
                 result.getReason().withStyle(ChatFormatting.GREEN) :
                 result.getReason().withStyle(ChatFormatting.RED);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(
      @NotNull final ItemStack stack,
      final Level worldIn,
      @NotNull final List<Component> tooltip,
      @NotNull final TooltipFlag advanced)
    {
        super.appendHoverText(stack, worldIn, tooltip, advanced);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpMagnifyingGlass, tooltip);
    }
}
