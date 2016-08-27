package mod.chiselsandbits.chiseledblock.serialization;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.PacketBuffer;

public class CrossWorldBlobSerializer extends BlobSerializer
{

	public CrossWorldBlobSerializer(
			final PacketBuffer toInflate )
	{
		super( toInflate );
	}

	public CrossWorldBlobSerializer(
			final VoxelBlob toDeflate )
	{
		super( toDeflate );
	}

	@Override
	protected int readStateID(
			final PacketBuffer buffer )
	{
		final String name = buffer.readStringFromBuffer( 2047 );
		final String extra = buffer.readStringFromBuffer( 2047 );

		return Block.getStateId( ModUtil.getStateFromString( name, extra ) );
	}

	@Override
	protected void writeStateID(
			final PacketBuffer buffer,
			final int key )
	{
		final IBlockState state = Block.getStateById( key );
		final String sname = ModUtil.getStringFromState( state );

		buffer.writeString( sname );
		buffer.writeString( "" ); // extra data for later use.
	}

	@Override
	public int getVersion()
	{
		return VoxelBlob.VERSION_CROSSWORLD;
	}
}
