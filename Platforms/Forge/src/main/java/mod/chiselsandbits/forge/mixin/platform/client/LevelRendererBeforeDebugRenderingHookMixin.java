package mod.chiselsandbits.forge.mixin.platform.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import mod.chiselsandbits.client.input.FrameBasedInputTracker;
import mod.chiselsandbits.client.logic.MeasurementsRenderHandler;
import mod.chiselsandbits.client.logic.MultiStateBlockPreviewRenderHandler;
import mod.chiselsandbits.client.logic.SelectedObjectRenderHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererBeforeDebugRenderingHookMixin
{
    @Unique
    private boolean didRenderParticles;

    @Inject(
      method = {"renderLevel"},
      at = {@At(
        value = "INVOKE",
        target = "Lnet/minecraft/client/particle/ParticleEngine;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;Lnet/minecraft/client/renderer/LightTexture;Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/culling/Frustum;)V"
      )}
    )
    private void onRenderParticles(CallbackInfo ci) {
        this.didRenderParticles = true;
    }

    @Inject(
      method = {"renderLevel"},
      at = {@At(
        value = "INVOKE",
        target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"
      )}
    )
    private void beforeDebugRender(PoseStack poseStack, float partialTicks, long startTimeNano, boolean shouldRenderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        if (!didRenderParticles)
            return;

        didRenderParticles = false;
        final LevelRenderer levelRenderer = (LevelRenderer) (Object) this;

        SelectedObjectRenderHandler.renderCustomWorldHighlight(
          levelRenderer,
          poseStack,
          partialTicks
        );

        MeasurementsRenderHandler.renderMeasurements(poseStack);

        MultiStateBlockPreviewRenderHandler.renderMultiStateBlockPreview(poseStack);

        FrameBasedInputTracker.getInstance().onRenderFrame();
    }

}
