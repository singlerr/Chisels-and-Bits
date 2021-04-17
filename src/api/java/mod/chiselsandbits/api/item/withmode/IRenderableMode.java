package mod.chiselsandbits.api.item.withmode;

import mod.chiselsandbits.api.util.IWithDisplayName;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

/**
 * Represents a tool mode which can be rendered.
 */
public interface IRenderableMode extends IWithDisplayName
{

    /**
     * The icon to render for the mode.
     *
     * @return The icon for the mode.
     */
    ResourceLocation getIcon();

    /**
     * Indicates if the name of the mode should be rendered.
     *
     * @return {@code true} when the name should be rendered.
     */
    default boolean shouldRenderName() {
        return true;
    }
}
