package mod.chiselsandbits.api.item.withmode;

import mod.chiselsandbits.api.item.withmode.group.IToolModeGroup;
import mod.chiselsandbits.api.util.IWithDisplayName;
import net.minecraft.util.math.vector.Vector3d;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * A mode of a given tool.
 */
public interface IToolMode extends IWithDisplayName, IRenderableMode
{

    /**
     * An optional which indicates the group this tool mode
     * is part of.
     *
     * @return The optional tool mode group.
     */
    @NotNull
    Optional<IToolModeGroup> getGroup();

    /**
     * Indicates if this mode should render his name in the menu.
     *
     * @return {@code true} when then name should be rendered in the menu.
     */
    @NotNull
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
