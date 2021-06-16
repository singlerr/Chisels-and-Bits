package mod.chiselsandbits.api.item.withmode;

import mod.chiselsandbits.api.util.IWithDisplayName;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;

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
    @NotNull
    ResourceLocation getIcon();

    /**
     * Indicates if the name of the mode should be rendered.
     *
     * @return {@code true} when the name should be rendered.
     */
    default boolean shouldRenderName() {
        return true;
    }

    /**
     * Indicates if this mode should render his name in the menu.
     *
     * @return {@code true} when then name should be rendered in the menu.
     */
    default boolean shouldRenderDisplayNameInMenu() {
        return true;
    }

    /**
     * The color used to render the tool mode.
     * Applied to both the name as well as the icon.
     *
     * @return The color in a 3d double vector as RGB.
     */
    @NotNull
    default Vector3d getColorVector() {
        return new Vector3d(1d, 1d,1d);
    }
}
