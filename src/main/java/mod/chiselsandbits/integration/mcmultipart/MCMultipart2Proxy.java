package mod.chiselsandbits.integration.mcmultipart;

import java.util.Collections;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

import mcmultipart.RayTraceHelper;
import mcmultipart.api.container.IMultipartContainer;
import mcmultipart.api.container.IPartInfo;
import mcmultipart.api.multipart.IMultipartTile;
import mcmultipart.api.multipart.MultipartHelper;
import mcmultipart.api.multipart.OcclusionHelper;
import mcmultipart.multipart.PartInfo;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitCollisionIterator;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class MCMultipart2Proxy implements IMCMultiPart
{

	@Override
	public void swapRenderIfPossible(
			TileEntity current,
			TileEntityBlockChiseled newTileEntity )
	{
		// TODO Auto-generated method stub
	}

	@Override
	public void removePartIfPossible(
			TileEntity te )
	{
		if ( te instanceof IMultipartContainer && !te.getWorld().isRemote )
		{
			final IMultipartContainer container = (IMultipartContainer) te;
			container.removePart( MultiPartSlots.BITS );
		}
	}

	@Override
	public TileEntityBlockChiseled getPartIfPossible(
			World w,
			BlockPos pos,
			boolean create )
	{
		final Optional<IMultipartContainer> container = MultipartHelper.getOrConvertContainer( w, pos );

		if ( container.isPresent() )
		{
			Optional<IMultipartTile> part = container.get().getPartTile( MultiPartSlots.BITS );
			if ( part.isPresent() && part.get() instanceof TileEntityBlockChiseled )
				return (TileEntityBlockChiseled) part.get();

			if ( MultipartHelper.addPart( w, pos, MultiPartSlots.BITS, ChiselsAndBits.getBlocks().getChiseledDefaultState(), true ) )
			{
				if ( create && !w.isRemote )
				{
					final ChiseledBlockPart tx = new ChiseledBlockPart( null );
					tx.occlusionState = new MultipartContainerBuilder( w, pos, tx, container.get() );
					tx.setWorldObj( w );
					tx.setPos( pos );
					return tx;
				}
				else if ( create )
				{
					final ChiseledBlockPart tx = new ChiseledBlockPart( null );
					tx.setWorldObj( w );
					tx.setPos( pos );
					return tx;
				}
			}
		}

		return null;
	}

	@Override
	public void triggerPartChange(
			TileEntity te )
	{
		if ( te instanceof IMultipartContainer && !te.getWorld().isRemote )
		{
			Optional<IPartInfo> part = ( (IMultipartContainer) te ).get( MultiPartSlots.BITS );
			if ( part.isPresent() )
				part.get().notifyChange();
		}
	}

	@Override
	public boolean isMultiPart(
			World w,
			BlockPos pos )
	{
		return MultipartHelper.getContainer( w, pos ) != null ||
				MultipartHelper.addPart( w, pos, MultiPartSlots.BITS, ChiselsAndBits.getBlocks().getChiseledDefaultState(), true );
	}

	@Override
	public void populateBlobWithUsedSpace(
			World w,
			BlockPos pos,
			VoxelBlob vb )
	{
		if ( isMultiPart( w, pos ) )
		{
			final Optional<IMultipartContainer> mc = MultipartHelper.getOrConvertContainer( w, pos );
			if ( mc.isPresent() )
			{
				IMultipartContainer mcc = mc.get();

				final BitCollisionIterator bci = new BitCollisionIterator();
				while ( bci.hasNext() )
				{
					final AxisAlignedBB aabb = new AxisAlignedBB( bci.physicalX, bci.physicalY, bci.physicalZ, bci.physicalX + BitCollisionIterator.One16thf, bci.physicalYp1, bci.physicalZp1 );

					if ( OcclusionHelper.testContainerBoxIntersection( mcc, Collections.singleton( aabb ), which -> MultiPartSlots.BITS == which ) )
					{
						bci.setNext( vb, 1 );
					}
				}
			}
		}
	}

	@Override
	public boolean rotate(
			World world,
			BlockPos pos,
			EntityPlayer player )
	{
		final IMultipartContainer container = MultipartHelper.getContainer( world, pos ).orElse( null );
		if ( container != null )
		{
			final IBlockState state = world.getBlockState( pos );
			final Block blk = state.getBlock();

			if ( blk != null )
			{
				final Pair<Vec3d, Vec3d> atob = RayTraceHelper.getRayTraceVectors( player );
				final RayTraceResult crt = blk.collisionRayTrace( state, world, pos, atob.getKey(), atob.getValue() );

				if ( crt.hitInfo instanceof PartInfo )
				{
					// TODO : rotate the parts.
				}
			}
		}
		return false;
	}

	@Override
	public TileEntityBlockChiseled getPartFromBlockAccess(
			IBlockAccess w,
			BlockPos pos )
	{
		final TileEntity te = ModUtil.getTileEntitySafely( w, pos );
		IMultipartContainer container = null;

		if ( te instanceof IMultipartContainer )
		{
			container = (IMultipartContainer) te;
		}

		if ( container != null )
		{
			return (TileEntityBlockChiseled) container.getPartTile( MultiPartSlots.BITS ).orElse( null );
		}

		return null;
	}

}
