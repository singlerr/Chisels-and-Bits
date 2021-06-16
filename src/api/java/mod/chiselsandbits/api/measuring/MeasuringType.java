package mod.chiselsandbits.api.measuring;

import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public enum MeasuringType implements IToolModeGroup
{
    BIT( LocalStrings.TapeMeasureBit.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/bit.png"), v -> Vector3d.copy(new BlockPos(v.mul(16, 16, 16))).mul(1/16d, 1/16d, 1/16d)),
    BLOCK( LocalStrings.TapeMeasureBlock.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/block.png"), v -> Vector3d.copy(new BlockPos(v))),
    DISTANCE( LocalStrings.TapeMeasureDistance.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/line.png"), Function.identity());

    private final ITextComponent displayName;
    private final ResourceLocation icon;
    private final Function<Vector3d, Vector3d> positionAdapter;

    MeasuringType(
      final ITextComponent displayName,
      final ResourceLocation icon,
      final Function<Vector3d, Vector3d> positionAdapter) {
        this.displayName = displayName;
        this.icon = icon;
        this.positionAdapter = positionAdapter;
    }

    @Override
    public ResourceLocation getIcon()
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
}
