package mod.chiselsandbits.chiseledblock;

import java.util.HashMap;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import mod.chiselsandbits.api.IgnoreBlockLogic;
import mod.chiselsandbits.chiseledblock.data.VoxelType;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.registry.ModBlocks;
import mod.chiselsandbits.registry.ModTags;
import mod.chiselsandbits.render.helpers.ModelUtil;
import mod.chiselsandbits.utils.SingleBlockBlockReader;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ForgeRegistries;

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
	private static HashMap<Block, SupportsAnalysisResult> supportedBlocks = new HashMap<>();
    private static HashMap<Block, Boolean> forcedBlocks    = new HashMap<>();
	private static HashMap<Block, Fluid>   fluidBlocks  = new HashMap<>();
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

	public static void recalculate()
	{
		recalculateFluids();
	}

    public static void recalculateFluids()
    {
        fluidBlocks.clear();

        for ( final Fluid o : ForgeRegistries.FLUIDS )
        {
            if (o.getDefaultState().isSource())
                BlockBitInfo.addFluidBlock( o );
        }
    }

    public static void addFluidBlock(
			final Fluid fluid )
	{
		fluidBlocks.put( fluid.getDefaultState().getBlockState().getBlock(), fluid );

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
			final Block which,
			final boolean forceStatus )
	{
		forcedBlocks.put( which, forceStatus );
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
	public static SupportsAnalysisResult doSupportAnalysis(
			final BlockState state )
	{
	    if (state.getBlock() instanceof BlockChiseled) {
	        return new SupportsAnalysisResult(
	          true,
              LocalStrings.ChiselSupportGenericNotSupported,
              LocalStrings.ChiselSupportIsAlreadyChiseled
            );
        }

		if ( forcedBlocks.containsKey( state.getBlock() ) )
		{
			final boolean forcing = forcedBlocks.get( state.getBlock() );
			return new SupportsAnalysisResult(
			  forcing,
              LocalStrings.ChiselSupportForcedUnsupported,
              LocalStrings.ChiselSupportForcedSupported
            );
		}

		final Block blk = state.getBlock();
		if ( supportedBlocks.containsKey( blk ) )
		{
			return supportedBlocks.get( blk );
		}

		if (blk.isIn(ModTags.Blocks.BLOCKED_CHISELABLE))
        {
            final SupportsAnalysisResult result = new SupportsAnalysisResult(
              false,
              LocalStrings.ChiselSupportTagBlackListed,
              LocalStrings.ChiselSupportTagWhitelisted
            );
            supportedBlocks.put(blk, result);
            return result;
        }

		if (blk.isIn(ModTags.Blocks.FORCED_CHISELABLE)) {
            final SupportsAnalysisResult result = new SupportsAnalysisResult(
              true,
              LocalStrings.ChiselSupportTagBlackListed,
              LocalStrings.ChiselSupportTagWhitelisted
            );
            supportedBlocks.put(blk, result);

		    final BlockBitInfo info = BlockBitInfo.createFromState( state );
		    stateBitInfo.put(state, info);

		    return result;
        }

		try
		{
			// require basic hardness behavior...
			final ReflectionHelperBlock pb = new ReflectionHelperBlock();
			final Class<? extends Block> blkClass = blk.getClass();

			// custom dropping behavior?
			pb.getDrops(state, null);
			final Class<?> wc = getDeclaringClass( blkClass, pb.MethodName, BlockState.class, LootContext.Builder.class );
			final boolean quantityDroppedTest = wc == Block.class || wc == AbstractBlock.class;

			final boolean isNotSlab = Item.getItemFromBlock( blk ) != Items.AIR;
			boolean itemExistsOrNotSpecialDrops = quantityDroppedTest || isNotSlab;

			// ignore blocks with custom collision.
			pb.getShape( null, null, null, null );
			Class<?> collisionClass = getDeclaringClass( blkClass, pb.MethodName, BlockState.class, IBlockReader.class, BlockPos.class, ISelectionContext.class );
			boolean noCustomCollision = collisionClass == Block.class || collisionClass == AbstractBlock.class || blk.getClass() == SlimeBlock.class;

			// full cube specifically is tied to lighting... so for glass
			// Compatibility use isFullBlock which can be true for glass.
			boolean isFullBlock = state.isSolid();
			final BlockBitInfo info = BlockBitInfo.createFromState( state );

			final boolean tickingBehavior = blk.ticksRandomly(state) && ChiselsAndBits.getConfig().getServer().blackListRandomTickingBlocks.get();
			boolean hasBehavior = ( blk.hasTileEntity( state ) || tickingBehavior );

			final boolean supportedMaterial = ModBlocks.convertGivenStateToChiseledBlock( state ) != null;

			final Boolean IgnoredLogic = ignoreLogicBlocks.get( blk );
			if ( blkClass.isAnnotationPresent( IgnoreBlockLogic.class ) || IgnoredLogic != null && IgnoredLogic )
			{
				isFullBlock = true;
				noCustomCollision = true;
				hasBehavior = false;
				itemExistsOrNotSpecialDrops = true;
			}

			if ( info.isCompatible && noCustomCollision && info.hardness >= -0.01f && isFullBlock && supportedMaterial && !hasBehavior && itemExistsOrNotSpecialDrops )
			{
                final SupportsAnalysisResult result = new SupportsAnalysisResult(
                  true,
                  LocalStrings.ChiselSupportGenericNotSupported,
                  (blkClass.isAnnotationPresent( IgnoreBlockLogic.class ) || IgnoredLogic != null && IgnoredLogic) ? LocalStrings.ChiselSupportLogicIgnored : LocalStrings.ChiselSupportGenericSupported
                );

                supportedBlocks.put( blk, result);
                stateBitInfo.put( state, info );
                return result;
			}

			if ( fluidBlocks.containsKey( blk ) )
			{
				stateBitInfo.put( state, info );

                final SupportsAnalysisResult result = new SupportsAnalysisResult(
                  true,
                  LocalStrings.ChiselSupportGenericNotSupported,
                  LocalStrings.ChiselSupportGenericFluidSupport
                );

				supportedBlocks.put( blk, result );
				return result;
			}

            SupportsAnalysisResult result = null;
			if (!info.isCompatible) {
			    result = new SupportsAnalysisResult(
			      false,
                  LocalStrings.ChiselSupportCompatDeactivated,
                  LocalStrings.ChiselSupportGenericSupported
                );
            }
            else if (!noCustomCollision) {
                result = new SupportsAnalysisResult(
                  false,
                  LocalStrings.ChiselSupportCustomCollision,
                  LocalStrings.ChiselSupportGenericSupported
                );
            }
            else if (info.hardness < -0.01f) {
                result = new SupportsAnalysisResult(
                  false,
                  LocalStrings.ChiselSupportNoHardness,
                  LocalStrings.ChiselSupportGenericSupported
                );
            }
            else if (!isNotSlab) {
                result = new SupportsAnalysisResult(
                  false,
                  LocalStrings.ChiselSupportIsSlab,
                  LocalStrings.ChiselSupportGenericSupported
                );
            }
            else if (!isFullBlock) {
                result = new SupportsAnalysisResult(
                  false,
                  LocalStrings.ChiselSupportNotFullBlock,
                  LocalStrings.ChiselSupportGenericSupported
                );
            }
            else if (hasBehavior) {
                result = new SupportsAnalysisResult(
                  false,
                  LocalStrings.ChiselSupportHasBehaviour,
                  LocalStrings.ChiselSupportGenericSupported
                );
            }
            else if (!quantityDroppedTest) {
                result = new SupportsAnalysisResult(
                  false,
                  LocalStrings.ChiselSupportHasCustomDrops,
                  LocalStrings.ChiselSupportGenericSupported
                );
            }

			supportedBlocks.put( blk, result );
			return result;
		}
		catch ( final Throwable t )
		{
		    final SupportsAnalysisResult result = new SupportsAnalysisResult(
              false,
              LocalStrings.ChiselSupportFailureToAnalyze,
              LocalStrings.ChiselSupportGenericSupported
            );
			// if the above test fails for any reason, then the block cannot be
			// supported.
			supportedBlocks.put( blk, result );
			return result;
		}
	}

    public static boolean isSupported(
      final BlockState state )
    {
        return doSupportAnalysis(state).isSupported();
    }

    public static class SupportsAnalysisResult {
	    private final boolean      supported;
	    private final LocalStrings unsupportedReason;
	    private final LocalStrings supportedReason;

        public SupportsAnalysisResult(final boolean supported, final LocalStrings unsupportedReason, final LocalStrings supportedReason) {
            this.supported = supported;
            this.unsupportedReason = unsupportedReason;
            this.supportedReason = supportedReason;
        }

        public boolean isSupported()
        {
            return supported;
        }

        public LocalStrings getUnsupportedReason()
        {
            return unsupportedReason;
        }

        public LocalStrings getSupportedReason()
        {
            return supportedReason;
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

	public final boolean isCompatible;
	public final float   hardness;
	public final float   explosionResistance;

	private BlockBitInfo(
			final boolean isCompatible,
			final float hardness,
			final float explosionResistance )
	{
		this.isCompatible = isCompatible;
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
			final Class<?> b_Class = getDeclaringClass( blkClass, reflectBlock.MethodName, BlockState.class, PlayerEntity.class, IBlockReader.class, BlockPos.class );
			final boolean test_b = b_Class == Block.class || b_Class == AbstractBlock.class;

			reflectBlock.getExplosionResistance();
			Class<?> exploResistanceClz = getDeclaringClass( blkClass, reflectBlock.MethodName);
			final boolean test_c = exploResistanceClz == Block.class || exploResistanceClz == AbstractBlock.class;

			reflectBlock.getExplosionResistance( null, null, null, null );
			exploResistanceClz = getDeclaringClass( blkClass, reflectBlock.MethodName, BlockState.class, IBlockReader.class, BlockPos.class, Explosion.class );
			final boolean test_d = exploResistanceClz == Block.class || exploResistanceClz == AbstractBlock.class || exploResistanceClz == null;

			final boolean isFluid = fluidStates.containsKey( ModUtil.getStateId( state ) );

			// is it perfect?
			if ( test_b && test_c && test_d && !isFluid )
			{
				final float blockHardness = state.getBlockHardness(new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO);
				final float resistance = blk.getExplosionResistance(state, new SingleBlockBlockReader(state, state.getBlock()), BlockPos.ZERO, new Explosion(null, null,null,
                  null, 0,1,0, 10, false, Explosion.Mode.NONE));

				return new BlockBitInfo( true, blockHardness, resistance );
			}
			else
			{
				// less accurate, we can just pretend they are some fixed
				// hardness... say like stone?

				final Block stone = Blocks.STONE;
				return new BlockBitInfo( ChiselsAndBits.getConfig().getServer().compatabilityMode.get(), 2f, 6f );
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
		return state.getBlock() instanceof BlockChiseled || isSupported( state );
	}

    public static boolean canChisel(
      final ItemStack stack )
    {
        if (stack.isEmpty())
            return false;

        if (stack.getItem() instanceof ItemBlockChiseled)
            return true;

        if (stack.getItem() instanceof BlockItem) {
            final BlockItem blockItem = (BlockItem) stack.getItem();
            final Block block = blockItem.getBlock();
            final BlockState blockState = block.getDefaultState();
            final BlockBitInfo.SupportsAnalysisResult result = BlockBitInfo.doSupportAnalysis(blockState);

            return result.supported;
        }

        return false;
    }

    public static boolean isChiseled(
      final ItemStack stack )
    {
        if (stack.isEmpty())
            return false;

        if (stack.getItem() instanceof ItemBlockChiseled)
            return true;

        return false;
    }
}
