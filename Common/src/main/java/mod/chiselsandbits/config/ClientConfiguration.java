package mod.chiselsandbits.config;

import com.google.common.collect.Lists;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.platforms.core.config.ConfigurationType;
import mod.chiselsandbits.platforms.core.config.IConfigurationBuilder;
import mod.chiselsandbits.platforms.core.config.IConfigurationManager;

import java.util.List;
import java.util.function.Supplier;

public class ClientConfiguration implements IClientConfiguration
{

    private final Supplier<Boolean>               bitBagFullness;
    private final Supplier<List<? extends Float>> previewChiselingColor;
    private final Supplier<List<? extends Float>> previewPlacementColor;
    private final Supplier<String>                previewRenderer;
    private final Supplier<String>                toolModeRenderer;
    private final Supplier<Boolean>               invertPickBlockBehaviour;
    private final Supplier<Long>                  bitStorageContentCacheSize;
    private final Supplier<Boolean>               faceLightMapExtraction;
    private final Supplier<Boolean>               blockLightEmissionExtraction;
    private final Supplier<Boolean>               radialMenuMouseIndicator;
    private final Supplier<Long>                  modelCacheSize;
    private final Supplier<Long>                  stackModelCacheSize;
    private final Supplier<Long>                  faceLayerCacheSize;
    private final Supplier<Integer>               modelBuildingThreadCount;
    private final Supplier<Integer>               clipboardSize;
    private final Supplier<Boolean>               addBrokenBlocksToClipboard;
    private final Supplier<Boolean> addPickedBlocksToClipboard;
    private final Supplier<String>        patternExportPath;

    public ClientConfiguration()
    {
        final IConfigurationBuilder builder = IConfigurationManager.getInstance().createBuilder(ConfigurationType.CLIENT_ONLY, Constants.MOD_ID + "-client");

        bitBagFullness = builder.defineBoolean("settings.bit-bag.invert-durability-bar-indication", false);
        previewChiselingColor = builder.defineList("settings.chiseling-previews.default.colors.chiseling", Lists.newArrayList(0.85f, 0.0f, 0.0f, 0.65f), Float.class);
        previewPlacementColor = builder.defineList("settings.chiseling-previews.default.colors.placement", Lists.newArrayList(0.0f, 0.85f, 0.0f, 0.65f), Float.class);
        previewRenderer = builder.defineString("settings.chiseling-previews.renderer", Constants.MOD_ID + ":default");
        toolModeRenderer = builder.defineString("settings.selected-tool-mode-icons.renderer", Constants.MOD_ID + ":group");
        invertPickBlockBehaviour = builder.defineBoolean("settings.invert-pick-block-behaviour", false);
        clipboardSize = builder.defineInteger("settings.clipboard.size", 64, 0, 64);
        addBrokenBlocksToClipboard = builder.defineBoolean("settings.clipboard.addBrokenBlocks", true);
        addPickedBlocksToClipboard = builder.defineBoolean("settings.clipboard.addPickedBlocks", true);
        patternExportPath = builder.defineString("settings.patterns.export-path", "./chiselsandbits/patterns");
        bitStorageContentCacheSize = builder.defineLong("performance.caches.sizes.bit-storage-content-models", 100, 0, Long.MAX_VALUE);
        faceLightMapExtraction = builder.defineBoolean("performance.lighting.extract-lighting-values-from-faces", true);
        blockLightEmissionExtraction = builder.defineBoolean("performance.lighting.extract-lighting-values-from-blockstates", true);
        radialMenuMouseIndicator = builder.defineBoolean("gui.radial-menu.display-mouse-indicator", false);
        modelCacheSize = builder.defineLong("performance.caches.sizes.block-models", 10000, 3500, 20000);
        faceLayerCacheSize = builder.defineLong("performance.caches.sizes.block-faces", 1000000, 350000, 2000000);
        modelBuildingThreadCount = builder.defineInteger("performance.model-building.thead-count", Math.max(1, Runtime.getRuntime().availableProcessors()) / 2, 1, Runtime.getRuntime()
          .availableProcessors());
        stackModelCacheSize = builder.defineLong("performance.caches.sizes.stack-models", 100, 0, Long.MAX_VALUE);

        builder.setup();
    }

    @Override
    public Supplier<Boolean> getInvertBitBagFullness()
    {
        return bitBagFullness;
    }

    @Override
    public Supplier<List<? extends Float>> getPreviewChiselingColor()
    {
        return previewChiselingColor;
    }

    @Override
    public Supplier<List<? extends Float>> getPreviewPlacementColor()
    {
        return previewPlacementColor;
    }

    @Override
    public Supplier<String> getPreviewRenderer()
    {
        return previewRenderer;
    }

    @Override
    public Supplier<String> getToolModeRenderer()
    {
        return toolModeRenderer;
    }

    @Override
    public Supplier<Boolean> getInvertPickBlockBehaviour()
    {
        return invertPickBlockBehaviour;
    }

    @Override
    public Supplier<Long> getBitStorageContentCacheSize()
    {
        return bitStorageContentCacheSize;
    }

    @Override
    public Supplier<Boolean> getEnableFaceLightmapExtraction()
    {
        return faceLightMapExtraction;
    }

    @Override
    public Supplier<Boolean> getUseGetLightValue()
    {
        return blockLightEmissionExtraction;
    }

    @Override
    public Supplier<Boolean> getEnableMouseIndicatorInRadialMenu()
    {
        return radialMenuMouseIndicator;
    }

    @Override
    public Supplier<Long> getModelCacheSize()
    {
        return modelCacheSize;
    }

    @Override
    public Supplier<Long> getFaceLayerCacheSize()
    {
        return faceLayerCacheSize;
    }

    @Override
    public Supplier<Integer> getModelBuildingThreadCount()
    {
        return modelBuildingThreadCount;
    }

    @Override
    public Supplier<Integer> getClipboardSize()
    {
        return clipboardSize;
    }

    @Override
    public Supplier<Boolean> getShouldBrokenBlocksBeAddedToClipboard()
    {
        return addBrokenBlocksToClipboard;
    }

    @Override
    public Supplier<Boolean> getShouldPickedBlocksBeAddedToClipboard()
    {
        return addPickedBlocksToClipboard;
    }

    @Override
    public Supplier<String> getPatternExportPath()
    {
        return patternExportPath;
    }

    @Override
    public Supplier<Long> getStackModelCacheSize()
    {
        return stackModelCacheSize;
    }
}
