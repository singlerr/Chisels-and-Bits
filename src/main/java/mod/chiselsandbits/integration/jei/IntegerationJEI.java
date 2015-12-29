package mod.chiselsandbits.integration.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mod.chiselsandbits.integration.IntegrationBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

public class IntegerationJEI extends IntegrationBase
{

	List<ItemStack> items = new ArrayList<ItemStack>();

	public List<ItemStack> getBlacklisted()
	{
		return Collections.unmodifiableList( items );
	}

	public void blackListItem(
			final Item b )
	{
		if ( b != null )
		{
			items.add( new ItemStack( b, 1, OreDictionary.WILDCARD_VALUE ) );
		}
	}

}
