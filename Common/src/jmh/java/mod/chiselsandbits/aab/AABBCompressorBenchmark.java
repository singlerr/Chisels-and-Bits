package mod.chiselsandbits.aab;

import mod.chiselsandbits.aabb.AABBCompressor;
import mod.chiselsandbits.api.axissize.CollisionType;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.BlockPosStreamProvider;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.mockito.stubbing.Answer;
import org.openjdk.jmh.annotations.*;

import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AABBCompressorBenchmark
{
    @State(Scope.Benchmark)
    public static class ExecutionPlan {

        @Param({ "6", "7"})
        public int xSize;

        @Param({ "2", "3"})
        public int ySize;

        @Param({ "9", "10"})
        public int zSize;

        //@Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"})
        public int xOffset;

        //@Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"})
        public int yOffset;

        //@Param({ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"})
        public int zOffset;

        public IAreaAccessor accessor;

        @SuppressWarnings("unchecked")
        @Setup(Level.Iteration)
        public void setUp() {
            final int runtimeXSize = Math.max(0, Math.min(xSize, StateEntrySize.current().getBitsPerBlockSide() - xOffset));
            final int runtimeYSize = Math.max(0, Math.min(ySize, StateEntrySize.current().getBitsPerBlockSide() - yOffset));
            final int runtimeZSize = Math.max(0, Math.min(zSize, StateEntrySize.current().getBitsPerBlockSide() - zOffset));

            final List<IStateEntryInfo> states = BlockPosStreamProvider.getForRange(StateEntrySize.current().getBitsPerBlockSide())
              .map(offset -> {
                  final BlockState candidateState;
                  if (offset.getX() > xOffset && offset.getX() < xOffset + runtimeXSize ||
                    offset.getY() > yOffset && offset.getY() < yOffset + runtimeYSize ||
                    offset.getZ() > zOffset && offset.getZ() < zOffset + runtimeZSize) {
                      candidateState = Blocks.STONE.defaultBlockState();
                  }
                  else {
                      candidateState = Blocks.AIR.defaultBlockState();
                  }

                  final IStateEntryInfo info = mock(IStateEntryInfo.class);

                  final Vec3 startPoint = Vec3.atLowerCornerOf(offset);
                  final Vec3 endPoint = startPoint.add(StateEntrySize.current().getSizePerBitScalingVector());

                  when(info.getStartPoint()).thenReturn(startPoint);
                  when(info.getEndPoint()).thenReturn(endPoint);
                  when(info.getCenterPoint()).thenReturn(startPoint.add(endPoint).multiply(0.5, 0.5, 0.5));
                  when(info.getState()).thenReturn(candidateState);
                  when(info.getBoundingBox()).thenReturn(AABB.ofSize(startPoint,
                    StateEntrySize.current().getSizePerBit(),
                    StateEntrySize.current().getSizePerBit(),
                    StateEntrySize.current().getSizePerBit()
                  ));

                  return info;
              })
              .sorted(Comparator.<IStateEntryInfo, Double>comparing(stateEntryInfo -> stateEntryInfo.getStartPoint().x())
                .thenComparing(stateEntryInfo -> stateEntryInfo.getStartPoint().y())
                .thenComparing(stateEntryInfo -> stateEntryInfo.getStartPoint().z())).toList();

            accessor = mock(IAreaAccessor.class);

            when(accessor.streamWithPositionMutator(any())).then((Answer<Stream<IStateEntryInfo>>) invocation -> states.stream());
            doAnswer((Answer<Void>) invocation -> {
                final Consumer<IStateEntryInfo> callback = invocation.getArgumentAt(1, Consumer.class);
                states.forEach(callback);
                return null;
            }).when(accessor).forEachWithPositionMutator(any(), any());
        }

        @Setup(Level.Trial)
        public void bootStrap() {
            SharedConstants.tryDetectVersion();
            Bootstrap.bootStrap();
        }
    }

    @Benchmark
    @Warmup(iterations = 5, time = 1)
    public void benchmark(ExecutionPlan plan) {
        AABBCompressor.compressStates(plan.accessor, CollisionType.NONE_AIR);
    }
}
