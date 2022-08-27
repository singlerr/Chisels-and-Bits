package mod.chiselsandbits.client.culling;

import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import mod.chiselsandbits.api.multistate.accessor.world.IInWorldStateEntryInfo;
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
        if(!(aInfo instanceof IInWorldStateEntryInfo aBlockEntity))
        {
            // Shouldn't happen?  Maybe if someone's picked up a Chiseled block or another mod is rendering it?
            return true;
        }
        if(b.getBlockState().canOcclude() && Minecraft.getInstance().level != null)
        {
            final BlockPos aPos = new BlockPos(aBlockEntity.getBlockPos());
            final BlockPos bPos = aPos.relative(dir);
            // This is largely a reimplementation of Block#shouldRenderFace without the caching and more readily used in ChiseledBlockBakedModel's context.
            if (a.getBlockState().skipRendering(b.getBlockState(), dir))
            {
                return false;
            }
            // There's already some awkward caching underneath getFaceOcclusion, so this call shouldn't be too expensive in bulk.
            final VoxelShape bShape = b.getBlockState().getFaceOcclusionShape(Minecraft.getInstance().level, aPos, dir);
            if (bShape.isEmpty())
            {
                // If there's no geometry on a face that's touching the block in question, nearly always want to render that 'side'.
                return true;
            }
            final VoxelShape aShape = a.getBlockState().getFaceOcclusionShape(Minecraft.getInstance().level, bPos, dir);
            return Shapes.joinIsNotEmpty(aShape, bShape, BooleanOp.ONLY_FIRST);
            // Vanilla implements an Object2ByteLinkedOpenHashmap to cache these checks, albeit a fairly small (2048)-sized one.
            // Not sure if it makes sense to do for this implementation, since variations are likely to be much higher and duplicates much, much lower.
        }
        return true;
    }
}
