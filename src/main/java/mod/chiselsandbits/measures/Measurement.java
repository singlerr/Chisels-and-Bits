package mod.chiselsandbits.measures;

import mod.chiselsandbits.api.measuring.IMeasurement;
import mod.chiselsandbits.api.measuring.MeasuringMode;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Measurement implements IMeasurement, IPacketBufferSerializable
{

    private UUID owner;
    private Vector3d from;
    private Vector3d         to;
    private MeasuringMode    mode;
    private ResourceLocation worldKey;

    public Measurement()
    {
    }

    public Measurement(final UUID owner, final Vector3d from, final Vector3d to, final MeasuringMode mode, final ResourceLocation worldKey) {
        this.owner = owner;
        this.from = new Vector3d(
          Math.min(from.getX(), to.getX()),
          Math.min(from.getY(), to.getY()),
          Math.min(from.getZ(), to.getZ())
        );
        this.to = new Vector3d(
          Math.max(from.getX(), to.getX()),
          Math.max(from.getY(), to.getY()),
          Math.max(from.getZ(), to.getZ())
        );;
        this.mode = mode;
        this.worldKey = worldKey;
    }

    @Override
    public UUID getOwner()
    {
        return owner;
    }

    @Override
    public Vector3d getFrom()
    {
        return from;
    }

    @Override
    public Vector3d getTo()
    {
        return to;
    }

    @Override
    public MeasuringMode getMode()
    {
        return mode;
    }

    @Override
    public ResourceLocation getWorldKey()
    {
        return worldKey;
    }

    @Override
    public void serializeInto(final @NotNull PacketBuffer packetBuffer)
    {
        packetBuffer.writeUniqueId(getOwner());
        packetBuffer.writeDouble(getFrom().getX());
        packetBuffer.writeDouble(getFrom().getY());
        packetBuffer.writeDouble(getFrom().getZ());
        packetBuffer.writeDouble(getTo().getX());
        packetBuffer.writeDouble(getTo().getY());
        packetBuffer.writeDouble(getTo().getZ());
        packetBuffer.writeVarInt(mode.ordinal());
        packetBuffer.writeString(getWorldKey().toString(), Integer.MAX_VALUE / 4);
    }

    @Override
    public void deserializeFrom(final @NotNull PacketBuffer packetBuffer)
    {
        owner = packetBuffer.readUniqueId();
        from = new Vector3d(
          packetBuffer.readDouble(),
          packetBuffer.readDouble(),
          packetBuffer.readDouble()
        );
        to = new Vector3d(
          packetBuffer.readDouble(),
          packetBuffer.readDouble(),
          packetBuffer.readDouble()
        );
        mode = MeasuringMode.values()[packetBuffer.readVarInt()];
        worldKey = new ResourceLocation(packetBuffer.readString(Integer.MAX_VALUE / 4));
    }
}
