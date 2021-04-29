package mod.chiselsandbits.voxelshape;

import mod.chiselsandbits.aabb.AABBManager;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessorWithVoxelShape;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;

import java.util.function.Predicate;

public class VoxelShapeCalculator
{
    public static VoxelShape calculate(
      final IAreaAccessor areaAccessor,
      final BlockPos offset,
      final Predicate<IStateEntryInfo> selectablePredicate) {
        if (areaAccessor instanceof IAreaAccessorWithVoxelShape)
            return ((IAreaAccessorWithVoxelShape) areaAccessor).provideShape(selectablePredicate, offset);

        return
            AABBManager.getInstance()
              .get(areaAccessor, selectablePredicate)
              .stream()
              .map(aabb -> aabb.offset(offset))
        .reduce(
          VoxelShapes.empty(),
          (voxelShape, axisAlignedBB) -> {
              final VoxelShape bbShape = VoxelShapes.create(axisAlignedBB);
              return VoxelShapes.combine(voxelShape, bbShape, IBooleanFunction.OR);
          },
          (voxelShape, voxelShape2) -> VoxelShapes.combine(voxelShape, voxelShape2, IBooleanFunction.OR)
        ).simplify();
    }
}
