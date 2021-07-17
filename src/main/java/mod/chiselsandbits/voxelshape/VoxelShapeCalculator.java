package mod.chiselsandbits.voxelshape;

import mod.chiselsandbits.aabb.AABBManager;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessorWithVoxelShape;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.function.Function;
import java.util.function.Predicate;

public class VoxelShapeCalculator
{
    public static VoxelShape calculate(
      final IAreaAccessor areaAccessor,
      final BlockPos offset,
      final Function<IAreaAccessor, Predicate<IStateEntryInfo>> selectablePredicateBuilder,
      final boolean simplify) {
        if (areaAccessor instanceof IAreaAccessorWithVoxelShape)
            return ((IAreaAccessorWithVoxelShape) areaAccessor).provideShape(selectablePredicateBuilder, offset, simplify);

        final VoxelShape shape =
            AABBManager.getInstance()
              .get(areaAccessor, selectablePredicateBuilder)
              .stream()
              .map(aabb -> aabb.offset(offset))
        .reduce(
          VoxelShapes.empty(),
          (voxelShape, axisAlignedBB) -> {
              final VoxelShape bbShape = VoxelShapes.create(axisAlignedBB);
              return VoxelShapes.combine(voxelShape, bbShape, IBooleanFunction.OR);
          },
          (voxelShape, voxelShape2) -> VoxelShapes.combine(voxelShape, voxelShape2, IBooleanFunction.OR)
        );

        return simplify ? shape.simplify() : shape;
    }
}
