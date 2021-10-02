package mod.chiselsandbits.chiseling.modes.plane;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IWorldAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.chiseling.modes.sphere.SphereChiselMode;
import mod.chiselsandbits.registrars.ModChiselModeGroups;
import mod.chiselsandbits.utils.BitInventoryUtils;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import static mod.chiselsandbits.block.entities.ChiseledBlockEntity.ONE_THOUSANDS;

public class PlaneChiseledMode extends ForgeRegistryEntry<IChiselMode> implements IChiselMode
{
    private final int                       depth;
    private final IFormattableTextComponent displayName;
    private final ResourceLocation          iconName;
    private final boolean filterOnTarget;

    PlaneChiseledMode(final int depth, final IFormattableTextComponent displayName, final ResourceLocation iconName, final boolean filterOnTarget)
    {
        this.depth = depth;
        this.displayName = displayName;
        this.iconName = iconName;
        this.filterOnTarget = filterOnTarget;
    }
    @Override
    public ClickProcessingState onLeftClickBy(
      final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vector3d.atLowerCornerOf(face.getOpposite().getNormal()),
          Direction::getOpposite
        );


        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              try (IBatchMutation ignored =
                     mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity)))
              {
                  context.setComplete();

                  final Map<BlockState, Integer> resultingBitCount = Maps.newHashMap();

                  final Predicate<IStateEntryInfo> filter = context.getStateFilter()
                                                                     .map(builder -> builder.apply(mutator))
                                                                     .orElse((state) -> true);

                  mutator.inWorldMutableStream()
                    .filter(filter)
                    .forEach(state -> {
                        final BlockState currentState = state.getState();

                        if (context.tryDamageItem()) {
                            resultingBitCount.putIfAbsent(currentState, 0);
                            resultingBitCount.computeIfPresent(currentState, (s, currentCount) -> currentCount + 1);

                            state.clear();
                        }
                    });

                  resultingBitCount.forEach((blockState, count) -> BitInventoryUtils.insertIntoOrSpawn(
                    playerEntity,
                    blockState,
                    count
                  ));
              }

              return new ClickProcessingState(true, Event.Result.ALLOW);
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedLeftClicking(final PlayerEntity playerEntity, final IChiselingContext context)
    {

    }

    @SuppressWarnings("deprecation")
    @Override
    public ClickProcessingState onRightClickBy(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          face -> Vector3d.atLowerCornerOf(face.getNormal()),
          Function.identity()
        );

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              final BlockState heldBlockState = ItemStackUtils.getHeldBitBlockStateFromPlayer(playerEntity);
              if (heldBlockState.isAir(new SingleBlockBlockReader(heldBlockState), BlockPos.ZERO))
              {
                  return ClickProcessingState.DEFAULT;
              }

              final int missingBitCount = (int) mutator.stream()
                                                  .filter(state -> state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO))
                                                  .count();

              final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(playerEntity);

              context.setComplete();
              if (playerBitInventory.canExtract(heldBlockState, missingBitCount) || playerEntity.isCreative())
              {
                  if (!playerEntity.isCreative())
                  {
                      playerBitInventory.extract(heldBlockState, missingBitCount);
                  }

                  try (IBatchMutation ignored =
                         mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity)))
                  {
                      mutator.inWorldMutableStream()
                        .filter(state -> state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO))
                        .forEach(state -> state.overrideState(heldBlockState)); //We can use override state here to prevent the try-catch block.
                  }
              }

              return new ClickProcessingState(true, Event.Result.ALLOW);
          }).orElse(ClickProcessingState.DEFAULT)
        );
    }

    @Override
    public void onStoppedRightClicking(final PlayerEntity playerEntity, final IChiselingContext context)
    {

    }

    @Override
    public Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context)
    {
        return context.getMutator().map(mutator -> mutator);
    }

    private Optional<ClickProcessingState> processRayTraceIntoContext(
      final PlayerEntity playerEntity,
      final IChiselingContext context,
      final Function<Direction, Vector3d> placementFacingAdapter,
      final Function<Direction, Direction> iterationAdaptor
    )
    {
        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
        {
            return Optional.of(ClickProcessingState.DEFAULT);
        }

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final Vector3d hitVector = blockRayTraceResult.getLocation().add(
          placementFacingAdapter.apply(blockRayTraceResult.getDirection())
            .multiply(StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit())
        );

        final Vector3d hitBlockPosVector = Vector3d.atLowerCornerOf(new BlockPos(hitVector));
        final Vector3d inBlockHitVector = hitVector.subtract(hitBlockPosVector);
        final Vector3d inBlockBitVector = inBlockHitVector.multiply(StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide(), StateEntrySize.current().getBitsPerBlockSide());

        if (context.getWorld() != null && filterOnTarget)
        {
            final IAreaAccessor worldAreaAccessor = IMutatorFactory.getInstance().in(context.getWorld(), new BlockPos(hitBlockPosVector));
            final BlockState filterState = worldAreaAccessor.getInAreaTarget(inBlockHitVector).map(IStateEntryInfo::getState).orElse(Blocks.AIR.defaultBlockState());
            context.setStateFilter(areaAccessor -> new BlockStateAreaFilter(filterState));
        }

        final Direction iterationDirection = iterationAdaptor.apply(blockRayTraceResult.getDirection());
        switch (iterationDirection)
        {
            case DOWN:
                includeDownAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case UP:
                includeUpAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case NORTH:
                includeNorthAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case SOUTH:
                includeSouthAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case WEST:
                includeWestAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
            case EAST:
                includeEastAxis(context, hitBlockPosVector, inBlockBitVector);
                break;
        }

        return Optional.empty();
    }

    private void includeDownAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.y() - depth, 0).add(Vector3d.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.y() - depth, 15.5).add(Vector3d.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.y() - 0.5f, 0).add(Vector3d.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.y() - 0.5f , 15.5).add(Vector3d.atLowerCornerOf(Direction.UP.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeUpAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.y() + depth, 0).add(Vector3d.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.y() + depth, 15.5).add(Vector3d.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, inBlockBitVector.y() + 0.5f, 0).add(Vector3d.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, inBlockBitVector.y() + 0.5f , 15.5).add(Vector3d.atLowerCornerOf(Direction.DOWN.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeNorthAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.z() - depth).add(Vector3d.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.z() - depth).add(Vector3d.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.z() - 0.5f).add(Vector3d.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.z() - 0.5f).add(Vector3d.atLowerCornerOf(Direction.SOUTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeSouthAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.z() + depth).add(Vector3d.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.z() + depth).add(Vector3d.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(0, 0, inBlockBitVector.z() + 0.5f).add(Vector3d.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(15.5, 15.5, inBlockBitVector.z() + 0.5f).add(Vector3d.atLowerCornerOf(Direction.NORTH.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeWestAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() - depth, 0, 0).add(Vector3d.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() - depth, 15.5, 15.5).add(Vector3d.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() - 0.5f, 0, 0).add(Vector3d.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() - 0.5f , 15.5, 15.5).add(Vector3d.atLowerCornerOf(Direction.EAST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private void includeEastAxis(final IChiselingContext context, final Vector3d hitBlockPosVector, final Vector3d inBlockBitVector)
    {
        final BlockPos position = new BlockPos(hitBlockPosVector);
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() + depth, 0, 0).add(Vector3d.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() + depth, 15.5, 15.5).add(Vector3d.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));

        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() + 0.5f, 0, 0).add(Vector3d.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
        context.include(position, clampVectorToBlock(new Vector3d(inBlockBitVector.x() + 0.5f , 15.5, 15.5).add(Vector3d.atLowerCornerOf(Direction.WEST.getNormal())).multiply(StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit(), StateEntrySize.current().getSizePerBit())));
    }

    private Vector3d clampVectorToBlock(final Vector3d v)
    {
        return new Vector3d(
          v.x() < 0 ? 0 : (v.x() >= 1 ? 1 - ONE_THOUSANDS : v.x()),
          v.y() < 0 ? 0 : (v.y() >= 1 ? 1 - ONE_THOUSANDS : v.y()),
          v.z() < 0 ? 0 : (v.z() >= 1 ? 1 - ONE_THOUSANDS : v.z())
        );
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return iconName;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return this.displayName;
    }

    @NotNull
    @Override
    public Optional<IToolModeGroup> getGroup()
    {
        return Optional.of(filterOnTarget ? ModChiselModeGroups.PLANE_FILTERED : ModChiselModeGroups.PLANE);
    }

    private final class BlockStateAreaFilter implements Predicate<IStateEntryInfo>
    {
        private final BlockState targetState;
        private final int stateHash;

        private BlockStateAreaFilter(final BlockState targetState) {
            this.targetState = targetState;
            this.stateHash = targetState.hashCode();
        }

        @Override
        public boolean test(final IStateEntryInfo stateEntryInfo)
        {
            return stateHash == stateEntryInfo.getState().hashCode();
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof BlockStateAreaFilter))
            {
                return false;
            }

            final BlockStateAreaFilter that = (BlockStateAreaFilter) o;

            return targetState.equals(that.targetState);
        }

        @Override
        public int hashCode()
        {
            return stateHash;
        }
    }
}
