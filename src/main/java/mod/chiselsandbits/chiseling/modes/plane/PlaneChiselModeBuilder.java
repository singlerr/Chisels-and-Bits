package mod.chiselsandbits.chiseling.modes.plane;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class PlaneChiselModeBuilder
{
    private int                       depth;
    private IFormattableTextComponent displayName;
    private ResourceLocation iconName;
    private boolean filterOnTarget = false;

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

    public PlaneChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public PlaneChiselModeBuilder withFilterOnTarget() {
        this.filterOnTarget = true;
        return this;
    }

    public PlaneChiseledMode createPlaneChiselMode()
    {
        return new PlaneChiseledMode(depth, displayName, iconName, filterOnTarget);
    }
}