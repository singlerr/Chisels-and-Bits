package mod.chiselsandbits.chiseling.modes.draw;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

public class DrawChiselModeBuilder
{
    private MutableComponent displayName;
    private MutableComponent multiLineDisplayName;
    private ResourceLocation          iconName;

    public DrawChiselModeBuilder setDisplayName(final MutableComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public DrawChiselModeBuilder setMultiLineDisplayName(final MutableComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public DrawChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public DrawChiselMode createDrawChiselMode()
    {
        return new DrawChiselMode(displayName, multiLineDisplayName, iconName);
    }
}