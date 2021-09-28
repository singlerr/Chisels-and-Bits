package mod.chiselsandbits.api.measuring;

import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum MeasuringType implements IToolModeGroup
{
    BIT(LocalStrings.TapeMeasureBit.getText(),
      new ResourceLocation(Constants.MOD_ID, "textures/icons/bit.png"),
      v -> Vector3d.atLowerCornerOf(new BlockPos(v.multiply(16, 16, 16))).multiply(1 / 16d, 1 / 16d, 1 / 16d),
      (from, to) -> {
          return new Vector3d(
            Math.min(from.x(), to.x()),
            Math.min(from.y(), to.y()),
            Math.min(from.z(), to.z())
          );
      },
      (from, to ) -> {
          return new Vector3d(
            Math.max(from.x(), to.x()),
            Math.max(from.y(), to.y()),
            Math.max(from.z(), to.z())
          );
      }),
    BLOCK( LocalStrings.TapeMeasureBlock.getText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/block.png"), v -> Vector3d.atLowerCornerOf(new BlockPos(v)),
      (from, to) -> {
          return new Vector3d(
            Math.min(from.x(), to.x()),
            Math.min(from.y(), to.y()),
            Math.min(from.z(), to.z())
          );
      },
      (from, to ) -> {
          return new Vector3d(
            Math.max(from.x(), to.x()),
            Math.max(from.y(), to.y()),
            Math.max(from.z(), to.z())
          );
      }),
    DISTANCE( LocalStrings.TapeMeasureDistance.getText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/line.png"), Function.identity(),
      (from, to) -> from,
      (from, to) -> to);

    private final ITextComponent displayName;
    private final ResourceLocation icon;
    private final Function<Vector3d, Vector3d>             positionAdapter;
    private final BiFunction<Vector3d, Vector3d, Vector3d> startPositionAdapter;
    private final BiFunction<Vector3d, Vector3d, Vector3d> endPositionAdapter;

    MeasuringType(
      final ITextComponent displayName,
      final ResourceLocation icon,
      final Function<Vector3d, Vector3d> positionAdapter,
      final BiFunction<Vector3d, Vector3d, Vector3d> startPositionAdapter,
      final BiFunction<Vector3d, Vector3d, Vector3d> endPositionAdapter) {
        this.displayName = displayName;
        this.icon = icon;
        this.positionAdapter = positionAdapter;
        this.startPositionAdapter = startPositionAdapter;
        this.endPositionAdapter = endPositionAdapter;
    }

    @Override
    public @NotNull ResourceLocation getIcon()
    {
        return icon;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return displayName;
    }

    @NotNull
    public Vector3d adaptPosition(@NotNull final Vector3d position) {
        return this.positionAdapter.apply(position);
    }

    @NotNull
    public Vector3d adaptStartPosition(@NotNull final Vector3d startPosition, @NotNull final Vector3d endPosition) {
        return this.startPositionAdapter.apply(startPosition, endPosition);
    }

    @NotNull
    public Vector3d adaptEndPosition(@NotNull final Vector3d startPosition, @NotNull final Vector3d endPosition) {
        return this.endPositionAdapter.apply(startPosition, endPosition);
    }
}
