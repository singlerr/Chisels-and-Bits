package mod.chiselsandbits.fabric.mixin.platform.client.render.block;

import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockRenderDispatcher.class)
public interface BlockRenderDispatcherAccessor
{

    @Accessor("blockEntityRenderer")
    @Mutable()
    public void setBlockEntityRenderer(BlockEntityWithoutLevelRenderer renderer);

    @Accessor("blockEntityRenderer")
    BlockEntityWithoutLevelRenderer getBlockEntityRenderer();
}
