package mod.chiselsandbits.chiseling;

import net.minecraft.util.text.IFormattableTextComponent;

public class CubedChiselModeBuilder {
    private int                       bitsPerSide;
    private boolean                   aligned = false;
    private IFormattableTextComponent displayName;

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

    public CubedChiselMode createCubedChiselMode()
    {
        return new CubedChiselMode(bitsPerSide, aligned, displayName);
    }
}