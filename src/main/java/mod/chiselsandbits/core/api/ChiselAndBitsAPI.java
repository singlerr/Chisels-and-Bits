package mod.chiselsandbits.core.api;

import mod.chiselsandbits.api.APIExceptions.CannotBeChiseled;
import mod.chiselsandbits.api.APIExceptions.InvalidBitItem;
import mod.chiselsandbits.api.IBitAccess;
import mod.chiselsandbits.api.IBitBrush;
import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.api.IChiselAndBitsAPI;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.items.ItemBitBag;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ChiselAndBitsAPI implements IChiselAndBitsAPI
{

	@Override
	public boolean canBeChiseled(
			final World world,
			final BlockPos pos )
	{
		if ( world == null || pos == null )
		{
			return false;
		}

		return ModUtil.getChiseledTileEntity( world, pos, true ) != null;
	}

	@Override
	public boolean isBlockChiseled(
			final World world,
			final BlockPos pos )
	{
		if ( world == null || pos == null )
		{
			return false;
		}

		return ModUtil.getChiseledTileEntity( world, pos, false ) != null;
	}

	@Override
	public IBitAccess getBitAccess(
			final World world,
			final BlockPos pos ) throws CannotBeChiseled
	{
		if ( world == null || pos == null )
		{
			throw new CannotBeChiseled();
		}

		// TODO IMPLEMENT
		return null;
	}

	@Override
	public IBitBrush createBrush(
			final ItemStack bitItem ) throws InvalidBitItem
	{
		if ( bitItem == null )
		{
			return new BitBrush( 0 );
		}

		if ( isChisledBitBlock( bitItem ) )
		{
			final int stateID = ItemChiseledBit.getStackState( bitItem );
			final IBlockState state = Block.getStateById( stateID );

			if ( state != null && BlockBitInfo.supportsBlock( state ) )
			{
				return new BitBrush( stateID );
			}
		}

		throw new InvalidBitItem();
	}

	@Override
	public IBitLocation getBitPos(
			final float hitX,
			final float hitY,
			final float hitZ,
			final EnumFacing side,
			final BlockPos pos,
			final boolean placement )
	{
		if ( side == null || pos == null )
		{
			return null;
		}

		final MovingObjectPosition mop = new MovingObjectPosition( MovingObjectType.BLOCK, new Vec3( hitX, hitY, hitZ ), side, pos );
		return new BitLocation( mop, false, placement ? ChiselToolType.BIT : ChiselToolType.CHISEL );
	}

	@Override
	public boolean isBitItem(
			final ItemStack item )
	{
		return item != null && item.getItem() instanceof ItemChiseledBit;
	}

	@Override
	public boolean isChisledBitBlock(
			final ItemStack item )
	{
		return item != null && item.getItem() instanceof ItemBlockChiseled;
	}

	@Override
	public boolean isChisel(
			final ItemStack item )
	{
		return item != null && item.getItem() instanceof ItemChisel;
	}

	@Override
	public boolean isBitBag(
			final ItemStack item )
	{
		return item != null && item.getItem() instanceof ItemBitBag;
	}

	@Override
	public IBitAccess createBitItem(
			final ItemStack BitItemStack )
	{
		// TODO IMPLEMENT
		return new BitAccess( null, null, new VoxelBlob() );
	}

}
