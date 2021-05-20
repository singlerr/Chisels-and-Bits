package mod.chiselsandbits.client.colors;

import mod.chiselsandbits.item.BitBagItem;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class BitBagItemColor implements IItemColor
{
    @Override
    public int getColor(final @NotNull ItemStack stack, final int vertexColor)
    {
        if ( vertexColor == 1 )
        {
            DyeColor color = BitBagItem.getDyedColor( stack );
            if ( color != null )
                return color.getColorValue();
        }

        return -1;
    }
}
