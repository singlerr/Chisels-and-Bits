package mod.chiselsandbits.items;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class ItemBitSaw extends Item
{

	public ItemBitSaw(Item.Properties properties)
	{
        super(properties.maxStackSize(1).maxDamage(ChiselsAndBits.getConfig().getServer().damageTools.get() ? (int) Math.max( 0, Math.min( Short.MAX_VALUE, ChiselsAndBits.getConfig().getServer().diamondSawUses.get() ) ) : 0));
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<ITextComponent> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().getCommon().helpText( LocalStrings.HelpBitSaw, tooltip );
	}

	@Override
	public ItemStack getContainerItem(
			final ItemStack itemStack )
	{
		if ( ChiselsAndBits.getConfig().getServer().damageTools.get() )
		{
			itemStack.setDamage( itemStack.getDamage() + 1 );
			if (itemStack.getDamage() == itemStack.getMaxDamage())
            {
                return ItemStack.EMPTY;
            }
		}

		return itemStack.copy();
	}

	@Override
	public boolean hasContainerItem()
	{
		return true;
	}

}
