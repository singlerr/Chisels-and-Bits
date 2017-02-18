package mod.chiselsandbits.integration.mcmultipart;

import java.util.ArrayList;
import java.util.List;

import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipart;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.slot.IPartSlot;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.BoxType;
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
			BlockChiseled myBlock )
	{
		blk = myBlock;
	}

	@Override
	public List<AxisAlignedBB> getOcclusionBoxes(
			IBlockAccess world,
			BlockPos pos,
			IPartInfo part )
	{
		List<AxisAlignedBB> l = new ArrayList<AxisAlignedBB>();

		TileEntityBlockChiseled te = ModUtil.getChiseledTileEntity( world, pos );
		if ( te != null )
			l.addAll( te.getBoxes( BoxType.OCCLUSION ) );

		return l;
	}

	@Override
	public Block getBlock()
	{
		return blk;
	}

	@Override
	public IMultipartTile convertToMultipartTile(
			TileEntity tileEntity )
	{
		return new ChiseledBlockPart( tileEntity );
	}

	@Override
	public IPartSlot getSlotForPlacement(
			World world,
			BlockPos pos,
			IBlockState state,
			EnumFacing facing,
			float hitX,
			float hitY,
			float hitZ,
			EntityLivingBase placer )
	{
		return MultiPartSlots.BITS;
	}

	@Override
	public IPartSlot getSlotFromWorld(
			IBlockAccess world,
			BlockPos pos,
			IBlockState state )
	{
		return MultiPartSlots.BITS;
	}

}
