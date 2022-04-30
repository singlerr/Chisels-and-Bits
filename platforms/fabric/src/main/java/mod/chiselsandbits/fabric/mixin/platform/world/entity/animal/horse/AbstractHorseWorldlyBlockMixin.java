package mod.chiselsandbits.fabric.mixin.platform.world.entity.animal.horse;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseWorldlyBlockMixin extends Entity
{

    public AbstractHorseWorldlyBlockMixin(final EntityType<?> entityType, final Level level)
    {
        super(entityType, level);
    }

    @Redirect(
      method = "playStepSound",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;",
        ordinal = 0
      )
    )
    public SoundType redirectGetBlockStateSoundType(BlockState blockState, BlockPos blockPos, BlockState givenState)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, this.level, blockPos, this
            );
        }
        return blockState.getSoundType();
    }

    @Redirect(
      method = "playStepSound",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;",
        ordinal = 1
      )
    )
    public SoundType redirectGetSnowedBlockStateSoundType(BlockState blockState, BlockPos blockPos, BlockState givenState)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, this.level, blockPos.above(), this
            );
        }
        return blockState.getSoundType();
    }
}
