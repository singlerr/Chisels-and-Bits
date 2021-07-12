package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SealingNotSupportedException;
import mod.chiselsandbits.api.item.pattern.IMultiUsePatternItem;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MultiUsePatternItem extends SingleUsePatternItem implements IMultiUsePatternItem
{
    public MultiUsePatternItem(final Properties builder)
    {
        super(builder);
    }

    @Override
    protected ActionResultType determineSuccessResult(final BlockItemUseContext context)
    {
        return ActionResultType.SUCCESS;
    }

    @Override
    public @NotNull ItemStack seal(final @NotNull ItemStack source) throws SealingNotSupportedException
    {
        throw new SealingNotSupportedException();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(
      final @NotNull ItemStack stack, final @Nullable World worldIn, final @NotNull List<ITextComponent> tooltip, final @NotNull ITooltipFlag flagIn)
    {
        if ((Minecraft.getInstance().getMainWindow() != null && Screen.hasShiftDown())) {
            tooltip.add(new StringTextComponent("        "));
            tooltip.add(new StringTextComponent("        "));
        }

        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpSealedPattern, tooltip);
    }
}
