package mod.chiselsandbits.aabb;

import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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
        final AABB initialBox = AABB.ofSize(Vec3.ZERO, 1/16d, 1/16d, 1/16d);
        final AABB neighborBox = initialBox.move(direction.getStepX() /16d, direction.getStepY() /16d, direction.getStepZ() /16d);

        final AABB expectedBox = initialBox.minmax(neighborBox);

        RunCompressionTest(
          Lists.newArrayList(initialBox, neighborBox),
          Lists.newArrayList(expectedBox),
          "Direct neighborhood in direction: " + direction.name()
        );
    }

    @SuppressWarnings("unchecked")
    private void RunCompressionTest(
      final Collection<AABB> sources,
      final Collection<AABB> expectedResults,
      final String testName
    ) {
        final List<IStateEntryInfo> entrySources = sources
          .stream()
          .map(box -> {
              final Vec3 startPoint = new Vec3(box.minX, box.minY, box.minZ);
              final Vec3 endPoint = new Vec3(box.maxX, box.maxY, box.maxZ);

              final IStateEntryInfo mock = mock(IStateEntryInfo.class);

              when(mock.getBoundingBox()).thenReturn(box);
              when(mock.getStartPoint()).thenReturn(startPoint);
              when(mock.getEndPoint()).thenReturn(endPoint);
              when(mock.getCenterPoint()).thenReturn(startPoint.add(endPoint).multiply(0.5, 0.5, 0.5));

              return mock;
          })
          .sorted(Comparator.<IStateEntryInfo, Double>comparing(stateEntryInfo -> stateEntryInfo.getStartPoint().x())
            .thenComparing(stateEntryInfo -> stateEntryInfo.getStartPoint().y())
            .thenComparing(stateEntryInfo -> stateEntryInfo.getStartPoint().z())).toList();

        final IAreaAccessor mock = mock(IAreaAccessor.class);

        when(mock.streamWithPositionMutator(any())).then((Answer<Stream<IStateEntryInfo>>) invocation -> entrySources.stream());
        doAnswer((Answer<Void>) invocation -> {
            final Consumer<IStateEntryInfo> callback = invocation.getArgumentAt(1, Consumer.class);
            entrySources.forEach(callback);
            return null;
        }).when(mock).forEachWithPositionMutator(any(), any());

        RunCompressionTest(
          mock,
          expectedResults,
          testName
        );
    }

    private void RunCompressionTest(
      final IAreaAccessor areaAccessor,
      final Collection<AABB> expectedResults,
      final String testName
    ) {
        final Collection<AABB> calculatedResults = AABBCompressor.compressStates(
          areaAccessor, CollisionType.ALL
        );

        Assert.assertEquals(String.format("The calculated results for: %s do not match.", testName), expectedResults, calculatedResults);
    }


}