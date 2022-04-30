package mod.chiselsandbits.fabric.mixin.platform.client.render;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererWorldlyBlockMixin implements ResourceManagerReloadListener, AutoCloseable
{

    @Shadow private ClientLevel level;

    @Redirect(
      method = "getLightColor(Lnet/minecraft/world/level/BlockAndTintGetter;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)I",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"
      )
    )
    private static int redirectGetBlockStateLightEmission(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockState givenState, BlockPos blockPos)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getLightEmission(
              blockState, blockAndTintGetter, blockPos
            );
        }
        return blockState.getLightEmission();
    }

    @Redirect(
      method = "levelEvent",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"
      )
    )
    public SoundType redirectGetBlockStateSoundType(BlockState blockState, Player player, int i, BlockPos blockPos, int j)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, this.level, blockPos, null
            );
        }
        return blockState.getSoundType();
    }
}
