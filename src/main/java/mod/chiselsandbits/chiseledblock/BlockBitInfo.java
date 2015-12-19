
package mod.chiselsandbits.chiseledblock;

import java.lang.reflect.Method;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockBitInfo
{

	public final boolean isCompatiable;
	public final float hardness;
	public final float explosionResistance;

	private BlockBitInfo(
			final boolean isCompatiable,
			final float hardness,
			final float explosionResistance )
	{
		this.isCompatiable = isCompatiable;
		this.hardness = hardness;
		this.explosionResistance = explosionResistance;
	}

	public static BlockBitInfo createFromState(
			final IBlockState state )
	{
		try
		{
			// require basic hardness behavior...
			final ProxyBlock pb = new ProxyBlock();
			final Block blk = state.getBlock();
			final Class<? extends Block> blkClass = blk.getClass();

			pb.getBlockHardness( null, null );
			final Method hardnessMethod = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class );
			final boolean test_a = hardnessMethod.getDeclaringClass() == Block.class;

			pb.getPlayerRelativeBlockHardness( null, null, null );
			final boolean test_b = blkClass.getMethod( pb.MethodName, EntityPlayer.class, World.class, BlockPos.class ).getDeclaringClass() == Block.class;

			pb.getExplosionResistance( null );
			final Method exploResistance = blkClass.getMethod( pb.MethodName, Entity.class );
			final boolean test_c = exploResistance.getDeclaringClass() == Block.class;

			pb.getExplosionResistance( null, null, null, null );
			final boolean test_d = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class, Entity.class, Explosion.class ).getDeclaringClass() == Block.class;

			// is it perfect?
			if ( test_a && test_b && test_c && test_d )
			{
				final float blockHardness = blk.getBlockHardness( null, null );
				final float resistance = blk.getExplosionResistance( null );

				return new BlockBitInfo( true, blockHardness, resistance );
			}
			else
			{
				// okay.. so maybe its a bit fancy.. but it still might work..
				// just fill in the gaps.

				final Block stone = Blocks.stone;
				return new BlockBitInfo( ChiselsAndBits.instance.config.compatabilityMode, stone.getBlockHardness( null, null ), stone.getExplosionResistance( null ) );
			}
		}
		catch ( final Exception err )
		{
			return new BlockBitInfo( false, -1, -1 );
		}
	}

}
