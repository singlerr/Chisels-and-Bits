package mod.chiselsandbits.fabric.mixin.client;

import mod.chiselsandbits.block.ChiseledBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public abstract class ParticleEngineRenderPropertiesMixin
{

    @Inject(method = "destroy", at = @At("HEAD"), cancellable = true)
    public void onDestroy(BlockPos pos, BlockState state, final CallbackInfo callbackInfo) {
        if (state.getBlock() instanceof ChiseledBlock chiseledBlock
                && chiseledBlock.getRenderProperties().addDestroyEffects(
                        state,
                        Minecraft.getInstance().level,
                        pos,
                        Minecraft.getInstance().particleEngine))
            callbackInfo.cancel();
    }

    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    public void onCrack(BlockPos pos, Direction direction, final CallbackInfo callbackInfo) {
        BlockState state = Minecraft.getInstance().level.getBlockState(pos);
        if (state.getBlock() instanceof ChiseledBlock chiseledBlock
                && chiseledBlock.getRenderProperties().addHitEffects(
                        state,
                        Minecraft.getInstance().level,
                        Minecraft.getInstance().hitResult,
                        Minecraft.getInstance().particleEngine))
            callbackInfo.cancel();
    }
}