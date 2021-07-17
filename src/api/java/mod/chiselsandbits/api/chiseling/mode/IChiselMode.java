package mod.chiselsandbits.api.chiseling.mode;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IToolMode;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.api.util.IWithDisplayName;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

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
}
