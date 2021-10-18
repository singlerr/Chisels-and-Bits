package mod.chiselsandbits.chiseling.modes.plane;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

public class PlaneChiselModeBuilder
{
    private int                       depth;
    private MutableComponent displayName;
    private MutableComponent multiLineDisplayName;
    private ResourceLocation          iconName;
    private boolean                   filterOnTarget = false;

    public PlaneChiselModeBuilder setDepth(final int depth)
    {
        this.depth = depth;
        return this;
    }

    public PlaneChiselModeBuilder setDisplayName(final MutableComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public PlaneChiselModeBuilder setMultiLineDisplayName(final MutableComponent multiLineDisplayName)
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