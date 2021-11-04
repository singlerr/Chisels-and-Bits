package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.metadata.IMetadataKey;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.metadata.SimpleMetadataKey;
import mod.chiselsandbits.platforms.core.registries.IChiselsAndBitsRegistry;
import mod.chiselsandbits.platforms.core.registries.deferred.ICustomRegistrar;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public final class ModMetadataKeys
{

    private ModMetadataKeys()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModMetadataKeys. This is a utility class");
    }

    @SuppressWarnings("unchecked") //Blah blah metadata is generic i know, the compiler erases it at runtime anyway..... Just do what i tell you.
    private static final ICustomRegistrar<IMetadataKey<?>> KEY_REGISTRAR = ICustomRegistrar.create(IMetadataKey.class, Constants.MOD_ID);

    public static final Supplier<IChiselsAndBitsRegistry<IMetadataKey<?>>>
      REGISTRY_SUPPLIER = KEY_REGISTRAR.makeRegistry("metadata_key", IChiselsAndBitsRegistry.Builder::simple
    );

    public static final Supplier<IMetadataKey<Vec3>> ANCHOR = KEY_REGISTRAR.register("anchor", () -> new SimpleMetadataKey<>()
    {

        @Override
        public Vec3 snapshot(final Vec3 value)
        {
            return new Vec3(value.x(), value.y(), value.z());
        }
    });

    public static final Supplier<IMetadataKey<Vec3>> END_POINT = KEY_REGISTRAR.register("endpoint", () -> new SimpleMetadataKey<>()
    {

        @Override
        public Vec3 snapshot(final Vec3 value)
        {
            return new Vec3(value.x(), value.y(), value.z());
        }
    });

    public static final Supplier<IMetadataKey<Direction>> TARGETED_SIDE = KEY_REGISTRAR.register("targeted_side", () -> new SimpleMetadataKey<>()
    {
        @Override
        public Direction snapshot(final Direction value)
        {
            return value;
        }
    });

    public static final Supplier<IMetadataKey<Set<Vec3i>>> VALID_POSITIONS = KEY_REGISTRAR.register("valid_positions", () -> new SimpleMetadataKey<>()
    {
        @Override
        public Set<Vec3i> snapshot(final Set<Vec3i> value)
        {
            return value.stream()
              .map(val -> new Vec3i(val.getX(), val.getY(), val.getZ()))
              .collect(Collectors.toSet());
        }
    });

    public static final Supplier<IMetadataKey<BlockPos>> TARGETED_BLOCK = KEY_REGISTRAR.register("targeted_block", () -> new SimpleMetadataKey<>()
    {
        @Override
        public BlockPos snapshot(final BlockPos value)
        {
            return value;
        }
    });
}
