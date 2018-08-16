package mod.chiselsandbits.integration.mcmultipart;

import java.util.ArrayList;
import java.util.List;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import mod.chiselsandbits.api.BoxType;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class ChiseledBlockMultiPart implements IMultipart
{
	BlockChiseled blk;

	public ChiseledBlockMultiPart(
			final BlockChiseled myBlock )
	{
		blk = myBlock;
	}

	@Override
	public List<AxisAlignedBB> getOcclusionBoxes(
			final IPartInfo part )
	{
		final List<AxisAlignedBB> l = new ArrayList<AxisAlignedBB>();

		final TileEntityBlockChiseled te = ModUtil.getChiseledTileEntity( part.getActualWorld(), part.getPartPos() );
		if ( te != null )
		{
			l.addAll( te.getBoxes( BoxType.OCCLUSION ) );
		}

		return l;
	}

	@Override
	public Block getBlock()
	{
		return blk;
	}

	@Override
	public IMultipartTile convertToMultipartTile(
			final TileEntity tileEntity )
	{
		return new ChiseledBlockPart( tileEntity );
	}

	@Override
	public IPartSlot getSlotForPlacement(
			final World world,
			final BlockPos pos,
			final IBlockState state,
			final EnumFacing facing,
			final float hitX,
			final float hitY,
			final float hitZ,
			final EntityLivingBase placer )
	{
		return MultiPartSlots.BITS;
	}

	@Override
	public IPartSlot getSlotFromWorld(
			final IBlockAccess world,
			final BlockPos pos,
			final IBlockState state )
	{
		return MultiPartSlots.BITS;
	}

}
