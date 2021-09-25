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
        return isAInsideB(a, b) || isACoveringB(a, b);
    }

    public static boolean isAInsideB(final IWorldObject a, final IWorldObject b) {
        final Vector3d aStart = a.getInWorldStartPoint();
        final Vector3d aEnd = a.getInWorldEndPoint();

        final Vector3d bStart = b.getInWorldStartPoint();
        final Vector3d bEnd = b.getInWorldEndPoint();

        return bStart.x() <= aStart.x() && aEnd.x() <= bEnd.x() &&
                 bStart.y() <= aStart.y() && aEnd.y() <= bEnd.y() &&
                 bStart.z() <= aStart.z() && aEnd.z() <= bEnd.z();
    }

    public static boolean isACoveringB(final IWorldObject a, final IWorldObject b) {
        final Vector3d bLowerLeftFront = b.getInWorldStartPoint();
        final Vector3d bUpperRightBack = b.getInWorldEndPoint();

        final Vector3d bLowerLeftBack = new Vector3d(bLowerLeftFront.x(), bLowerLeftFront.y(), bUpperRightBack.z());
        final Vector3d bUpperLeftFront = new Vector3d(bLowerLeftFront.x(), bUpperRightBack.y(), bLowerLeftFront.z());
        final Vector3d bLowerRightFront = new Vector3d(bUpperRightBack.x(), bLowerLeftFront.y(), bLowerLeftFront.z());
        final Vector3d bUpperLeftBack = new Vector3d(bLowerLeftFront.x(), bUpperRightBack.y(), bUpperRightBack.z());
        final Vector3d bLowerRightBack = new Vector3d(bUpperRightBack.x(), bLowerLeftFront.y(), bUpperRightBack.z());
        final Vector3d bUpperRightFront = new Vector3d(bUpperRightBack.x(), bUpperRightBack.y(), bLowerLeftFront.z());

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

        return aStart.x() <= point.x() && point.x() <= aEnd.x() &&
                 aStart.y() <= point.y() && point.y() <= aEnd.y() &&
                 aStart.z() <= point.z() && point.z() <= aEnd.z();
    }
}
