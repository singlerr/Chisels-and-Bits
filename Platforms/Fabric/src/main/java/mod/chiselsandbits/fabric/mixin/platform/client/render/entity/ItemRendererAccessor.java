package mod.chiselsandbits.fabric.mixin.platform.client.render.entity;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ItemRenderer.class)
public interface ItemRendererAccessor
{

    @Accessor("blockEntityRenderer")
    @Mutable
    void setBlockEntityRenderer(BlockEntityWithoutLevelRenderer value);

    @Accessor("blockEntityRenderer")
    BlockEntityWithoutLevelRenderer getBlockEntityRenderer();
}
