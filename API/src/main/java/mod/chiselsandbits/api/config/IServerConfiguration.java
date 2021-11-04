package mod.chiselsandbits.api.config;

import mod.chiselsandbits.api.multistate.StateEntrySize;
import java.util.function.Supplier;

/**
 * Gives access to the current server's configuration.
 * Elements in this configuration are relevant for both the server and client side of C&B.
 * Since this options influence gameplay mechanics they need to be kept in sync.
 */
public interface IServerConfiguration
{

    /**
     * Gives access to the current server's configuration.
     * Elements in this configuration are relevant for both the server and client side of C&B.
     * Since this options influence gameplay mechanics they need to be kept in sync.
     *
     * @return The server configuration.
     */
    static IServerConfiguration getInstance() {
        return IChiselsAndBitsConfiguration.getInstance().getServer();
    }

    /**
     * Determines if random ticking blocks like grass or others should be eligible for chiselability.
     *
     * @return A supplier that determines if random ticking blocks should be chiselable.
     */
    Supplier<Boolean>        getBlackListRandomTickingBlocks();

    /**
     * Determines if the eligibility compatibility mode is active or not.
     *
     * @return A supplier that determines if the compatibility mode is active or not.
     */
    Supplier<Boolean>        getCompatabilityMode();

    Supplier<Integer>        getBagStackSize();

    Supplier<StateEntrySize> getBitSize();

    Supplier<Integer>        getChangeTrackerSize();
}
