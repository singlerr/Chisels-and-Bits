package mod.chiselsandbits.chiseling.modes.line;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class LinedChiselModeBuilder {
    private int                       bitsPerSide;
    private IFormattableTextComponent displayName;
    private IFormattableTextComponent multiLineDisplayName;
    private ResourceLocation          iconName;

    public LinedChiselModeBuilder setBitsPerSide(final int bitsPerSide)
    {
        this.bitsPerSide = bitsPerSide;
        return this;
    }

    public LinedChiselModeBuilder setDisplayName(final IFormattableTextComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public LinedChiselModeBuilder setMultiLineDisplayName(final IFormattableTextComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public LinedChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public LinedChiselMode createLinedChiselMode()
    {
        return new LinedChiselMode(bitsPerSide, displayName, multiLineDisplayName, iconName);
    }
}