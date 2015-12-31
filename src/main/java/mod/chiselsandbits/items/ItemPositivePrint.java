package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ChiselInventory;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemPositivePrint extends ItemNegativePrint
{

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		defaultAddInfo( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.instance.config.helpText( LocalStrings.HelpPositivePrint, tooltip );

		if ( stack.hasTagCompound() )
		{
			if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
			{
				if ( cachedInfo != stack )
				{
					cachedInfo = stack;
					details.clear();

					final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
					tmp.readChisleData( stack.getTagCompound() );
					final VoxelBlob blob = tmp.getBlob();

					blob.listContents( details );
				}

				tooltip.addAll( details );
			}
			else
			{
				tooltip.add( LocalStrings.ShiftDetails.getLocal() );
			}
		}
	}

	@Override
	protected NBTTagCompound getCompoundFromBlock(
			final World world,
			final BlockPos pos,
			final EntityPlayer player )
	{
		final IBlockState state = world.getBlockState( pos );
		final Block blkObj = state.getBlock();

		if ( !( blkObj instanceof BlockChiseled ) && BlockBitInfo.supportsBlock( state ) )
		{
			final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();

			tmp.fillWith( state );

			final NBTTagCompound comp = new NBTTagCompound();
			tmp.writeChisleData( comp );

			comp.setByte( "side", (byte) ModUtil.getPlaceFace( player ).ordinal() );
			return comp;
		}

		return super.getCompoundFromBlock( world, pos, player );
	}

	@Override
	protected boolean convertToStone()
	{
		return false;
	}

	@Override
	protected void applyPrint(
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final VoxelBlob vb,
			final VoxelBlob pattern,
			final EntityPlayer player )
	{
		// snag a tool...
		final ChiselInventory selected = new ChiselInventory( player, pos, side );
		ItemStack spawnedItem = null;

		final ArrayList<BagInventory> bags = new ArrayList<BagInventory>();
		final IInventory inv = player.inventory;
		for ( int zz = 0; zz < inv.getSizeInventory(); zz++ )
		{
			final ItemStack which = inv.getStackInSlot( zz );
			if ( which != null && which.getItem() instanceof ItemBitBag )
			{
				bags.add( new BagInventory( which ) );
			}
		}

		final List<EntityItem> spawnlist = new ArrayList<EntityItem>();

		for ( int y = 0; y < vb.detail && selected.isValid(); y++ )
		{
			for ( int z = 0; z < vb.detail && selected.isValid(); z++ )
			{
				for ( int x = 0; x < vb.detail && selected.isValid(); x++ )
				{
					int inPlace = vb.get( x, y, z );
					final int inPattern = pattern.get( x, y, z );
					if ( inPlace != inPattern )
					{
						if ( inPlace != 0 )
						{
							spawnedItem = ItemChisel.chiselBlock( selected, player, vb, world, pos, side, x, y, z, spawnedItem, spawnlist );

							if ( spawnedItem != null )
							{
								inPlace = 0;
							}
						}

						if ( inPlace == 0 && inPattern != 0 )
						{
							final ItemStackSlot bit = ModUtil.findBit( player, inPattern );
							if ( bit.isValid() )
							{
								vb.set( x, y, z, inPattern );
								if ( !player.capabilities.isCreativeMode )
								{
									bit.consume();
								}
							}
							else if ( consumeBagBit( bags, inPattern ) )
							{
								vb.set( x, y, z, inPattern );
							}
						}
					}
				}
			}
		}

		for ( final EntityItem ei : spawnlist )
		{
			world.spawnEntityInWorld( ei );
		}

	}

	private boolean consumeBagBit(
			final ArrayList<BagInventory> bags,
			final int inPattern )
	{
		for ( final BagInventory inv : bags )
		{
			if ( inv.extractBit( inPattern, 1 ) == 1 )
			{
				return true;
			}
		}

		return false;
	}

}
