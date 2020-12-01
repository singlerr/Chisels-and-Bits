package mod.chiselsandbits.config;

import net.minecraftforge.common.ForgeConfigSpec;

import static mod.chiselsandbits.config.AbstractConfiguration.defineBoolean;

/**
 * Mod client configuration.
 * Loaded clientside, not synced.
 */
public class ClientConfiguration extends AbstractConfiguration
{
    public ForgeConfigSpec.BooleanValue enableRightClickModeChange;
    public ForgeConfigSpec.BooleanValue showUsage;
    public ForgeConfigSpec.BooleanValue invertBitBagFullness;
    public ForgeConfigSpec.BooleanValue enableChiselMode_Plane;
    public ForgeConfigSpec.BooleanValue enableChiselMode_SameMaterial;
    public ForgeConfigSpec.BooleanValue enableChiselMode_ConnectedPlane;
    public ForgeConfigSpec.BooleanValue enableChiselMode_ConnectedMaterial;
    public ForgeConfigSpec.BooleanValue enableChiselMode_Line;
    public ForgeConfigSpec.BooleanValue enableChiselMode_SmallCube;
    public ForgeConfigSpec.BooleanValue enableChiselMode_MediumCube;
    public ForgeConfigSpec.BooleanValue enableChiselMode_LargeCube;
    public ForgeConfigSpec.BooleanValue enableChiselMode_DrawnRegion;
    public ForgeConfigSpec.BooleanValue enableChiselMode_Snap2;
    public ForgeConfigSpec.BooleanValue enableChiselMode_Snap4;
    public ForgeConfigSpec.BooleanValue enableChiselMode_Snap8;
    public ForgeConfigSpec.BooleanValue enablePositivePatternMode_Additive;
    public ForgeConfigSpec.BooleanValue enablePositivePatternMode_Impose;
    public ForgeConfigSpec.BooleanValue enablePositivePatternMode_Placement;
    public ForgeConfigSpec.BooleanValue enablePositivePatternMode_Replace;
    public ForgeConfigSpec.BooleanValue enableTapeMeasure_Bit;
    public ForgeConfigSpec.BooleanValue enableTapeMeasure_Block;
    public ForgeConfigSpec.BooleanValue enableTapeMeasure_Distance;
    public ForgeConfigSpec.BooleanValue enableToolbarIcons;
    public ForgeConfigSpec.BooleanValue perChiselMode;
    public ForgeConfigSpec.BooleanValue chatModeNotification;
    public ForgeConfigSpec.BooleanValue itemNameModeDisplay;
    public ForgeConfigSpec.BooleanValue addBrokenBlocksToCreativeClipboard;
    public ForgeConfigSpec.BooleanValue fluidBitsAreClickThrough;
    public ForgeConfigSpec.BooleanValue persistCreativeClipboard;
    public ForgeConfigSpec.IntValue maxUndoLevel;
    public ForgeConfigSpec.IntValue maxTapeMeasures;
    public ForgeConfigSpec.BooleanValue displayMeasuringTapeInChat;
    public ForgeConfigSpec.DoubleValue radialMenuVolume;
    
    public ForgeConfigSpec.IntValue maxMillisecondsPerBlock;
    public ForgeConfigSpec.IntValue maxMillisecondsUploadingPerFrame;
    public ForgeConfigSpec.IntValue dynamicModelFaceCount;
    public ForgeConfigSpec.IntValue dynamicModelRange;
    public ForgeConfigSpec.BooleanValue dynamicModelMinimizeLatancy;
    public ForgeConfigSpec.LongValue minimizeLatancyMaxTime;
    public ForgeConfigSpec.IntValue dynamicMaxConcurrentTessalators;
    public ForgeConfigSpec.BooleanValue forceDynamicRenderer;
    public ForgeConfigSpec.BooleanValue defaultToDynamicRenderer;
    public ForgeConfigSpec.BooleanValue dynamicRenderFullChunksOnly;

    public ForgeConfigSpec.LongValue bitStorageContentCacheSize;

    /**
     * Builds client configuration.
     *
     * @param builder config builder
     */
    protected ClientConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "client.settings");
        
        enableRightClickModeChange = defineBoolean(builder, "client.enable-right-click-mode-change", false);
        showUsage = defineBoolean(builder, "client.settings.show-usage", true);
        invertBitBagFullness = defineBoolean(builder, "client.settings.invert-bit-bag-fullness", false);
        enableChiselMode_Plane = defineBoolean(builder, "client.settings.enable.chisel-mode.plane", true);
        enableChiselMode_SameMaterial = defineBoolean(builder, "client.settings.enable.chisel-mode.same-material", true);
        enableChiselMode_ConnectedPlane = defineBoolean(builder, "client.settings.enable.chisel-mode.connected-plane", true);
        enableChiselMode_ConnectedMaterial = defineBoolean(builder, "client.settings.enable.chisel-mode.connected-material", true);
        enableChiselMode_Line = defineBoolean(builder, "client.settings.enable.chisel-mode.line", true);
        enableChiselMode_SmallCube = defineBoolean(builder, "client.settings.enable.chisel-mode.cube-small", true);
        enableChiselMode_MediumCube = defineBoolean(builder, "client.settings.enable.chisel-mode.cube-medium", true);
        enableChiselMode_LargeCube = defineBoolean(builder, "client.settings.enable.chisel-mode.cube-large", true);
        enableChiselMode_DrawnRegion = defineBoolean(builder, "client.settings.enable.chisel-mode.drawn-region", true);
        enableChiselMode_Snap2 = defineBoolean(builder, "client.settings.enable.chisel-mode.snap-2", true);
        enableChiselMode_Snap4 = defineBoolean(builder, "client.settings.enable.chisel-mode.snap-4", true);
        enableChiselMode_Snap8 = defineBoolean(builder, "client.settings.enable.chisel-mode.snap-8", true);
        enablePositivePatternMode_Additive = defineBoolean(builder, "client.settings.enable.pattern-mode.additive", true);
        enablePositivePatternMode_Impose = defineBoolean(builder, "client.settings.enable.pattern-mode.impose", true);
        enablePositivePatternMode_Placement = defineBoolean(builder, "client.settings.enable.pattern-mode.placement", true);
        enablePositivePatternMode_Replace = defineBoolean(builder, "client.settings.enable.pattern-mode.replace", true);
        enableTapeMeasure_Bit = defineBoolean(builder, "client.settings.enable.tape-measure.bit", true);
        enableTapeMeasure_Block = defineBoolean(builder, "client.settings.enable.tape-measure.block", true);
        enableTapeMeasure_Distance = defineBoolean(builder, "client.settings.enable.tape-measure.distance", true);
        enableToolbarIcons = defineBoolean(builder, "client.settings.enable.toolbar.icons", true);;
        perChiselMode = defineBoolean(builder, "client.settings.per-chisel-mode", true);;
        chatModeNotification = defineBoolean(builder, "client.settings.chat-mode-notification", true);;
        itemNameModeDisplay = defineBoolean(builder, "client.settings.item-name-mode-display", true);;
        addBrokenBlocksToCreativeClipboard = defineBoolean(builder, "client.settings.clipboard.add-broken-blocks", false);
        fluidBitsAreClickThrough = defineBoolean(builder, "client.settings.bits.fluid.click-through", false);
        persistCreativeClipboard = defineBoolean(builder, "client.settings.clipboard.persist.creative", true);
        maxUndoLevel = defineInteger(builder, "client.settings.undo.max-count", 10);
        maxTapeMeasures = defineInteger(builder, "client.settings.tape-measure.max-count", 10);
        displayMeasuringTapeInChat = defineBoolean(builder, "client.settings.tape-measure.display-in-chat", true);
        radialMenuVolume = defineDouble(builder, "settings.radial.menu.volume", 0.1f);
        
        finishCategory(builder);
        createCategory(builder, "client.performance");

        maxMillisecondsPerBlock = defineInteger(builder, "client.performance.timings.max-milliseconds-per-block", 10);
        maxMillisecondsUploadingPerFrame = defineInteger(builder, "client.performance.timings.max-milliseconds-uploading-per-frame", 15);
        dynamicModelFaceCount = defineInteger(builder, "client.performance.dynamic-models.face-count", 40);
        dynamicModelRange = defineInteger(builder, "client.performance.dynamic-models.range", 128);
        dynamicModelMinimizeLatancy = defineBoolean(builder, "client.performance.dynamic-models.minimize-latency", true);
        minimizeLatancyMaxTime = defineLong(builder, "client.performance.latency.max-time", 100);
        dynamicMaxConcurrentTessalators = defineInteger(builder, "client.performance.tesselators.max-concurrent", 32);
        forceDynamicRenderer = defineBoolean(builder, "client.performance.dynamic-rendering.force", false);
        defaultToDynamicRenderer = defineBoolean(builder, "client.performance.dynamic-rendering.default", false);
        dynamicRenderFullChunksOnly = defineBoolean(builder, "client.performance.dynamic-rendering.full-chunks-only", false);
        bitStorageContentCacheSize = defineLong(builder, "client.performance.bit-storage.contents.cache.size", 100, 0, Long.MAX_VALUE);

        finishCategory(builder);

    }
}