package mod.chiselsandbits.chiseling.modes.connected.material;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;

public class ConnectedMaterialChiselingModeBuilder
{
    private int                       depth;
    private IFormattableTextComponent displayName;
    private IFormattableTextComponent multiLineDisplayName;
    private ResourceLocation          iconName;

    public ConnectedMaterialChiselingModeBuilder setDepth(final int depth)
    {
        this.depth = depth;
        return this;
    }

    public ConnectedMaterialChiselingModeBuilder setDisplayName(final IFormattableTextComponent displayName)
    {
        this.displayName = displayName;
        return this;
    }

    public ConnectedMaterialChiselingModeBuilder setMultiLineDisplayName(final IFormattableTextComponent multiLineDisplayName)
    {
        this.multiLineDisplayName = multiLineDisplayName;
        return this;
    }

    public ConnectedMaterialChiselingModeBuilder setIconName(final ResourceLocation iconName)
    {
        this.iconName = iconName;
        return this;
    }

    public ConnectedMaterialChiselingMode createConnectedMaterialChiselingMode()
    {
        return new ConnectedMaterialChiselingMode(depth, displayName, multiLineDisplayName, iconName);
    }
}