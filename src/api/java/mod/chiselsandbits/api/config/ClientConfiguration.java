package mod.chiselsandbits.api.config;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.util.constants.Constants;
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
        createCategory(builder, "client.settings");
        
        enableRightClickModeChange = defineBoolean(builder, "enable-right-click-mode-change", false);
        invertBitBagFullness = defineBoolean(builder, "invert-bit-bag-fullness", false);
        enableToolbarIcons = defineBoolean(builder, "enable.toolbar.icons", true);
        perChiselMode = defineBoolean(builder, "per-chisel-mode", true);
        chatModeNotification = defineBoolean(builder, "chat-mode-notification", true);
        itemNameModeDisplay = defineBoolean(builder, "item-name-mode-display", true);
        addBrokenBlocksToCreativeClipboard = defineBoolean(builder, "clipboard.add-broken-blocks", false);
        maxUndoLevel = defineInteger(builder, "undo.max-count", 10);
        maxTapeMeasures = defineInteger(builder, "tape-measure.max-count", 10);
        displayMeasuringTapeInChat = defineBoolean(builder, "tape-measure.display-in-chat", true);
        radialMenuVolume = defineDouble(builder, "radial.menu.volume", 0.1f);

        previewRenderer = defineString(builder, "preview.renderer", Constants.MOD_ID + ":default");
        previewChiselingColor = defineList(builder, "preview.chisel.color", Lists.newArrayList(0.85f, 0.0f, 0.0f, 0.65f), value -> true);
        previewPlacementColor = defineList(builder, "preview.placement.color", Lists.newArrayList(0.0f, 0.85f, 0.0f, 0.65f), (value) -> true);

        finishCategory(builder);
        createCategory(builder, "client.performance");

        bitStorageContentCacheSize = defineLong(builder, "bit-storage.contents.cache.size", 100, 0, Long.MAX_VALUE);
        maxDrawnRegionSize = defineDouble(builder, "max-drawn-region.size", 4);
        enableFaceLightmapExtraction = defineBoolean(builder, "lighting.face-lightmap-extraction", true);
        useGetLightValue = defineBoolean(builder, "lighting.use-value", true);
        disableCustomVertexFormats = defineBoolean(builder, "vertexformats.custom.disabled", true);
        modelCacheSize = defineLong(builder, "models.cache.size", 10000, 3500, 20000);
        faceLayerCacheSize = defineLong(builder, "faces.cache.size", 10000, 3500, 20000);
        modelBuildingThreadCount = defineInteger(builder, "models.builder.threadcount", 1, 1, Runtime.getRuntime()
          .availableProcessors());

        finishCategory(builder);
        createCategory(builder, "client.gui");

        enableMouseIndicatorInRadialMenu = defineBoolean(builder, "radial-menu.mouse-indicator", false);

        finishCategory(builder);
        createCategory(builder, "compat.jei");

        injectIntoJEI = defineBoolean(builder, "inject-bits", true);

        finishCategory(builder);

    }
}