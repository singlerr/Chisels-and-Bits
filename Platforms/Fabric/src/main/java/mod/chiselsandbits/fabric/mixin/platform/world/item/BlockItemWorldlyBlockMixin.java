package mod.chiselsandbits.fabric.mixin.platform.world.item;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BlockItem.class)
public abstract class BlockItemWorldlyBlockMixin
{

    @Redirect(
      method = "place",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"
      )
    )
    public SoundType redirectGetBlockStateSoundType(BlockState blockState, BlockPlaceContext blockPlaceContext)
    {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        Player player = blockPlaceContext.getPlayer();

        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, level, blockPos, player
            );
        }
        return blockState.getSoundType();
    }

    @Redirect(
      method = "place",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/item/BlockItem;getPlaceSound(Lnet/minecraft/world/level/block/state/BlockState;)Lnet/minecraft/sounds/SoundEvent;"
      )
    )
    public SoundEvent redirectGetBlockStateSoundTypePlace(BlockItem blockItem, BlockState blockState, BlockPlaceContext blockPlaceContext)
    {
        BlockPos blockPos = blockPlaceContext.getClickedPos();
        Level level = blockPlaceContext.getLevel();
        Player player = blockPlaceContext.getPlayer();

        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, level, blockPos, player
            ).getPlaceSound();
        }
        return blockState.getSoundType().getPlaceSound();
    }
}
