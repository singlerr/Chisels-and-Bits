package mod.chiselsandbits.chiseling.eligibility;

import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.SingleBlockBlockReader;
import mod.chiselsandbits.utils.ClassUtils;
import mod.chiselsandbits.utils.ReflectionHelperBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.extensions.IForgeBlock;

public class BlockEligibilityAnalysisData
{

    private final boolean isCompatible;
    private final float   hardness;
    private final float   explosionResistance;

    private BlockEligibilityAnalysisData(
      final boolean isCompatible,
      final float hardness,
      final float explosionResistance )
    {
        this.isCompatible = isCompatible;
        this.hardness = hardness;
        this.explosionResistance = explosionResistance;
    }

    public boolean isCompatible()
    {
        return isCompatible;
    }

    public float getHardness()
    {
        return hardness;
    }

    public float getExplosionResistance()
    {
        return explosionResistance;
    }

    public static BlockEligibilityAnalysisData createFromState(
      final BlockState state )
    {
        try
        {
            // require basic hardness behavior...
            final ReflectionHelperBlock reflectBlock = new ReflectionHelperBlock();
            final Block blk = state.getBlock();
            final Class<? extends Block> blkClass = blk.getClass();

            reflectBlock.getPlayerRelativeBlockHardness( null, null, null, null );
            final Class<?> b_Class = ClassUtils.getDeclaringClass( blkClass, reflectBlock.MethodName, BlockState.class, PlayerEntity.class, IBlockReader.class, BlockPos.class );
            final boolean test_b = b_Class == Block.class || b_Class == AbstractBlock.class;

            reflectBlock.getExplosionResistance();
            Class<?> exploResistanceClz = ClassUtils.getDeclaringClass( blkClass, reflectBlock.MethodName);
            final boolean test_c = exploResistanceClz == Block.class || exploResistanceClz == AbstractBlock.class;

            reflectBlock.getExplosionResistance( null, null, null, null );
            exploResistanceClz = ClassUtils.getDeclaringClass( blkClass, reflectBlock.MethodName, BlockState.class, IBlockReader.class, BlockPos.class, Explosion.class );
            final boolean test_d = exploResistanceClz == Block.class || exploResistanceClz == AbstractBlock.class || exploResistanceClz == null || exploResistanceClz == IForgeBlock.class;

            final boolean isFluid = !state.getFluidState().isEmpty();

            // is it perfect?
            if ( test_b && test_c && test_d && !isFluid )
            {
                final float blockHardness = state.getBlockHardness(new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO);
                final float resistance = blk.getExplosionResistance(state, new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO, new Explosion(null, null,null,
                  null, 0,1,0, 10, false, Explosion.Mode.NONE));

                return new BlockEligibilityAnalysisData( true, blockHardness, resistance );
            }
            else
            {
                // less accurate, we can just pretend they are some fixed
                // hardness... say like stone?

                final Block stone = Blocks.STONE;
                return new BlockEligibilityAnalysisData( Configuration.getInstance().getServer().compatabilityMode.get(), 2f, 6f );
            }
        }
        catch ( final Exception err )
        {
            return new BlockEligibilityAnalysisData( false, -1, -1 );
        }
    }
}
