package mod.chiselsandbits.items;

import java.util.List;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemBitSaw extends Item
{

	public ItemBitSaw()
	{
		setMaxStackSize( 1 );
		setMaxDamage( ChiselsAndBits.getConfig().diamondSawUses );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpBitSaw, tooltip );
	}

	@Override
	public ItemStack getContainerItem(
			final ItemStack itemStack )
	{
		itemStack.setItemDamage( itemStack.getItemDamage() + 1 );
		return itemStack;
	}

	@Override
	public boolean hasContainerItem()
	{
		return true;
	}

}
