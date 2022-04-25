package mod.chiselsandbits.aabb;

import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.utils.ChiselsAndBitsAPITestUtils;
import net.minecraft.SharedConstants;
import net.minecraft.WorldVersion;
import net.minecraft.core.Direction;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.storage.DataVersion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.stubbing.Answer;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static mod.chiselsandbits.utils.ChiselsAndBitsAPITestUtils.mockBlockStateIdManager;
import static mod.chiselsandbits.utils.ChiselsAndBitsAPITestUtils.setupApi;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class AABBCompressorTest
{

    private final Direction direction;
    private final CollisionType collisionType;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        setupApi(
          ChiselsAndBitsAPITestUtils::mockBlockStateIdManager,
          ChiselsAndBitsAPITestUtils::mockDefaultStateEntrySize
        );

        return Arrays.asList(Arrays.stream(Direction.values())
                 .flatMap(direction -> {
                     return Arrays.stream(CollisionType.values())
                              .map(collisionType -> new Object[] { direction, collisionType });
                 })
                 .toArray(Object[][]::new));
    }

    public AABBCompressorTest(final Direction direction, final CollisionType collisionType) {
        this.direction = direction;
        this.collisionType = collisionType;
    }

    @Test
    public void RunDirectNeighboringTest() {
        final AABB initialBox = AABB.ofSize(Vec3.ZERO, 1/16d, 1/16d, 1/16d);
        final AABB neighborBox = initialBox.move(direction.getStepX() /16d, direction.getStepY() /16d, direction.getStepZ() /16d);

        final AABB expectedBox = initialBox.minmax(neighborBox);

        RunCompressionTest(
          Lists.newArrayList(initialBox, neighborBox),
          Lists.newArrayList(expectedBox),
          "Direct neighborhood in direction: " + direction.name() + " for type: " + collisionType.name(),
          collisionType
        );
    }

    @SuppressWarnings("unchecked")
    private void RunCompressionTest(
      final Collection<AABB> sources,
      final Collection<AABB> expectedResults,
      final String testName,
      final CollisionType type
    ) {
        final List<IStateEntryInfo> entrySources = sources
          .stream()
          .map(box -> {
              final Vec3 startPoint = new Vec3(box.minX, box.minY, box.minZ);
              final Vec3 endPoint = new Vec3(box.maxX, box.maxY, box.maxZ);

              final IStateEntryInfo mock = mock(IStateEntryInfo.class);

              final BlockInformation blockInformation = new BlockInformation(type.getExampleState());

              when(mock.getBoundingBox()).thenReturn(box);
              when(mock.getStartPoint()).thenReturn(startPoint);
              when(mock.getEndPoint()).thenReturn(endPoint);
              when(mock.getCenterPoint()).thenReturn(startPoint.add(endPoint).multiply(0.5, 0.5, 0.5));
              when(mock.getBlockInformation()).thenReturn(blockInformation);

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
          testName,
          type
        );
    }

    private void RunCompressionTest(
      final IAreaAccessor areaAccessor,
      final Collection<AABB> expectedResults,
      final String testName,
      final CollisionType collisionType
    ) {
        final Collection<AABB> calculatedResults = AABBCompressor.compressStates(
          areaAccessor, collisionType
        );

        Assert.assertEquals(String.format("The calculated results for: %s do not match.", testName), expectedResults, calculatedResults);
    }


}