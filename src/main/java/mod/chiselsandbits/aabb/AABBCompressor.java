package mod.chiselsandbits.aabb;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.sortable.IPositionMutator;
import mod.chiselsandbits.api.util.VectorUtils;
import mod.chiselsandbits.utils.AABBUtils;
import mod.chiselsandbits.utils.DirectionUtils;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class AABBCompressor
{

    private AABBCompressor()
    {
        throw new IllegalStateException("Can not instantiate an instance of: AABBCompressor. This is a utility class");
    }

    public static Collection<AxisAlignedBB> compressStates(
      final IAreaAccessor accessor,
      final Predicate<IStateEntryInfo> selectablePredicate)
    {
        final BuildingState state = new BuildingState();

        //X == REGION
        //Y == FACE

        accessor.streamWithPositionMutator(IPositionMutator.xyz())
          .forEach(stateEntryInfo -> {
              if (state.getRegionBuildingAxisValue() != stateEntryInfo.getStartPoint().x()) {
                  state.setCurrentBox(null, null);
              }
              state.setRegionBuildingAxisValue(stateEntryInfo.getStartPoint().x());

              if (state.getFaceBuildingAxisValue() != stateEntryInfo.getStartPoint().y()) {
                  state.setCurrentBox(null, null);
              }
              state.setFaceBuildingAxisValue(stateEntryInfo.getStartPoint().y());

              final Optional<Vector3d> previousCenterPoint = state.getLastCenter();
              final Vector3d centerPoint = stateEntryInfo.getCenterPoint();
              state.onNextEntry(centerPoint);

              final Optional<Direction> stepDirection = previousCenterPoint.flatMap(
                d -> DirectionUtils.getDirectionVectorBetweenIfAligned(centerPoint, d)
              );

              final Optional<AxisAlignedBB> potentialEntryData = buildBoundingBox(stateEntryInfo, selectablePredicate);

              if (!potentialEntryData.isPresent()) {
                  state.setCurrentBox(null, centerPoint);
                  return;
              }
              final AxisAlignedBB entryData = potentialEntryData.get();

              if (state.getCurrentBox() != null) {
                  if (stepDirection
                    .map(direction -> AABBUtils.areBoxesNeighbors(state.getCurrentBox(), entryData, direction))
                    .filter(b -> b)
                    .isPresent()) {
                      state.expandCurrentBoxToInclude(entryData, centerPoint);

                      if (attemptMergeWithNeighbors(state, centerPoint, state.getCurrentBox()))
                      {
                          return;
                      }

                      return;
                  }
              }

              if (attemptMergeWithNeighbors(state, centerPoint, entryData))
              {
                  return;
              }

              state.setCurrentBox(potentialEntryData.get(), centerPoint);
          });

        return Lists.newArrayList(state.getBoxes());
    }

    private static boolean attemptMergeWithNeighbors(final BuildingState state, final Vector3d centerPoint, final AxisAlignedBB entryData)
    {
        for (final Direction offsetDirection :Direction.values())
        {
            final Vector3d neighborCenter = centerPoint.add(Vector3d.atLowerCornerOf(offsetDirection.getNormal()).multiply(
              StateEntrySize.current().getSizePerBit(),
              StateEntrySize.current().getSizePerBit(),
              StateEntrySize.current().getSizePerBit()
              ));
            final Optional<AxisAlignedBB> potentialNeighborBox = state.getBoxFor(neighborCenter);

            if (potentialNeighborBox.isPresent()) {
                final AxisAlignedBB neighborBox = potentialNeighborBox.get();
                if (AABBUtils.areBoxesNeighbors(entryData, neighborBox, offsetDirection)) {
                    state.expandBoxAt(neighborCenter, entryData, centerPoint);
                    return true;
                }
            }
        }
        return false;
    }

    public static Optional<AxisAlignedBB> buildBoundingBox(
      final IStateEntryInfo stateEntryInfo,
      final Predicate<IStateEntryInfo> selectablePredicate) {
        if (!selectablePredicate.test(stateEntryInfo))
            return Optional.empty();

        return Optional.of(stateEntryInfo.getBoundingBox());
    }

    private static final class BuildingState {
        private double regionBuildingAxis = Double.NEGATIVE_INFINITY;
        private double faceBuildingAxis = Double.NEGATIVE_INFINITY;

        private Vector3d lastCenterPoint = null;
        private AxisAlignedBB currentBox;

        private final Map<Vector3d, AxisAlignedBB>      boxAssignments   = Maps.newHashMap();
        private final Multimap<AxisAlignedBB, Vector3d> stateAssignments = HashMultimap.create();

        public double getRegionBuildingAxisValue()
        {
            return regionBuildingAxis;
        }

        public void setRegionBuildingAxisValue(final double regionBuildingAxis)
        {
            this.regionBuildingAxis = regionBuildingAxis;
        }

        public double getFaceBuildingAxisValue()
        {
            return faceBuildingAxis;
        }

        public void setFaceBuildingAxisValue(final double faceBuildingAxis)
        {
            this.faceBuildingAxis = faceBuildingAxis;
        }

        public AxisAlignedBB getCurrentBox()
        {
            return currentBox;
        }

        public void setCurrentBox(final AxisAlignedBB currentBox, final Vector3d centerPoint)
        {
            this.currentBox = currentBox;
            if (currentBox != null) {
                boxAssignments.put(centerPoint, currentBox);
                stateAssignments.put(currentBox, centerPoint);
            }
        }

        public Optional<AxisAlignedBB> getBoxFor(final Vector3d target) {
            return Optional.ofNullable(boxAssignments.get(target));
        }

        public Optional<Vector3d> getLastCenter() {
            return Optional.ofNullable(lastCenterPoint);
        }

        public void onNextEntry(final Vector3d lastCenterPoint) {
            this.lastCenterPoint = lastCenterPoint;
        }

        public void expandCurrentBoxToInclude(final AxisAlignedBB entryData, final Vector3d centerPoint)
        {
            final AxisAlignedBB current = this.getCurrentBox();
            if (current == null)
                throw new IllegalStateException("Can not expand current box, if current is not set.");

            final AxisAlignedBB expanded = current.minmax(entryData);

            final Collection<Vector3d> currentlyAssignedToCurrent = stateAssignments.removeAll(current);

            currentlyAssignedToCurrent.forEach(v -> boxAssignments.put(v, expanded));
            stateAssignments.putAll(expanded, currentlyAssignedToCurrent);

            boxAssignments.put(centerPoint, expanded);
            stateAssignments.put(expanded, centerPoint);

            this.currentBox = expanded;
        }

        public Collection<AxisAlignedBB> getBoxes()
        {
            return stateAssignments.keySet();
        }

        public void expandBoxAt(final Vector3d neighborCenter, final AxisAlignedBB entryData, final Vector3d centerPoint)
        {
            final AxisAlignedBB current = boxAssignments.get(neighborCenter);
            if (current == null)
                throw new IllegalStateException(String.format("Can not expand box at: %s, if current is not set.", neighborCenter));

            final AxisAlignedBB expanded = current.minmax(entryData);

            final Collection<Vector3d> currentlyAssignedToCurrent = stateAssignments.removeAll(current);

            currentlyAssignedToCurrent.forEach(v -> boxAssignments.put(v, expanded));
            stateAssignments.putAll(expanded, currentlyAssignedToCurrent);

            boxAssignments.put(centerPoint, expanded);
            stateAssignments.put(expanded, centerPoint);
        }
    }
}
