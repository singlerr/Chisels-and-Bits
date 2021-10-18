package mod.chiselsandbits.chiseling.modes.connected.plane;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class ConnectedPlaneChiselingModeBuilder {
    private int                       depth;
    private IFormattableTextComponent displayName;
    private IFormattableTextComponent multiLineDisplayName;
    private ResourceLocation          iconName;

    public ConnectedPlaneChiselingModeBuilder setDepth(final int depth)
    {
        this.depth = depth;
        return this;
    }

    public ConnectedPlaneChiselingModeBuilder setDisplayName(final IFormattableTextComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public ConnectedPlaneChiselingModeBuilder setMultiLineDisplayName(final IFormattableTextComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public ConnectedPlaneChiselingModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public ConnectedPlaneChiselingMode createConnectedPlaneChiselingMode()
    {
        return new ConnectedPlaneChiselingMode(depth, displayName, multiLineDisplayName, iconName);
    }
}