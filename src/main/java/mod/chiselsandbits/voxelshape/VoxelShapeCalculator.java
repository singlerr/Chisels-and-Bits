package mod.chiselsandbits.voxelshape;

import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
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
      final Predicate<IStateEntryInfo> selectablePredicate) {
        return
            areaAccessor.stream()
              .filter(selectablePredicate)
              .map(stateEntryInfo -> new AxisAlignedBB(stateEntryInfo.getStartPoint().subtract(Vector3d.copy(areaAccessor.getAreaOrigin())), stateEntryInfo.getEndPoint().subtract(Vector3d.copy(areaAccessor.getAreaOrigin()))))
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
