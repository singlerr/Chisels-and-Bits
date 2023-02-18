package mod.chiselsandbits.chiseling.modes.depthmap;

import mod.chiselsandbits.api.map.bit.IDepthMapFilter;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.Collection;

public class DepthMapBasedChiselModeBuilder
{
    private MutableComponent displayName;
    private MutableComponent multiLineDisplayName;
    private ResourceLocation          iconName;
    private final Collection<IDepthMapFilter> filter = new ArrayList<>();

    public DepthMapBasedChiselModeBuilder setDisplayName(final MutableComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public DepthMapBasedChiselModeBuilder setMultiLineDisplayName(final MutableComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public DepthMapBasedChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public DepthMapBasedChiselModeBuilder withFilter(final IDepthMapFilter filter)
    {
        this.filter.add(filter);
        return this;
    }

    public DepthMapBasedChiselMode createDepthMapChiselMode()
    {
        return new DepthMapBasedChiselMode(filter.toArray(IDepthMapFilter[]::new), displayName, multiLineDisplayName, iconName);
    }
}