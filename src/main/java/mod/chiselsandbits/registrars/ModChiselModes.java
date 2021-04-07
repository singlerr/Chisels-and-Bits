package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.CubedChiselModeBuilder;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryBuilder;

public final class ModChiselModes
{
    private static final DeferredRegister<IChiselMode> MODE_REGISTRAR = DeferredRegister.create(IChiselMode.class, Constants.MOD_ID);

    private ModChiselModes()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModChiselModes. This is a utility class");
    }

    public static final RegistryObject<IChiselMode> SINGLE_BIT = MODE_REGISTRAR.register(
      "single_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(1)
        .setDisplayName(
          TranslationUtils.build(
            "chisel.modes.single"
          )
        )
        .createCubedChiselMode()
    );

    public static void onModConstruction() {
        MODE_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
        MODE_REGISTRAR.makeRegistry("chiselMode", RegistryBuilder::new);
    }
}
