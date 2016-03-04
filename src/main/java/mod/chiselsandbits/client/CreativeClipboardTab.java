package mod.chiselsandbits.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeClipboardTab extends CreativeTabs
{
	static private List<ItemStack> myItems = new ArrayList<ItemStack>();
	static private ClipboardStorage clipStorage = null;

	public static void load(
			final File file )
	{
		clipStorage = new ClipboardStorage( file );
		myItems = clipStorage.read();
	}

	static public void addItem(
			final ItemStack iss )
	{
		// this is a client side things.
		if ( FMLCommonHandler.instance().getEffectiveSide().isClient() )
		{
			final IBitAccess bitData = ChiselsAndBits.getApi().createBitItem( iss );

			if ( bitData == null )
			{
				return;
			}

			final ItemStack is = bitData.getBitsAsItem( null, ItemType.CHISLED_BLOCK, false );

			if ( is == null )
			{
				return;
			}

			// remove duplicates if they exist...
			for ( final ItemStack isa : myItems )
			{
				if ( ItemStack.areItemStackTagsEqual( is, isa ) )
				{
					myItems.remove( isa );
					break;
				}
			}

			// add item to front...
			myItems.add( 0, is );

			// remove extra items from back..
			while ( myItems.size() > ChiselsAndBits.getConfig().creativeClipboardSize && !myItems.isEmpty() )
			{
				myItems.remove( myItems.size() - 1 );
			}

			clipStorage.write( myItems );
		}
	}

	public CreativeClipboardTab()
	{
		super( ChiselsAndBits.MODID + ".Clipboard" );
	}

	@Override
	public Item getTabIconItem()
	{
		final ModItems cbitems = ChiselsAndBits.getItems();
		return ModUtil.firstNonNull(
				cbitems.itemPositiveprint,
				cbitems.itemNegativeprint,
				cbitems.itemBitBag,
				cbitems.itemChiselDiamond,
				cbitems.itemChiselGold,
				cbitems.itemChiselIron,
				cbitems.itemChiselStone,
				cbitems.itemWrench );
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void displayAllReleventItems(
			final List<ItemStack> p_78018_1_ )
	{
		p_78018_1_.addAll( myItems );
	}

}
