package mod.chiselsandbits.registrars;

import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.chiseling.modes.connected.material.ConnectedMaterialChiselingMode;
import mod.chiselsandbits.chiseling.modes.connected.material.ConnectedMaterialChiselingModeBuilder;
import mod.chiselsandbits.chiseling.modes.connected.plane.ConnectedPlaneChiselingModeBuilder;
import mod.chiselsandbits.chiseling.modes.cubed.CubedChiselModeBuilder;
import mod.chiselsandbits.chiseling.modes.line.LinedChiselModeBuilder;
import mod.chiselsandbits.chiseling.modes.plane.PlaneChiselModeBuilder;
import mod.chiselsandbits.chiseling.modes.replace.ReplaceChiselingModeBuilder;
import mod.chiselsandbits.chiseling.modes.sphere.SphereChiselModeBuilder;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.function.Supplier;

@SuppressWarnings("unused")
public final class ModChiselModes
{
    private static final DeferredRegister<IChiselMode> MODE_REGISTRAR = DeferredRegister.create(IChiselMode.class, Constants.MOD_ID);
    public static final RegistryObject<IChiselMode> SINGLE_BIT = MODE_REGISTRAR.register(
      "single_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(1)
        .setDisplayName(LocalStrings.ChiselModeSingle.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineSingle.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/bit.png"))
        .createCubedChiselMode()
    );
    public static final RegistryObject<IChiselMode> SMALL_BIT = MODE_REGISTRAR.register(
      "small_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(2)
        .setDisplayName(LocalStrings.ChiselModeCubeSmall.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineCubeSmall.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/cube_small.png"))
        .createCubedChiselMode()
    );
    public static final RegistryObject<IChiselMode> MEDIUM_BIT = MODE_REGISTRAR.register(
      "medium_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(4)
        .setDisplayName(LocalStrings.ChiselModeCubeMedium.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineCubeMedium.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/cube_medium.png"))
        .createCubedChiselMode()
    );
    public static final RegistryObject<IChiselMode> LARGE_BIT = MODE_REGISTRAR.register(
      "large_bit",
      () -> new CubedChiselModeBuilder().setBitsPerSide(8)
        .setDisplayName(LocalStrings.ChiselModeCubeLarge.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineCubeLarge.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/cube_large.png"))
        .createCubedChiselMode()
    );
    public static final RegistryObject<IChiselMode> SMALL_BIT_ALIGNED = MODE_REGISTRAR.register(
      "small_bit_aligned",
      () -> new CubedChiselModeBuilder().setBitsPerSide(2)
        .setAligned(true)
        .setDisplayName(LocalStrings.ChiselModeSnap2.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineSnap2.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/snap2.png"))
        .createCubedChiselMode()
    );
    public static final RegistryObject<IChiselMode> MEDIUM_BIT_ALIGNED = MODE_REGISTRAR.register(
      "medium_bit_aligned",
      () -> new CubedChiselModeBuilder().setBitsPerSide(4)
        .setAligned(true)
        .setDisplayName(LocalStrings.ChiselModeSnap4.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineSnap4.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/snap4.png"))
        .createCubedChiselMode()
    );
    public static final RegistryObject<IChiselMode> LARGE_BIT_ALIGNED = MODE_REGISTRAR.register(
      "large_bit_aligned",
      () -> new CubedChiselModeBuilder().setBitsPerSide(8)
        .setAligned(true)
        .setDisplayName(LocalStrings.ChiselModeSnap8.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineSnap8.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/snap8.png"))
        .createCubedChiselMode()
    );
    public static final RegistryObject<IChiselMode> LINE_ONE = MODE_REGISTRAR.register(
      "line_1",
      () -> new LinedChiselModeBuilder()
        .setBitsPerSide(1)
        .setDisplayName(LocalStrings.ChiselModeLine.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineLine.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/line.png"))
        .createLinedChiselMode()
    );
    public static final RegistryObject<IChiselMode> LINE_TWO = MODE_REGISTRAR.register(
      "line_2",
      () -> new LinedChiselModeBuilder()
        .setBitsPerSide(2)
        .setDisplayName(LocalStrings.ChiselModeLine2.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineLine2.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/line2.png"))
        .createLinedChiselMode()
    );
    public static final RegistryObject<IChiselMode> LINE_FOUR = MODE_REGISTRAR.register(
      "line_4",
      () -> new LinedChiselModeBuilder()
        .setBitsPerSide(4)
        .setDisplayName(LocalStrings.ChiselModeLine4.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineLine4.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/line4.png"))
        .createLinedChiselMode()
    );
    public static final RegistryObject<IChiselMode> LINE_EIGHT = MODE_REGISTRAR.register(
      "line_8",
      () -> new LinedChiselModeBuilder()
        .setBitsPerSide(8)
        .setDisplayName(LocalStrings.ChiselModeLine8.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineLine8.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/line8.png"))
        .createLinedChiselMode()
    );
    public static final RegistryObject<IChiselMode> PLANE_ONE = MODE_REGISTRAR.register(
      "plane_1",
      () -> new PlaneChiselModeBuilder()
        .setDepth(1)
        .setDisplayName(LocalStrings.ChiselModePlane.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane.png"))
        .createPlaneChiselMode()
    );
    public static final RegistryObject<IChiselMode> PLANE_TWO = MODE_REGISTRAR.register(
      "plane_2",
      () -> new PlaneChiselModeBuilder()
        .setDepth(2)
        .setDisplayName(LocalStrings.ChiselModePlane2.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane2.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane2.png"))
        .createPlaneChiselMode()
    );
    public static final RegistryObject<IChiselMode> PLANE_FOUR = MODE_REGISTRAR.register(
      "plane_4",
      () -> new PlaneChiselModeBuilder()
        .setDepth(4)
        .setDisplayName(LocalStrings.ChiselModePlane4.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane4.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane4.png"))
        .createPlaneChiselMode()
    );
    public static final RegistryObject<IChiselMode> PLANE_EIGHT = MODE_REGISTRAR.register(
      "plane_8",
      () -> new PlaneChiselModeBuilder()
        .setDepth(8)
        .setDisplayName(LocalStrings.ChiselModePlane8.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane8.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane8.png"))
        .createPlaneChiselMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_ONE = MODE_REGISTRAR.register(
      "connected_material_1",
      () -> new ConnectedMaterialChiselingModeBuilder()
        .setDepth(1)
        .setDisplayName(LocalStrings.ChiselModePlane.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane.png"))
        .createConnectedMaterialChiselingMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_TWO = MODE_REGISTRAR.register(
      "connected_material_2",
      () -> new ConnectedMaterialChiselingModeBuilder()
        .setDepth(2)
        .setDisplayName(LocalStrings.ChiselModePlane2.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane2.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane2.png"))
        .createConnectedMaterialChiselingMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_FOUR = MODE_REGISTRAR.register(
      "connected_material_4",
      () -> new ConnectedMaterialChiselingModeBuilder()
        .setDepth(4)
        .setDisplayName(LocalStrings.ChiselModePlane4.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane4.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane4.png"))
        .createConnectedMaterialChiselingMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_MATERIAL_EIGHT = MODE_REGISTRAR.register(
      "connected_material_8",
      () -> new ConnectedMaterialChiselingModeBuilder()
        .setDepth(8)
        .setDisplayName(LocalStrings.ChiselModePlane8.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane8.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane8.png"))
        .createConnectedMaterialChiselingMode()
    );
    public static final RegistryObject<IChiselMode> SMALL_SPHERE = MODE_REGISTRAR.register(
      "small_sphere",
      () -> new SphereChiselModeBuilder().setDiameter(4)
        .setDisplayName(LocalStrings.ChiselModeSphereSmall.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineSphereSmall.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/sphere_small.png"))
        .createSphereChiselMode()
    );
    public static final RegistryObject<IChiselMode> MEDIUM_SPHERE = MODE_REGISTRAR.register(
      "medium_sphere",
      () -> new SphereChiselModeBuilder().setDiameter(8)
        .setDisplayName(LocalStrings.ChiselModeSphereMedium.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineSphereMedium.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/sphere_medium.png"))
        .createSphereChiselMode()
    );
    public static final RegistryObject<IChiselMode> LARGE_SPHERE = MODE_REGISTRAR.register(
      "large_sphere",
      () -> new SphereChiselModeBuilder().setDiameter(16)
        .setDisplayName(LocalStrings.ChiselModeSphereLarge.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineSphereLarge.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/sphere_large.png"))
        .createSphereChiselMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_PLANE_ONE = MODE_REGISTRAR.register(
      "connected_plane_1",
      () -> new ConnectedPlaneChiselingModeBuilder()
        .setDepth(1)
        .setDisplayName(LocalStrings.ChiselModePlane.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane.png"))
        .createConnectedPlaneChiselingMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_PLANE_TWO = MODE_REGISTRAR.register(
      "connected_plane_2",
      () -> new ConnectedPlaneChiselingModeBuilder()
        .setDepth(2)
        .setDisplayName(LocalStrings.ChiselModePlane2.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane2.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane2.png"))
        .createConnectedPlaneChiselingMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_PLANE_FOUR = MODE_REGISTRAR.register(
      "connected_plane_4",
      () -> new ConnectedPlaneChiselingModeBuilder()
        .setDepth(4)
        .setDisplayName(LocalStrings.ChiselModePlane4.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane4.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane4.png"))
        .createConnectedPlaneChiselingMode()
    );
    public static final RegistryObject<IChiselMode> CONNECTED_PLANE_EIGHT = MODE_REGISTRAR.register(
      "connected_plane_8",
      () -> new ConnectedPlaneChiselingModeBuilder()
        .setDepth(8)
        .setDisplayName(LocalStrings.ChiselModePlane8.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLinePlane8.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/plane8.png"))
        .createConnectedPlaneChiselingMode()
    );
    public static final RegistryObject<IChiselMode> REPLACE = MODE_REGISTRAR.register(
      "replace",
      () -> new ReplaceChiselingModeBuilder()
        .setDisplayName(LocalStrings.ChiselModeReplace.getText())
        .setMultiLineDisplayName(LocalStrings.ChiselModeMultiLineReplace.getText())
        .setIconName(new ResourceLocation(Constants.MOD_ID, "textures/icons/replace.png"))
        .createReplaceChiselingMode()
    );
    public static Supplier<IForgeRegistry<IChiselMode>> REGISTRY = () -> {throw new IllegalStateException("Registry is not setup yet. Use a Deferred Register!");};

    private ModChiselModes()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ModChiselModes. This is a utility class");
    }

    public static void onModConstruction()
    {
        REGISTRY = MODE_REGISTRAR.makeRegistry("chisel_mode", () -> new RegistryBuilder<IChiselMode>()
          .allowModification());
        MODE_REGISTRAR.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
