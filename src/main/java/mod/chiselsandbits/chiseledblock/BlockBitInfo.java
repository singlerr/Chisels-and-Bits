package mod.chiselsandbits.chiseledblock;

import java.lang.reflect.Method;
import java.util.HashMap;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import mod.chiselsandbits.api.IgnoreBlockLogic;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockSlime;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;

public class BlockBitInfo
{
	// imc api...
	private static HashMap<Block, Boolean> ignoreLogicBlocks = new HashMap<Block, Boolean>();

	static
	{
		ignoreLogicBlocks.put( Blocks.leaves, true );
		ignoreLogicBlocks.put( Blocks.leaves2, true );
		ignoreLogicBlocks.put( Blocks.snow, true );
	}

	// cache data..
	private static HashMap<IBlockState, BlockBitInfo> stateBitInfo = new HashMap<IBlockState, BlockBitInfo>();
	private static HashMap<Block, Boolean> supportedBlocks = new HashMap<Block, Boolean>();
	private static HashMap<Block, Fluid> fluidBlocks = new HashMap<Block, Fluid>();
	private static TIntObjectMap<Fluid> fluidStates = new TIntObjectHashMap<Fluid>();
	private static HashMap<IBlockState, Integer> bitColor = new HashMap<IBlockState, Integer>();

	public static int getColorFor(
			final IBlockState state,
			final int renderPass )
	{
		Integer out = bitColor.get( state );

		if ( out == null )
		{
			final Block blk = state.getBlock();
			final ItemStack target = new ItemStack( blk, 1, blk.damageDropped( state ) );

			if ( target.getItem() == null )
			{
				out = 0xffffff;
			}
			else
			{
				out = target.getItem().getColorFromItemStack( target, renderPass );
			}

			bitColor.put( state, out );
		}

		return out;
	}

	public static void addFluidBlock(
			final Block blk,
			final Fluid fluid )
	{
		if ( blk == null )
		{
			return;
		}

		fluidBlocks.put( blk, fluid );

		for ( final IBlockState state : blk.getBlockState().getValidStates() )
		{
			try
			{
				fluidStates.put( Block.getStateId( state ), fluid );
			}
			catch ( final Throwable t )
			{
				Log.logError( "Error while determining fluid state.", t );
			}
		}

		stateBitInfo.clear();
		supportedBlocks.clear();
	}

	static public Fluid getFluidFromBlock(
			final Block blk )
	{
		return fluidBlocks.get( blk );
	}

	public static VoxelType getTypeFromStateID(
			final int bit )
	{
		if ( bit == 0 )
		{
			return VoxelType.AIR;
		}

		return fluidStates.containsKey( bit ) ? VoxelType.FLUID : VoxelType.SOLID;
	}

	public static void ignoreBlockLogic(
			final Block which )
	{
		ignoreLogicBlocks.put( which, true );

		stateBitInfo.clear();
		supportedBlocks.clear();
	}

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
			final ReflectionHelperBlock pb = new ReflectionHelperBlock();
			final Class<? extends Block> blkClass = blk.getClass();

			// require default drop behavior...
			pb.onEntityCollidedWithBlock( null, null, null );
			final boolean entityCollisionTest = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class, Entity.class ).getDeclaringClass() == Block.class || blkClass == BlockSlime.class;

			pb.onEntityCollidedWithBlock( null, null, null, null );
			final boolean entityCollision2Test = blkClass.getMethod( pb.MethodName, World.class, BlockPos.class, IBlockState.class, Entity.class ).getDeclaringClass() == Block.class || blkClass == BlockSlime.class;

			// full cube specifically is tied to lighting... so for glass
			// Compatibility use isFullBlock which can be true for glass.

			boolean isFullBlock = blk.isFullBlock() || blkClass == BlockStainedGlass.class || blkClass == BlockGlass.class || blk == Blocks.slime_block || blk == Blocks.ice;

			final BlockBitInfo info = BlockBitInfo.createFromState( state );

			boolean requiredImplementation = entityCollisionTest && entityCollision2Test;
			boolean hasBehavior = ( blk.hasTileEntity( state ) || blk.getTickRandomly() ) && blkClass != BlockGrass.class && blkClass != BlockIce.class;

			final boolean supportedMaterial = ChiselsAndBits.getBlocks().getConversion( blk ) != null;

			final Boolean IgnoredLogic = ignoreLogicBlocks.get( blk );
			if ( blkClass.isAnnotationPresent( IgnoreBlockLogic.class ) || IgnoredLogic != null && IgnoredLogic )
			{
				isFullBlock = true;
				requiredImplementation = true;
				hasBehavior = false;
			}

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

			if ( fluidBlocks.containsKey( blk ) )
			{
				supportedBlocks.put( blk, true );
				return true;
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
			final ReflectionHelperBlock reflectBlock = new ReflectionHelperBlock();
			final Block blk = state.getBlock();
			final Class<? extends Block> blkClass = blk.getClass();

			reflectBlock.getBlockHardness( null, null );
			final Method hardnessMethod = blkClass.getMethod( reflectBlock.MethodName, World.class, BlockPos.class );
			final boolean test_a = hardnessMethod.getDeclaringClass() == Block.class;

			reflectBlock.getPlayerRelativeBlockHardness( null, null, null );
			final boolean test_b = blkClass.getMethod( reflectBlock.MethodName, EntityPlayer.class, World.class, BlockPos.class ).getDeclaringClass() == Block.class;

			reflectBlock.getExplosionResistance( null );
			final Method exploResistance = blkClass.getMethod( reflectBlock.MethodName, Entity.class );
			final boolean test_c = exploResistance.getDeclaringClass() == Block.class;

			reflectBlock.getExplosionResistance( null, null, null, null );
			final boolean test_d = blkClass.getMethod( reflectBlock.MethodName, World.class, BlockPos.class, Entity.class, Explosion.class ).getDeclaringClass() == Block.class;

			// is it perfect?
			if ( test_a && test_b && test_c && test_d )
			{
				final float blockHardness = blk.getBlockHardness( null, null );
				final float resistance = blk.getExplosionResistance( null );

				return new BlockBitInfo( true, blockHardness, resistance );
			}
			else
			{
				// less accurate, we can just pretend they are some fixed
				// hardness... say like stone?

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
