package mod.chiselsandbits.api.chiseling;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.entity.player.PlayerEntity;

public interface IChiselingManager
{

    static IChiselingManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getChiselingManager();
    }

    /**
     * Gets or creates a new chiseling context for the given player.
     *
     * A new context is created when either one of the following conditions is met:
     *  - No context has been created before.
     *  - The world of the player and the world of the existing context are not equal
     *  - The new chisel mode and the chisel mode of the existing context are not equal.
     *
     * @param playerEntity The player for which the context is looked up.
     * @param mode The mode which the player wants to chisel in.
     *
     * @return The context.
     */
    IChiselingContext getOrCreateContext(final PlayerEntity playerEntity, final IChiselMode mode);
}
