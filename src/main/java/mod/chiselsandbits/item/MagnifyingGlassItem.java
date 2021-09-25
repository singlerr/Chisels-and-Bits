package mod.chiselsandbits.item;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import net.minecraft.item.Item.Properties;

public class MagnifyingGlassItem extends Item
{

    public MagnifyingGlassItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextComponent getHighlightTip(final ItemStack item, final ITextComponent displayName)
    {
        if (Minecraft.getInstance().level == null)
        {
            return displayName;
        }

        if (Minecraft.getInstance().hitResult == null)
        {
            return displayName;
        }

        if (Minecraft.getInstance().hitResult.getType() != RayTraceResult.Type.BLOCK)
        {
            return displayName;
        }

        final BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) Minecraft.getInstance().hitResult;
        final BlockState state = Minecraft.getInstance().level.getBlockState(rayTraceResult.getBlockPos());

        final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(state);
        return result.isAlreadyChiseled() || result.canBeChiseled() ?
                 result.getReason().withStyle(TextFormatting.GREEN) :
                 result.getReason().withStyle(TextFormatting.RED);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(
      @NotNull final ItemStack stack,
      final World worldIn,
      @NotNull final List<ITextComponent> tooltip,
      @NotNull final ITooltipFlag advanced)
    {
        super.appendHoverText(stack, worldIn, tooltip, advanced);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpMagnifyingGlass, tooltip);
    }
}
