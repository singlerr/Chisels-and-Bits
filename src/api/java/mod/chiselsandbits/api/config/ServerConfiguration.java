package mod.chiselsandbits.api.config;

import mod.chiselsandbits.api.multistate.StateEntrySize;
import net.minecraftforge.common.ForgeConfigSpec;

/**
 * Mod server configuration.
 * Loaded serverside, synced on connection.
 */
public class ServerConfiguration extends AbstractConfiguration
{
    public ForgeConfigSpec.BooleanValue blackListRandomTickingBlocks;
    public ForgeConfigSpec.BooleanValue compatabilityMode;
    public ForgeConfigSpec.IntValue bagStackSize;
    public ForgeConfigSpec.EnumValue<StateEntrySize> bitSize;
    public ForgeConfigSpec.IntValue changeTrackerSize;

    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "balancing");

        blackListRandomTickingBlocks = defineBoolean(builder, "blacklist-random-ticking-blocks", false);
        compatabilityMode = defineBoolean(builder, "enable-compatibility-mode", false);
        bagStackSize = defineInteger(builder, "bit-bag-stack-size", 512);
        changeTrackerSize = defineInteger(builder, "change-tracker-size", 10, 20, 40);

        finishCategory(builder);

        createCategory(builder, "style");

        bitSize = defineEnum(builder, "bit-size", StateEntrySize.ONE_SIXTEENTH);

        finishCategory(builder);
    }
}