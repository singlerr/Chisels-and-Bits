package mod.chiselsandbits.client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.ItemType;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeClipboardTab extends CreativeTabs
{
	static boolean renewMappings = true;
	static private List<ItemStack> myWorldItems = new ArrayList<ItemStack>();
	static private List<ItemStack> myCrossItems = new ArrayList<ItemStack>();
	static private ClipboardStorage clipStorage = null;

	public static void load(
			final File file )
	{
		clipStorage = new ClipboardStorage( file );
		myCrossItems = clipStorage.read();
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

			final ItemStack is = bitData.getBitsAsItem( null, ItemType.CHISLED_BLOCK, true );

			if ( is == null )
			{
				return;
			}

			// remove duplicates if they exist...
			for ( final ItemStack isa : myCrossItems )
			{
				if ( ItemStack.areItemStackTagsEqual( is, isa ) )
				{
					myCrossItems.remove( isa );
					break;
				}
			}

			// add item to front...
			myCrossItems.add( 0, is );

			// remove extra items from back..
			while ( myCrossItems.size() > ChiselsAndBits.getConfig().creativeClipboardSize && !myCrossItems.isEmpty() )
			{
				myCrossItems.remove( myCrossItems.size() - 1 );
			}

			clipStorage.write( myCrossItems );
			myWorldItems.clear();
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
			final List<ItemStack> itemList )
	{
		if ( renewMappings )
		{
			myWorldItems.clear();
			renewMappings = false;

			for ( final ItemStack is : myCrossItems )
			{
				final TileEntityBlockChiseled tebc = new TileEntityBlockChiseled();
				tebc.readChisleData( is.getSubCompound( "BlockEntityTag", true ) );

				// recalculate.
				tebc.setBlob( tebc.getBlob() );

				final IBlockState state = tebc.getBlockState( Blocks.stone );
				final Block blk = ChiselsAndBits.getBlocks().getConversion( state.getBlock() );

				if ( blk != null )
				{
					final ItemStack worldItem = tebc.getItemStack( null );

					if ( worldItem != null )
					{
						myWorldItems.add( worldItem );
					}
				}
			}
		}

		itemList.addAll( myWorldItems );
	}

	public static void clearMappings()
	{
		renewMappings = true;
	}

}
