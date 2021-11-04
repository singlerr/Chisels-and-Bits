package mod.chiselsandbits.platforms.core.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.platforms.core.client.IClientManager;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Gives access to the platforms specific rendering tasks.
 */
public interface IRenderingManager
{

    /**
     * Gives access to the clients rendering manager.
     *
     * @return The client rendering manager.
     */
    static IRenderingManager getInstance() {
        return IClientManager.getInstance().getRenderingManager();
    }

    /**
     * Renders a specific blockstate on the given position.
     *
     * @param last The current position matrix.
     * @param buffer The buffer to render into.
     * @param defaultBlockState The blockstate to render the model for.
     * @param model The model to render.
     * @param r The r color channel to apply.
     * @param g The g color channel to apply.
     * @param b The b color channel to apply-
     * @param combinedLight The combined light value to render.
     * @param combinedOverlay The combined overlay value to render.
     */
    void renderModel(
      PoseStack.Pose last,
      VertexConsumer buffer,
      BlockState defaultBlockState,
      BakedModel model,
      float r,
      float g,
      float b,
      int combinedLight,
      int combinedOverlay);

    /**
     * Indicates if the blockstate needs to be rendered in the render type.
     *
     * @param blockState The block state in question.
     * @param renderType The render type.
     * @return True when rendering in the given render type is required, false when not.
     */
    boolean canRenderInType(final BlockState blockState, final RenderType renderType);

    /**
     * Indicates if the fluidState needs to be rendered in the render type.
     *
     * @param fluidState The fluid state in question.
     * @param renderType The render type.
     * @return True when rendering in the given render type is required, false when not.
     */
    boolean canRenderInType(final FluidState fluidState, final RenderType renderType);

    /**
     * Gains access to the texture that is used to render a flowing fluid.
     *
     * @param fluidInformation The fluid to get the texture for.
     * @return The texture.
     */
    TextureAtlasSprite getFlowingFluidTexture(final FluidInformation fluidInformation);

    /**
     * Gains access to the texture that is used to render a still fluid.
     *
     * @param fluidInformation The fluid to get the texture for.
     * @return The texture.
     */
    TextureAtlasSprite getStillFluidTexture(final FluidInformation fluidInformation);
}
