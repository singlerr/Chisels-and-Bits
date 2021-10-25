package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.metadata.IMetadataKey;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.metadata.SimpleMetadataKey;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

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
    private static final DeferredRegister<IMetadataKey<?>> KEY_REGISTRAR = (DeferredRegister<IMetadataKey<?>>) (Object) DeferredRegister.create(IMetadataKey.class, Constants.MOD_ID);

    public static final Supplier<IForgeRegistry<IMetadataKey<?>>>
      REGISTRY_SUPPLIER = KEY_REGISTRAR.makeRegistry("metadata_key", () -> new RegistryBuilder<IMetadataKey<?>>()
                                                                                             .allowModification()
                                                                                             .disableOverrides()
                                                                                             .disableSaving()
    );

    public static final RegistryObject<IMetadataKey<Vector3d>> ANCHOR = KEY_REGISTRAR.register("anchor", () -> new SimpleMetadataKey<Vector3d>() {

        @Override
        public Vector3d snapshot(final Vector3d value)
        {
            return new Vector3d(value.x(), value.y(), value.z());
        }
    });

    public static final RegistryObject<IMetadataKey<Vector3d>> END_POINT = KEY_REGISTRAR.register("endpoint", () -> new SimpleMetadataKey<Vector3d>() {

        @Override
        public Vector3d snapshot(final Vector3d value)
        {
            return new Vector3d(value.x(), value.y(), value.z());
        }
    });

    public static final RegistryObject<IMetadataKey<Direction>> TARGETED_SIDE = KEY_REGISTRAR.register("targeted_side", () -> new SimpleMetadataKey<Direction>() {
        @Override
        public Direction snapshot(final Direction value)
        {
            return value;
        }
    });

    public static final RegistryObject<IMetadataKey<Set<Vector3i>>> VALID_POSITIONS = KEY_REGISTRAR.register("valid_positions", () -> new SimpleMetadataKey<Set<Vector3i>>() {
        @Override
        public Set<Vector3i> snapshot(final Set<Vector3i> value)
        {
            return value.stream()
                     .map(val -> new Vector3i(val.getX(), val.getY(), val.getZ()))
                     .collect(Collectors.toSet());
        }
    });

    public static final RegistryObject<IMetadataKey<BlockPos>> TARGETED_BLOCK = KEY_REGISTRAR.register("targeted_block", () -> new SimpleMetadataKey<BlockPos>() {
        @Override
        public BlockPos snapshot(final BlockPos value)
        {
            return value;
        }
    });

    public static void onModConstruction() {
        KEY_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
