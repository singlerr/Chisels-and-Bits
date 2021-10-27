package mod.chiselsandbits.api.config;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public ForgeConfigSpec.BooleanValue             enableRightClickModeChange;
    public ForgeConfigSpec.BooleanValue             invertBitBagFullness;
    public ForgeConfigSpec.BooleanValue             enableToolbarIcons;
    public ForgeConfigSpec.BooleanValue             perChiselMode;
    public ForgeConfigSpec.BooleanValue chatModeNotification;
    public ForgeConfigSpec.BooleanValue itemNameModeDisplay;
    public ForgeConfigSpec.BooleanValue addBrokenBlocksToCreativeClipboard;
    public ForgeConfigSpec.IntValue maxUndoLevel;
    public ForgeConfigSpec.IntValue maxTapeMeasures;
    public ForgeConfigSpec.BooleanValue displayMeasuringTapeInChat;
    public ForgeConfigSpec.DoubleValue              radialMenuVolume;
    public ForgeConfigSpec.ConfigValue<List<? extends Float>> previewChiselingColor;
    public ForgeConfigSpec.ConfigValue<List<? extends Float>>    previewPlacementColor;
    public ForgeConfigSpec.ConfigValue<String> previewRenderer;

    public ForgeConfigSpec.LongValue bitStorageContentCacheSize;
    public ForgeConfigSpec.DoubleValue maxDrawnRegionSize;
    public ForgeConfigSpec.BooleanValue enableFaceLightmapExtraction;
    public ForgeConfigSpec.BooleanValue useGetLightValue;
    public ForgeConfigSpec.BooleanValue disableCustomVertexFormats;

    public ForgeConfigSpec.BooleanValue enableMouseIndicatorInRadialMenu;

    public ForgeConfigSpec.LongValue modelCacheSize;
    public ForgeConfigSpec.LongValue faceLayerCacheSize;
    public ForgeConfigSpec.IntValue modelBuildingThreadCount;

    public ForgeConfigSpec.BooleanValue injectIntoJEI;


    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "settings.bit-bag");

        invertBitBagFullness = defineBoolean(builder, "invert-durability-bar-indication", false);

        swapToCategory(builder, "settings.chiseling-previews");

        previewRenderer = defineString(builder, "renderer", Constants.MOD_ID + ":default");

        swapToCategory(builder, "settings.chiseling-previews.default.colors");

        previewChiselingColor = defineList(builder, "chiseling", Lists.newArrayList(0.85f, 0.0f, 0.0f, 0.65f), value -> true);
        previewPlacementColor = defineList(builder, "placement", Lists.newArrayList(0.0f, 0.85f, 0.0f, 0.65f), (value) -> true);

        swapToCategory(builder, "performance.caches.sizes");

        bitStorageContentCacheSize = defineLong(builder, "bit-storage-content-models", 100, 0, Long.MAX_VALUE);
        modelCacheSize = defineLong(builder, "block-models", 10000, 3500, 20000);
        faceLayerCacheSize = defineLong(builder, "block-faces", 10000, 3500, 20000);

        swapToCategory(builder, "performance.model-building");

        modelBuildingThreadCount = defineInteger(builder, "thead-count", Math.max(1, Runtime.getRuntime().availableProcessors()) / 2, 1, Runtime.getRuntime()
          .availableProcessors());

        swapToCategory(builder, "performance.lighting");

        enableFaceLightmapExtraction = defineBoolean(builder, "extract-lighting-values-from-faces", true);
        useGetLightValue = defineBoolean(builder, "extract-lighting-values-from-blockstates", true);

        swapToCategory(builder, "gui.radial-menu");

        enableMouseIndicatorInRadialMenu = defineBoolean(builder, "display-mouse-indicator", false);

        finishCategory(builder);
        createCategory(builder, "compat.jei");

        injectIntoJEI = defineBoolean(builder, "inject-bits", true);

        finishCategory(builder);
    }
}