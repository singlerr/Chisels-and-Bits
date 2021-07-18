package mod.chiselsandbits.api.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;

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
                  vector3d.getX(),
                  vector3d.getY() * Math.cos((float) angleInRadian) - vector3d.getZ() * Math.sin((float) angleInRadian),
                  vector3d.getY() * Math.sin((float) angleInRadian) + vector3d.getZ() * Math.cos((float) angleInRadian)
                );
            case Y:
                return new Vector3d(
                  vector3d.getX() * Math.cos((float) angleInRadian) + vector3d.getZ() * Math.sin((float) angleInRadian),
                  vector3d.getY(),
                  -vector3d.getX() * Math.sin((float) angleInRadian) + vector3d.getZ() * Math.cos((float) angleInRadian)
                );
            case Z:
                return new Vector3d(
                  vector3d.getX() * Math.cos((float) angleInRadian) - vector3d.getY() * Math.sin((float) angleInRadian),
                  vector3d.getX() * Math.sin((float) angleInRadian) + vector3d.getY() * Math.cos((float) angleInRadian),
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

    public static BlockPos invert(final BlockPos v) {
        return new BlockPos(-1 * v.getX(), -1 * v.getY(), -1 * v.getZ());
    }

    public static Vector3d minimizeTowardsZero(final Vector3d start, final Vector3d end) {
        return new Vector3d(
          MathUtil.minimizeTowardsZero(start.getX(), end.getX()),
          MathUtil.minimizeTowardsZero(start.getY(), end.getY()),
          MathUtil.minimizeTowardsZero(start.getZ(), end.getZ())
        );
    }

    public static Vector3d maximizeAwayFromZero(final Vector3d start, final Vector3d end) {
        return new Vector3d(
          MathUtil.maximizeAwayFromZero(start.getX(), end.getX()),
          MathUtil.maximizeAwayFromZero(start.getY(), end.getY()),
          MathUtil.maximizeAwayFromZero(start.getZ(), end.getZ())
        );
    }

    public static Vector3d absolute(final Vector3d vector3d) {
        return new Vector3d(
          vector3d.getX() < 0 ? -1 * vector3d.getX() : vector3d.getX(),
          vector3d.getY() < 0 ? -1 * vector3d.getY() : vector3d.getY(),
          vector3d.getZ() < 0 ? -1 * vector3d.getZ() : vector3d.getZ()
        );
    }

    public static Vector3d offsetRandomly(Vector3d source, Random random, float radius) {
        return new Vector3d(source.x + (random.nextFloat() - .5f) * 2 * radius, source.y + (random.nextFloat() - .5f) * 2 * radius,
          source.z + (random.nextFloat() - .5f) * 2 * radius);
    }

    public static Vector3d minimize(final Vector3d a, final Vector3d b)
    {
        return new Vector3d(
          Math.min(a.getX(), b.getX()),
          Math.min(a.getY(), b.getY()),
          Math.min(a.getZ(), b.getZ())
        );
    }

    public static Vector3d maximize(final Vector3d a, final Vector3d b)
    {
        return new Vector3d(
          Math.max(a.getX(), b.getX()),
          Math.max(a.getY(), b.getY()),
          Math.max(a.getZ(), b.getZ())
        );
    }

    public static Vector3d makePositive(final Vector3d inBlockOffset)
    {
        return new Vector3d(
          MathUtil.makePositive(inBlockOffset.getX()),
          MathUtil.makePositive(inBlockOffset.getY()),
          MathUtil.makePositive(inBlockOffset.getZ())
        );
    }
}
