package mod.chiselsandbits.chiseling.modes.connected.plane;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.util.IQuadFunction;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.registrars.ModChiselModeGroups;
import mod.chiselsandbits.registrars.ModMetadataKeys;
import mod.chiselsandbits.utils.BitInventoryUtils;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.registries.ForgeRegistryEntry;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.*;

public class ConnectedPlaneChiselingMode extends ForgeRegistryEntry<IChiselMode> implements IChiselMode
{
    private final int                       depth;
    private final     IFormattableTextComponent displayName;
    private final     IFormattableTextComponent multiLineDisplayName;
    private final     ResourceLocation          iconName;

    public ConnectedPlaneChiselingMode(
      final int depth,
      final IFormattableTextComponent displayName,
      final IFormattableTextComponent multiLineDisplayName,
      final ResourceLocation iconName) {
        this.depth = depth;
        this.displayName = displayName;
        this.multiLineDisplayName = multiLineDisplayName;
        this.iconName = iconName;
    }

    @Override
    public ClickProcessingState onLeftClickBy(
      final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          Direction::getOpposite,
          face -> Vector3d.atLowerCornerOf(face.getOpposite().getNormal()),
          IQuadFunction.fourthIdentity(),
          position -> IMutatorFactory.getInstance().in(
            context.getWorld(),
            new BlockPos(position)
          ),
          direction -> Vector3i.ZERO
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
        //NOOP
    }

    @Override
    public ClickProcessingState onRightClickBy(final PlayerEntity playerEntity, final IChiselingContext context)
    {
        final Optional<ClickProcessingState> rayTraceHandle = this.processRayTraceIntoContext(
          playerEntity,
          context,
          UnaryOperator.identity(),
          face -> Vector3d.atLowerCornerOf(face.getNormal()),
          (hitPos, inBlockTargetedPosition, hitFace, candidatePosition) -> {
              if (inBlockTargetedPosition.equals(candidatePosition)) {
                  return Vector3d.atLowerCornerOf(hitPos)
                           .add(inBlockTargetedPosition)
                           .add(Vector3d.atLowerCornerOf(
                             hitFace.getOpposite().getNormal()
                             )
                             .multiply(StateEntrySize.current().getSizePerBitScalingVector())
                           );
              }

              final Vector3d relevantAxisCandidate = candidatePosition.multiply(
                Math.abs(hitFace.getNormal().getX()),
                Math.abs(hitFace.getNormal().getY()),
                Math.abs(hitFace.getNormal().getZ())
              );
              final Vector3d noneRelevantAxisCandidates = candidatePosition.subtract(relevantAxisCandidate);
              final Vector3d relevantAxisTarget = inBlockTargetedPosition.multiply(
                Math.abs(hitFace.getNormal().getX()),
                Math.abs(hitFace.getNormal().getY()),
                Math.abs(hitFace.getNormal().getZ())
              );

              final Vector3d offset = candidatePosition.subtract(inBlockTargetedPosition);
              final Vector3d relevantAxisOffset = offset.multiply(
                Math.abs(hitFace.getNormal().getX()),
                Math.abs(hitFace.getNormal().getY()),
                Math.abs(hitFace.getNormal().getZ())
              );

              return noneRelevantAxisCandidates.add(relevantAxisTarget).subtract(relevantAxisOffset)
                .add(hitPos.getX(), hitPos.getY(), hitPos.getZ())
                .add(Vector3d.atLowerCornerOf(
                      hitFace.getOpposite().getNormal()
                    )
                    .multiply(StateEntrySize.current().getSizePerBitScalingVector())
                );
          },
          position -> IMutatorFactory.getInstance().covering(
            context.getWorld(),
            new BlockPos(position.subtract(1, 1, 1)),
            new BlockPos(position.add(1, 1, 1))
          ),
          direction -> new Vector3i(
            direction.getNormal().getX() * depth,
            direction.getNormal().getY() * depth,
            direction.getNormal().getZ() * depth
          )
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

            final Predicate<IStateEntryInfo> filter = context.getStateFilter()
              .map(builder -> builder.apply(mutator))
              .orElse((state) -> true);

            final int missingBitCount = (int) mutator.stream()
                .filter(state -> state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) && filter.test(state))
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
                        .filter(state -> state.getState().isAir(new SingleBlockBlockReader(state.getState()), BlockPos.ZERO) && filter.test(state))
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
        //NOOP
    }

    @Override
    public boolean isStillValid(final PlayerEntity playerEntity, final IChiselingContext context, final ChiselingOperation modeOfOperation)
    {
        final Optional<Set<Vector3i>> validPositions = context.getMetadata(ModMetadataKeys.VALID_POSITIONS.get());
        final Optional<Direction> targetedSide = context.getMetadata(ModMetadataKeys.TARGETED_SIDE.get());
        final Optional<BlockPos> targetedBlockPos = context.getMetadata(ModMetadataKeys.TARGETED_BLOCK.get());

        if (!validPositions.isPresent() || !targetedSide.isPresent() || !targetedBlockPos.isPresent())
            return false;

        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
        {
            return false;
        }

        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        if (blockRayTraceResult.getDirection() != targetedSide.get())
            return false;

        final Function<Direction, Vector3d> placementFacingAdapter = modeOfOperation == ChiselingOperation.CHISELING ?
                                                                       face -> Vector3d.atLowerCornerOf(face.getOpposite().getNormal()) :
                                                                       face -> Vector3d.atLowerCornerOf(face.getNormal());

        final Vector3d hitVector = blockRayTraceResult.getLocation().add(
          placementFacingAdapter.apply(blockRayTraceResult.getDirection())
            .multiply(StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit())
        );

        final BlockPos hitPos = new BlockPos(hitVector);
        final Vector3d hitBlockPosVector = Vector3d.atLowerCornerOf(hitPos);
        final Vector3d inBlockHitVector = hitVector.subtract(hitBlockPosVector);
        final Vector3i selectedPosition = new Vector3i(
          inBlockHitVector.x() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.y() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.z() * StateEntrySize.current().getBitsPerBlockSide()
        );

        return validPositions.get().contains(selectedPosition) && hitPos.equals(targetedBlockPos.get());
    }

    @Override
    public Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context)
    {
        return context.getMutator().map(mutator -> mutator);
    }

    private Optional<ClickProcessingState> processRayTraceIntoContext(
      final PlayerEntity playerEntity,
      final IChiselingContext context,
      final UnaryOperator<Direction> searchDirectionAdapter,
      final Function<Direction, Vector3d> placementFacingAdapter,
      final IQuadFunction<BlockPos, Vector3d, Direction, Vector3d, Vector3d> stateExtractionAdapter,
      final Function<Vector3d, IAreaAccessor> areaAccessorBuilder,
      final Function<Direction, Vector3i> filterOffsetProducer
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

        final BlockPos hitPos = new BlockPos(hitVector);
        final Vector3d hitBlockPosVector = Vector3d.atLowerCornerOf(hitPos);
        final Vector3d inBlockHitVector = hitVector.subtract(hitBlockPosVector);

        final Deque<Vector3i> toProcess = new LinkedList<>();
        final IAreaAccessor worldAccessor = areaAccessorBuilder.apply(hitVector);

        final Set<Vector3i> processed = new HashSet<>();
        final Set<Vector3i> validPositions = new HashSet<>();

        final Set<Vector3i> offsets = new HashSet<>();
        offsets.add(searchDirectionAdapter.apply(blockRayTraceResult.getDirection()).getNormal());
        Arrays.stream(Direction.values())
                .filter(direction -> direction.getAxis() != blockRayTraceResult.getDirection().getAxis())
                  .map(Direction::getNormal)
                    .forEach(offsets::add);

        final Vector3i selectedPosition = new Vector3i(
          inBlockHitVector.x() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.y() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.z() * StateEntrySize.current().getBitsPerBlockSide()
        );
        toProcess.addLast(selectedPosition);

        final Vector3i relevantSelectedAxisVector =
          new Vector3i(
            selectedPosition.getX() * Math.abs(blockRayTraceResult.getDirection().getNormal().getX()),
            selectedPosition.getY() * Math.abs(blockRayTraceResult.getDirection().getNormal().getY()),
            selectedPosition.getZ() * Math.abs(blockRayTraceResult.getDirection().getNormal().getZ())
          );

        final Vector3d selectedInBlockPosition = Vector3d.atLowerCornerOf(
          selectedPosition
        ).multiply(
          StateEntrySize.current().getSizePerBitScalingVector()
        );

        final Optional<IStateEntryInfo> targetedInfo = worldAccessor.getInAreaTarget(
          stateExtractionAdapter.apply(hitPos, selectedInBlockPosition, blockRayTraceResult.getDirection(), selectedInBlockPosition)
        );

        if (!targetedInfo.isPresent())
            return Optional.of(ClickProcessingState.DEFAULT);

        while(!toProcess.isEmpty()) {
            final Vector3i targetedPosition = toProcess.removeFirst();
            final Vector3d targetedInBlockPosition = Vector3d.atLowerCornerOf(
              targetedPosition
            ).multiply(
              StateEntrySize.current().getSizePerBitScalingVector()
            );
            final Optional<IStateEntryInfo> targetCandidate = worldAccessor.getInAreaTarget(
              stateExtractionAdapter.apply(hitPos, selectedInBlockPosition, blockRayTraceResult.getDirection(), targetedInBlockPosition)
            );

            processed.add(targetedPosition);

            if (!targetCandidate.isPresent())
                continue;

            IStateEntryInfo target = targetCandidate.get();

            if (target.getState().equals(targetedInfo.get().getState())) {
                //We are somehow connected to the targeted bit and of the same state.
                validPositions.add(targetedPosition);

                offsets.forEach(offset -> {
                    final Vector3i newTarget = new Vector3i(
                      targetedPosition.getX() + offset.getX(),
                      targetedPosition.getY() + offset.getY(),
                      targetedPosition.getZ() + offset.getZ()
                    );
                    if (newTarget.getX() >= 0 && newTarget.getX() < StateEntrySize.current().getBitsPerBlockSide() &&
                          newTarget.getY() >= 0 && newTarget.getY() < StateEntrySize.current().getBitsPerBlockSide() &&
                          newTarget.getZ() >= 0 && newTarget.getZ() < StateEntrySize.current().getBitsPerBlockSide()) {

                        final Vector3i relevantNewTargetAxisVector =
                          new Vector3i(
                            newTarget.getX() * Math.abs(blockRayTraceResult.getDirection().getNormal().getX()),
                            newTarget.getY() * Math.abs(blockRayTraceResult.getDirection().getNormal().getY()),
                            newTarget.getZ() * Math.abs(blockRayTraceResult.getDirection().getNormal().getZ())
                          );

                        final int targetedDepth = Math.abs(
                          relevantSelectedAxisVector.get(blockRayTraceResult.getDirection().getAxis()) -
                             relevantNewTargetAxisVector.get(blockRayTraceResult.getDirection().getAxis())
                        );

                        if (targetedDepth <= depth - 1) {
                            //Valid offset found
                            if (!processed.contains(newTarget) && !toProcess.contains(newTarget)) {
                                toProcess.addLast(newTarget);
                            }
                        }
                    } else {
                        processed.add(newTarget);
                    }
                });
            }
        }

        context.include(hitPos, Vector3d.ZERO);
        context.include(hitPos, new Vector3d(0.9999, 0.9999, 0.9999));
        context.setStateFilter(accessor -> new SelectedBitStateFilter(filterOffsetProducer.apply(blockRayTraceResult.getDirection()), validPositions));

        context.setMetadata(ModMetadataKeys.VALID_POSITIONS.get(), validPositions);
        context.setMetadata(ModMetadataKeys.TARGETED_SIDE.get(), blockRayTraceResult.getDirection());
        context.setMetadata(ModMetadataKeys.TARGETED_BLOCK.get(), hitPos);

        return Optional.empty();
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return iconName;
    }

    @Override
    public @NotNull Optional<IToolModeGroup> getGroup()
    {
        return Optional.of(ModChiselModeGroups.CONNECTED_PLANE);
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    @Override
    public ITextComponent getMultiLineDisplayName()
    {
        return multiLineDisplayName;
    }

    private static final class SelectedBitStateFilter implements Predicate<IStateEntryInfo> {

        private final Vector3i offset;
        private final Set<Vector3i> validPositions;

        public SelectedBitStateFilter(final Vector3i offset, final Set<Vector3i> validPositions)
        {
            this.offset = offset;
            this.validPositions = validPositions;
        }

        @Override
        public boolean test(final IStateEntryInfo iStateEntryInfo)
        {
            final Vector3i position = new Vector3i(
              iStateEntryInfo.getStartPoint().x() * StateEntrySize.current().getBitsPerBlockSide(),
              iStateEntryInfo.getStartPoint().y() * StateEntrySize.current().getBitsPerBlockSide(),
              iStateEntryInfo.getStartPoint().z() * StateEntrySize.current().getBitsPerBlockSide()
            );

            return validPositions.contains(
                position
            );
        }

        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (!(o instanceof SelectedBitStateFilter))
            {
                return false;
            }

            final SelectedBitStateFilter that = (SelectedBitStateFilter) o;

            if (!offset.equals(that.offset))
            {
                return false;
            }
            return validPositions.equals(that.validPositions);
        }

        @Override
        public int hashCode()
        {
            int result = offset.hashCode();
            result = 31 * result + validPositions.hashCode();
            return result;
        }

        @Override
        public String toString()
        {
            return "SelectedBitStateFilter{" +
                     "offset=" + offset +
                     ", validPositions=" + validPositions +
                     '}';
        }
    }
}
