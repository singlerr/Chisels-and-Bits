package mod.chiselsandbits.api.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;

public class BlockPosUtils
{

    private BlockPosUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BlockPosUtils. This is a utility class");
    }

    public static BlockPos fromCeil(final Vector3d vector3d) {
        return new BlockPos(
          (int) Math.ceil(vector3d.getX()),
          (int) Math.ceil(vector3d.getY()),
          (int) Math.ceil(vector3d.getZ())
        );
    }
}
