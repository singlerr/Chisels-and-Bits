package mod.chiselsandbits.registry;

import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

class ModCreativeTab extends CreativeTabs
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
	public Item getTabIconItem()
	{
		final ModItems cbitems = ChiselsAndBits.getItems();
		return ModUtil.firstNonNull(
				cbitems.itemChiselDiamond,
				cbitems.itemChiselGold,
				cbitems.itemChiselIron,
				cbitems.itemChiselStone,
				cbitems.itemBitBag,
				cbitems.itemPositiveprint,
				cbitems.itemNegativeprint,
				cbitems.itemWrench );
	}

}
