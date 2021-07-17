package mod.chiselsandbits.client.model.baked.face;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;

public class FaceRegion
{
    private final Direction  face;
    private final BlockState blockState;
    private final boolean    isEdge;
    private double minX;
    private double minY;
    private double minZ;
    private double maxX;
    private double maxY;
    private double maxZ;

    public static FaceRegion createFrom3DObjectWithFacing(
      final Vector3d start,
      final Vector3d end,
      final Direction facing,
      final BlockState blockState,
      final boolean isEdge
    ) {
        return new FaceRegion(
          facing,
          blockState,
          isEdge,
          facing.getXOffset() == 0 ? start.getX() : (facing.getXOffset() == 1 ? Math.max(start.getX(), end.getX()) : Math.min(start.getX(), end.getX())),
          facing.getYOffset() == 0 ? start.getY() : (facing.getYOffset() == 1 ? Math.max(start.getY(), end.getY()) : Math.min(start.getY(), end.getY())),
          facing.getZOffset() == 0 ? start.getZ() : (facing.getZOffset() == 1 ? Math.max(start.getZ(), end.getZ()) : Math.min(start.getZ(), end.getZ())),
          facing.getXOffset() == 0 ? end.getX() : (facing.getXOffset() == 1 ? Math.max(start.getX(), end.getX()) : Math.min(start.getX(), end.getX())),
          facing.getYOffset() == 0 ? end.getY() : (facing.getYOffset() == 1 ? Math.max(start.getY(), end.getY()) : Math.min(start.getY(), end.getY())),
          facing.getZOffset() == 0 ? end.getZ() : (facing.getZOffset() == 1 ? Math.max(start.getZ(), end.getZ()) : Math.min(start.getZ(), end.getZ()))
        );
    }

    public FaceRegion(
      final Direction facingDirection,
      final double centerX,
      final double centerY,
      final double centerZ,
      final BlockState blockState,
      final boolean isEdgeFace)
    {
        face = facingDirection;
        this.blockState = blockState;
        isEdge = isEdgeFace;
        minX = centerX;
        minY = centerY;
        minZ = centerZ;
        maxX = centerX;
        maxY = centerY;
        maxZ = centerZ;
    }

    public FaceRegion(
      final Direction face,
      final BlockState blockState,
      final boolean isEdge,
      final double minX,
      final double minY,
      final double minZ,
      final double maxX,
      final double maxY,
      final double maxZ)
    {
        this.face = face;
        this.blockState = blockState;
        this.isEdge = isEdge;
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
    }

    public BlockState getBlockState()
    {
        return blockState;
    }

    public double getMinX()
    {
        return minX;
    }

    public double getMinY()
    {
        return minY;
    }

    public double getMinZ()
    {
        return minZ;
    }

    public double getMaxX()
    {
        return maxX;
    }

    public double getMaxY()
    {
        return maxY;
    }

    public double getMaxZ()
    {
        return maxZ;
    }

    public boolean isEdge()
    {
        return isEdge;
    }

    public Direction getFace()
    {
        return face;
    }

    public boolean extend(
      final FaceRegion faceToExtendTo
    ) {
        if (faceToExtendTo.blockState != blockState)
        {
            return false;
        }

        switch (face)
        {
            case DOWN:
            case UP:
            {
                final boolean a = maxX == faceToExtendTo.minX && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean b = minX == faceToExtendTo.maxX && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean c = maxZ == faceToExtendTo.minZ && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;
                final boolean d = minZ == faceToExtendTo.maxZ && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;

                if (a || b || c || d)
                {
                    minX = Math.min(faceToExtendTo.minX, minX);
                    minY = Math.min(faceToExtendTo.minY, minY);
                    minZ = Math.min(faceToExtendTo.minZ, minZ);
                    maxX = Math.max(faceToExtendTo.maxX, maxX);
                    maxY = Math.max(faceToExtendTo.maxY, maxY);
                    maxZ = Math.max(faceToExtendTo.maxZ, maxZ);
                    return true;
                }

                return false;
            }

            case WEST:
            case EAST:
            {
                final boolean a = maxY == faceToExtendTo.minY && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean b = minY == faceToExtendTo.maxY && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean c = maxZ == faceToExtendTo.minZ && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;
                final boolean d = minZ == faceToExtendTo.maxZ && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;

                if (a || b || c || d)
                {
                    minX = Math.min(faceToExtendTo.minX, minX);
                    minY = Math.min(faceToExtendTo.minY, minY);
                    minZ = Math.min(faceToExtendTo.minZ, minZ);
                    maxX = Math.max(faceToExtendTo.maxX, maxX);
                    maxY = Math.max(faceToExtendTo.maxY, maxY);
                    maxZ = Math.max(faceToExtendTo.maxZ, maxZ);
                    return true;
                }

                return false;
            }

            case NORTH:
            case SOUTH:
            {
                final boolean a = maxY == faceToExtendTo.minY && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;
                final boolean b = minY == faceToExtendTo.maxY && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;
                final boolean c = maxX == faceToExtendTo.minX && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;
                final boolean d = minX == faceToExtendTo.maxX && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;

                if (a || b || c || d)
                {
                    minX = Math.min(faceToExtendTo.minX, minX);
                    minY = Math.min(faceToExtendTo.minY, minY);
                    minZ = Math.min(faceToExtendTo.minZ, minZ);
                    maxX = Math.max(faceToExtendTo.maxX, maxX);
                    maxY = Math.max(faceToExtendTo.maxY, maxY);
                    maxZ = Math.max(faceToExtendTo.maxZ, maxZ);
                    return true;
                }

                return false;
            }

            default:
                return false;
        }
    }
}
