package mod.chiselsandbits.fabric.mixin.platform.world.level;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourWorldlyBlockMixin
{

    @Redirect(
      method = "getDestroyProgress",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/entity/player/Player;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"
      )
    )
    public boolean redirectGetBlockStateLightEmission(Player player, BlockState blockState, BlockState param0, Player param1, BlockGetter param2, BlockPos param3)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            blockWithWorldlyProperties.canHarvestBlock(
              blockState, param2, param3, player
            );
        }
        return player.hasCorrectToolForDrops(blockState);
    }
}
