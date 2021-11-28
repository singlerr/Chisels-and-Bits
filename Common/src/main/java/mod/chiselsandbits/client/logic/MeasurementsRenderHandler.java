package mod.chiselsandbits.client.logic;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.client.render.MeasurementRenderer;

public class MeasurementsRenderHandler
{

    public static void renderMeasurements(final PoseStack poseStack)
    {
        MeasurementRenderer.getInstance().renderMeasurements(poseStack);
    }

}
