package mod.chiselsandbits.fabric.mixin.platform.world.entity;

import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class LivingEntityWorldlyBlockMixin extends Entity
{
    public LivingEntityWorldlyBlockMixin(final EntityType<?> entityType, final Level level)
    {
        super(entityType, level);
    }

    @Redirect(
      method = "travel",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/Block;getFriction()F"
      )
    )
    public float redirectGetBlockFriction(Block block)
    {
        final BlockPos pos = this.getBlockPosBelowThatAffectsMyMovement();
        final BlockState blockState = this.level.getBlockState(pos);
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties) {
            return blockWithWorldlyProperties.getFriction(blockState, this.level, pos, this);
        }

        return block.getFriction();
    }

    @Redirect(
      method = "playBlockFallSound",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"
      )
    )
    public SoundType redirectGetBlockStateSoundType(BlockState blockState)
    {
        int x = Mth.floor(this.getX());
        int y = Mth.floor(this.getY() - 0.20000000298023224D);
        int z = Mth.floor(this.getZ());
        final BlockPos blockPos = new BlockPos(x, y, z);

        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, level, blockPos, this
            );
        }
        return blockState.getSoundType();
    }
}
