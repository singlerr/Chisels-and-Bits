package mod.chiselsandbits.chiseling.modes.plane;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class PlaneChiselModeBuilder
{
    private int                       depth;
    private IFormattableTextComponent displayName;
    private IFormattableTextComponent multiLineDisplayName;
    private ResourceLocation          iconName;
    private boolean                   filterOnTarget = false;

    public PlaneChiselModeBuilder setDepth(final int depth)
    {
        this.depth = depth;
        return this;
    }

    public PlaneChiselModeBuilder setDisplayName(final IFormattableTextComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public PlaneChiselModeBuilder setMultiLineDisplayName(final IFormattableTextComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public PlaneChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public PlaneChiselModeBuilder withFilterOnTarget()
    {
        this.filterOnTarget = true;
        return this;
    }

    public PlaneChiselMode createPlaneChiselMode()
    {
        return new PlaneChiselMode(depth, displayName, multiLineDisplayName, iconName, filterOnTarget);
    }
}