package mod.chiselsandbits.api.pattern.placement;

import com.mojang.math.Vector3d;
import com.mojang.math.Vector4f;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.phys.Vec3;

/**
 * Represents a result of a placement attempt.
 */
public class PlacementResult
{

    private static final Vector4f ZERO = new Vector4f(0f, 0f, 0f, 0f);

    /**
     * Creates a new successful placement result.
     * @return A new result indicating successful placement.
     */
    public static PlacementResult success(){
        return new PlacementResult(true, ZERO, new TextComponent(""));
    }

    /**
     * Creates a new placement result which indicates failure.
     *
     * @param color The color of the wireframe to indicate the failure type.
     * @param message The message to show to the user.
     * @return A new result indicating a failure to place the pattern.
     */
    public static PlacementResult failure(final Vector4f color, final Component message) {
        return new PlacementResult(false, color, message);
    }

    private final boolean success;
    private final Vector4f failureColor;
    private final Component failureMessage;

    private PlacementResult(final boolean success, final Vector4f failureColor, final Component failureMessage) {
        this.success = success;
        this.failureColor = failureColor;
        this.failureMessage = failureMessage;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public Vector4f getFailureColor()
    {
        return failureColor;
    }

    public Component getFailureMessage()
    {
        return failureMessage;
    }
}
