package mod.chiselsandbits.fabric.mixin.platform.client.multiplayer;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeWorldlyBlockMixin
{

    @Redirect(
      method = "continueDestroyBlock",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"
      )
    )
    private static SoundType redirectGetBlockStateSoundType(BlockState blockState, BlockPos blockPos)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, Minecraft.getInstance().level, blockPos, Minecraft.getInstance().player
            );
        }
        return blockState.getSoundType();
    }
}
