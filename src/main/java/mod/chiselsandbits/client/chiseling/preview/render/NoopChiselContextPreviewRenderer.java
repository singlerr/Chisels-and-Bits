package mod.chiselsandbits.client.chiseling.preview.render;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.chiseling.IChiselingContext;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRenderer;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.resources.ResourceLocation;

public class NoopChiselContextPreviewRenderer implements IChiselContextPreviewRenderer
{
    static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "noop");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void renderExistingContextsBoundingBox(
      final PoseStack matrixStack, final IChiselingContext currentContextSnapshot)
    {
        //Some people do not want this, so we have this.
    }
}
