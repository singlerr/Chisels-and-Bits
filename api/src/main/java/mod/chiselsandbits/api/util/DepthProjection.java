package mod.chiselsandbits.api.util;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;

public enum DepthProjection {

    DOWN(ProjectionPlane.XZ, Direction.DOWN),
    UP(ProjectionPlane.XZ, Direction.UP),
    NORTH(ProjectionPlane.XY, Direction.NORTH),
    SOUTH(ProjectionPlane.XY, Direction.SOUTH),
    EAST(ProjectionPlane.ZY, Direction.EAST),
    WEST(ProjectionPlane.ZY, Direction.WEST);

    private final ProjectionPlane plane;
    private final Direction depthDirection;

    DepthProjection(ProjectionPlane plane, Direction depthDirection) {
        this.plane = plane;
        this.depthDirection = depthDirection;
    }

    public static DepthProjection from(Direction direction) {
        return switch (direction) {
            case DOWN -> DOWN;
            case UP -> UP;
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case EAST -> EAST;
            case WEST -> WEST;
        };
    }

    public ProjectionPlane getPlane() {
        return plane;
    }

    public Direction getDepthDirection() {
        return depthDirection;
    }

    /**
     * Loops from the back to the front through the projection plane with the given sizes.
     * Starting at 0,0,0 and ending at xSize, ySize, zSize.
     *
     * @param xSize    The size of the iterations along the x-axis.
     * @param ySize    The size of the iterations along the y-axis.
     * @param zSize    The size of the iterations along the z-axis.
     * @param callback The callback to invoke for each iteration step.
     */
    public void forEach(final int xSize, final int ySize, final int zSize, ForEach callback) {

        final int horizontalIterationSize = getPlane().getHorizontalSpanAxis().choose(xSize, ySize, zSize);
        final int verticalIterationSize = getPlane().getVerticalSpanAxis().choose(xSize, ySize, zSize);
        final int depthIterationSize = getDepthDirection().getAxis().choose(xSize, ySize, zSize);

        if (getDepthDirection().getAxisDirection() == Direction.AxisDirection.POSITIVE) {
            for (int h = 0; h < horizontalIterationSize; h++) {
                for (int v = 0; v < verticalIterationSize; v++) {
                    for (int d = 0; d < depthIterationSize; d++) {
                        final Vec3i input = new Vec3i(h, v, d);
                        final Vec3i output = unchoose(input);
                        callback.accept(output.getX(), output.getY(), output.getZ(), h, v, d, horizontalIterationSize, verticalIterationSize, depthIterationSize);
                    }
                }
            }
        } else {
            for (int h = horizontalIterationSize - 1; h >= 0; h--) {
                for (int v = 0; v < verticalIterationSize; v++) {
                    for (int d = depthIterationSize - 1; d >= 0; d--) {
                        final Vec3i input = new Vec3i(h, v, d);
                        final Vec3i output = unchoose(input);
                        callback.accept(output.getX(), output.getY(), output.getZ(), h, v, d, horizontalIterationSize, verticalIterationSize, depthIterationSize);
                    }
                }
            }
        }
    }

    public void traverse(final int horizontalPos, final int verticalPos, final int depthStart, final Direction.AxisDirection depthDirection, final int depthSize, final int xSize, final int ySize, final int zSize, Traverse callback) {
        final int depthIterationSize = getDepthDirection().getAxis().choose(xSize, ySize, zSize);

        if (depthDirection == Direction.AxisDirection.POSITIVE) {
            for (int d = depthStart; d < depthStart + depthSize && d < depthIterationSize; d++) {
                final Vec3i input = new Vec3i(horizontalPos, verticalPos, d);
                final Vec3i output = unchoose(input);
                callback.accept(output.getX(), output.getY(), output.getZ());
            }
        } else {
            for (int d = depthStart; d > (depthStart - depthSize) && d > 0; d--) {
                final Vec3i input = new Vec3i(horizontalPos, verticalPos, d);
                final Vec3i output = unchoose(input);
                callback.accept(output.getX(), output.getY(), output.getZ());
            }
        }
    }

    private Vec3i unchoose(final Vec3i input) {
        final int x;
        if (getPlane().getHorizontalSpanAxis() == Direction.Axis.X) {
            x = input.getX();
        } else if (getPlane().getVerticalSpanAxis() == Direction.Axis.X) {
            x = input.getY();
        } else {
            x = input.getZ();
        }

        final int y;
        if (getPlane().getHorizontalSpanAxis() == Direction.Axis.Y) {
            y = input.getX();
        } else if (getPlane().getVerticalSpanAxis() == Direction.Axis.Y) {
            y = input.getY();
        } else {
            y = input.getZ();
        }

        final int z;
        if (getPlane().getHorizontalSpanAxis() == Direction.Axis.Z) {
            z = input.getX();
        } else if (getPlane().getVerticalSpanAxis() == Direction.Axis.Z) {
            z = input.getY();
        } else {
            z = input.getZ();
        }

        return new Vec3i(x, y, z);
    }

    public interface ForEach {
        void accept(int x, int y, int z, int h, int v, int d, int hSize, int vSize, int dSize);
    }

    public interface Traverse {
        void accept(int x, int y, int z);
    }
}
