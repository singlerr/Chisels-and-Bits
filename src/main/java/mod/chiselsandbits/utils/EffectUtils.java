package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.item.INoHitEffectsItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Random;

@SuppressWarnings("deprecation")
public class EffectUtils
{

    private static final Random RANDOM = new Random();

    private EffectUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: EffectUtils. This is a utility class");
    }

    public static boolean addBlockDestroyEffects(final IWorldReader world, final BlockPos pos, final BlockState primaryState, final ParticleManager manager, final World renderingWorld)
    {
        if (!primaryState.getBlock().isAir(primaryState, world, pos))
        {
            VoxelShape voxelshape = primaryState.getShape(world, pos);
            voxelshape.forEachBox((p_228348_3_, p_228348_5_, p_228348_7_, p_228348_9_, p_228348_11_, p_228348_13_) -> {
                double d1 = Math.min(1.0D, p_228348_9_ - p_228348_3_);
                double d2 = Math.min(1.0D, p_228348_11_ - p_228348_5_);
                double d3 = Math.min(1.0D, p_228348_13_ - p_228348_7_);
                int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
                int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
                int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

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
                            manager.addEffect((new DiggingParticle((ClientWorld) renderingWorld,
                              (double) pos.getX() + d7,
                              (double) pos.getY() + d8,
                              (double) pos.getZ() + d9,
                              d4 - 0.5D,
                              d5 - 0.5D,
                              d6 - 0.5D,
                              primaryState)).setBlockPos(pos));
                        }
                    }
                }
            });
        }

        return true;
    }

    public static boolean addHitEffects(final World world, final BlockRayTraceResult blockRayTraceResult, final BlockState primaryState, final ParticleManager manager)
    {
        if (Minecraft.getInstance().player == null)
            return false;

        final ItemStack hitWith = Minecraft.getInstance().player.getHeldItemMainhand();

        if (!hitWith.isEmpty() && hitWith.getItem() instanceof INoHitEffectsItem)
        {
            return true;
        }

        final BlockPos pos = blockRayTraceResult.getPos();
        final float boxOffset = 0.1F;

        AxisAlignedBB bb = world.getBlockState(pos).getBlock().getShape(primaryState, world, pos, ISelectionContext.dummy()).getBoundingBox();

        double x = RANDOM.nextDouble() * (bb.maxX - bb.minX - boxOffset * 2.0F) + boxOffset + bb.minX;
        double y = RANDOM.nextDouble() * (bb.maxY - bb.minY - boxOffset * 2.0F) + boxOffset + bb.minY;
        double z = RANDOM.nextDouble() * (bb.maxZ - bb.minZ - boxOffset * 2.0F) + boxOffset + bb.minZ;

        switch (blockRayTraceResult.getFace())
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

        manager.addEffect((new DiggingParticle((ClientWorld) world, x, y, z, 0.0D, 0.0D, 0.0D, primaryState)).setBlockPos(pos)
                                   .multiplyVelocity(0.2F)
                                   .multiplyParticleScaleBy(0.6F));

        return true;
    }
}
