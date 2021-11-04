package mod.chiselsandbits.item;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.exceptions.SealingNotSupportedException;
import mod.chiselsandbits.api.item.pattern.IMultiUsePatternItem;
import mod.chiselsandbits.api.pattern.placement.IPatternPlacementType;
import mod.chiselsandbits.api.sealing.ISupportsUnsealing;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
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
    protected InteractionResult determineSuccessResult(final BlockPlaceContext context, final ItemStack resultingStack)
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
        final IPatternPlacementType mode = getMode(stack);
        if (mode.getGroup().isPresent())
        {
            tooltip.add(LocalStrings.PatternItemTooltipModeGrouped.getText(mode.getGroup().get().getDisplayName(), mode.getDisplayName()));
        }
        else
        {
            tooltip.add(LocalStrings.PatternItemTooltipModeSimple.getText(mode.getDisplayName()));
        }

        if ((Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown())) {
            tooltip.add(new TextComponent("        "));
            tooltip.add(new TextComponent("        "));

            Configuration.getInstance().getCommon().helpText(LocalStrings.HelpSealedPattern, tooltip);
        }
    }

    @Override
    public @NotNull ItemStack unseal(@NotNull final ItemStack source) throws SealingNotSupportedException
    {
        if (source.getItem() instanceof ISupportsUnsealing)
        {
            final ItemStack seal = new ItemStack(ModItems.SINGLE_USE_PATTERN_ITEM.get());
            seal.setTag(source.getOrCreateTag().copy());
            return seal;
        }

        return source;
    }
}
