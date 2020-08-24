package mod.chiselsandbits.client;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModItemGroup extends ItemGroup
{

	public ModItemGroup()
	{
		super( ChiselsAndBits.MODID );
		setBackgroundImageName( "item_search.png" );
	}

	@Override
	public boolean hasSearchBar()
	{
		return true;
	}

	@Override
	public ItemStack createIcon()
	{
		return new ItemStack(ModItems.ITEM_BIT_BAG_DEFAULT.get());
	}

}
