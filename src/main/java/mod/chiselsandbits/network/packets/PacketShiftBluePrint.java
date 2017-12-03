package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.blueprints.EntityBlueprint;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;

public class PacketShiftBluePrint extends ModPacket
{

	public int EntityID;

	public EnumFacing x, y, z;
	public int min_x, min_y, min_z, max_x, max_y, max_z;

	public boolean placing;

	@Override
	public void server(
			final EntityPlayerMP playerEntity )
	{
		Entity e = playerEntity.getEntityWorld().getEntityByID( EntityID );
		if ( e instanceof EntityBlueprint && !e.isDead )
		{
			( (EntityBlueprint) e ).setConfiguration( this );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeVarIntToBuffer( EntityID );
		buffer.writeBoolean( placing );
		buffer.writeEnumValue( x );
		buffer.writeEnumValue( y );
		buffer.writeEnumValue( z );
		buffer.writeInt( min_x );
		buffer.writeInt( min_y );
		buffer.writeInt( min_z );
		buffer.writeInt( max_x );
		buffer.writeInt( max_y );
		buffer.writeInt( max_z );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		EntityID = buffer.readVarIntFromBuffer();
		placing = buffer.readBoolean();
		x = buffer.readEnumValue( EnumFacing.class );
		y = buffer.readEnumValue( EnumFacing.class );
		z = buffer.readEnumValue( EnumFacing.class );
		min_x = buffer.readInt();
		min_y = buffer.readInt();
		min_z = buffer.readInt();
		max_x = buffer.readInt();
		max_y = buffer.readInt();
		max_z = buffer.readInt();
	}

}
