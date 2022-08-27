package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Determine Culling using Block's Native Checks.
 *
 * Simplified version of {@link net.minecraft.world.level.block.Block#shouldRenderFace Block.shouldRenderFace}
 */
public class MCCullTest implements ICullTest
{
    @Override
    public boolean isVisible(
      final IStateEntryInfo aInfo,
      final BlockInformation b,
      final BlockPos bPos,
      final Direction dir)
    {
        final BlockInformation a = aInfo.getBlockInformation();
        if(a == b)
        {
            return false;
        }
        if(a.getBlockState().skipRendering(b.getBlockState(), Direction.NORTH))
        {
            return false;
        }
        if(b.getBlockState().canOcclude() && Minecraft.getInstance().level != null)
        {
            // This is largely a reimplementation of Block#shouldRenderFace without the caching and more readily used in ChiseledBlockBakedModel's context.
            if (a.getBlockState().skipRendering(b.getBlockState(), dir))
            {
                return false;
            }
            // There's already some awkward caching underneath getFaceOcclusion, so this call shouldn't be too expensive in bulk.
            final VoxelShape bShape = b.getBlockState().getFaceOcclusionShape(Minecraft.getInstance().level, bPos, dir);
            if (bShape.isEmpty())
            {
                // If there's no geometry on a face that's touching the block in question, nearly always want to render that 'side'.
                return true;
            }
            final VoxelShape aShape = a.getBlockState().getFaceOcclusionShape(Minecraft.getInstance().level, new BlockPos(aInfo.getCenterPoint()), dir);
            return Shapes.joinIsNotEmpty(aShape, bShape, BooleanOp.ONLY_FIRST);
            // Vanilla implements an Object2ByteLinkedOpenHashmap to cache these checks, albeit a fairly small (2048)-sized one.
            // Not sure if it makes sense to do for this implementation, since variations are likely to be much higher and duplicates much, much lower.
        }
        return true;
    }
}
