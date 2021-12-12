package mod.chiselsandbits.fabric.mixin.client.resources;

import mod.chiselsandbits.client.icon.IconManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaintingTextureManager.class)
public class PaintingTextureManagerConstructionHandler
{

    @Inject(
      method = "<init>",
      at = @At("RETURN")
    )
    public void onConstruction(final TextureManager textureManager, final CallbackInfo ci) {
        IconManager.getInstance().initialize();
    }
}
