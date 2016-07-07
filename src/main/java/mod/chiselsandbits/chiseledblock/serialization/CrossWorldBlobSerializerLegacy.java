package mod.chiselsandbits.chiseledblock.serialization;

import mod.chiselsandbits.chiseledblock.data.BitState;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.DeprecationHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;

public class CrossWorldBlobSerializerLegacy extends BlobSerializer
{

	public CrossWorldBlobSerializerLegacy(
			final VoxelBlob voxelBlob,
			final PacketBuffer toInflate )
	{
		super( voxelBlob, toInflate );
	}

	public CrossWorldBlobSerializerLegacy(
			final VoxelBlob toDeflate )
	{
		super( toDeflate );
	}

	@Override
	protected BitState readStateID(
			final PacketBuffer buffer )
	{
		final String name = buffer.readStringFromBuffer( 512 );
		final int meta = buffer.readVarIntFromBuffer();

		final Block blk = Block.REGISTRY.getObject( new ResourceLocation( name ) );

		if ( blk == null )
		{
			return target.getStateFor( null );
		}

		final IBlockState state = DeprecationHelper.getStateFromMeta( blk, meta );
		if ( state == null )
		{
			return target.getStateFor( null );
		}

		return target.getStateFor( new BitState( Block.getStateId( state ), state ) );
	}

	@Override
	protected void writeStateID(
			final PacketBuffer buffer,
			final BitState key )
	{
		final IBlockState state = key.getBlockState();
		final Block blk = state.getBlock();

		final String name = Block.REGISTRY.getNameForObject( blk ).toString();
		final int meta = blk.getMetaFromState( state );

		buffer.writeString( name );
		buffer.writeVarIntToBuffer( meta );
	}

	@Override
	public int getVersion()
	{
		return VoxelBlob.VERSION_CROSSWORLD;
	}
}
