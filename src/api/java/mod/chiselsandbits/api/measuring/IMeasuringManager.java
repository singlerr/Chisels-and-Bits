package mod.chiselsandbits.api.measuring;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.UUID;

public interface IMeasuringManager
{
    static IMeasuringManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getMeasuringManager();
    }

    default Collection<? extends IMeasurement> getInWorld(final World world) {
        return getInWorld(world.getDimensionKey().getLocation());
    }

    Collection<? extends IMeasurement> getInWorld(final ResourceLocation worldKey);

    default Collection<? extends IMeasurement> getForPlayer(final PlayerEntity playerEntity) {
        return getForPlayer(playerEntity.getUniqueID());
    }

    Collection<? extends IMeasurement> getForPlayer(final UUID playerId);

    IMeasurement create(
      final World world,
      final PlayerEntity playerEntity,
      final Vector3d from,
      final Vector3d to,
      final MeasuringMode mode
    );

    default void resetMeasurementsFor(PlayerEntity playerEntity) {
        resetMeasurementsFor(playerEntity.getUniqueID());
    }

    void resetMeasurementsFor(UUID playerId);
}
