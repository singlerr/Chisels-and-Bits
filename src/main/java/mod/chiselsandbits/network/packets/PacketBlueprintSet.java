package mod.chiselsandbits.network.packets;

import java.io.IOException;

import mod.chiselsandbits.blueprints.BlueprintData;
import mod.chiselsandbits.blueprints.ItemBlueprint;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

public class PacketBlueprintSet extends ModPacket
{

	public NBTTagCompound data;
	public int slot;

	@Override
	public void server(
			final EntityPlayerMP playerEntity )
	{
		final ItemStack is = playerEntity.inventory.getStackInSlot( slot );
		if ( is.getItem() instanceof ItemBlueprint )
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

			is.setTagCompound( tag );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeVarIntToBuffer( slot );
		buffer.writeNBTTagCompoundToBuffer( data );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		try
		{
			slot = buffer.readVarIntFromBuffer();
			data = buffer.readNBTTagCompoundFromBuffer();
		}
		catch ( final IOException e )
		{
			data = new NBTTagCompound();
		}
	}

	public void setFrom(
			final BlueprintData dat ) throws IOException
	{
		data = new NBTTagCompound();
		data.setInteger( "xSize", dat.getXSize() );
		data.setInteger( "ySize", dat.getYSize() );
		data.setInteger( "zSize", dat.getZSize() );
		data.setByteArray( "data", dat.getStuctureData() );
	}

}
