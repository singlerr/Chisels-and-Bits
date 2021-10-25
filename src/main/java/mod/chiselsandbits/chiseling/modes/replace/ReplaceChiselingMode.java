package mod.chiselsandbits.chiseling.modes.replace;

import com.google.common.collect.Maps;
import cpw.mods.modlauncher.api.LamdbaExceptionUtils;
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
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
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
import java.util.function.Function;
import java.util.function.Predicate;

public class ReplaceChiselingMode extends ForgeRegistryEntry<IChiselMode> implements IChiselMode
{
    private final     IFormattableTextComponent displayName;
    private final     IFormattableTextComponent multiLineDisplayName;
    private final     ResourceLocation          iconName;

    public ReplaceChiselingMode(
      final IFormattableTextComponent displayName,
      final IFormattableTextComponent multiLineDisplayName,
      final ResourceLocation iconName) {
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
          context
        );

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return rayTraceHandle.orElseGet(() -> context.getMutator().map(mutator -> {
              try (IBatchMutation ignored =
                     mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity)))
              {
                  final BlockState heldBlockState = ItemStackUtils.getHeldBitBlockStateFromPlayer(playerEntity);
                  if (heldBlockState.isAir(new SingleBlockBlockReader(heldBlockState), BlockPos.ZERO))
                  {
                      return ClickProcessingState.DEFAULT;
                  }

                  context.setComplete();

                  final Map<BlockState, Integer> resultingBitCount = Maps.newHashMap();

                  final Predicate<IStateEntryInfo> filter = context.getStateFilter()
                    .map(builder -> builder.apply(mutator))
                    .orElse((state) -> true);

                  final int missingBitCount = (int) mutator.stream()
                    .filter(filter)
                    .count();

                  final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(playerEntity);

                  if (!playerEntity.isCreative() && !playerBitInventory.canExtract(heldBlockState, missingBitCount))
                      return ClickProcessingState.DEFAULT;

                  mutator.inWorldMutableStream()
                    .filter(filter)
                    .forEach(LamdbaExceptionUtils.rethrowConsumer(state -> {
                        final BlockState currentState = state.getState();

                        if (context.tryDamageItem()) {
                            resultingBitCount.putIfAbsent(currentState, 0);
                            resultingBitCount.computeIfPresent(currentState, (s, currentCount) -> currentCount + 1);
                        }

                        state.clear();
                        state.setState(heldBlockState);
                    }));

                  if (!playerEntity.isCreative())
                        playerBitInventory.extract(heldBlockState, missingBitCount);

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
        return onLeftClickBy(playerEntity, context);
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
      final IChiselingContext context
    )
    {
        final RayTraceResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != RayTraceResult.Type.BLOCK || !(rayTraceResult instanceof BlockRayTraceResult))
        {
            return Optional.of(ClickProcessingState.DEFAULT);
        }

        final Function<Direction, Vector3d> placementFacingAdapter = face -> Vector3d.atLowerCornerOf(face.getOpposite().getNormal());
        final BlockRayTraceResult blockRayTraceResult = (BlockRayTraceResult) rayTraceResult;
        final Vector3d hitVector = blockRayTraceResult.getLocation().add(
          placementFacingAdapter.apply(blockRayTraceResult.getDirection())
            .multiply(StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit(), StateEntrySize.current().getSizePerHalfBit())
        );

        final BlockPos hitPos = new BlockPos(hitVector);
        final Vector3d hitBlockPosVector = Vector3d.atLowerCornerOf(hitPos);
        final Vector3d inBlockHitVector = hitVector.subtract(hitBlockPosVector);

        final Deque<Vector3i> toProcess = new LinkedList<>();
        final IAreaAccessor worldAccessor = IMutatorFactory.getInstance().in(context.getWorld(), hitPos);

        final Set<Vector3i> validPositions = new HashSet<>();

        final Vector3i selectedPosition = new Vector3i(
          inBlockHitVector.x() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.y() * StateEntrySize.current().getBitsPerBlockSide(),
          inBlockHitVector.z() * StateEntrySize.current().getBitsPerBlockSide()
        );
        toProcess.addLast(selectedPosition);

        final Vector3d selectedInBlockPosition = Vector3d.atLowerCornerOf(
          selectedPosition
        ).multiply(
          StateEntrySize.current().getSizePerBitScalingVector()
        );

        final Optional<IStateEntryInfo> targetedInfo = worldAccessor.getInAreaTarget(
          selectedInBlockPosition
        );

        if (!targetedInfo.isPresent())
            return Optional.of(ClickProcessingState.DEFAULT);

        worldAccessor.stream()
          .filter(state -> state.getState().equals(targetedInfo.get().getState()))
          .map(state -> state.getStartPoint().multiply(StateEntrySize.current().getBitsPerBlockSideScalingVector()))
          .map(position -> new Vector3i(
            position.x(),
            position.y(),
            position.z()
          ))
          .forEach(validPositions::add);

        context.include(hitPos, Vector3d.ZERO);
        context.include(hitPos, new Vector3d(0.9999, 0.9999, 0.9999));
        context.setStateFilter(accessor -> new SelectedBitStateFilter(validPositions));

        context.setMetadata(ModMetadataKeys.VALID_POSITIONS.get(), validPositions);
        context.setMetadata(ModMetadataKeys.TARGETED_SIDE.get(), blockRayTraceResult.getDirection());
        context.setMetadata(ModMetadataKeys.TARGETED_BLOCK.get(), hitPos);

        return Optional.empty();
    }

    @Override
    public boolean requiresPlaceableEditStack()
    {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return iconName;
    }

    @Override
    public @NotNull Optional<IToolModeGroup> getGroup()
    {
        return Optional.empty();
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    @Override
    public ITextComponent getMultiLineDisplayName()
    {
        return this.multiLineDisplayName;
    }

    private static final class SelectedBitStateFilter implements Predicate<IStateEntryInfo> {

        private final Set<Vector3i> validPositions;

        public SelectedBitStateFilter( final Set<Vector3i> validPositions)
        {
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

            return validPositions.equals(that.validPositions);
        }

        @Override
        public int hashCode()
        {
            return validPositions.hashCode();
        }

        @Override
        public String toString()
        {
            return "SelectedBitStateFilter{" +
                     "validPositions=" + validPositions +
                     '}';
        }
    }
}
