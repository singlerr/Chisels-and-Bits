package mod.chiselsandbits.registry;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

class ModCreativeTab extends CreativeTabs
{

	public ModCreativeTab()
	{
		super( ChiselsAndBits.MODID );
	}

	@Override
	public Item getTabIconItem()
	{
		final ModItems cbitems = ChiselsAndBits.instance.items;
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
