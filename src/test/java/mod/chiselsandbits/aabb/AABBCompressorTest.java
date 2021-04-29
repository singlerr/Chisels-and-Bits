package mod.chiselsandbits.aabb;

import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AABBCompressorTest
{

    @Test
    public void RunDirectNorthNeighborhoodTest() {
        RunDirectNeighboringTest(Direction.NORTH);
    }

    @Test
    public void RunDirectSouthNeighborhoodTest() {
        RunDirectNeighboringTest(Direction.SOUTH);
    }

    @Test
    public void RunDirectEastNeighborhoodTest() {
        RunDirectNeighboringTest(Direction.EAST);
    }

    @Test
    public void RunDirectWestNeighborhoodTest() {
        RunDirectNeighboringTest(Direction.WEST);
    }

    @Test
    public void RunDirectUpNeighborhoodTest() {
        RunDirectNeighboringTest(Direction.UP);
    }

    @Test
    public void RunDirectDownNeighborhoodTest() {
        RunDirectNeighboringTest(Direction.DOWN);
    }
    
    private void RunDirectNeighboringTest(final Direction direction) {
        final AxisAlignedBB initialBox = AxisAlignedBB.withSizeAtOrigin(1, 1, 1);
        final AxisAlignedBB neighborBox = initialBox.offset(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());

        final AxisAlignedBB expectedBox = initialBox.union(neighborBox);

        RunCompressionTest(
          Lists.newArrayList(initialBox, neighborBox),
          Lists.newArrayList(expectedBox),
          "Direct neighborhood in direction: " + direction.name()
        );
    }

    private void RunCompressionTest(
      final Collection<AxisAlignedBB> sources,
      final Collection<AxisAlignedBB> expectedResults,
      final String testName
    ) {
        final List<IStateEntryInfo> entrySources = sources
                                                     .stream()
                                                     .map(box -> {
                                                         final Vector3d startPoint = new Vector3d(box.minX, box.minY, box.minZ);
                                                         final Vector3d endPoint = new Vector3d(box.maxX, box.maxY, box.maxZ);

                                                         final IStateEntryInfo mock = mock(IStateEntryInfo.class);

                                                         when(mock.getBoundingBox()).thenReturn(box);
                                                         when(mock.getStartPoint()).thenReturn(startPoint);
                                                         when(mock.getEndPoint()).thenReturn(endPoint);
                                                         when(mock.getCenterPoint()).thenReturn(startPoint.add(endPoint).mul(0.5,0.5,0.5));

                                                         return mock;
                                                     })
                                                     .sorted(Comparator.<IStateEntryInfo, Double>comparing(stateEntryInfo -> stateEntryInfo.getStartPoint().getX())
                                                               .thenComparing(stateEntryInfo -> stateEntryInfo.getStartPoint().getY())
                                                               .thenComparing(stateEntryInfo -> stateEntryInfo.getStartPoint().getZ()))
                                                     .collect(Collectors.toList());

        final IAreaAccessor mock = mock(IAreaAccessor.class);

        when(mock.streamWithPositionMutator(any())).then((Answer<Stream<IStateEntryInfo>>) invocation -> entrySources.stream());

        RunCompressionTest(
          mock,
          s -> true,
          expectedResults,
          testName
        );
    }

    private void RunCompressionTest(
      final IAreaAccessor areaAccessor,
      final Predicate<IStateEntryInfo> selectionPredicate,
      final Collection<AxisAlignedBB> expectedResults,
      final String testName
    ) {
        final Collection<AxisAlignedBB> calculatedResults = AABBCompressor.compressStates(
          areaAccessor, selectionPredicate
        );

        Assert.assertEquals(String.format("The calculated results for: %s do not match.", testName), expectedResults, calculatedResults);
    }
}