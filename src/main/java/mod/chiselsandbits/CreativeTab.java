
package mod.chiselsandbits;

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
		return ChiselsAndBits.instance.itemChiselDiamond;
	}

}
