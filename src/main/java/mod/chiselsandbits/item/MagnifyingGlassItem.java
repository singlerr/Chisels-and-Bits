package mod.chiselsandbits.item;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.core.ChiselsAndBits;
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

public class MagnifyingGlassItem extends Item
{

    public MagnifyingGlassItem(Properties properties)
    {
        super(properties.maxStackSize(1));
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ITextComponent getHighlightTip(final ItemStack item, final ITextComponent displayName)
    {
        if (Minecraft.getInstance().world == null)
        {
            return displayName;
        }

        if (Minecraft.getInstance().objectMouseOver == null)
        {
            return displayName;
        }

        if (Minecraft.getInstance().objectMouseOver.getType() != RayTraceResult.Type.BLOCK)
        {
            return displayName;
        }

        final BlockRayTraceResult rayTraceResult = (BlockRayTraceResult) Minecraft.getInstance().objectMouseOver;
        final BlockState state = Minecraft.getInstance().world.getBlockState(rayTraceResult.getPos());

        final IEligibilityAnalysisResult result = IEligibilityManager.getInstance().analyse(state);
        return result.isAlreadyChiseled() || result.canBeChiseled() ?
                 result.getReason().mergeStyle(TextFormatting.GREEN) :
                 result.getReason().mergeStyle(TextFormatting.RED);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void addInformation(
      @NotNull final ItemStack stack,
      final World worldIn,
      @NotNull final List<ITextComponent> tooltip,
      @NotNull final ITooltipFlag advanced)
    {
        super.addInformation(stack, worldIn, tooltip, advanced);
        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpMagnifyingGlass, tooltip);
    }
}
