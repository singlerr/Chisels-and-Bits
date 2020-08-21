package mod.chiselsandbits.chiseledblock.serialization;

import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
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
		final String name = buffer.readString();
		return StringStates.getStateIDFromName( name );
	}

	@Override
	protected void writeStateID(
			final PacketBuffer buffer,
			final int key )
	{
		final String sname = StringStates.getNameFromStateID( key );

		buffer.writeString( sname );
	}

	@Override
	public int getVersion()
	{
		return VoxelBlob.VERSION_CROSSWORLD;
	}
}
