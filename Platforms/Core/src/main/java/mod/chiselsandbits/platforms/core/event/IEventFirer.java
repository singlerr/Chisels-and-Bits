package mod.chiselsandbits.platforms.core.event;

import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

/**
 * Fires specific events in a way that the platform can understand.
 */
public interface IEventFirer {


    /**
     * Gives access to a system which can fire event data on the given platform.
     *
     * @return The system to fire the events.
     */
    static IEventFirer getInstance() {
        return IChiselsAndBitsPlatformCore.getInstance().getEventFirer();
    }

    /**
     * Indicates if an event fired on the platform allows for breaking the block on the given positoin
     * by the given player.
     *
     * @param level The level in question.
     * @param position The position of the block in the level.
     * @param playerEntity The player that is about to break the block.
     * @return True when allowed, false when not.
     */
    boolean canBreakBlock(Level level, BlockPos position, Player playerEntity);
}
