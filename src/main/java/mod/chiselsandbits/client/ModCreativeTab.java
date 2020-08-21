package mod.chiselsandbits.client;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public class ModCreativeTab extends ItemGroup
{

	public ModCreativeTab()
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
		final ModItems cbitems = ChiselsAndBits.getItems();
		return new ItemStack( ModUtil.firstNonNull(
				cbitems.itemChiselDiamond,
				cbitems.itemChiselGold,
				cbitems.itemChiselIron,
				cbitems.itemChiselStone,
				cbitems.itemBitBagDefault,
				cbitems.itemPositiveprint,
				cbitems.itemNegativePrint,
				cbitems.itemWrench ) );
	}

}
