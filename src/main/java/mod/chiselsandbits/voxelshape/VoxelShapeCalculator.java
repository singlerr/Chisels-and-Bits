package mod.chiselsandbits.voxelshape;

import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public class VoxelShapeCalculator
{
    public static VoxelShape calculate(final IAreaAccessor areaAccessor) {
        return
            areaAccessor.stream()
              .map(stateEntryInfo -> new AxisAlignedBB(stateEntryInfo.getStartPoint(), stateEntryInfo.getEndPoint()))
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
