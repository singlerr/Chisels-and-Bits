package mod.chiselsandbits.api.measuring;

import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public enum MeasuringType implements IToolModeGroup
{
    BIT(LocalStrings.TapeMeasureBit.getLocalText(),
      new ResourceLocation(Constants.MOD_ID, "textures/icons/bit.png"),
      v -> Vec3.atLowerCornerOf(new BlockPos(v.multiply(16, 16, 16))).multiply(1 / 16d, 1 / 16d, 1 / 16d),
      (from, to) -> {
          return new Vec3(
            Math.min(from.x(), to.x()),
            Math.min(from.y(), to.y()),
            Math.min(from.z(), to.z())
          );
      },
      (from, to ) -> {
          return new Vec3(
            Math.max(from.x(), to.x()),
            Math.max(from.y(), to.y()),
            Math.max(from.z(), to.z())
          );
      }),
    BLOCK( LocalStrings.TapeMeasureBlock.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/block.png"), v -> Vec3.atLowerCornerOf(new BlockPos(v)),
      (from, to) -> {
          return new Vec3(
            Math.min(from.x(), to.x()),
            Math.min(from.y(), to.y()),
            Math.min(from.z(), to.z())
          );
      },
      (from, to ) -> {
          return new Vec3(
            Math.max(from.x(), to.x()),
            Math.max(from.y(), to.y()),
            Math.max(from.z(), to.z())
          );
      }),
    DISTANCE( LocalStrings.TapeMeasureDistance.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/line.png"), Function.identity(),
      (from, to) -> from,
      (from, to) -> to);

    private final Component displayName;
    private final ResourceLocation icon;
    private final Function<Vec3, Vec3>             positionAdapter;
    private final BiFunction<Vec3, Vec3, Vec3> startPositionAdapter;
    private final BiFunction<Vec3, Vec3, Vec3> endPositionAdapter;

    MeasuringType(
      final Component displayName,
      final ResourceLocation icon,
      final Function<Vec3, Vec3> positionAdapter,
      final BiFunction<Vec3, Vec3, Vec3> startPositionAdapter,
      final BiFunction<Vec3, Vec3, Vec3> endPositionAdapter) {
        this.displayName = displayName;
        this.icon = icon;
        this.positionAdapter = positionAdapter;
        this.startPositionAdapter = startPositionAdapter;
        this.endPositionAdapter = endPositionAdapter;
    }

    @Override
    public ResourceLocation getIcon()
    {
        return icon;
    }

    @Override
    public Component getDisplayName()
    {
        return displayName;
    }

    @NotNull
    public Vec3 adaptPosition(@NotNull final Vec3 position) {
        return this.positionAdapter.apply(position);
    }

    @NotNull
    public Vec3 adaptStartPosition(@NotNull final Vec3 startPosition, @NotNull final Vec3 endPosition) {
        return this.startPositionAdapter.apply(startPosition, endPosition);
    }

    @NotNull
    public Vec3 adaptEndPosition(@NotNull final Vec3 startPosition, @NotNull final Vec3 endPosition) {
        return this.endPositionAdapter.apply(startPosition, endPosition);
    }
}
