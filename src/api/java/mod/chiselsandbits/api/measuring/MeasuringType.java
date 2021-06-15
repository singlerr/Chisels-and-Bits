package mod.chiselsandbits.api.measuring;

import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public enum MeasuringType implements IToolModeGroup
{
    BIT( LocalStrings.TapeMeasureBit.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/bit.png") ),
    BLOCK( LocalStrings.TapeMeasureBlock.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/block.png") ),
    DISTANCE( LocalStrings.TapeMeasureDistance.getLocalText(), new ResourceLocation(Constants.MOD_ID,"textures/icons/line.png") );

    private final ITextComponent displayName;
    private final ResourceLocation icon;

    MeasuringType(final ITextComponent displayName, final ResourceLocation icon) {
        this.displayName = displayName;
        this.icon = icon;
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
}
