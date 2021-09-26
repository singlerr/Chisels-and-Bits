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


    protected ServerConfiguration(final ForgeConfigSpec.Builder builder)
    {
        createCategory(builder, "server.balancing");

        blackListRandomTickingBlocks = defineBoolean(builder, "server.balancing.random-ticking-blocks.blacklisted", false);
        compatabilityMode = defineBoolean(builder, "server.balancing.compatibility-mode.enabled", false);
        bagStackSize = defineInteger(builder, "server.balancing.bag.stack-size", 512);

        finishCategory(builder);

        createCategory(builder, "server.style");

        bitSize = defineEnum(builder, "server.style.bitsize", StateEntrySize.ONE_SIXTEENTH);

        finishCategory(builder);
    }
}