package mod.chiselsandbits.fabric.mixin.platform.client.render.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(ModelBlockRenderer.class)
public abstract class ModelBlockRendererWorldlyBlockMixin
{
    @Redirect(
      method = "tesselateBlock",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"
      )
    )
    private static int redirectGetBlockStateLightEmission(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BakedModel bakedModel, BlockState givenState, BlockPos blockPos, PoseStack poseStack, VertexConsumer vertexConsumer, boolean bl, Random random, long l, int i)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getLightEmission(
              blockState, blockAndTintGetter, blockPos
            );
        }
        return blockState.getLightEmission();
    }
}
