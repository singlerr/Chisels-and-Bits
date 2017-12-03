package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.blueprints.EntityBlueprint;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;

public class PacketCompleteBlueprint extends ModPacket
{

	public int EntityID;

	@Override
	public void server(
			final EntityPlayerMP playerEntity )
	{
		Entity e = playerEntity.getEntityWorld().getEntityByID( EntityID );
		if ( e instanceof EntityBlueprint && !e.isDead )
		{
			e.setDead();
			if ( !playerEntity.capabilities.isCreativeMode )
				playerEntity.inventory.addItemStackToInventory( ( (EntityBlueprint) e ).getItemStack() );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeVarIntToBuffer( EntityID );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		EntityID = buffer.readVarIntFromBuffer();
	}

}
