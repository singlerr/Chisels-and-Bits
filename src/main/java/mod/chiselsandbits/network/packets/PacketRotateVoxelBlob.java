package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.interfaces.IVoxelBlobItem;
import mod.chiselsandbits.network.ModPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Rotation;

public class PacketRotateVoxelBlob extends ModPacket
{

	private Direction.Axis axis;
    private Rotation  rotation;

    public PacketRotateVoxelBlob(PacketBuffer buffer)
    {
        readPayload(buffer);
    }

    public PacketRotateVoxelBlob(final Direction.Axis axis, final Rotation rotation)
    {
        this.axis = axis;
        this.rotation = rotation;
    }

    @Override
	public void server(
			final ServerPlayerEntity player )
	{
		final ItemStack is = player.getHeldItemMainhand();
		if ( is != null && is.getItem() instanceof IVoxelBlobItem )
		{
			( (IVoxelBlobItem) is.getItem() ).rotate( is, axis, rotation );
		}
	}

	@Override
	public void getPayload(
			final PacketBuffer buffer )
	{
		buffer.writeEnumValue( axis );
		buffer.writeEnumValue( rotation );
	}

	@Override
	public void readPayload(
			final PacketBuffer buffer )
	{
		axis = buffer.readEnumValue( Direction.Axis.class );
		rotation = buffer.readEnumValue( Rotation.class );
	}

    public Direction.Axis getAxis()
    {
        return axis;
    }

    public void setAxis(final Direction.Axis axis)
    {
        this.axis = axis;
    }

    public Rotation getRotation()
    {
        return rotation;
    }

    public void setRotation(final Rotation rotation)
    {
        this.rotation = rotation;
    }
}
