package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.util.IWorldObject;
import net.minecraft.util.math.vector.Vector3d;

public class WorldObjectUtils
{

    private WorldObjectUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: WorldObjectUtils. This is a utility class");
    }

    public static boolean isInsideOrCoveredBy(final IWorldObject a, final IWorldObject b) {
        return isAInsideOrCoveredByB(a, b) || isAInsideOrCoveredByB(b, a);
    }

    public static boolean isAInsideOrCoveredByB(final IWorldObject a, final IWorldObject b) {
        return isAInsideB(a, b) || isACoveredByB(a, b);
    }

    public static boolean isAInsideB(final IWorldObject a, final IWorldObject b) {
        final Vector3d aStart = a.getInWorldStartPoint();
        final Vector3d aEnd = a.getInWorldEndPoint();

        final Vector3d bStart = b.getInWorldStartPoint();
        final Vector3d bEnd = b.getInWorldEndPoint();

        return bStart.getX() <= aStart.getX() && aEnd.getX() <= bEnd.getX() &&
                 bStart.getY() <= aStart.getY() && aEnd.getY() <= bEnd.getY() &&
                 bStart.getZ() <= bStart.getZ() && aEnd.getZ() <= bEnd.getZ();
    }

    public static boolean isACoveredByB(final IWorldObject a, final IWorldObject b) {
        final Vector3d bLowerLeftFront = b.getInWorldStartPoint();
        final Vector3d bUpperRightBack = b.getInWorldEndPoint();

        final Vector3d bLowerLeftBack = new Vector3d(bLowerLeftFront.getX(), bLowerLeftFront.getY(), bUpperRightBack.getZ());
        final Vector3d bLowerRightFront = new Vector3d(bUpperRightBack.getX(), bLowerLeftFront.getY(), bLowerLeftFront.getZ());
        final Vector3d bUpperLeftFront = new Vector3d(bLowerLeftFront.getX(), bUpperRightBack.getY(), bLowerLeftFront.getZ());
        final Vector3d bLowerRightBack = new Vector3d(bUpperRightBack.getX(), bLowerLeftFront.getY(), bUpperRightBack.getZ());
        final Vector3d bUpperLeftBack = new Vector3d(bLowerLeftFront.getX(), bUpperRightBack.getY(), bUpperRightBack.getZ());
        final Vector3d bUpperRightFront = new Vector3d(bUpperRightBack.getX(), bUpperRightBack.getY(), bLowerLeftFront.getZ());

        return isPointInside(a, bLowerLeftFront) ||
            isPointInside(a, bUpperRightBack) ||
            isPointInside(a, bLowerLeftBack) ||
            isPointInside(a, bLowerRightFront) ||
            isPointInside(a, bUpperLeftFront) ||
            isPointInside(a, bLowerRightBack) ||
            isPointInside(a, bUpperLeftBack) ||
            isPointInside(a, bUpperRightFront);
    }

    public static boolean isPointInside(final IWorldObject a, final Vector3d point) {
        final Vector3d aStart = a.getInWorldStartPoint();
        final Vector3d aEnd = a.getInWorldEndPoint();

        return aStart.getX() <= point.getX() && point.getX() <= aEnd.getX() &&
                 aStart.getY() <= point.getY() && point.getY() <= aEnd.getY() &&
                 aStart.getZ() <= point.getZ() && point.getZ() <= aEnd.getZ();
    }
}
