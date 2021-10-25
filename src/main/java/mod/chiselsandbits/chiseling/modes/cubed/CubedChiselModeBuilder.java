package mod.chiselsandbits.chiseling.modes.cubed;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class CubedChiselModeBuilder {
    private int                       bitsPerSide;
    private boolean                   aligned;
    private IFormattableTextComponent displayName;
    private IFormattableTextComponent multiLineDisplayName;
    private ResourceLocation          iconName;

    public CubedChiselModeBuilder setBitsPerSide(final int bitsPerSide)
    {
        this.bitsPerSide = bitsPerSide;
        return this;
    }

    public CubedChiselModeBuilder setAligned(final boolean aligned)
    {
        this.aligned = aligned;
        return this;
    }

    public CubedChiselModeBuilder setDisplayName(final IFormattableTextComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public CubedChiselModeBuilder setMultiLineDisplayName(final IFormattableTextComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public CubedChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public CubedChiselMode createCubedChiselMode()
    {
        return new CubedChiselMode(bitsPerSide, aligned, displayName, multiLineDisplayName, iconName);
    }
}