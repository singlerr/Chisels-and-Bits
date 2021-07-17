package mod.chiselsandbits.api.voxelshape;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.identifier.IAreaShapeIdentifier;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A manager for dealing with voxelshapes related to chiseled blocks and areas.
 */
public interface IVoxelShapeManager
{

    static IVoxelShapeManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getVoxelShapeManager();
    }

    /**
     * Returns the shape of the multistate entries which are contained in the given area accessor.
     * Excludes all air states.
     *
     * @param accessor The accessor to get the shape of.
     * @return The shape of the accessor.
     */
    default VoxelShape get(
      final IAreaAccessor accessor) {
        return get(accessor, BlockPos.ZERO);
    }

    /**
     * Returns the shape of the multistate entries which are contained in the given area accessor.
     * Excludes all air states.
     *
     * @param accessor The accessor to get the shape of.
     * @param offset The offset to apply to the voxelshape during calculation or cache lookup.
     * @return The shape of the accessor.
     */
    @SuppressWarnings("deprecation")
    default VoxelShape get(
      final IAreaAccessor accessor,
      final BlockPos offset) {
        return get(accessor, offset, ignored -> iStateEntryInfo -> !iStateEntryInfo.getState().isAir(new SingleBlockBlockReader(iStateEntryInfo.getState()), BlockPos.ZERO), true);
    }

    /**
     * Returns the shape of the multistate entries which are contained in the given area accessor.
     *
     * @param accessor The accessor to get the shape of.
     * @param selectablePredicateBuilder The predicate that determines what state entries to include.
     * @return The shape of the accessor.
     */
    default VoxelShape get(final IAreaAccessor accessor,
      final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder) {
        return get(
          accessor,
          BlockPos.ZERO,
          selectablePredicateBuilder,
          true);
    }

    /**
     * Returns the shape of the multistate entries which are contained in the given area accessor.
     *
     * @param accessor The accessor to get the shape of.
     * @param selectablePredicateBuilder The predicate that determines what state entries to include.
     * @param simplify Indicates if the returned voxelshape should be simplified. Generally good for performance, but might cause some shapes to not work properly.
     * @return The shape of the accessor.
     */
    default VoxelShape get(final IAreaAccessor accessor,
      final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder,
      final boolean simplify) {
        return get(
          accessor,
          BlockPos.ZERO,
          selectablePredicateBuilder,
          simplify);
    }

    /**
     * Returns the shape of the multistate entries which are contained in the given area accessor.
     *
     * @param accessor The accessor to get the shape of.
     * @param offset The offset to apply to the voxelshape during calculation or cache lookup.
     * @param selectablePredicateBuilder The predicate that determines what state entries to include.
     * @param simplify Indicates if the returned voxelshape should be simplified. Generally good for performance, but might cause some shapes to not work properly.
     * @return The shape of the accessor.
     */
    VoxelShape get(
      final IAreaAccessor accessor,
      final BlockPos offset,
      final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder,
      final boolean simplify);

    /**
     * Returns the shape that is referenced by a given area shape identifier.
     * If no shape with the given identifier is known then an empty optional is returned.
     *
     * @param identifier The identifier to get the voxel shape for.
     * @param offset The offset to apply to the voxelshape during cache lookup.
     * @return The optional, optionally containing the voxel shape.
     */
    @SuppressWarnings("deprecation")
    default Optional<VoxelShape> getCached(
      final IAreaShapeIdentifier identifier,
      final BlockPos offset) {
        return getCached(identifier, offset, iStateEntryInfo -> !iStateEntryInfo.getState().isAir(new SingleBlockBlockReader(iStateEntryInfo.getState()), BlockPos.ZERO));
    };

    /**
     * Returns the shape that is referenced by a given area shape identifier.
     * If no shape with the given identifier is known then an empty optional is returned.
     *
     * @param identifier The identifier to get the voxel shape for.
     * @param offset The offset to apply to the voxelshape during cache lookup.
     * @param selectablePredicate The predicate that determines what state entries to include.
     * @return The optional, optionally containing the voxel shape.
     */
    default Optional<VoxelShape> getCached(
      final IAreaShapeIdentifier identifier,
      final BlockPos offset,
      final Predicate<IStateEntryInfo> selectablePredicate) {
        return getCached(
          identifier, offset, selectablePredicate, true
        );
    }

    /**
     * Returns the shape that is referenced by a given area shape identifier.
     * If no shape with the given identifier is known then an empty optional is returned.
     *
     * @param identifier The identifier to get the voxel shape for.
     * @param offset The offset to apply to the voxelshape during cache lookup.
     * @param selectablePredicate The predicate that determines what state entries to include.
     * @param simplify Indicates if the returned voxelshape should be simplified. Generally good for performance, but might cause some shapes to not work properly.
     * @return The optional, optionally containing the voxel shape.
     */
    Optional<VoxelShape> getCached(
      final IAreaShapeIdentifier identifier,
      final BlockPos offset,
      final Predicate<IStateEntryInfo> selectablePredicate,
      boolean simplify);
}
