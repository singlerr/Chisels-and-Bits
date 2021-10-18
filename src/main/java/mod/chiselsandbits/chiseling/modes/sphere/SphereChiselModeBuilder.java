package mod.chiselsandbits.chiseling.modes.sphere;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class SphereChiselModeBuilder {
    private int                       diameter;
    private IFormattableTextComponent displayName;
    private IFormattableTextComponent multiLineDisplayName;
    private ResourceLocation          iconName;

    public SphereChiselModeBuilder setDiameter(final int diameter)
    {
        this.diameter = diameter;
        return this;
    }

    public SphereChiselModeBuilder setDisplayName(final IFormattableTextComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public SphereChiselModeBuilder setMultiLineDisplayName(final IFormattableTextComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public SphereChiselModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public SphereChiselMode createSphereChiselMode()
    {
        return new SphereChiselMode(diameter, displayName, multiLineDisplayName, iconName);
    }
}