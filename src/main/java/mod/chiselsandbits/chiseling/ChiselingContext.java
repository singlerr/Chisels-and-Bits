package mod.chiselsandbits.chiseling;

import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import java.util.Optional;

public class ChiselingContext implements IChiselingContext
{
    private final IWorld world;
    private final IChiselMode chiselMode;

    private final Runnable onCompleteCallback;

    private boolean complete = false;

    private IWorldAreaMutator mutator = null;

    public ChiselingContext(final IWorld world, final IChiselMode chiselMode, final Runnable onCompleteCallback) {
        this.world = world;
        this.chiselMode = chiselMode;
        this.onCompleteCallback = onCompleteCallback;
    }

    /**
     * Returns the current {@link IWorldAreaMutator} if there is one. If a new chiseling operation is started no {@link IWorldAreaMutator} is available, as such an empty {@link
     * Optional} will be returned in that case.
     * <p>
     * Only after the first call to {@link #include(Vector3d)} the returned {@link Optional} can contain a {@link IWorldAreaMutator}.
     *
     * @return The {@link Optional} containing the {@link IWorldAreaMutator}.
     */
    @Override
    public Optional<IWorldAreaMutator> getMutator()
    {
        return Optional.ofNullable(mutator);
    }

    /**
     * The {@link IWorld} in which the current chiseling context is valid.
     *
     * @return The {@link IWorld}.
     */
    @Override
    public IWorld getWorld()
    {
        return world;
    }

    /**
     * Returns the current {@link IChiselMode} for which this context is valid.
     *
     * @return The {@link IChiselMode}.
     */
    @Override
    public IChiselMode getMode()
    {
        return chiselMode;
    }

    /**
     * Includes the given exact position in the world of this context, retrievable via {@link #getWorld()}, in the current {@link IWorldAreaMutator}.
     * <p>
     * If the given position is already contained in the current {@link IWorldAreaMutator}, this method makes no changes to the current context.
     * <p>
     * It is up to the contexts implementation as well as the entire implementation of the chisels and bits api to round the given value up and down into a precision which it can
     * process, meaning that a given exact position might already be included in the current {@link IWorldAreaMutator} if it is within the precision of the current runtime. Even if
     * the given exact vector itself is not included in the current {@link IWorldAreaMutator}.
     *
     * @param worldPosition The position in the current world to include.
     * @return The context, possibly with a mutated {@link IWorldAreaMutator}.
     */
    @Override
    public IChiselingContext include(final Vector3d worldPosition)
    {
        if (getMutator().map(m -> !m.isInside(worldPosition)).orElse(true)) {
            if (getMutator().isPresent()) {
                final IWorldAreaMutator worldAreaMutator = getMutator().get();

                Vector3d start = new Vector3d(
                  Math.min(worldPosition.getX(), worldAreaMutator.getInWorldStartPoint().getX()),
                  Math.min(worldPosition.getY(), worldAreaMutator.getInWorldStartPoint().getY()),
                  Math.min(worldPosition.getZ(), worldAreaMutator.getInWorldStartPoint().getZ())
                );
                Vector3d end = new Vector3d(
                  Math.max(worldPosition.getX(), worldAreaMutator.getInWorldEndPoint().getX()),
                  Math.max(worldPosition.getY(), worldAreaMutator.getInWorldEndPoint().getY()),
                  Math.max(worldPosition.getZ(), worldAreaMutator.getInWorldEndPoint().getZ())
                );

                this.mutator = IMutatorFactory.getInstance().covering(world, start, end);
            }
            else
            {
                this.mutator = IMutatorFactory.getInstance().covering(
                  world,
                  worldPosition,
                  worldPosition
                );
            }
        }

        return this;
    }

    /**
     * Marks the current context as complete, so that it can not be reused for interactions which will follow this one.
     * <p>
     * Indicates that an action has been performed using this context, making it invalid.
     */
    @Override
    public void setComplete()
    {
        this.complete = true;
        this.onCompleteCallback.run();
    }

    /**
     * Indicates if the context is completed or not.
     *
     * @return True when complete.
     */
    @Override
    public boolean isComplete()
    {
        return complete;
    }

    @Override
    public Optional<IStateEntryInfo> getInAreaTarget(final Vector3d inAreaTarget)
    {
        if (getMutator().isPresent() && getMutator().map(m -> m.isInside(inAreaTarget)).orElse(false)) {
            return getMutator().flatMap(m -> m.getInAreaTarget(inAreaTarget));
        }

        final BlockPos position = new BlockPos(inAreaTarget);
        final Vector3d inBlockOffset = inAreaTarget.subtract(position.getX(), position.getY(), position.getZ());

        return IMutatorFactory.getInstance().in(
          getWorld(),
          position
        ).getInAreaTarget(
          inBlockOffset
        );
    }

    @Override
    public Optional<IStateEntryInfo> getInBlockTarget(final BlockPos inAreaBlockPosOffset, final Vector3d inBlockTarget)
    {
        return getInAreaTarget(Vector3d.copy(inAreaBlockPosOffset).add(inBlockTarget));
    }
}
