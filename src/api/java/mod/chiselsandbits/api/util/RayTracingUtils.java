package mod.chiselsandbits.api.util;

import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.ForgeMod;

/**
 * Utility class for dealing with raytracing.
 */
public class RayTracingUtils
{

    private RayTracingUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: RayTracingUtils. This is a utility class");
    }

    /**
     * Performs a raytrace result within the given reach distance of the given player.
     *
     * @param playerEntity The player to run a raytrace for.
     * @return The raytrace result.
     */
    public static RayTraceResult rayTracePlayer(final PlayerEntity playerEntity) {
        final ModifiableAttributeInstance reachAttribute = playerEntity.getAttribute(ForgeMod.REACH_DISTANCE.get());
        if (reachAttribute == null)
        {
            return playerEntity.pick(5d, 0.5f, true);
        }

        final double reachAttributeValue = reachAttribute.getValue();
        final double reachDistance = playerEntity.isCreative() ? reachAttributeValue : reachAttributeValue - 0.5D;

        return playerEntity.pick(reachDistance, 0.5f, true);
    }
}
