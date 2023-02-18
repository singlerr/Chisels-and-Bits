package mod.chiselsandbits.api.util;

import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;

import static net.minecraft.core.Direction.Axis.X;
import static net.minecraft.core.Direction.Axis.Y;
import static net.minecraft.core.Direction.Axis.Z;

public enum ProjectionPlane {
    XY(X, Y, Z),
    XZ(X, Z, Y),
    ZY(Z, Y, X);

    private final Direction.Axis horizontalSpanAxis;
    private final Direction.Axis verticalSpanAxis;
    private final Direction.Axis depthAxis;

    ProjectionPlane(Direction.Axis horizontalSpanAxis, Direction.Axis verticalSpanAxis, Direction.Axis depthAxis) {
        this.horizontalSpanAxis = horizontalSpanAxis;
        this.verticalSpanAxis = verticalSpanAxis;
        this.depthAxis = depthAxis;
    }

    public Direction.Axis getHorizontalSpanAxis() {
        return horizontalSpanAxis;
    }

    public Direction.Axis getVerticalSpanAxis() {
        return verticalSpanAxis;
    }

    public Direction.Axis getDepthAxis() {
        return depthAxis;
    }

    public Vector2i getPlaneSize(Vec3i size) {
        return new Vector2i(
            horizontalSpanAxis.choose(size.getX(), size.getY(), size.getZ()),
            verticalSpanAxis.choose(size.getX(), size.getY(), size.getZ())
        );
    }

    public AABB getPlaneBoundingBox(final float planeSize, final float depth) {
        final float planeSizeFloat = planeSize / 2f;
        final float depthSizeFloat = depth / 2f;

        return switch (this) {
            case XY -> new AABB(-planeSizeFloat, -planeSizeFloat, -depthSizeFloat, planeSizeFloat, planeSizeFloat, depthSizeFloat);
            case XZ -> new AABB(-planeSizeFloat, -depthSizeFloat, -planeSizeFloat, planeSizeFloat, depthSizeFloat, planeSizeFloat);
            case ZY -> new AABB(-depthSizeFloat, -planeSizeFloat, -planeSizeFloat, depthSizeFloat, planeSizeFloat, planeSizeFloat);
        };
    }
}
