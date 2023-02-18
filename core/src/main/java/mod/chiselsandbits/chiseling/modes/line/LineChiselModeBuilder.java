package mod.chiselsandbits.chiseling.modes.line;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

public class LineChiselModeBuilder
{
    private int                       bitsPerSide;
    private MutableComponent displayName;
    private MutableComponent multiLineDisplayName;
    private ResourceLocation          iconName;

    public LineChiselModeBuilder setBitsPerSide(final int bitsPerSide)
    {
        this.bitsPerSide = bitsPerSide;
        return this;
    }

    public LineChiselModeBuilder setDisplayName(final MutableComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public LineChiselModeBuilder setMultiLineDisplayName(final MutableComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public LineChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public LineChiselMode createLineChiselMode()
    {
        return new LineChiselMode(bitsPerSide, displayName, multiLineDisplayName, iconName);
    }
}