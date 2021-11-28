package mod.chiselsandbits.forge.platform.item;

import mod.chiselsandbits.platforms.core.item.IDyeItemHelper;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;

import java.util.Optional;

public class DyeItemHelper implements IDyeItemHelper
{
    private static final DyeItemHelper INSTANCE = new DyeItemHelper();

    public static DyeItemHelper getInstance()
    {
        return INSTANCE;
    }

    private DyeItemHelper()
    {
    }


    @Override
    public Optional<DyeColor> getColorFromItem(final ItemStack stack)
    {
        if (Tags.Items.DYES_WHITE.contains(stack.getItem()))
            return Optional.of(DyeColor.WHITE);
        if (Tags.Items.DYES_ORANGE.contains(stack.getItem()))
            return Optional.of(DyeColor.ORANGE);
        if (Tags.Items.DYES_MAGENTA.contains(stack.getItem()))
            return Optional.of(DyeColor.MAGENTA);
        if (Tags.Items.DYES_LIGHT_BLUE.contains(stack.getItem()))
            return Optional.of(DyeColor.LIGHT_BLUE);
        if (Tags.Items.DYES_YELLOW.contains(stack.getItem()))
            return Optional.of(DyeColor.YELLOW);
        if (Tags.Items.DYES_LIME.contains(stack.getItem()))
            return Optional.of(DyeColor.LIME);
        if (Tags.Items.DYES_PINK.contains(stack.getItem()))
            return Optional.of(DyeColor.PINK);
        if (Tags.Items.DYES_GRAY.contains(stack.getItem()))
            return Optional.of(DyeColor.GRAY);
        if (Tags.Items.DYES_LIGHT_GRAY.contains(stack.getItem()))
            return Optional.of(DyeColor.LIGHT_GRAY);
        if (Tags.Items.DYES_CYAN.contains(stack.getItem()))
            return Optional.of(DyeColor.CYAN);
        if (Tags.Items.DYES_PURPLE.contains(stack.getItem()))
            return Optional.of(DyeColor.PURPLE);
        if (Tags.Items.DYES_BLUE.contains(stack.getItem()))
            return Optional.of(DyeColor.BLUE);
        if (Tags.Items.DYES_BROWN.contains(stack.getItem()))
            return Optional.of(DyeColor.BROWN);
        if (Tags.Items.DYES_GREEN.contains(stack.getItem()))
            return Optional.of(DyeColor.GREEN);
        if (Tags.Items.DYES_RED.contains(stack.getItem()))
            return Optional.of(DyeColor.RED);
        if (Tags.Items.DYES_BLACK.contains(stack.getItem()))
            return Optional.of(DyeColor.BLACK);
        
        return Optional.empty();
    }
}
