package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.item.INoHitEffectsItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;

import java.util.Random;

@SuppressWarnings("deprecation")
public class EffectUtils
{

    private static final Random RANDOM = new Random();

    private EffectUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: EffectUtils. This is a utility class");
    }

    public static boolean addBlockDestroyEffects(final LevelReader world, final BlockPos pos, final BlockState primaryState, final ParticleEngine manager, final Level renderingWorld)
    {
        if (!primaryState.isAir())
        {
            VoxelShape voxelshape = primaryState.getShape(world, pos);
            voxelshape.forAllBoxes((p_228348_3_, p_228348_5_, p_228348_7_, p_228348_9_, p_228348_11_, p_228348_13_) -> {
                double d1 = Math.min(1.0D, p_228348_9_ - p_228348_3_);
                double d2 = Math.min(1.0D, p_228348_11_ - p_228348_5_);
                double d3 = Math.min(1.0D, p_228348_13_ - p_228348_7_);
                int i = Math.max(2, Mth.ceil(d1 / 0.25D));
                int j = Math.max(2, Mth.ceil(d2 / 0.25D));
                int k = Math.max(2, Mth.ceil(d3 / 0.25D));

                for (int l = 0; l < i; ++l)
                {
                    for (int i1 = 0; i1 < j; ++i1)
                    {
                        for (int j1 = 0; j1 < k; ++j1)
                        {
                            double d4 = ((double) l + 0.5D) / (double) i;
                            double d5 = ((double) i1 + 0.5D) / (double) j;
                            double d6 = ((double) j1 + 0.5D) / (double) k;
                            double d7 = d4 * d1 + p_228348_3_;
                            double d8 = d5 * d2 + p_228348_5_;
                            double d9 = d6 * d3 + p_228348_7_;
                            manager.add((new TerrainParticle((ClientLevel) renderingWorld,
                              (double) pos.getX() + d7,
                              (double) pos.getY() + d8,
                              (double) pos.getZ() + d9,
                              d4 - 0.5D,
                              d5 - 0.5D,
                              d6 - 0.5D,
                              primaryState, pos)));
                        }
                    }
                }
            });
        }

        return true;
    }

    public static boolean addHitEffects(final Level world, final BlockHitResult blockRayTraceResult, final BlockState primaryState, final ParticleEngine manager)
    {
        if (Minecraft.getInstance().player == null)
            return false;

        final ItemStack hitWith = Minecraft.getInstance().player.getMainHandItem();

        if (!hitWith.isEmpty() && hitWith.getItem() instanceof INoHitEffectsItem)
        {
            return true;
        }

        final BlockPos pos = blockRayTraceResult.getBlockPos();
        final float boxOffset = 0.1F;

        AABB bb = world.getBlockState(pos).getBlock().getShape(primaryState, world, pos, CollisionContext.empty()).bounds();

        double x = RANDOM.nextDouble() * (bb.maxX - bb.minX - boxOffset * 2.0F) + boxOffset + bb.minX;
        double y = RANDOM.nextDouble() * (bb.maxY - bb.minY - boxOffset * 2.0F) + boxOffset + bb.minY;
        double z = RANDOM.nextDouble() * (bb.maxZ - bb.minZ - boxOffset * 2.0F) + boxOffset + bb.minZ;

        switch (blockRayTraceResult.getDirection())
        {
            case DOWN:
                y = bb.minY - boxOffset;
                break;
            case EAST:
                x = bb.maxX + boxOffset;
                break;
            case NORTH:
                z = bb.minZ - boxOffset;
                break;
            case SOUTH:
                z = bb.maxZ + boxOffset;
                break;
            case UP:
                y = bb.maxY + boxOffset;
                break;
            case WEST:
                x = bb.minX - boxOffset;
                break;
            default:
                break;
        }

        manager.add((new TerrainParticle((ClientLevel) world, x, y, z, 0.0D, 0.0D, 0.0D, primaryState, pos))
                                   .setPower(0.2F)
                                   .scale(0.6F));

        return true;
    }
}
