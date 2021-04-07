package mod.chiselsandbits.client.model.baked.chiseled.face;

import net.minecraft.block.BlockState;
import net.minecraft.util.Direction;

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
                final boolean a = maxX == faceToExtendTo.minX - 2 && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean b = minX == faceToExtendTo.maxX + 2 && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean c = maxZ == faceToExtendTo.minZ - 2 && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;
                final boolean d = minZ == faceToExtendTo.maxZ + 2 && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;

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
                final boolean a = maxY == faceToExtendTo.minY - 2 && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean b = minY == faceToExtendTo.maxY + 2 && maxZ == faceToExtendTo.maxZ && minZ == faceToExtendTo.minZ;
                final boolean c = maxZ == faceToExtendTo.minZ - 2 && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;
                final boolean d = minZ == faceToExtendTo.maxZ + 2 && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;

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
                final boolean a = maxY == faceToExtendTo.minY - 2 && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;
                final boolean b = minY == faceToExtendTo.maxY + 2 && maxX == faceToExtendTo.maxX && minX == faceToExtendTo.minX;
                final boolean c = maxX == faceToExtendTo.minX - 2 && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;
                final boolean d = minX == faceToExtendTo.maxX + 2 && maxY == faceToExtendTo.maxY && minY == faceToExtendTo.minY;

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
