package mod.chiselsandbits.legacy.serialization.blob;

import mod.chiselsandbits.legacy.LegacyLoadManager;
import mod.chiselsandbits.utils.StringStateUtils;
import net.minecraft.network.PacketBuffer;

public class CrossWorldBlobSerializer extends BlobSerializer
{

	public CrossWorldBlobSerializer(
			final PacketBuffer toInflate )
	{
		super( toInflate );
	}

	@Override
	protected int readStateID(
			final PacketBuffer buffer )
	{
		final String name = buffer.readUtf();
		return StringStateUtils.getStateIDFromName( name );
	}

	@Override
	protected void writeStateID(
			final PacketBuffer buffer,
			final int key )
	{
		final String stateName = StringStateUtils.getNameFromStateID( key );
		buffer.writeUtf( stateName );
	}

	@Override
	public int getVersion()
	{
		return LegacyLoadManager.VERSION_CROSSWORLD;
	}
}
