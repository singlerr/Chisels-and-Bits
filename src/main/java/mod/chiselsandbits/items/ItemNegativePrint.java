
package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.CommonBlock;
import mod.chiselsandbits.helpers.ChiselInventory;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;


public class ItemNegativePrint extends Item
{

	public ItemNegativePrint()
	{
		setCreativeTab( ChiselsAndBits.creativeTab );
	}

	@SuppressWarnings( { "rawtypes" } )
	protected void defaultAddInfo(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
	}

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		defaultAddInfo( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.instance.config.helpText( LocalStrings.HelpNegativePrint, tooltip );

		if ( stack.hasTagCompound() )
		{
			if ( Keyboard.isKeyDown( Keyboard.KEY_LSHIFT ) || Keyboard.isKeyDown( Keyboard.KEY_RSHIFT ) )
			{

			}
			else
			{
				tooltip.add( LocalStrings.ShiftDetails.getLocal() );
			}
		}
	}

	@Override
	public String getUnlocalizedName(
			final ItemStack stack )
	{
		if ( stack.hasTagCompound() )
		{
			return super.getUnlocalizedName( stack ) + "_written";
		}
		return super.getUnlocalizedName( stack );
	}

	@Override
	public boolean onItemUseFirst(
			final ItemStack stack,
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final IBlockState blkstate = world.getBlockState( pos );
		final Block blkObj = blkstate.getBlock();

		if ( !player.canPlayerEdit( pos, side, stack ) )
		{
			return true;
		}

		if ( !stack.hasTagCompound() )
		{
			final NBTTagCompound comp = getCompoundFromBlock( world, pos, player );
			if ( comp != null )
			{
				stack.setTagCompound( comp );
				return false;
			}

			return true;
		}

		if ( blkObj instanceof BlockChiseled )
		{
			// we can do this!
		}
		else if ( !BlockChiseled.replaceWithChisled( world, pos, blkstate ) )
		{
			return true;
		}

		final TileEntity te = world.getTileEntity( pos );

		if ( te != null && te instanceof TileEntityBlockChiseled )
		{
			final TileEntityBlockChiseled tec = ( TileEntityBlockChiseled ) te;

			final NBTTagCompound blueprintTag = stack.getTagCompound();

			// float newPitch = player.rotationPitch;
			// float oldPitch = blueprintTag.getFloat("rotationPitch" );

			int rotations = ModUtil.getRotations( player, blueprintTag.getByte( "side" ) );

			final VoxelBlob vb = tec.getBlob();

			final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
			tmp.readChisleData( blueprintTag );
			VoxelBlob pattern = tmp.getBlob();

			while ( rotations-- > 0 )
			{
				pattern = pattern.spin( Axis.Y );
			}

			applyPrint( world, pos, side, vb, pattern, player );

			tec.setBlob( vb );
			return false;
		}

		return true;
	}

	protected NBTTagCompound getCompoundFromBlock(
			final World world,
			final BlockPos pos,
			final EntityPlayer player )
	{

		final Block blkObj = world.getBlockState( pos ).getBlock();
		if ( blkObj instanceof BlockChiseled )
		{
			final TileEntity te = world.getTileEntity( pos );
			if ( te instanceof TileEntityBlockChiseled )
			{
				final NBTTagCompound comp = new NBTTagCompound();
				( ( TileEntityBlockChiseled ) te ).writeChisleData( comp );

				comp.setByte( "side", ( byte ) ModUtil.getPlaceFace( player ).ordinal() );
				return comp;
			}
		}
		return null;
	}

	public ItemStack getPatternedItem(
			final ItemStack stack )
	{
		if ( !stack.hasTagCompound() )
		{
			return null;
		}

		final NBTTagCompound tag = stack.getTagCompound();

		// Detect and provide full blocks if pattern solid full and solid.
		final TileEntityBlockChiseled tebc = new TileEntityBlockChiseled();
		tebc.readChisleData( tag );

		final CommonBlock common = tebc.getBlob().mostCommonBlock();
		if ( common.isFull )
		{
			final IBlockState state = Block.getStateById( common.ref );
			return new ItemStack( state.getBlock(), 1, state.getBlock().getMetaFromState( state ) );
		}

		final IBlockState blk = Block.getStateById( tag.getInteger( TileEntityBlockChiseled.block_prop ) );
		final ItemStack itemstack = new ItemStack( ChiselsAndBits.instance.getConversion( blk.getBlock().getMaterial() ), 1 );

		itemstack.setTagInfo( "BlockEntityTag", tag );
		return itemstack;
	}

	protected void applyPrint(
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final VoxelBlob vb,
			final VoxelBlob pattern,
			final EntityPlayer player )
	{
		// snag a tool...
		ChiselInventory selected = new ChiselInventory( player, pos, side );
		ItemStack spawnedItem = null;

		final List<EntityItem> spawnlist = new ArrayList<EntityItem>();

		for ( int z = 0; z < vb.detail && selected.isValid(); z++ )
		{
			for ( int y = 0; y < vb.detail && selected.isValid(); y++ )
			{
				for ( int x = 0; x < vb.detail && selected.isValid(); x++ )
				{
					int blkID = vb.get( x, y, z );
					if ( blkID != 0 && pattern.get( x, y, z ) == 0 )
					{
						spawnedItem = ItemChisel.chiselBlock( selected, player, vb, world, pos, side, x, y, z, spawnedItem, spawnlist );
					}
				}
			}
		}

		for ( final EntityItem ei : spawnlist )
		{
			world.spawnEntityInWorld( ei );
		}
	}

}