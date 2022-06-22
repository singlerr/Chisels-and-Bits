package mod.chiselsandbits.api.placement;

import com.mojang.math.Vector4f;
import mod.chiselsandbits.api.config.IClientConfiguration;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

/**
 * Represents a result of a placement attempt.
 */
public class PlacementResult
{

    /**
     * Creates a new successful placement result.
     * @return A new result indicating successful placement.
     */
    public static PlacementResult success()
    {
        return success(IClientConfiguration.getInstance().getSuccessfulPlacementColor().get());
    }

    /**
     * Creates a new successful placement result with specified color.
     * @return A new result indicating successful placement.
     */
    public static PlacementResult success(final Vector4f color)
    {
        return new PlacementResult(true, color, new TextComponent(""));
    }

    /**
     * Creates a new placement result which indicates failure.
     *
     * @param color The color of the wireframe to indicate the failure type.
     * @return A new result indicating a failure to place the pattern.
     */
    public static PlacementResult failure(final Vector4f color)
    {
        return failure(color, new TextComponent(""));
    }

    /**
     * Creates a new placement result which indicates failure.
     *
     * @param color The color of the wireframe to indicate the failure type.
     * @param message The message to show to the user.
     * @return A new result indicating a failure to place the pattern.
     */
    public static PlacementResult failure(final Vector4f color, final Component message)
    {
        return new PlacementResult(false, color, message);
    }

    private final boolean success;
    private final Vector4f color;
    private final Component failureMessage;

    private PlacementResult(final boolean success, final Vector4f color, final Component failureMessage)
    {
        this.success = success;
        this.color = color;
        this.failureMessage = failureMessage;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public Vector4f getColor()
    {
        return color;
    }

    public Component getFailureMessage()
    {
        return failureMessage;
    }
}
