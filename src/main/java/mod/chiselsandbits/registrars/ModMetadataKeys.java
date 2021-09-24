package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.metadata.IMetadataKey;
import mod.chiselsandbits.api.modification.operation.IModificationTableOperation;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.metadata.SimpleMetadataKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ModMetadataKeys
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
            return new Vector3d(value.getX(), value.getY(), value.getZ());
        }
    });

    public static final RegistryObject<IMetadataKey<Vector3d>> END_POINT = KEY_REGISTRAR.register("endpoint", () -> new SimpleMetadataKey<Vector3d>() {

        @Override
        public Vector3d snapshot(final Vector3d value)
        {
            return new Vector3d(value.getX(), value.getY(), value.getZ());
        }
    });

    public static void onModConstruction() {
        KEY_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
