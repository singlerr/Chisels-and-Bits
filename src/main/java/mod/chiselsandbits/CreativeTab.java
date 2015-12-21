package mod.chiselsandbits;

import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;

public class CreativeTab extends CreativeTabs
{

	public CreativeTab()
	{
		super( ChiselsAndBits.MODID );
	}

	@Override
	public Item getTabIconItem()
	{
		final ChiselsAndBits cb = ChiselsAndBits.instance;
		return ModUtil.firstNonNull(
				cb.itemChiselDiamond,
				cb.itemChiselGold,
				cb.itemChiselIron,
				cb.itemChiselStone,
				cb.itemBitBag,
				cb.itemPositiveprint,
				cb.itemNegativeprint,
				cb.itemWrench );
	}

}
