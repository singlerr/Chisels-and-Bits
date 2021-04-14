package mod.chiselsandbits.voxelshape;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import mod.chiselsandbits.api.voxelshape.IVoxelShapeManager;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class VoxelShapeManager implements IVoxelShapeManager
{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final VoxelShapeManager INSTANCE = new VoxelShapeManager();

    private static final Cache<IAreaShapeIdentifier, VoxelShape> cache = CacheBuilder.newBuilder()
                                                                           .expireAfterAccess(1, TimeUnit.MINUTES)
                                                                           .build();

    private VoxelShapeManager()
    {
    }

    public static VoxelShapeManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Returns the shape of the multistate entries which are contained in the given area accessor.
     *
     * @param accessor The accessor to get the shape of.
     * @return The shape of the accessor.
     */
    @Override
    public VoxelShape get(final IAreaAccessor accessor)
    {
        try {
            return cache.get(accessor.createNewShapeIdentifier(),
              () -> {
                final VoxelShape calculatedShape = VoxelShapeCalculator.calculate(accessor);
                if (calculatedShape.isEmpty())
                    return VoxelShapes.fullCube();

                return calculatedShape;
            });
        }
        catch (ExecutionException e)
        {
            LOGGER.warn("Failed to calculate voxelshape.", e);
            return VoxelShapes.empty();
        }
    }

    /**
     * Returns the shape that is referenced by a given area shape identifier. If no shape with the given identifier is known then an empty optional is returned.
     *
     * @param identifier The identifier to get the voxel shape for.
     * @return The optional, optionally containing the voxel shape.
     */
    @Override
    public Optional<VoxelShape> getCached(final IAreaShapeIdentifier identifier)
    {
        return Optional.ofNullable(cache.getIfPresent(identifier));
    }
}
