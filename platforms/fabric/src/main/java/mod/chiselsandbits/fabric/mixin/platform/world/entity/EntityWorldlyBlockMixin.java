package mod.chiselsandbits.fabric.mixin.platform.world.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.platforms.core.block.IBlockWithWorldlyProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(Entity.class)
public abstract class EntityWorldlyBlockMixin
{

    @Shadow public Level level;

    private Entity getThis() {
        return (Entity) (Object) this;
    }

    @Redirect(
      method = "playStepSound",
      at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;",
        ordinal = 0
      )
    )
    public SoundType redirectGetOffsetBlockStateSoundType(BlockState blockState, BlockPos blockPos, BlockState givenState)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, getThis().level, blockPos.above(), getThis()
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
    public SoundType redirectGetBlockStateSoundType(BlockState blockState, BlockPos blockPos, BlockState givenState)
    {
        if (blockState.getBlock() instanceof IBlockWithWorldlyProperties blockWithWorldlyProperties)
        {
            return blockWithWorldlyProperties.getSoundType(
              blockState, getThis().level, blockPos, getThis()
            );
        }
        return blockState.getSoundType();
    }
}
