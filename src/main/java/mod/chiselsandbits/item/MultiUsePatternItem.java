package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SealingNotSupportedException;
import mod.chiselsandbits.api.item.pattern.IMultiUsePatternItem;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class MultiUsePatternItem extends SingleUsePatternItem implements IMultiUsePatternItem
{
    public MultiUsePatternItem(final Properties builder)
    {
        super(builder);
    }

    @Override
    protected InteractionResult determineSuccessResult(final BlockPlaceContext context)
    {
        return InteractionResult.SUCCESS;
    }

    @Override
    public @NotNull ItemStack seal(final @NotNull ItemStack source) throws SealingNotSupportedException
    {
        throw new SealingNotSupportedException();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(
      final @NotNull ItemStack stack, final @Nullable Level worldIn, final @NotNull List<Component> tooltip, final @NotNull TooltipFlag flagIn)
    {
        if ((Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown())) {
            tooltip.add(new TextComponent("        "));
            tooltip.add(new TextComponent("        "));
        }

        Configuration.getInstance().getCommon().helpText(LocalStrings.HelpSealedPattern, tooltip);
    }
}
