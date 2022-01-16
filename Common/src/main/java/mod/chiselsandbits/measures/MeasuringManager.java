package mod.chiselsandbits.measures;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.measuring.IMeasurement;
import mod.chiselsandbits.api.measuring.IMeasuringManager;
import mod.chiselsandbits.api.measuring.MeasuringMode;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import mod.chiselsandbits.network.packets.MeasurementsUpdatedPacket;
import mod.chiselsandbits.platforms.core.dist.Dist;
import mod.chiselsandbits.platforms.core.dist.DistExecutor;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class MeasuringManager implements IMeasuringManager, IPacketBufferSerializable
{
    private static final MeasuringManager INSTANCE = new MeasuringManager();

    public static MeasuringManager getInstance()
    {
        return INSTANCE;
    }

    private final Table<ResourceLocation, UUID, Map<MeasuringMode, Measurement>> measurements = HashBasedTable.create();

    private MeasuringManager()
    {
    }

    @Override
    public Collection<? extends IMeasurement> getInWorld(final ResourceLocation worldKey)
    {
        return measurements.row(worldKey).values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends IMeasurement> getForPlayer(final UUID playerId)
    {
        return measurements.column(playerId).values().stream().flatMap(m -> m.values().stream()).collect(Collectors.toSet());
    }

    @Override
    public Measurement create(
      final Level world, final Player playerEntity, final Vec3 from, final Vec3 to, final Direction hitFace, final MeasuringMode mode)
    {
        return new Measurement(
          playerEntity.getUUID(),
          from,
          to,
          hitFace,
          mode,
          world.dimension().location()
        );
    }

    @Override
    public void resetMeasurementsFor(final UUID playerId)
    {
        measurements.columnMap().remove(playerId);
        syncToAll();
    }

    public void syncToAll()
    {
        ChiselsAndBits.getInstance().getNetworkChannel().sendToEveryone(
          new MeasurementsUpdatedPacket()
        );
    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {
        packetBuffer.writeVarInt(measurements.size());
        measurements.values().forEach(m -> {
            packetBuffer.writeVarInt(m.size());
            m.values().forEach(measurement -> measurement.serializeInto(packetBuffer));
        });
    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {
        measurements.clear();
        Collection<Measurement> measurements = IntStream.range(0, packetBuffer.readVarInt())
          .mapToObj(index -> {
              final int measurementCount = packetBuffer.readVarInt();

              final List<Measurement> measurementList = Lists.newArrayList();
              for (int i = 0; i < measurementCount; i++)
              {
                  final Measurement measurement = new Measurement();
                  measurement.deserializeFrom(packetBuffer);
                  measurementList.add(measurement);
              }

              return measurementList;
          })
          .flatMap(Collection::stream).toList();

        measurements.forEach(measurement -> {
            if (!this.measurements.contains(measurement.getWorldKey(), measurement.getOwner()))
                this.measurements.put(measurement.getWorldKey(), measurement.getOwner(), new HashMap<>());

            this.measurements.get(measurement.getWorldKey(), measurement.getOwner()).put(measurement.getMode(), measurement);
        });
    }

    public void addOrUpdate(final Measurement measurement)
    {
        if (!this.measurements.contains(measurement.getWorldKey(), measurement.getOwner()))
            this.measurements.put(measurement.getWorldKey(), measurement.getOwner(), new HashMap<>());

        this.measurements.get(measurement.getWorldKey(), measurement.getOwner()).put(measurement.getMode(), measurement);

        this.syncToAll();
    }

    public void createAndSend(
      final Vec3 from, final Vec3 to, final Direction hitFace, final MeasuringMode mode
    ) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> MeasurementNetworkUtil.createAndSend(from, to, hitFace, mode));
    }
}
