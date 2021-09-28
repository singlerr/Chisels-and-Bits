package mod.chiselsandbits.api.pattern.placement;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Represents a result of a placement attempt.
 */
public class PlacementResult
{

    /**
     * Creates a new successful placement result.
     * @return A new result indicating successful placement.
     */
    public static PlacementResult success(){
        return new PlacementResult(true, Vector3d.ZERO, new StringTextComponent(""));
    }

    /**
     * Creates a new placement result which indicates failure.
     *
     * @param color The color of the wireframe to indicate the failure type.
     * @param message The message to show to the user.
     * @return A new result indicating a failure to place the pattern.
     */
    public static PlacementResult failure(final Vector3d color, final ITextComponent message) {
        return new PlacementResult(false, color, message);
    }

    private final boolean success;
    private final Vector3d failureColor;
    private final ITextComponent failureMessage;

    private PlacementResult(final boolean success, final Vector3d failureColor, final ITextComponent failureMessage) {
        this.success = success;
        this.failureColor = failureColor;
        this.failureMessage = failureMessage;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public Vector3d getFailureColor()
    {
        return failureColor;
    }

    public ITextComponent getFailureMessage()
    {
        return failureMessage;
    }
}
