package mod.chiselsandbits.api.chiseling.mode;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.IChiselingManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.api.util.IWithDisplayName;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.apache.logging.log4j.core.appender.rolling.action.IfAll;

import java.util.Optional;

/**
 * Represents a chiselable operation that can be completed by a chisel or bit for example.
 */
public interface IChiselMode extends IForgeRegistryEntry<IChiselMode>, IToolMode<IToolModeGroup>
{

    /**
     * The default mode of the chisel or bit.
     * @return The default mode.
     */
    static IChiselMode getDefaultMode() {
        return IChiselsAndBitsAPI.getInstance().getDefaultChiselMode();
    }

    /**
     * The underlying registry that contains the different modes a chisel can assume.
     * @return The underlying forge registry.
     */
    static IForgeRegistry<IChiselMode> getRegistry() {
        return IRegistryManager.getInstance().getChiselModeRegistry();
    }

    /**
     * Invoked by the system when a chisel or a bit is left clicked to perform an associated operation.
     *
     * @param playerEntity The entity that is executing the operation.
     * @param context The chiseling context.
     *
     * @return The processing state, which indicates how the interaction should proceed.
     */
    ClickProcessingState onLeftClickBy(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    /**
     * Invoked by the system when the player has released the left click button.
     *
     * Currently not invoked by the system. Future endpoint.
     *
     * @param playerEntity The player who released the button.
     * @param context The chiseling context.
     */
    void onStoppedLeftClicking(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    /**
     * Invoked by the system when a chisel or a bit is right clicked to perform an associated operation.
     *
     * @param playerEntity The entity that is executing the operation.
     * @param context The chiseling context.
     *
     * @return The processing state, which indicates how the interaction should proceed.
     */
    ClickProcessingState onRightClickBy(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    /**
     * Invoked by the system when the player has released the right click button.
     *
     * Currently not invoked by the system. Future endpoint.
     *
     * @param playerEntity The player who released the button.
     * @param context The chiseling context.
     */
    void onStoppedRightClicking(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    /**
     * Performs an extraction and potential modification of the accessor for the given context as determined by this mode.
     *
     * @param context The chiseling context.
     * @return An optional, potentially containing an area accessor.
     */
    Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context);

    /**
     * Checks if the passed context is still valid for the given entity.
     * @param playerEntity The entity to check for.
     * @param context The context to check.
     * @param modeOfOperation The mode of operandus for the check.
     * @return True when still valid, false when not.
     */
    default boolean isStillValid(
      final PlayerEntity playerEntity,
      final IChiselingContext context,
      final ChiselingOperation modeOfOperation
    ) {
        final IChiselingContext snapshot = IChiselingManager.getInstance().create(
          playerEntity,
          this,
          modeOfOperation,
          true,
          ItemStack.EMPTY
        );

        if (modeOfOperation == ChiselingOperation.CHISELING) {
            onLeftClickBy(playerEntity, snapshot);
        }
        else {
            onRightClickBy(playerEntity, snapshot);
        }

        if (!snapshot.getMutator().isPresent())
            return !context.getMutator().isPresent();

        if (!context.getMutator().isPresent())
            return false;

        return context.getMutator().get().getInWorldBoundingBox().equals(snapshot.getMutator().get().getInWorldBoundingBox());
    }

    /**
     * Indicates that this mode is only available on a stack which has a placeable mode.
     * Useful when placement and removal perform the same task AND requires a bit to be held.
     * @return True when a "bit" needs to be held.
     */
    default boolean requiresPlaceableEditStack() {
        return false;
    }
}
