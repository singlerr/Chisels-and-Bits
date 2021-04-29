package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.util.MathUtil;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AABBUtils
{

    private AABBUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: AABBUtils. This is a utility class");
    }

    public static boolean areBoxesNeighbors(@NotNull final AxisAlignedBB l, @NotNull final AxisAlignedBB r,@NotNull final Direction direction) {
        final double endOfL = getDirectionalValue(l, direction);
        final double startOfR = getDirectionalValue(r, direction.getOpposite());

        if (!MathUtil.almostEqual(endOfL, startOfR)) {
            return false;
        }

        final List<Direction> remainingDirectionsToCheck = DirectionUtils.getNotOnAxisOf(direction);
        final Map<Direction, Double> valuesOfL = remainingDirectionsToCheck.stream().collect(Collectors.toMap(Function.identity(), d -> getDirectionalValue(l, d)));
        final Map<Direction, Double> valuesOfR = remainingDirectionsToCheck.stream().collect(Collectors.toMap(Function.identity(), d -> getDirectionalValue(r, d)));

        return remainingDirectionsToCheck.stream().allMatch(d -> MathUtil.almostEqual(
          valuesOfL.get(d),
          valuesOfR.get(d)
        ));
    }

    public static double getDirectionalValue(@NotNull final AxisAlignedBB bb, @NotNull final Direction direction) {
        return direction.getAxisDirection() == Direction.AxisDirection.POSITIVE ?
                 bb.getMax(direction.getAxis()) :
                 bb.getMin(direction.getAxis());
    }
}
