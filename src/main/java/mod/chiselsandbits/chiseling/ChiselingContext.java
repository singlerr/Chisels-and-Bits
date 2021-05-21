package mod.chiselsandbits.chiseling;

import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.item.chisel.IChiselingItem;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorld;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ChiselingContext implements IChiselingContext
{
    private final IWorld             world;
    private final IChiselMode        chiselMode;
    private final ChiselingOperation modeOfOperandus;
    private final boolean            simulation;
    private final Runnable           onCompleteCallback;
    private final ItemStack          causingItemStack;
    private final boolean      supportsDamaging;
    private final PlayerEntity playerEntity;

    private boolean           complete = false;
    private IWorldAreaMutator mutator  = null;

    public ChiselingContext(
      final IWorld world,
      final IChiselMode chiselMode,
      final ChiselingOperation modeOfOperandus,
      final boolean simulation,
      final Runnable onCompleteCallback,
      final ItemStack causingItemStack,
      final PlayerEntity playerEntity)
    {
        this.world = world;
        this.chiselMode = chiselMode;
        this.simulation = simulation;
        this.onCompleteCallback = onCompleteCallback;
        this.modeOfOperandus = modeOfOperandus;
        this.causingItemStack = causingItemStack;

        if (this.causingItemStack.getItem() instanceof IChiselingItem) {
            this.supportsDamaging = ((IChiselingItem) this.causingItemStack.getItem()).isDamageableDuringChiseling();
        }
        else
        {
            this.supportsDamaging = false;
        }

        this.playerEntity = playerEntity;
    }

    private ChiselingContext(
      final IWorld world,
      final IChiselMode chiselMode,
      final ChiselingOperation modeOfOperandus,
      final boolean complete,
      final IWorldAreaMutator mutator,
      final PlayerEntity playerEntity)
    {
        this.world = world;
        this.chiselMode = chiselMode;
        this.causingItemStack = ItemStack.EMPTY;
        this.supportsDamaging = false;
        this.onCompleteCallback = () -> {}; //Noop this is the snapshot constructor which has no callback logic.
        this.simulation = true; //Always the case for snapshots.
        this.modeOfOperandus = modeOfOperandus;
        this.complete = complete;
        this.mutator = mutator;
        this.playerEntity = playerEntity;
    }

    private ChiselingContext(
      final IWorld world,
      final IChiselMode chiselMode,
      final ChiselingOperation modeOfOperandus,
      final boolean complete,
      final PlayerEntity playerEntity)
    {
        this.world = world;
        this.chiselMode = chiselMode;
        this.causingItemStack = ItemStack.EMPTY;
        this.supportsDamaging = false;
        this.onCompleteCallback = () -> {}; //Noop this is the snapshot constructor which has no callback logic.
        this.simulation = true; //Always the case for snapshots.
        this.modeOfOperandus = modeOfOperandus;
        this.complete = complete;
        this.playerEntity = playerEntity;
    }

    @Override
    public Optional<IWorldAreaMutator> getMutator()
    {
        return Optional.ofNullable(mutator);
    }

    @Override
    public IWorld getWorld()
    {
        return world;
    }

    @Override
    public IChiselMode getMode()
    {
        return chiselMode;
    }

    @Override
    public IChiselingContext include(final Vector3d worldPosition)
    {
        if (getMutator().map(m -> !m.isInside(worldPosition)).orElse(true))
        {
            if (getMutator().isPresent())
            {
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

    @Override
    public void setComplete()
    {
        this.complete = true;
        this.onCompleteCallback.run();
    }

    @Override
    public boolean isComplete()
    {
        return complete;
    }

    @Override
    public boolean isSimulation()
    {
        return simulation;
    }

    @Override
    public ChiselingOperation getModeOfOperandus()
    {
        return modeOfOperandus;
    }

    @Override
    public IChiselingContext createSnapshot()
    {
        if (mutator == null) {
            return new ChiselingContext(
              world,
              chiselMode,
              modeOfOperandus,
              this.complete,
              playerEntity
            );
        }

        return new ChiselingContext(
          world,
          chiselMode,
          modeOfOperandus,
          this.complete,
          IMutatorFactory.getInstance().covering(
            world,
            mutator.getInWorldStartPoint(),
            mutator.getInWorldEndPoint()
          ),
          playerEntity
        );
    }

    @Override
    public boolean tryDamageItem(final int damage)
    {
        if (!this.supportsDamaging || this.simulation)
            return true;

        final AtomicBoolean broken = new AtomicBoolean(false);
        this.causingItemStack.damageItem(damage, playerEntity, playerEntity -> {
            broken.set(true);

            Hand hand = Hand.MAIN_HAND;
            if (playerEntity.getHeldItemOffhand() == causingItemStack)
                hand = Hand.OFF_HAND;
            playerEntity.sendBreakAnimation(hand);
        });

        return !broken.get();
    }

    @Override
    public Optional<IStateEntryInfo> getInAreaTarget(final Vector3d inAreaTarget)
    {
        if (getMutator().isPresent() && getMutator().map(m -> m.isInside(inAreaTarget)).orElse(false))
        {
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
