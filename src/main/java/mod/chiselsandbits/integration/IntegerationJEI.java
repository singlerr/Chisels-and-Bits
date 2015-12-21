package mod.chiselsandbits.integration;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.IItemBlacklist;
import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

public class IntegerationJEI
{

	List<ItemStack> items = new ArrayList<ItemStack>();

	public void blackListItem(
			final Item b )
	{
		if ( b != null )
		{
			items.add( new ItemStack( b, 1, OreDictionary.WILDCARD_VALUE ) );
		}
	}

	public void init()
	{
		if ( Loader.isModLoaded( "JEI" ) && !ChiselsAndBits.instance.config.ShowBitsInJEI )
		{
			sendtoJEI();
		}

		items = null;
	}

	private void sendtoJEI()
	{
		final IItemBlacklist blacklist = mezz.jei.api.JEIManager.itemBlacklist;
		for ( final ItemStack is : items )
		{
			blacklist.addItemToBlacklist( is );
		}
	}

}
