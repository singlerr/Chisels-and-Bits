package mod.chiselsandbits.measures;

import mod.chiselsandbits.api.measuring.IMeasurement;
import mod.chiselsandbits.api.measuring.MeasuringMode;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class Measurement implements IMeasurement, IPacketBufferSerializable
{

    private UUID owner;
    private Vec3 from;
    private Vec3         to;
    private MeasuringMode    mode;
    private ResourceLocation worldKey;


    public Measurement(final UUID owner, final Vec3 from, final Vec3 to, final Direction hitFace, final MeasuringMode mode, final ResourceLocation worldKey) {
        this.owner = owner;
        this.mode = mode;
        this.worldKey = worldKey;

        adaptPositions(from, to, hitFace, mode);
    }

    public Measurement()
    {
    }

    private void adaptPositions(final Vec3 from, final Vec3 to, final Direction hitFace, final MeasuringMode mode)
    {
        this.from = mode.getType().isNeedsNormalization() ? new Vec3(
          Math.min(from.x(), to.x()),
          Math.min(from.y(), to.y()),
          Math.min(from.z(), to.z())
        ) : from;

        this.to = mode.getType().isNeedsNormalization() ? new Vec3(
          Math.max(from.x(), to.x()),
          Math.max(from.y(), to.y()),
          Math.max(from.z(), to.z())
        ) : to;

        this.from = mode.getType().adaptStartCorner(this.from, this.to, hitFace);
        this.to = mode.getType().adaptEndCorner(this.from, this.to, hitFace);
    }

    @Override
    public UUID getOwner()
    {
        return owner;
    }

    @Override
    public Vec3 getFrom()
    {
        return from;
    }

    @Override
    public Vec3 getTo()
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
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeUUID(getOwner());
        packetBuffer.writeDouble(getFrom().x());
        packetBuffer.writeDouble(getFrom().y());
        packetBuffer.writeDouble(getFrom().z());
        packetBuffer.writeDouble(getTo().x());
        packetBuffer.writeDouble(getTo().y());
        packetBuffer.writeDouble(getTo().z());
        packetBuffer.writeVarInt(mode.ordinal());
        packetBuffer.writeUtf(getWorldKey().toString(), Integer.MAX_VALUE / 4);
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        owner = packetBuffer.readUUID();
        from = new Vec3(
          packetBuffer.readDouble(),
          packetBuffer.readDouble(),
          packetBuffer.readDouble()
        );
        to = new Vec3(
          packetBuffer.readDouble(),
          packetBuffer.readDouble(),
          packetBuffer.readDouble()
        );
        mode = MeasuringMode.values()[packetBuffer.readVarInt()];
        worldKey = new ResourceLocation(packetBuffer.readUtf(Integer.MAX_VALUE / 4));
    }
}
