package mod.chiselsandbits.chiseling.modes.depthmap;

import com.communi.suggestu.scena.core.registries.AbstractCustomRegistryEntry;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.chiseling.ChiselingOperation;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.chiseling.mode.IChiselMode;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.map.bit.BitDepthMap;
import mod.chiselsandbits.api.map.bit.BitDepthMapBuilder;
import mod.chiselsandbits.api.map.bit.IBitDepthMap;
import mod.chiselsandbits.api.map.bit.IDepthMapFilter;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IWorldAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.util.DepthProjection;
import mod.chiselsandbits.api.util.IBatchMutation;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.RayTracingUtils;
import mod.chiselsandbits.api.util.Vector2i;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.registrars.ModMetadataKeys;
import mod.chiselsandbits.utils.BitInventoryUtils;
import mod.chiselsandbits.utils.BlockPosUtils;
import mod.chiselsandbits.utils.ItemStackUtils;
import mod.chiselsandbits.voxelshape.MultiStateBlockEntityDiscreteVoxelShape;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CubeVoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

public class DepthMapBasedChiselMode extends AbstractCustomRegistryEntry implements IChiselMode {

    private final IDepthMapFilter[] depthMapFilter;
    private final MutableComponent displayName;
    private final MutableComponent multiLineDisplayName;
    private final ResourceLocation iconName;

    DepthMapBasedChiselMode(IDepthMapFilter[] depthMapFilter, final MutableComponent displayName, final MutableComponent multiLineDisplayName, final ResourceLocation iconName) {
        this.depthMapFilter = depthMapFilter;
        this.displayName = displayName;
        this.multiLineDisplayName = multiLineDisplayName;
        this.iconName = iconName;
    }

    @Override
    public ClickProcessingState onLeftClickBy(
            final Player playerEntity, final IChiselingContext context) {
        final Optional<ClickProcessingState>  processingState = processRayTraceIntoContext(
                playerEntity,
                context,
                direction -> Vec3.atLowerCornerOf(direction.getNormal()).multiply(StateEntrySize.current().getSizePerHalfBitScalingVector())
        );

        context.setComplete();

        if (context.isSimulation())
        {
            return ClickProcessingState.DEFAULT;
        }

        return processingState.orElseGet(() -> context.getMutator().map(mutator -> {
            try (IBatchMutation ignored =
                         mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity))) {
                final Map<IBlockInformation, Integer> resultingBitCount = Maps.newHashMap();

                final Predicate<IStateEntryInfo> filter = context.getStateFilter()
                        .map(factory -> factory.apply(mutator))
                        .orElse((s) -> true);

                mutator.inWorldMutableStream()
                        .filter(filter)
                        .forEach(state -> {
                            final IBlockInformation currentState = state.getBlockInformation();
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
            return ClickProcessingState.ALLOW;
        }).orElse(ClickProcessingState.DEFAULT));
    }

    @Override
    public void onStoppedLeftClicking(final Player playerEntity, final IChiselingContext context) {
        //Noop
    }

    @Override
    public ClickProcessingState onRightClickBy(final Player playerEntity, final IChiselingContext context) {
        final Optional<ClickProcessingState> processingState = processRayTraceIntoContext(
                playerEntity,
                context,
                direction -> Vec3.atLowerCornerOf(direction.getNormal()).multiply(StateEntrySize.current().getSizePerHalfBitScalingVector())
        );

        context.setComplete();

        if (context.isSimulation()) {
            return ClickProcessingState.DEFAULT;
        }

        return processingState.orElseGet(() -> {
            return context.getMutator().map(mutator -> {
                final IBlockInformation heldBlockState = ItemStackUtils.getHeldBitBlockInformationFromPlayer(playerEntity);
                if (heldBlockState.isAir()) {
                    return ClickProcessingState.DEFAULT;
                }

                final Predicate<IStateEntryInfo> filter = context.getStateFilter()
                        .map(factory -> factory.apply(mutator))
                        .orElse((s) -> true);

                final int missingBitCount = (int) mutator.stream()
                        .filter(state -> state.getBlockInformation().isAir() && filter.test(state))
                        .count();

                final IBitInventory playerBitInventory = IBitInventoryManager.getInstance().create(playerEntity);

                context.setComplete();
                if (playerBitInventory.canExtract(heldBlockState, missingBitCount) || playerEntity.isCreative()) {
                    if (!playerEntity.isCreative()) {
                        playerBitInventory.extract(heldBlockState, missingBitCount);
                    }

                    try (IBatchMutation ignored =
                                 mutator.batch(IChangeTrackerManager.getInstance().getChangeTracker(playerEntity))) {
                        mutator.inWorldMutableStream()
                                .filter(state -> state.getBlockInformation().isAir() && filter.test(state))
                                .forEach(state -> state.overrideState(heldBlockState)); //We can use override state here to prevent the try-catch block.
                    }
                } else {
                    context.setError(LocalStrings.ChiselAttemptFailedNotEnoughBits.getText(heldBlockState.getBlockState().getBlock().getName()));
                }

                if (missingBitCount == 0) {
                    final BlockPos heightPos = new BlockPos(mutator.getInWorldEndPoint());
                    if (heightPos.getY() >= context.getWorld().getMaxBuildHeight()) {
                        Component component = (Component.translatable("build.tooHigh", context.getWorld().getMaxBuildHeight() - 1)).withStyle(ChatFormatting.RED);
                        playerEntity.sendSystemMessage(component);
                    }
                }
                return ClickProcessingState.ALLOW;
            }).orElse(ClickProcessingState.DEFAULT);
        });
    }

    @Override
    public void onStoppedRightClicking(final Player playerEntity, final IChiselingContext context) {
        //Noop
    }

    private Optional<ClickProcessingState>  processRayTraceIntoContext(final Player playerEntity, final IChiselingContext context, Function<Direction, Vec3> offsetGenerator) {
        final HitResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != HitResult.Type.BLOCK || !(rayTraceResult instanceof final BlockHitResult blockRayTraceResult)) {
            context.setError(LocalStrings.ChiselAttemptFailedNoBlock.getText());
            return Optional.of(ClickProcessingState.DEFAULT);
        }

        final Vec3 currentTarget = blockRayTraceResult.getLocation().add(offsetGenerator.apply(blockRayTraceResult.getDirection()));

        Optional<Direction> targetedSide = context.getMetadata(ModMetadataKeys.TARGETED_SIDE.get());
        if (targetedSide.isEmpty()) {
            context.setMetadata(ModMetadataKeys.TARGETED_SIDE.get(), blockRayTraceResult.getDirection());
            targetedSide = context.getMetadata(ModMetadataKeys.TARGETED_SIDE.get());
        }

        final DepthProjection projection = DepthProjection.from(targetedSide.orElseThrow());
        Optional<Vec3> anchor = context.getMetadata(ModMetadataKeys.ANCHOR.get());
        if (anchor.isEmpty() || anchor.get() != currentTarget) {
            context.resetMutator();
            final AABB box = projection.getPlane().getPlaneBoundingBox(1f, 0.5f);

            context.include(currentTarget.add(box.minX, box.minY, box.minZ));
            context.include(currentTarget.add(box.maxX, box.maxY, box.maxZ));

            Optional<Direction> finalTargetedSide = targetedSide;

            final Optional<IWorldAreaMutator> mutator = context.getMutator();
            final Optional<DepthFilterBasedAreaFilter> filter = mutator.map(
                    worldAreaMutator -> new DepthFilterBasedAreaFilter(currentTarget, depthMapFilter, worldAreaMutator, finalTargetedSide.orElseThrow())
            );

            context.setStateFilter(areaAccessor -> {
                if (areaAccessor instanceof IWorldAreaAccessor worldAreaAccessor && mutator.isPresent() && mutator.get() == worldAreaAccessor) {
                    return filter.orElseThrow();
                }

                return (s) -> false;
            });
            context.setMetadata(ModMetadataKeys.ANCHOR.get(), currentTarget);
        }

        return Optional.empty();
    }

    @Override
    public Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context) {
        return context.getMutator()
                .map(IAreaAccessor.class::cast);
    }

    @Override
    public VoxelShape getShape(final IChiselingContext context) {
        if (context.getMutator().isEmpty())
            return Shapes.empty();

        final IWorldAreaMutator mutator = context.getMutator().get();

        final Optional<Predicate<IStateEntryInfo>> filter = context.getStateFilter().map(factory -> factory.apply(mutator));
        if (filter.isEmpty())
            return Shapes.empty();

        final Predicate<IStateEntryInfo> stateFilter = filter.get();
        if (!(stateFilter instanceof DepthFilterBasedAreaFilter lineAreaFilter))
            return Shapes.empty();

        final BlockPos offset = VectorUtils.invert(new BlockPos(mutator.getInWorldStartPoint()));

        final Set<Vec3i> startPoints =
                context.getModeOfOperandus() == ChiselingOperation.CHISELING ?
                        lineAreaFilter.removed :
                        lineAreaFilter.added;

        final AABB bitSizedBox = mutator.getBitScaledBoundingBox();

        final int xSize = (int) bitSizedBox.getXsize();
        final int ySize = (int) bitSizedBox.getYsize();
        final int zSize = (int) bitSizedBox.getZsize();

        final BitSet set = new BitSet(StateEntrySize.current().getBitsPerBlock());
        startPoints.forEach(p -> set.set(BlockPosUtils.getCollisionIndex(p.getX(), p.getY(), p.getZ())));
        return new CubeVoxelShape(new MultiStateBlockEntityDiscreteVoxelShape(set));
    }

    @Override
    public boolean isStillValid(Player playerEntity, IChiselingContext context, ChiselingOperation modeOfOperation) {
        final Optional<Vec3> anchor = context.getMetadata(ModMetadataKeys.ANCHOR.get());
        if (anchor.isEmpty())
            return false;


        final HitResult rayTraceResult = RayTracingUtils.rayTracePlayer(playerEntity);
        if (rayTraceResult.getType() != HitResult.Type.BLOCK || !(rayTraceResult instanceof final BlockHitResult blockRayTraceResult)) {
            return false;
        }

        final Function<Direction, Vec3> offsetGenerator = direction -> Vec3.atLowerCornerOf(direction.getNormal()).multiply(StateEntrySize.current().getSizePerHalfBitScalingVector());

        final Vec3 currentTarget = blockRayTraceResult.getLocation().add(offsetGenerator.apply(blockRayTraceResult.getDirection()));
        return anchor.get().equals(currentTarget);
    }

    @Override
    public @NotNull ResourceLocation getIcon() {
        return iconName;
    }

    @Override
    public Component getDisplayName() {
        return displayName;
    }

    @Override
    public Component getMultiLineDisplayName() {
        return multiLineDisplayName;
    }

    @Override
    public @NotNull Optional<IToolModeGroup> getGroup() {
        return Optional.empty();
    }

    @Override
    public boolean requiresPlaceableEditStack() {
        return true;
    }

    @Override
    public boolean requiresRestrainingOfShape() {
        return false;
    }

    private static final class DepthFilterBasedAreaFilter implements Predicate<IStateEntryInfo> {

        private final Vec3 anchor;
        private final IDepthMapFilter[] depthMapFilter;
        private final Direction projectionDirection;

        private final Set<Vec3i> removed = Sets.newHashSet();
        private final Set<Vec3i> added = Sets.newHashSet();

        private DepthFilterBasedAreaFilter(Vec3 anchor, IDepthMapFilter[] filters, IAreaAccessor accessor, Direction projectionDirection) {
            this.anchor = anchor;
            this.depthMapFilter = filters;
            this.projectionDirection = projectionDirection;

            final DepthProjection projection = DepthProjection.from(projectionDirection);
            final AABB bitBox = accessor.getBitScaledBoundingBox();

            final IBitDepthMap clonedMap = BitDepthMapBuilder.create(accessor, projection).build();
            final IBitDepthMap originalMap = new BitDepthMap(clonedMap);
            for (IDepthMapFilter filter : depthMapFilter) {
                clonedMap.applyFilter(filter);
            }
            clonedMap.subtract(originalMap);

            final Vec3i clonedMapSize = clonedMap.getSize();

            clonedMap.getEntries().forEach(entry -> {
                final Vector2i position = entry.depthMapPosition();
                final double difference = entry.depth();

                if (Math.abs(difference) < 0.0001)
                    return;

                final int originalDepth = projection.getDepthDirection().getAxisDirection() == Direction.AxisDirection.NEGATIVE ? (int) originalMap.getDepth(position) : clonedMapSize.getZ() - (int) originalMap.getDepth(position);
                final int depthDelta = (int) (difference);

                projection.traverse(position.getX(), position.getY(), originalDepth,
                        originalDepth < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE,
                        depthDelta, (int) bitBox.getXsize(), (int) bitBox.getYsize(), (int) bitBox.getZsize(), (x, y, z) -> {
                            final Vec3i bitPosition = new Vec3i(x, y, z);
                            if (difference < 0) {
                                removed.add(bitPosition);
                            } else {
                                added.add(bitPosition);
                            }
                        });
            });
        }


        @Override
        public boolean test(IStateEntryInfo stateEntryInfo) {
            if (!(stateEntryInfo instanceof final IInWorldStateEntryInfo inWorldStateEntryInfo)) {
                return false;
            }

            final Vec3 startPoint = inWorldStateEntryInfo.getStartPoint().scale(StateEntrySize.current().getBitsPerBlockSide());
            final Vec3i bitPosition = new Vec3i(
                    (int) Math.floor(startPoint.x()),
                    (int) Math.floor(startPoint.y()),
                    (int) Math.floor(startPoint.z())
            );

            return this.added.contains(bitPosition) && !this.removed.contains(bitPosition);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DepthFilterBasedAreaFilter that)) return false;

            if (!anchor.equals(that.anchor)) return false;
            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            if (!Arrays.equals(depthMapFilter, that.depthMapFilter)) return false;
            if (projectionDirection != that.projectionDirection) return false;
            if (!removed.equals(that.removed)) return false;
            return added.equals(that.added);
        }

        @Override
        public int hashCode() {
            int result = anchor.hashCode();
            result = 31 * result + Arrays.hashCode(depthMapFilter);
            result = 31 * result + projectionDirection.hashCode();
            result = 31 * result + removed.hashCode();
            result = 31 * result + added.hashCode();
            return result;
        }
    }
}
