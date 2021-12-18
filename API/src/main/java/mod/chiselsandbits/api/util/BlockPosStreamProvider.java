package mod.chiselsandbits.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Utility class which provides a stream of BlockPos objects within a given range.
 */
public class BlockPosStreamProvider
{

    private BlockPosStreamProvider()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockPosStreamProvider. This is a utility class");
    }

    /**
     * Provides a stream of BlockPos objects within a given range which is {@code size} cubed in size.
     * The coordinates will as such (on each axis) run from 0 to {@code size - 1}.
     *
     * Internally invokes {@link #getForRange(int, int)}, with {@code 0} as the min value,
     * and {@code size - 1} as max value.
     *
     * @param size The size of each axis for the block positions stream.
     * @return A stream of blockpositions in the {@code size} cubed in area.
     */
    public static Stream<BlockPos> getForRange(final int size) {
        return getForRange(0, size - 1);
    }

    /**
     * Provides a stream of BlockPos objects within a given range from the given minimal value to the given maximal value (on all axis).
     *
     * @param min The minimal value of the block positions stream on all axis.
     * @param max The maximal value of the block positions stream on all axis.
     * @return The block position stream within the given range.
     */
    public static Stream<BlockPos> getForRange(final int min, final int max) {
        return getForRange(min, min, min, max, max, max);
    }

    /**
     * Provides a stream of BlockPos objects within a given range from the given minimal value to the given maximal value.
     *
     * This rounds the vectors down to the block positions they are in, and then extracts the minimal and maximal values
     * for each axis.
     *
     * Internally invokes {@link #getForRange(int, int, int, int, int, int)}, with the rounded down vectors as the min and max values.
     *
     * @param min The unrounded minimal value of the block positions stream on all axis.
     * @param max The unrounded maximal value of the block positions stream on all axis.
     * @return The block position stream within the given range.
     */
    public static Stream<BlockPos> getForRange(final Vec3 min, final Vec3 max) {
        final BlockPos minPos = new BlockPos(min);
        final BlockPos maxPos = new BlockPos(max);

        return getForRange(
          minPos.getX(), minPos.getY(), minPos.getZ(),
          maxPos.getX(), maxPos.getY(), maxPos.getZ()
        );
    }

    /**
     * Provides a stream of BlockPos objects within a given range from the given minimal value to the given maximal value.
     *
     * Internally this calls {@link #getForRange(Vec3, Vec3)}, with vectors constructed from the minimal and maximal values.
     * All of its logic (so the rounding) is as such also applied.
     *
     * @param minX The minimal value of the X coordinate.
     * @param minY The minimal value of the Y coordinate.
     * @param minZ The minimal value of the Z coordinate.
     * @param maxX The maximal value of the X coordinate.
     * @param maxY The maximal value of the Y coordinate.
     * @param maxZ The maximal value of the Z coordinate.
     * @return The block position stream within the given range.
     */
    public static Stream<BlockPos> getForRange(final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ)
    {
        return getForRange(
          new Vec3(minX, minY, minZ),
          new Vec3(maxX, maxY, maxZ)
        );
    }

    /**
     * Provides a stream of BlockPos objects within a given range from the given minimal value to the given maximal value.
     *
     * If the minimal and maximal values are the same, then the stream will contain only one block position.
     *
     * @param minX The minimal value of the X coordinate.
     * @param minY The minimal value of the Y coordinate.
     * @param minZ The minimal value of the Z coordinate.
     * @param maxX The maximal value of the X coordinate.
     * @param maxY The maximal value of the Y coordinate.
     * @param maxZ The maximal value of the Z coordinate.
     * @return The block position stream within the given range.
     */
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
