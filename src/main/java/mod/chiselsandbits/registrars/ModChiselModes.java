package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.IChiselMode;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.CubedChiselModeBuilder;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

public final class ModChiselModes
{
    private static final DeferredRegister<IChiselMode> MODE_REGISTRAR = DeferredRegister.create(IChiselMode.class, Constants.MOD_ID);

    private ModChiselModes()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModChiselModes. This is a utility class");
    }

    public static Supplier<IForgeRegistry<IChiselMode>> REGISTRY = () -> {throw new IllegalStateException("Registry is not setup yet. Use a Deferred Register!"); };

    public static final RegistryObject<IChiselMode> SINGLE_BIT = MODE_REGISTRAR.register(
      "single_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(1)
        .setDisplayName(
          TranslationUtils.build(
            "chiselmode.single"
          )
        )
        .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/bit.png"))
        .createCubedChiselMode()
    );

    public static final RegistryObject<IChiselMode> SMALL_BIT = MODE_REGISTRAR.register(
      "small_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(2)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.cube_small"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/cube_small.png"))
              .createCubedChiselMode()
    );

    public static final RegistryObject<IChiselMode> MEDIUM_BIT = MODE_REGISTRAR.register(
      "medium_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(4)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.cube_medium"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/cube_medium.png"))
              .createCubedChiselMode()
    );

    public static final RegistryObject<IChiselMode> LARGE_BIT = MODE_REGISTRAR.register(
      "large_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(8)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.cube_large"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/cube_large.png"))
              .createCubedChiselMode()
    );

    public static final RegistryObject<IChiselMode> SMALL_BIT_ALIGNED = MODE_REGISTRAR.register(
      "small_bit_aligned",
      () -> new CubedChiselModeBuilder().setBitsPerSide(2)
              .setAligned(true)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.snap2"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/snap2.png"))
              .createCubedChiselMode()
    );

    public static final RegistryObject<IChiselMode> MEDIUM_BIT_ALIGNED = MODE_REGISTRAR.register(
      "medium_bit_aligned",
      () -> new CubedChiselModeBuilder().setBitsPerSide(4)
              .setAligned(true)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.snap4"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/snap4.png"))
              .createCubedChiselMode()
    );

    public static final RegistryObject<IChiselMode> LARGE_BIT_ALIGNED = MODE_REGISTRAR.register(
      "large_bit_aligned",
      () -> new CubedChiselModeBuilder().setBitsPerSide(8)
              .setAligned(true)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.snap8"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/snap8.png"))
              .createCubedChiselMode()
    );



    public static void onModConstruction() {
        REGISTRY = MODE_REGISTRAR.makeRegistry("chisel_mode", () -> new RegistryBuilder<IChiselMode>()
                                                                     .allowModification());
        MODE_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
