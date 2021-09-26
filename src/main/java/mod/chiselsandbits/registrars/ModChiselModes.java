package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.modes.cubed.CubedChiselModeBuilder;
import mod.chiselsandbits.chiseling.modes.line.LinedChiselModeBuilder;
import mod.chiselsandbits.chiseling.modes.plane.PlaneChiselModeBuilder;
import mod.chiselsandbits.chiseling.modes.sphere.SphereChiselModeBuilder;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@SuppressWarnings("unused")
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

    public static final RegistryObject<IChiselMode> LINE_ONE = MODE_REGISTRAR.register(
      "line_1",
      () -> new LinedChiselModeBuilder()
            .setBitsPerSide(1)
            .setDisplayName(
              TranslationUtils.build(
                "chiselmode.line1"
              )
            )
           .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/line.png"))
           .createLinedChiselMode()
    );

    public static final RegistryObject<IChiselMode> LINE_TWO = MODE_REGISTRAR.register(
      "line_2",
      () -> new LinedChiselModeBuilder()
              .setBitsPerSide(2)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.line2"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/line2.png"))
              .createLinedChiselMode()
    );

    public static final RegistryObject<IChiselMode> LINE_FOUR = MODE_REGISTRAR.register(
      "line_4",
      () -> new LinedChiselModeBuilder()
              .setBitsPerSide(4)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.line4"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/line4.png"))
              .createLinedChiselMode()
    );

    public static final RegistryObject<IChiselMode> LINE_EIGHT = MODE_REGISTRAR.register(
      "line_8",
      () -> new LinedChiselModeBuilder()
              .setBitsPerSide(8)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.line8"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/line8.png"))
              .createLinedChiselMode()
    );

    public static final RegistryObject<IChiselMode> PLANE_ONE = MODE_REGISTRAR.register(
      "plane_1",
      () -> new PlaneChiselModeBuilder()
              .setDepth(1)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane1"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane.png"))
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> PLANE_TWO = MODE_REGISTRAR.register(
      "plane_2",
      () -> new PlaneChiselModeBuilder()
              .setDepth(2)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane2"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane2.png"))
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> PLANE_FOUR = MODE_REGISTRAR.register(
      "plane_4",
      () -> new PlaneChiselModeBuilder()
              .setDepth(4)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane4"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane4.png"))
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> PLANE_EIGHT = MODE_REGISTRAR.register(
      "plane_8",
      () -> new PlaneChiselModeBuilder()
              .setDepth(8)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane8"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane8.png"))
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_ONE = MODE_REGISTRAR.register(
      "connected_material_1",
      () -> new PlaneChiselModeBuilder()
              .setDepth(1)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane1"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane.png"))
              .withFilterOnTarget()
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_TWO = MODE_REGISTRAR.register(
      "connected_material_2",
      () -> new PlaneChiselModeBuilder()
              .setDepth(2)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane2"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane2.png"))
              .withFilterOnTarget()
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_FOUR = MODE_REGISTRAR.register(
      "connected_material_4",
      () -> new PlaneChiselModeBuilder()
              .setDepth(4)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane4"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane4.png"))
              .withFilterOnTarget()
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_EIGHT = MODE_REGISTRAR.register(
      "connected_material_8",
      () -> new PlaneChiselModeBuilder()
              .setDepth(8)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.plane8"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/plane8.png"))
              .withFilterOnTarget()
              .createPlaneChiselMode()
    );

    public static final RegistryObject<IChiselMode> SMALL_SPHERE = MODE_REGISTRAR.register(
      "small_sphere",
      () -> new SphereChiselModeBuilder().setDiameter(4)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.sphere_small"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/sphere_small.png"))
              .createSphereChiselMode()
    );

    public static final RegistryObject<IChiselMode> MEDIUM_SPHERE = MODE_REGISTRAR.register(
      "medium_sphere",
      () -> new SphereChiselModeBuilder().setDiameter(8)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.sphere_medium"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/sphere_medium.png"))
              .createSphereChiselMode()
    );

    public static final RegistryObject<IChiselMode> LARGE_SPHERE = MODE_REGISTRAR.register(
      "large_sphere",
      () -> new SphereChiselModeBuilder().setDiameter(16)
              .setDisplayName(
                TranslationUtils.build(
                  "chiselmode.sphere_large"
                )
              )
              .setIconName(new ResourceLocation(Constants.MOD_ID,"textures/icons/sphere_large.png"))
              .createSphereChiselMode()
    );
    
    public static void onModConstruction() {
        REGISTRY = MODE_REGISTRAR.makeRegistry("chisel_mode", () -> new RegistryBuilder<IChiselMode>()
                                                                     .allowModification());
        MODE_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
