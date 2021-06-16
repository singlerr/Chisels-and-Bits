package mod.chiselsandbits.measures;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.measuring.IMeasurement;
import mod.chiselsandbits.api.measuring.IMeasuringManager;
import mod.chiselsandbits.api.measuring.MeasuringMode;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import mod.chiselsandbits.network.packets.MeasurementUpdatedPacket;
import mod.chiselsandbits.network.packets.MeasurementsUpdatedPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
      final World world, final PlayerEntity playerEntity, final Vector3d from, final Vector3d to, final MeasuringMode mode)
    {
        return new Measurement(
          playerEntity.getUniqueID(),
          from,
          to,
          mode,
          world.getDimensionKey().getLocation()
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
        ChiselsAndBits.getInstance().getNetworkChannel().sendToEveryone(new MeasurementsUpdatedPacket());
    }

    @Override
    public void serializeInto(final @NotNull PacketBuffer packetBuffer)
    {
        packetBuffer.writeVarInt(measurements.size());
        measurements.values().forEach(m -> {
            packetBuffer.writeVarInt(m.size());
            m.values().forEach(measurement -> measurement.serializeInto(packetBuffer));
        });
    }

    @Override
    public void deserializeFrom(final @NotNull PacketBuffer packetBuffer)
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
          .flatMap(Collection::stream)
          .collect(Collectors.toList());

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

    @OnlyIn(Dist.CLIENT)
    public void createAndSend(
      final Vector3d from, final Vector3d to, final MeasuringMode mode
    ) {
        if (Minecraft.getInstance().world == null || Minecraft.getInstance().player == null)
            return;

        final Measurement measurement = this.create(
          Minecraft.getInstance().world,
          Minecraft.getInstance().player,
          from,
          to,
          mode
        );

        final MeasurementUpdatedPacket packet = new MeasurementUpdatedPacket(measurement);

        ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(packet);
    }
}
