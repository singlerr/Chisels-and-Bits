package mod.chiselsandbits.api.item.withhighlight;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

/**
 * Indicates an item that is used to draw custom highlights,
 * using custom logic instead of the default one.
 */
public interface IWithHighlightItem
{
    /**
     * Indicates if the given player is allowed to use the
     * default render method.
     *
     * If this is false. The default highlight rendering is cancelled.
     *
     * @param playerEntity The player entity in question.
     *
     * @return True to let minecraft handle the highlight rendering, false when not.
     */
    @OnlyIn(Dist.CLIENT)
    boolean shouldDrawDefaultHighlight(@NotNull final PlayerEntity playerEntity);

    /**
     * Renders the highlight for the current item.
     *
     * @param playerEntity The player entity in question.
     * @param worldRenderer The rendering world renderer.
     * @param matrixStack The matrix stack used to render the world.
     * @param partialTicks The partial ticks used for animations.
     * @param projectionMatrix The projection matrix used to render the world.
     * @param finishTimeNano The finish time of the world renderer in nano seconds.
     */
    void renderHighlight(PlayerEntity playerEntity, WorldRenderer worldRenderer, MatrixStack matrixStack, float partialTicks, Matrix4f projectionMatrix, long finishTimeNano);
}
