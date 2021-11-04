package mod.chiselsandbits.api.config;

import java.util.List;
import java.util.function.Supplier;

/**
 * All client configuration values that are relevant for the client side of C&B.
 *
 * These options are only available on the client, and are not synced between
 * the server and the client.
 */
public interface IClientConfiguration
{
    /**
     * The client configuration.
     * Elements in this configuration are only relevant for the client side of C&B.
     * This configuration does not need to be in-sync with the server values.
     *
     * @return The client configuration.
     */
    static IClientConfiguration getInstance() {
        return IChiselsAndBitsConfiguration.getInstance().getClient();
    }

    /**
     * Indicates if currently it is possible to change modes using right clicks.
     *
     * @return A configuration supplier that indicates if right click based mode changes are possible.
     */
    Supplier<Boolean> getEnableRightClickModeChange();

    /**
     * Indicates if currently the bit bag fullness indication via the damage bar of the stack
     * shows fullness or emptynes.
     *
     * @return A configuration supplier that indicates the current bit bag fullness indication system.
     */
    Supplier<Boolean> getInvertBitBagFullness();

    Supplier<Boolean> getEnableToolbarIcons();

    Supplier<Boolean> getPerChiselMode();

    Supplier<Boolean> getChatModeNotification();

    Supplier<Boolean> getItemNameModeDisplay();

    Supplier<Boolean> getAddBrokenBlocksToCreativeClipboard();

    Supplier<Integer> getMaxUndoLevel();

    Supplier<Integer> getMaxTapeMeasures();

    Supplier<Boolean> getDisplayMeasuringTapeInChat();

    Supplier<Double> getRadialMenuVolume();

    Supplier<List<? extends Float>> getPreviewChiselingColor();

    Supplier<List<? extends Float>> getPreviewPlacementColor();

    Supplier<String> getPreviewRenderer();

    Supplier<String> getToolModeRenderer();

    Supplier<Long> getBitStorageContentCacheSize();

    Supplier<Double> getMaxDrawnRegionSize();

    Supplier<Boolean> getEnableFaceLightmapExtraction();

    Supplier<Boolean> getUseGetLightValue();

    Supplier<Boolean> getDisableCustomVertexFormats();

    Supplier<Boolean> getEnableMouseIndicatorInRadialMenu();

    Supplier<Long> getModelCacheSize();

    Supplier<Long> getFaceLayerCacheSize();

    Supplier<Integer> getModelBuildingThreadCount();

    Supplier<Boolean> getInjectIntoJEI();
}
