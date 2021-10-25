package mod.chiselsandbits.api.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector4f;

import java.util.Random;

public class VectorUtils
{

    public final static double DEG_TO_RAD_FACTOR = (2*Math.PI) / 360d;

    private VectorUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: VectorUtils. This is a utility class");
    }

    public static Vector3d rotateMultipleTimes90Degrees(final Vector3d vector3d, final Direction.Axis axis, final int times) {
        return rotateDegrees(vector3d, axis, (times * 90) % 360);
    }

    public static Vector3d rotate90Degrees(final Vector3d vector3d, final Direction.Axis axis) {
        return rotateDegrees(vector3d, axis, 90);
    }

    public static Vector3d rotateHalfRadian(final Vector3d vector3d, final Direction.Axis axis) {
        return rotate(vector3d, axis, 0.5 * Math.PI);
    }

    public static Vector3d rotateDegrees(final Vector3d vector3d, final Direction.Axis axis, final double angleInDegrees) {
        return rotate(vector3d, axis, angleInDegrees * DEG_TO_RAD_FACTOR);
    }

    public static Vector3d rotate(final Vector3d vector3d, final Direction.Axis axis, final double angleInRadian) {
        switch (axis) {
            case X:
                return new Vector3d(
                  vector3d.x(),
                  vector3d.y() * Math.cos((float) angleInRadian) - vector3d.z() * Math.sin((float) angleInRadian),
                  vector3d.y() * Math.sin((float) angleInRadian) + vector3d.z() * Math.cos((float) angleInRadian)
                );
            case Y:
                return new Vector3d(
                  vector3d.x() * Math.cos((float) angleInRadian) + vector3d.z() * Math.sin((float) angleInRadian),
                  vector3d.y(),
                  -vector3d.x() * Math.sin((float) angleInRadian) + vector3d.z() * Math.cos((float) angleInRadian)
                );
            case Z:
                return new Vector3d(
                  vector3d.x() * Math.cos((float) angleInRadian) - vector3d.y() * Math.sin((float) angleInRadian),
                  vector3d.x() * Math.sin((float) angleInRadian) + vector3d.y() * Math.cos((float) angleInRadian),
                  vector3d.z()
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
        final double x = Math.abs(v.x());
        final double y = Math.abs(v.y());
        final double z = Math.abs(v.z());

        if (x >= y && x >= z) {
            return v.x();
        }

        if (y >= x && y >= z) {
            return v.y();
        }

        if (z >= x && z >= y) {
            return v.z();
        }

        return 0;
    }

    public static Vector3d invert(final Vector3d v) {
        return v.multiply(-1, -1, -1);
    }

    public static BlockPos invert(final BlockPos v) {
        return new BlockPos(-1 * v.getX(), -1 * v.getY(), -1 * v.getZ());
    }

    public static Vector3d minimizeTowardsZero(final Vector3d start, final Vector3d end) {
        return new Vector3d(
          MathUtil.minimizeTowardsZero(start.x(), end.x()),
          MathUtil.minimizeTowardsZero(start.y(), end.y()),
          MathUtil.minimizeTowardsZero(start.z(), end.z())
        );
    }

    public static Vector3d maximizeAwayFromZero(final Vector3d start, final Vector3d end) {
        return new Vector3d(
          MathUtil.maximizeAwayFromZero(start.x(), end.x()),
          MathUtil.maximizeAwayFromZero(start.y(), end.y()),
          MathUtil.maximizeAwayFromZero(start.z(), end.z())
        );
    }

    public static Vector3d absolute(final Vector3d vector3d) {
        return new Vector3d(
          vector3d.x() < 0 ? -1 * vector3d.x() : vector3d.x(),
          vector3d.y() < 0 ? -1 * vector3d.y() : vector3d.y(),
          vector3d.z() < 0 ? -1 * vector3d.z() : vector3d.z()
        );
    }

    public static Vector3d offsetRandomly(Vector3d source, Random random, float radius) {
        return new Vector3d(source.x + (random.nextFloat() - .5f) * 2 * radius, source.y + (random.nextFloat() - .5f) * 2 * radius,
          source.z + (random.nextFloat() - .5f) * 2 * radius);
    }

    public static Vector3d minimize(final Vector3d a, final Vector3d b)
    {
        return new Vector3d(
          Math.min(a.x(), b.x()),
          Math.min(a.y(), b.y()),
          Math.min(a.z(), b.z())
        );
    }

    public static Vector3d maximize(final Vector3d a, final Vector3d b)
    {
        return new Vector3d(
          Math.max(a.x(), b.x()),
          Math.max(a.y(), b.y()),
          Math.max(a.z(), b.z())
        );
    }

    public static Vector3d makePositive(final Vector3d inBlockOffset)
    {
        return new Vector3d(
          MathUtil.makePositive(inBlockOffset.x()),
          MathUtil.makePositive(inBlockOffset.y()),
          MathUtil.makePositive(inBlockOffset.z())
        );
    }

    public static boolean allValuesBetweenInclusive(final Vector4f vector4f, final float min, final float max) {
        return min <= vector4f.x() && vector4f.x() <= max &&
                 min <= vector4f.y() && vector4f.y() <= max &&
                 min <= vector4f.z() && vector4f.z() <= max &&
                 min <= vector4f.w() && vector4f.w() <= max;
    }
}
