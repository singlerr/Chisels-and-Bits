package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.metadata.IMetadataKey;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.metadata.SimpleMetadataKey;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

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

    public static final RegistryObject<IMetadataKey<Vec3>> ANCHOR = KEY_REGISTRAR.register("anchor", () -> new SimpleMetadataKey<Vec3>() {

        @Override
        public Vec3 snapshot(final Vec3 value)
        {
            return new Vec3(value.x(), value.y(), value.z());
        }
    });

    public static final RegistryObject<IMetadataKey<Vec3>> END_POINT = KEY_REGISTRAR.register("endpoint", () -> new SimpleMetadataKey<Vec3>() {

        @Override
        public Vec3 snapshot(final Vec3 value)
        {
            return new Vec3(value.x(), value.y(), value.z());
        }
    });

    public static void onModConstruction() {
        KEY_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
