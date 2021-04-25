package mod.chiselsandbits.api.chiseling;

import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.multistate.accessor.IStateAccessor;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import java.util.Optional;

/**
 * The current context for the running chiseling operation.
 */
public interface IChiselingContext extends IStateAccessor
{
    /**
     * Returns the current {@link IWorldAreaMutator} if there is one.
     * If a new chiseling operation is started no {@link IWorldAreaMutator} is available,
     * as such an empty {@link Optional} will be returned in that case.
     *
     * Only after the first call to {@link #include(Vector3d)} or {@link #include(BlockPos, Vector3d)}
     * the returned {@link Optional} can contain a {@link IWorldAreaMutator}.
     *
     * @return The {@link Optional} containing the {@link IWorldAreaMutator}.
     */
    Optional<IWorldAreaMutator> getMutator();

    /**
     * The {@link IWorld} in which the current chiseling context is valid.
     *
     * @return The {@link IWorld}.
     */
    IWorld getWorld();

    /**
     * Returns the current {@link IChiselMode} for which this context is valid.
     *
     * @return The {@link IChiselMode}.
     */
    IChiselMode getMode();

    /**
     * Includes the given exact position in the world of this context, retrievable via {@link #getWorld()}, in
     * the current {@link IWorldAreaMutator}.
     *
     * If the given position is already contained in the current {@link IWorldAreaMutator}, this method makes
     * no changes to the current context.
     *
     * It is up to the contexts implementation as well as the entire implementation of the chisels and bits api
     * to round the given value up and down into a precision which it can process, meaning that a given exact position
     * might already be included in the current {@link IWorldAreaMutator} if it is within the precision of the
     * current runtime. Even if the given exact vector itself is not included in the current {@link IWorldAreaMutator}.
     *
     * @param worldPosition The position in the current world to include.
     * @return The context, possibly with a mutated {@link IWorldAreaMutator}.
     */
    IChiselingContext include(final Vector3d worldPosition);

    /**
     * Includes the given exact position in the world of this context, retrievable via {@link #getWorld()}, in
     * the current {@link IWorldAreaMutator}.
     *
     * If the given position is already contained in the current {@link IWorldAreaMutator}, this method makes
     * no changes to the current context.
     *
     * It is up to the contexts implementation as well as the entire implementation of the chisels and bits api
     * to round the given value up and down into a precision which it can process, meaning that a given exact position
     * might already be included in the current {@link IWorldAreaMutator} if it is within the precision of the
     * current runtime. Even if the given exact vector itself is not included in the current {@link IWorldAreaMutator}.
     *
     * @param inWorldPosition The position of the block relative to which the {@code relativeInBlockPosition} is processed.
     * @param relativeInBlockPosition The relative position to include. Relative to the given {@code inWorldPosition}
     * @return The context, possibly with a mutated {@link IWorldAreaMutator}.
     */
    default IChiselingContext include(final BlockPos inWorldPosition, final Vector3d relativeInBlockPosition) {
        return this.include(Vector3d.copy(inWorldPosition).add(relativeInBlockPosition));
    }

    /**
     * Marks the current context as complete, so that it can not be reused for interactions which
     * will follow this one.
     *
     * Indicates that an action has been performed using this context, making it invalid.
     */
    void setComplete();

    /**
     * Indicates if the context is completed or not.
     *
     * @return True when complete.
     */
    boolean isComplete();

    /**
     * Indicates if the current context that is being executed is supposed to be a simulation.
     *
     * @return True when a simulation.
     */
    boolean isSimulation();

    /**
     * Indicates what kind of chiseling operation this context was created for.
     * This indicates if the mode is used for chiseling or placing.
     *
     * @return {@link ChiselingOperation#CHISELING} when the context is used for breaking blocks, {@link ChiselingOperation#PLACING} for bit placement.
     */
    ChiselingOperation getModeOfOperandus();

    /**
     * Creates a deep copy of the context, so that the copy can be modified, without modifying this instance.
     * A snapshot is automatically a simulation.
     *
     * @return The snapshot context of this context.
     */
    IChiselingContext createSnapshot();
}
