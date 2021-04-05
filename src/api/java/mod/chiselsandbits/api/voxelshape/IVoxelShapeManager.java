package mod.chiselsandbits.api.voxelshape;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IAreaShapeIdentifier;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.Optional;

public interface IVoxelShapeManager
{

    static IVoxelShapeManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getVoxelShapeManager();
    }

    /**
     * Returns the shape of the multistate entries which are contained in the given area accessor.
     *
     * @param accessor The accessor to get the shape of.
     * @return The shape of the accessor.
     */
    VoxelShape get(final IAreaAccessor accessor);

    /**
     * Returns the shape that is referenced by a given area shape identifier.
     * If no shape with the given identifier is known then an empty optional is returned.
     *
     * @param identifier The identifier to get the voxel shape for.
     * @return The optional, optionally containing the voxel shape.
     */
    Optional<VoxelShape> getCached(final IAreaShapeIdentifier identifier);
}
