package mod.chiselsandbits.config;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;
import java.util.Objects;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    public ForgeConfigSpec.BooleanValue showBitsInJEI;
    public ForgeConfigSpec.BooleanValue enableVivecraftCompatibility;
    public ForgeConfigSpec.BooleanValue logTileErrors;
    public ForgeConfigSpec.BooleanValue logEligibilityErrors;
    public ForgeConfigSpec.BooleanValue useGetLightValue;
    public ForgeConfigSpec.BooleanValue enableFaceLightmapExtraction;
    public ForgeConfigSpec.BooleanValue disableCustomVertexFormats;
    public ForgeConfigSpec.BooleanValue blackListRandomTickingBlocks;
    public ForgeConfigSpec.BooleanValue damageTools;
    public ForgeConfigSpec.BooleanValue enableChiselToolHarvestCheck;
    public ForgeConfigSpec.ConfigValue<String> enableChiselToolHarvestCheckTools;
    public ForgeConfigSpec.BooleanValue enableToolHarvestLevels;
    public ForgeConfigSpec.BooleanValue enableBitLightSource;
    public ForgeConfigSpec.DoubleValue bitLightPercentage;
    public ForgeConfigSpec.BooleanValue compatabilityMode;
    public ForgeConfigSpec.DoubleValue maxDrawnRegionSize;
    public ForgeConfigSpec.IntValue bagStackSize;
    public ForgeConfigSpec.IntValue stoneChiselUses;
    public ForgeConfigSpec.IntValue ironChiselUses;
    public ForgeConfigSpec.IntValue diamondChiselUses;
    public ForgeConfigSpec.IntValue goldChiselUses;
    public ForgeConfigSpec.IntValue wrenchUses;
    public ForgeConfigSpec.IntValue diamondSawUses;
    public ForgeConfigSpec.BooleanValue enableSetBitCommand;
    public ForgeConfigSpec.BooleanValue fullBlockCrafting;
    public ForgeConfigSpec.BooleanValue requireBagSpace;
    public ForgeConfigSpec.BooleanValue voidExcessBits;
    public ForgeConfigSpec.IntValue                            creativeClipboardSize;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> revertibleBlocks;

    public ForgeConfigSpec.BooleanValue lowMemoryMode;

    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "server.integration-settings");

        showBitsInJEI = defineBoolean(builder, "server.integration-settings.jei.show-in-jei", false);
        enableVivecraftCompatibility = defineBoolean(builder, "server.integrations-settings.vivecraft.enabled", false);

        finishCategory(builder);
        createCategory(builder, "server.troubleshooting");

        logTileErrors = defineBoolean(builder, "server.troubleshooting.logging.tile-errors", true);
        logEligibilityErrors = defineBoolean(builder, "server.troubleshooting.logging.eligibility-errors", true);
        useGetLightValue = defineBoolean(builder, "server.troubleshooting.lighting.use-value", true);
        enableFaceLightmapExtraction = defineBoolean(builder, "server.troubleshooting.lighting.face-lightmap-extraction", true);
        disableCustomVertexFormats = defineBoolean(builder, "server.troubleshooting.vertexformats.custom.disabled", true);;

        finishCategory(builder);
        createCategory(builder, "server.balancing");

        blackListRandomTickingBlocks = defineBoolean(builder, "server.balancing.random-ticking-blocks.blacklisted", false);
        damageTools = defineBoolean(builder, "server.balancing.tools.damage", true);
        enableChiselToolHarvestCheck = defineBoolean(builder, "server.balancing.chisel-tool.harvest-check.enabled", true);
        enableChiselToolHarvestCheckTools = defineString(builder, "server.balancing.chisel-tool.harvest-check.tools", "");
        enableToolHarvestLevels = defineBoolean(builder, "server.balancing.tools.harvest-levels.enabled", true);
        enableBitLightSource = defineBoolean(builder, "server.balancing.bits.act-as-light-source", true);
        bitLightPercentage = defineDouble(builder, "server.balancing.bits.light-percentage", 6.25);
        compatabilityMode = defineBoolean(builder, "server.balancing.compatibility-mode.enabled", false);
        maxDrawnRegionSize = defineDouble(builder, "server.balancing.max-drawn-region.size", 4);
        bagStackSize = defineInteger(builder, "server.balancing.bag.stack-size", 512);
        stoneChiselUses = defineInteger(builder, "server.balancing.chisel-uses.stone", 8384);
        ironChiselUses = defineInteger(builder, "server.balancing.chisel-uses.iron", 293440);
        diamondChiselUses = defineInteger(builder, "server.balancing.chisel-uses.diamond", 796480);
        goldChiselUses = defineInteger(builder, "server.balancing.chisel-uses.gold", 1024);
        wrenchUses = defineInteger(builder, "server.balancing.wrench-uses", 1888);
        diamondSawUses = defineInteger(builder, "server.balancing.diamond-saw.uses", 7980);
        fullBlockCrafting = defineBoolean(builder, "server.balancing.full-block-crafting.enabled", true);
        requireBagSpace = defineBoolean(builder, "server.balancing.bag-space.required", true);
        voidExcessBits = defineBoolean(builder, "server.balancing.bag-space.void-excess", true);
        creativeClipboardSize = defineInteger(builder, "server.balancing.clipboard.size.creative", 10);
        revertibleBlocks = defineList(builder, "server.balancing.revertible.blocks", Lists.newArrayList("*"), (o) -> o instanceof String);

        finishCategory(builder);
        createCategory(builder, "server.performance");

        lowMemoryMode = defineBoolean(builder, "server.performance.memory.low-mode.enabled", false);

        finishCategory(builder);
    }

    public boolean canRevertToBlock(
      BlockState newState )
    {
        final List<? extends String> blockNames = revertibleBlocks.get();
        return blockNames.contains("*") || blockNames.contains(Objects.requireNonNull(newState.getBlock().getRegistryName()).toString());
    }


}