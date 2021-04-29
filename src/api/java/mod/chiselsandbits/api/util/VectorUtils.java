package mod.chiselsandbits.api.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

public class VectorUtils
{

    private final static double DEG_TO_RAD_FACTOR = Math.PI / 360d;

    private VectorUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: VectorUtils. This is a utility class");
    }

    public static Vector3d rotate90Degrees(final Vector3d vector3d, final Direction.Axis axis) {
        return rotateHalfRadian(vector3d, axis);
    }

    public static Vector3d rotateHalfRadian(final Vector3d vector3d, final Direction.Axis axis) {
        return rotateDegrees(vector3d, axis, 0.5 * Math.PI);
    }

    public static Vector3d rotateDegrees(final Vector3d vector3d, final Direction.Axis axis, final double angleInDegrees) {
        return rotate(vector3d, axis, angleInDegrees * DEG_TO_RAD_FACTOR);
    }

    public static Vector3d rotate(final Vector3d vector3d, final Direction.Axis axis, final double angleInRadian) {
        switch (axis) {
            case X:
                return new Vector3d(
                  vector3d.getX(),
                  vector3d.getY() * MathHelper.cos((float) angleInRadian) - vector3d.getZ() * MathHelper.sin((float) angleInRadian),
                  vector3d.getY() * MathHelper.sin((float) angleInRadian) + vector3d.getZ() * MathHelper.cos((float) angleInRadian)
                );
            case Y:
                return new Vector3d(
                  vector3d.getX() * MathHelper.cos((float) angleInRadian) + vector3d.getZ() * MathHelper.sin((float) angleInRadian),
                  vector3d.getY(),
                  -vector3d.getX() * MathHelper.sin((float) angleInRadian) + vector3d.getZ() * MathHelper.cos((float) angleInRadian)
                );
            case Z:
                return new Vector3d(
                  vector3d.getX() * MathHelper.cos((float) angleInRadian) - vector3d.getY() * MathHelper.sin((float) angleInRadian),
                  vector3d.getX() * MathHelper.sin((float) angleInRadian) + vector3d.getY() * MathHelper.cos((float) angleInRadian),
                  vector3d.getZ()
                );
            default:
                throw new IllegalArgumentException(String.format("Unknown axis: %s", axis));
        }
    }

    public static Vector3d scaleToOne(final Vector3d v) {
        final double maxSize = Math.abs(getMaximalComponent(v));
        if (MathUtil.almostEqual(maxSize, 0))
            return Vector3d.ZERO;

        final double scale = 1 / maxSize;

        return v.scale(scale);
    }

    public static double getMaximalComponent(final Vector3d v) {
        final double x = Math.abs(v.getX());
        final double y = Math.abs(v.getY());
        final double z = Math.abs(v.getZ());

        if (x >= y && x >= z) {
            return v.getX();
        }

        if (y >= x && y >= z) {
            return v.getY();
        }

        if (z >= x && z >= y) {
            return v.getZ();
        }

        return 0;
    }

    public static Vector3d invert(final Vector3d v) {
        return v.mul(-1, -1, -1);
    }
}
