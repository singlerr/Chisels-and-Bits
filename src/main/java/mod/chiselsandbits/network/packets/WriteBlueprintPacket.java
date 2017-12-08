package mod.chiselsandbits.network.packets;

import java.io.IOException;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.blueprints.EntityBlueprint;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class WriteBlueprintPacket extends ModPacket
{

	public NBTTagCompound data;
	public int entityid;

	@Override
	public void server(
			final EntityPlayerMP playerEntity )
	{
		Entity e = playerEntity.getEntityWorld().getEntityByID( entityid );

		if ( e != null && e instanceof EntityBlueprint )
		{
			final NBTTagCompound tag = new NBTTagCompound();

			if ( data.hasKey( "xSize" ) )
			{
				tag.setInteger( "xSize", data.getInteger( "xSize" ) );
			}

			if ( data.hasKey( "ySize" ) )
			{
				tag.setInteger( "ySize", data.getInteger( "ySize" ) );
			}

			if ( data.hasKey( "zSize" ) )
			{
				tag.setInteger( "zSize", data.getInteger( "zSize" ) );
			}

			if ( data.hasKey( "data" ) )
			{
				tag.setByteArray( "data", data.getByteArray( "data" ) );
			}

			if ( data.hasKey( "url" ) )
			{
				tag.setString( "url", data.getString( "url" ) );
			}

			( (EntityBlueprint) e ).dropItem( tag );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeVarIntToBuffer( entityid );
		buffer.writeNBTTagCompoundToBuffer( data );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		try
		{
			entityid = buffer.readVarIntFromBuffer();
			data = buffer.readNBTTagCompoundFromBuffer();
		}
		catch ( final IOException e )
		{
			data = new NBTTagCompound();
		}
	}

	public void setFrom(
			final int entityid,
			final BlueprintData dat ) throws IOException
	{
		this.entityid = entityid;

		data = new NBTTagCompound();
		data.setInteger( "xSize", dat.getXSize() );
		data.setInteger( "ySize", dat.getYSize() );
		data.setInteger( "zSize", dat.getZSize() );

		final byte[] blob = dat.getStuctureData();
		if ( blob.length > 30000 )
		{
			data.setString( "url", dat.getURL() );
		}
		else
		{
			data.setByteArray( "data", blob );
		}
	}
}
