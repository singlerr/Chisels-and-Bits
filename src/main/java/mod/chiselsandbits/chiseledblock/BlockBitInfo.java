package mod.chiselsandbits.chiseledblock;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Random;

import mod.chiselsandbits.ChiselsAndBits;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockGlowstone;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class BlockBitInfo
{

	private static HashMap<IBlockState, BlockBitInfo> stateBitInfo = new HashMap<IBlockState, BlockBitInfo>();
	private static HashMap<Block, Boolean> supportedBlocks = new HashMap<Block, Boolean>();

	public static BlockBitInfo getBlockInfo(
			final IBlockState state )
	{
		BlockBitInfo bit = stateBitInfo.get( state );

		if ( bit == null )
		{
			bit = BlockBitInfo.createFromState( state );
			stateBitInfo.put( state, bit );
		}

		return bit;
	}

	public static boolean supportsBlock(
			final IBlockState state )
	{
		final Block blk = state.getBlock();

		if ( supportedBlocks.containsKey( blk ) )
		{
			return supportedBlocks.get( blk );
		}

		try
		{
			// require basic hardness behavior...
			final ProxyBlock pb = new ProxyBlock();
			final Class<? extends Block> blkClass = blk.getClass();

			// require default drop behavior...
			pb.quantityDropped( null );
			final Class<?> wc = blkClass.getMethod( pb.MethodName, Random.class ).getDeclaringClass();
			final boolean quantityDroppedTest = wc == Block.class || wc == BlockGlowstone.class || wc == BlockStainedGlass.class || wc == BlockGlass.class;

			pb.quantityDroppedWithBonus( 0, null );
			final boolean quantityDroppedWithBonusTest = blkClass.getMethod( pb.MethodName, int.class, Random.class ).getDeclaringClass() == Block.class || wc == BlockGlowstone.class;

			pb.quantityDropped( null, 0, null );
			final boolean quantityDropped2Test = blkClass.getMethod( pb.MethodName, IBlockState.class, int.class, Random.class ).getDeclaringClass() == Block.class;

			pb.onEntityCollidedWithBlock( null, null, null );
			final boolean entityCollisionTest = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class, Entity.class ).getDeclaringClass() == Block.class || blkClass == BlockSlime.class;

			pb.onEntityCollidedWithBlock( null, null, null, null );
			final boolean entityCollision2Test = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class, IBlockState.class, Entity.class ).getDeclaringClass() == Block.class || blkClass == BlockSlime.class;

			// full cube specifically is tied to lighting... so for glass
			// Compatibility use isFullBlock which can be true for glass.

			// final boolean isFullCube = blk.isFullCube()
			final boolean isFullBlock = blk.isFullBlock() || blkClass == BlockStainedGlass.class || blkClass == BlockGlass.class || blk == Blocks.slime_block;

			final BlockBitInfo info = BlockBitInfo.createFromState( state );

			final boolean requiredImplementation = quantityDroppedTest && quantityDroppedWithBonusTest && quantityDropped2Test && entityCollisionTest && entityCollision2Test;
			final boolean hasBehavior = blk.hasTileEntity( state ) || blk.getTickRandomly();

			final boolean supportedMaterial = ChiselsAndBits.getBlocks().getConversion( blk.getMaterial() ) != null;

			if ( info.isCompatiable && requiredImplementation && info.hardness >= -0.01f && isFullBlock && supportedMaterial && !hasBehavior )
			{
				final boolean result = ChiselsAndBits.getConfig().isEnabled( blkClass.getName() );
				supportedBlocks.put( blk, result );

				if ( result )
				{
					stateBitInfo.put( state, info );
				}

				return result;
			}

			supportedBlocks.put( blk, false );
			return false;
		}
		catch ( final Throwable t )
		{
			// if the above test fails for any reason, then the block cannot be
			// supported.
			supportedBlocks.put( blk, false );
			return false;
		}
	}

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
				return new BlockBitInfo( ChiselsAndBits.getConfig().compatabilityMode, stone.getBlockHardness( null, null ), stone.getExplosionResistance( null ) );
			}
		}
		catch ( final Exception err )
		{
			return new BlockBitInfo( false, -1, -1 );
		}
	}

}
