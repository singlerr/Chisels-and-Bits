package mod.chiselsandbits.chiseledblock.data;

import mod.chiselsandbits.api.IBitLocation;
import mod.chiselsandbits.helpers.BitOperation;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;

import javax.annotation.Nonnull;

public class BitLocation implements IBitLocation
{
	private static final double One32nd = 0.5 / VoxelBlob.dim;

	@Nonnull
	public BlockPos blockPos;
	public int bitX, bitY, bitZ;

	@Override
	public BlockPos getBlockPos()
	{
		return blockPos;
	}

	@Override
	public int getBitX()
	{
		return bitX;
	}

	@Override
	public int getBitY()
	{
		return bitY;
	}

	@Override
	public int getBitZ()
	{
		return bitZ;
	}

    @Override
    public IBitLocation offSet(final Direction direction)
    {
        final int newBitX = bitX + direction.getXOffset();
        final int newBitY = bitY + direction.getYOffset();
        final int newBitZ = bitZ + direction.getZOffset();

        return new BitLocation(
          blockPos,
          newBitX,
          newBitY,
          newBitZ
        );
    }

    public int snapToValid(
			final int x )
	{
		// rounding can sometimes create -1 or 16, just snap int to the nearest
		// valid position and move on.
		return Math.min( Math.max( 0, x ), 15 );
	}

	public BitLocation(
			final BlockRayTraceResult mop,
			final boolean absHit,
			final BitOperation type )
	{
		final BlockPos absOffset = absHit ? mop.getPos() : BlockPos.ZERO;

		if ( !type.usePlacementOffset() )
		{
			blockPos = mop.getPos();

			final double xCoord = mop.getHitVec().x - absOffset.getX() - mop.getFace().getXOffset() * One32nd;
			final double yCoord = mop.getHitVec().y - absOffset.getY() - mop.getFace().getYOffset() * One32nd;
			final double zCoord = mop.getHitVec().z - absOffset.getZ() - mop.getFace().getZOffset() * One32nd;

			bitX = snapToValid( (int) Math.floor( xCoord * VoxelBlob.dim ) );
			bitY = snapToValid( (int) Math.floor( yCoord * VoxelBlob.dim ) );
			bitZ = snapToValid( (int) Math.floor( zCoord * VoxelBlob.dim ) );
		}
		else
		{
            final double xCoord = mop.getHitVec().x - absOffset.getX() - mop.getFace().getXOffset() * One32nd;
            final double yCoord = mop.getHitVec().y - absOffset.getY() - mop.getFace().getYOffset() * One32nd;
            final double zCoord = mop.getHitVec().z - absOffset.getZ() - mop.getFace().getZOffset() * One32nd;

			final int bitXi = (int) Math.floor( xCoord * VoxelBlob.dim );
			final int bitYi = (int) Math.floor( yCoord * VoxelBlob.dim );
			final int bitZi = (int) Math.floor( zCoord * VoxelBlob.dim );

			if ( bitXi < 0 || bitYi < 0 || bitZi < 0 || bitXi >= VoxelBlob.dim || bitYi >= VoxelBlob.dim || bitZi >= VoxelBlob.dim )
			{
				blockPos = mop.getPos().offset( mop.getFace() );
				bitX = snapToValid( bitXi - mop.getFace().getZOffset() * VoxelBlob.dim );
				bitY = snapToValid( bitYi - mop.getFace().getYOffset() * VoxelBlob.dim );
				bitZ = snapToValid( bitZi - mop.getFace().getZOffset() * VoxelBlob.dim );
			}
			else
			{
				blockPos = mop.getPos();
				bitX = snapToValid( bitXi );
				bitY = snapToValid( bitYi );
				bitZ = snapToValid( bitZi );
			}
		}

		normalize();
	}

	public BitLocation(
			final BlockPos pos,
			final int x,
			final int y,
			final int z )
	{
		blockPos = pos;
		bitX = x;
		bitY = y;
		bitZ = z;
		normalize();
	}

	public static BitLocation min(
			final BitLocation from,
			final BitLocation to )
	{
		final int bitX = Min( from.blockPos.getX(), to.blockPos.getX(), from.bitX, to.bitX );
		final int bitY = Min( from.blockPos.getY(), to.blockPos.getY(), from.bitY, to.bitY );
		final int bitZ = Min( from.blockPos.getZ(), to.blockPos.getZ(), from.bitZ, to.bitZ );

		return new BitLocation( new BlockPos(
				Math.min( from.blockPos.getX(), to.blockPos.getX() ),
				Math.min( from.blockPos.getY(), to.blockPos.getY() ),
				Math.min( from.blockPos.getZ(), to.blockPos.getZ() ) ),
				bitX, bitY, bitZ );
	}

	public static BitLocation max(
			final BitLocation from,
			final BitLocation to )
	{
		final int bitX = Max( from.blockPos.getX(), to.blockPos.getX(), from.bitX, to.bitX );
		final int bitY = Max( from.blockPos.getY(), to.blockPos.getY(), from.bitY, to.bitY );
		final int bitZ = Max( from.blockPos.getZ(), to.blockPos.getZ(), from.bitZ, to.bitZ );

		return new BitLocation( new BlockPos(
				Math.max( from.blockPos.getX(), to.blockPos.getX() ),
				Math.max( from.blockPos.getY(), to.blockPos.getY() ),
				Math.max( from.blockPos.getZ(), to.blockPos.getZ() ) ),
				bitX, bitY, bitZ );
	}

	private static int Min(
			final int x,
			final int x2,
			final int bitX2,
			final int bitX3 )
	{
		if ( x < x2 )
		{
			return bitX2;
		}
		if ( x2 == x )
		{
			return Math.min( bitX2, bitX3 );
		}

		return bitX3;
	}

	private static int Max(
			final int x,
			final int x2,
			final int bitX2,
			final int bitX3 )
	{
		if ( x > x2 )
		{
			return bitX2;
		}
		if ( x2 == x )
		{
			return Math.max( bitX2, bitX3 );
		}

		return bitX3;
	}

	private void normalize() {
	    final double xOffset = Math.floor(bitX / 16d);
	    final double yOffset = Math.floor(bitY / 16d);
	    final double zOffset = Math.floor(bitZ / 16d);

	    bitX = bitX % 16;
	    bitY = bitY % 16;
	    bitZ = bitZ % 16;

	    this.blockPos = this.blockPos.add(
	      xOffset,
          yOffset,
          zOffset
        );
    }

}
