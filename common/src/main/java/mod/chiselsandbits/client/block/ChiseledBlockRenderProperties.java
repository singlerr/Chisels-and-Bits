package mod.chiselsandbits.client.block;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.utils.EffectUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class ChiseledBlockRenderProperties
{

    @Nullable
    private BlockState getPrimaryState(Level level, BlockPos pos) {
        final BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof IMultiStateBlockEntity multiStateBlockEntity))
            return null;

        return multiStateBlockEntity.getStatistics().getPrimaryState().getBlockState();
    }

    public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine engine)
    {
        if (!(target instanceof BlockHitResult blockTarget) || target.getType() == HitResult.Type.MISS)
            return false;

        BlockState primaryState = getPrimaryState(level, blockTarget.getBlockPos());
        if (primaryState == null)
            return false;

        return EffectUtils.addHitEffects(level, blockTarget, primaryState, engine);
    }

    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine engine)
    {
        BlockState primaryState = getPrimaryState(level, pos);
        if (primaryState == null)
            return false;

        EffectUtils.addBlockDestroyEffects(level, pos, primaryState, Minecraft.getInstance().particleEngine, level);
        return true;
    }
}
