package mod.chiselsandbits.api.item.click;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

/**
 * Deals with the way items are able to handle left click interactions.
 */
public interface IRightClickControllingItem
{

    /**
     * Callback invoked when a supported item is used to right click.
     *
     * Allows for said item to take over the processing logic of the right clicking
     * and can afterwards block the further vanilla processing.
     *
     * @param playerEntity The entity who right clicked.
     * @param hand The hand with which the entity right clicked.
     * @param position The position on which the entity right clicked.
     * @param face The face on which the entity right clicked.
     * @param currentState The current state of the right click processing.
     *
     * @return The processing state with which the processing of the right click should continue.
     */
    ClickProcessingState handleRightClickProcessing(
      final PlayerEntity playerEntity,
      final Hand hand,
      final BlockPos position,
      final Direction face,
      final ClickProcessingState currentState
    );

    /**
     * Indicates if the player can use the item in the current interaction.
     * If this returns false, then no processing is performed and the interaction
     * event is cancelled.
     *
     * @param playerEntity The player in question.
     *
     * @return True when useable, false when not.
     */
    default boolean canUse(final PlayerEntity playerEntity) {
        return true;
    }
}
