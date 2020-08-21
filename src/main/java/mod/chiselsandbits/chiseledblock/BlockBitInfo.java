package mod.chiselsandbits.chiseledblock;

import java.util.HashMap;
import java.util.Random;

import com.google.common.collect.Lists;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import mod.chiselsandbits.api.IgnoreBlockLogic;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.render.helpers.ModelUtil;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

public class BlockBitInfo
{
	// imc api...
	private static HashMap<Block, Boolean> ignoreLogicBlocks = new HashMap<>();

	static
	{
		ignoreLogicBlocks.put( Blocks.ACACIA_LEAVES, true );
        ignoreLogicBlocks.put( Blocks.BIRCH_LEAVES, true );
        ignoreLogicBlocks.put( Blocks.DARK_OAK_LEAVES, true );
        ignoreLogicBlocks.put( Blocks.JUNGLE_LEAVES, true );
        ignoreLogicBlocks.put( Blocks.OAK_LEAVES, true );
		ignoreLogicBlocks.put( Blocks.SPRUCE_LEAVES, true );
		ignoreLogicBlocks.put( Blocks.SNOW, true );
	}

	// cache data..
	private static HashMap<BlockState, BlockBitInfo> stateBitInfo    = new HashMap<>();
	private static HashMap<Block, Boolean>           supportedBlocks = new HashMap<>();
	private static HashMap<BlockState, Boolean>      forcedStates    = new HashMap<>();
	private static HashMap<BlockState, Fluid>        fluidBlocks     = new HashMap<>();
	private static IntObjectMap<Fluid>               fluidStates     = new IntObjectHashMap<>();
	private static HashMap<BlockState, Integer>      bitColor        = new HashMap<>();

	public static int getColorFor(
			final BlockState state,
			final int tint )
	{
		Integer out = bitColor.get( state );

		if ( out == null )
		{
			final Block blk = state.getBlock();

			final Fluid fluid = BlockBitInfo.getFluidFromBlock( blk );
			if ( fluid != null )
			{
				out = fluid.getAttributes().getColor();
			}
			else
			{
				final ItemStack target = ModUtil.getItemStackFromBlockState( state );

				if ( ModUtil.isEmpty( target ) )
				{
					out = 0xffffff;
				}
				else
				{
					out = ModelUtil.getItemStackColor( target, tint );
				}
			}

			bitColor.put( state, out );
		}

		return out;
	}

	public static void recalculateFluidBlocks()
	{
		fluidBlocks.clear();

		for ( final Fluid o : ForgeRegistries.FLUIDS )
		{
            BlockBitInfo.addFluidBlock( o );
        }
	}

	public static void addFluidBlock(
			final Fluid fluid )
	{
		fluidBlocks.put( fluid.getDefaultState().getBlockState(), fluid );

		for ( final BlockState state : fluid.getDefaultState().getBlockState().getBlock().getStateContainer().getValidStates() )
		{
			try
			{
				fluidStates.put( ModUtil.getStateId( state ), fluid );
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
		reset();
	}

	public static void forceStateCompatibility(
			final BlockState which,
			final boolean forceStatus )
	{
		forcedStates.put( which, forceStatus );
		reset();
	}

	public static void reset()
	{
		stateBitInfo.clear();
		supportedBlocks.clear();
	}

	public static BlockBitInfo getBlockInfo(
			final BlockState state )
	{
		BlockBitInfo bit = stateBitInfo.get( state );

		if ( bit == null )
		{
			bit = BlockBitInfo.createFromState( state );
			stateBitInfo.put( state, bit );
		}

		return bit;
	}

	@SuppressWarnings( "deprecation" )
	public static boolean supportsBlock(
			final BlockState state )
	{
		if ( forcedStates.containsKey( state ) )
		{
			return forcedStates.get( state );
		}

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

			// custom dropping behavior?
			pb.getDrops(state, null);
			final Class<?> wc = getDeclaringClass( blkClass, pb.MethodName, BlockState.class, LootContext.Builder.class );
			final boolean quantityDroppedTest = wc == Block.class;

			final boolean isNotSlab = Item.getItemFromBlock( blk ) != null;
			boolean itemExistsOrNotSpecialDrops = quantityDroppedTest || isNotSlab;

			// ignore blocks with custom collision.
			pb.getShape( null, null, null, null );
			boolean noCustomCollision = getDeclaringClass( blkClass, pb.MethodName, BlockState.class, IBlockReader.class, BlockPos.class, ISelectionContext.class ) == Block.class || blk.getClass() == SlimeBlock.class;

			// full cube specifically is tied to lighting... so for glass
			// Compatibility use isFullBlock which can be true for glass.
			boolean isFullBlock = state.isSolid();
			final BlockBitInfo info = BlockBitInfo.createFromState( state );

			final boolean tickingBehavior = blk.ticksRandomly(state) && ChiselsAndBits.getConfig().blacklistTickingBlocks;
			boolean hasBehavior = ( blk.hasTileEntity( state ) || tickingBehavior );
			final boolean hasItem = Item.getItemFromBlock( blk ) != null;

			final boolean supportedMaterial = ChiselsAndBits.getBlocks().getConversion( state ) != null;

			final Boolean IgnoredLogic = ignoreLogicBlocks.get( blk );
			if ( blkClass.isAnnotationPresent( IgnoreBlockLogic.class ) || IgnoredLogic != null && IgnoredLogic )
			{
				isFullBlock = true;
				noCustomCollision = true;
				hasBehavior = false;
				itemExistsOrNotSpecialDrops = true;
			}

			if ( info.isCompatiable && noCustomCollision && info.hardness >= -0.01f && isFullBlock && supportedMaterial && !hasBehavior && itemExistsOrNotSpecialDrops )
			{
				final boolean result = hasItem && ChiselsAndBits.getConfig().isEnabled( blkClass.getName() );
				supportedBlocks.put( blk, result );

				if ( result )
				{
					stateBitInfo.put( state, info );
				}

				return result;
			}

			if ( fluidBlocks.containsKey( blk ) )
			{
				stateBitInfo.put( state, info );
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

	private static Class<?> getDeclaringClass(
			final Class<?> blkClass,
			final String methodName,
			final Class<?>... args )
	{
		try
		{
			blkClass.getDeclaredMethod( methodName, args );
			return blkClass;
		}
		catch ( final NoSuchMethodException e )
		{
			// nothing here...
		}
		catch ( final SecurityException e )
		{
			// nothing here..
		}
		catch ( final NoClassDefFoundError e )
		{
			Log.eligibility( "Unable to determine blocks eligibility for chiseling, " + blkClass.getName() + " attempted to load " + e.getMessage() + " missing @OnlyIn( Dist.CLIENT ) or @Optional?" );
			return blkClass;
		}
		catch ( final Throwable t )
		{
			return blkClass;
		}

		return getDeclaringClass(
				blkClass.getSuperclass(),
				methodName,
				args );
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
			final BlockState state )
	{
		try
		{
			// require basic hardness behavior...
			final ReflectionHelperBlock reflectBlock = new ReflectionHelperBlock();
			final Block blk = state.getBlock();
			final Class<? extends Block> blkClass = blk.getClass();

			reflectBlock.getPlayerRelativeBlockHardness( null, null, null, null );
			final boolean test_b = getDeclaringClass( blkClass, reflectBlock.MethodName, BlockState.class, PlayerEntity.class, IBlockReader.class, BlockPos.class ) == Block.class;

			reflectBlock.getExplosionResistance();
			final Class<?> exploResistanceClz = getDeclaringClass( blkClass, reflectBlock.MethodName);
			final boolean test_c = exploResistanceClz == Block.class;

			reflectBlock.getExplosionResistance( null, null, null, null );
			final boolean test_d = getDeclaringClass( blkClass, reflectBlock.MethodName, BlockState.class, IBlockReader.class, BlockPos.class, Explosion.class ) == Block.class;

			final boolean isFluid = fluidStates.containsKey( ModUtil.getStateId( state ) );

			// is it perfect?
			if ( test_b && test_c && test_d && !isFluid )
			{
				final float blockHardness = state.getBlockHardness(new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO);
				final float resistance = blk.getExplosionResistance(state, new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO, new Explosion(null, null, 0,1,0, 10,
                  Lists.newArrayList(BlockPos.ZERO)));

				return new BlockBitInfo( true, blockHardness, resistance );
			}
			else
			{
				// less accurate, we can just pretend they are some fixed
				// hardness... say like stone?

				final Block stone = Blocks.STONE;
				return new BlockBitInfo( ChiselsAndBits.getConfig().compatabilityMode, 2f, 6f );
			}
		}
		catch ( final Exception err )
		{
			return new BlockBitInfo( false, -1, -1 );
		}
	}

	public static boolean canChisel(
			final BlockState state )
	{
		return state.getBlock() instanceof BlockChiseled || supportsBlock( state );
	}

}
