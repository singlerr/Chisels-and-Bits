package mod.chiselsandbits.api.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BlockPosStreamProvider
{

    private BlockPosStreamProvider()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockPosStreamProvider. This is a utility class");
    }

    public static Stream<BlockPos> getForRange(final int size) {
        return getForRange(0, size - 1);
    }

    public static Stream<BlockPos> getForRange(final int min, final int max) {
        return getForRange(min, min, min, max, max, max);
    }

    public static Stream<BlockPos> getForRange(final Vector3d min, final Vector3d max) {
        final BlockPos minPos = new BlockPos(min);
        final BlockPos maxPos = BlockPosUtils.fromCeil(max);

        return getForRange(
          minPos.getX(), minPos.getY(), minPos.getZ(),
          maxPos.getX(), maxPos.getY(), maxPos.getZ()
        );
    }

    public static Stream<BlockPos> getForRange(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ)
    {
        return getForRange(
          new Vector3d(minX, minY, minZ),
          new Vector3d(maxX, maxY, maxZ)
        );
    }

    public static Stream<BlockPos> getForRange(final int minX, final int minY, final int minZ, final int maxX, final int maxY, final int maxZ)
    {
        if (minX == maxX && minY == maxY && minZ == maxZ) {
            return Stream.of(new BlockPos(minX, minY, minZ));
        }

        return IntStream.range(minX, maxX + 1)
          .mapToObj(xCoord -> IntStream.range(minY, maxY + 1)
            .mapToObj(yCoord -> IntStream.range(minZ, maxZ + 1)
              .mapToObj(zCoord -> new BlockPos(xCoord, yCoord, zCoord))))
                 .flatMap(yBasedStream -> yBasedStream.flatMap(Function.identity()));
    }
}
