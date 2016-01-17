package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;

import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob.CommonBlock;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IPatternItem;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemMirrorPrint extends Item implements IPatternItem
{

	public ItemMirrorPrint()
	{

	}

	protected void defaultAddInfo(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List<String> tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
	}

	// add info cached info
	protected ItemStack cachedInfo;
	protected List<String> details = new ArrayList<String>();

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		defaultAddInfo( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpMirrorPrint, tooltip );

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
		if ( !player.canPlayerEdit( pos, side, stack ) )
		{
			return true;
		}

		if ( !stack.hasTagCompound() )
		{
			final NBTTagCompound comp = getCompoundFromBlock( world, pos, player, side );
			if ( comp != null )
			{
				stack.setTagCompound( comp );
				return false;
			}

			return true;
		}

		return true;
	}

	protected NBTTagCompound getCompoundFromBlock(
			final World world,
			final BlockPos pos,
			final EntityPlayer player,
			final EnumFacing face )
	{
		final TileEntityBlockChiseled te = ModUtil.getChiseledTileEntity( world, pos, false );

		if ( te != null )
		{
			final NBTTagCompound comp = new NBTTagCompound();
			te.writeChisleData( comp );

			final TileEntityBlockChiseled tmp = new TileEntityBlockChiseled();
			tmp.readChisleData( comp );

			final VoxelBlob bestBlob = tmp.getBlob();
			tmp.setBlob( bestBlob.mirror( face.getAxis() ) );
			tmp.writeChisleData( comp );

			comp.setByte( "side", (byte) ModUtil.getPlaceFace( player ).ordinal() );
			return comp;
		}

		return null;
	}

	@Override
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
		final ItemStack itemstack = new ItemStack( ChiselsAndBits.getBlocks().getConversionWithDefault( blk.getBlock().getMaterial() ), 1 );

		itemstack.setTagInfo( "BlockEntityTag", tag );
		return itemstack;
	}

}