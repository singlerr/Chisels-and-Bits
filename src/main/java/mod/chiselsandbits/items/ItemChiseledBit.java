package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.bittank.BlockBitTank;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.BlockChiseled.ReplaceWithChisledValue;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.core.Log;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.BitOperation;
import mod.chiselsandbits.helpers.ChiselModeManager;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.DeprecationHelper;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.IItemInInventory;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.ICacheClearable;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.items.ItemBitBag.BagPos;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.network.packets.PacketChisel;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.*;
import net.minecraft.nbt.IntNBT;
import net.minecraft.state.Property;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.*;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemChiseledBit extends Item implements IItemScrollWheel, IChiselModeItem, ICacheClearable
{

	public static boolean bitBagStackLimitHack;

	private ArrayList<ItemStack> bits;

	public ItemChiseledBit(Item.Properties properties)
	{
		super(properties);
		ChiselsAndBits.getInstance().addClearable( this );
	}

	@Override
	@OnlyIn( Dist.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<ITextComponent> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpBit, tooltip,
				ClientSide.instance.getKeyName( Minecraft.getInstance().gameSettings.keyBindAttack ),
				ClientSide.instance.getKeyName( Minecraft.getInstance().gameSettings.keyBindUseItem ),
				ClientSide.instance.getModeKey() );
	}

    @Override
    public ITextComponent getHighlightTip(final ItemStack item, final ITextComponent displayName)
    {
        return DistExecutor.unsafeRunForDist(() -> () -> {
            if ( ChiselsAndBits.getConfig().itemNameModeDisplay && displayName instanceof IFormattableTextComponent)
            {
                String extra = "";
                if ( getBitOperation( ClientSide.instance.getPlayer(), Hand.MAIN_HAND, item ) == BitOperation.REPLACE )
                {
                    extra = " - " + LocalStrings.BitOptionReplace.getLocal();
                }

                final IFormattableTextComponent comp = (IFormattableTextComponent) displayName;

                return comp.appendString(" - ").append(new StringTextComponent(ChiselModeManager.getChiselMode( ClientSide.instance.getPlayer(), ChiselToolType.BIT, Hand.MAIN_HAND ).getName().getLocal())).append(new StringTextComponent(extra));
            }

            return displayName;
        },
          () -> () -> displayName);
    }

	@Override
	/**
	 * alter digging behavior to chisel, uses packets to enable server to stay
	 * in-sync.
	 */
	public boolean onBlockStartBreak(
			final ItemStack itemstack,
			final BlockPos pos,
			final PlayerEntity player )
	{
		return ItemChisel.fromBreakToChisel( ChiselMode.castMode( ChiselModeManager.getChiselMode( player, ChiselToolType.BIT, Hand.MAIN_HAND ) ), itemstack, pos, player, Hand.MAIN_HAND );
	}

	public static ITextComponent getBitStateName(
			final BlockState state )
	{
		ItemStack target = null;
		Block blk = null;

		if ( state == null )
		{
			return new StringTextComponent("Null");
		}

		try
		{
			// for an unknown reason its possible to generate mod blocks without
			// proper state here...
			blk = state.getBlock();

			final Item item = Item.getItemFromBlock( blk );
			if ( ModUtil.isEmpty( item ) )
			{
				final Fluid f = BlockBitInfo.getFluidFromBlock( blk );
				if ( f != null )
				{
				    //TODO: Fix this: I need a way to get the fluid name.
					return new StringTextComponent(f.getRegistryName().getPath());
				}
			}
			else
			{
				target = new ItemStack(() -> Item.getItemFromBlock(state.getBlock()) ,1);
			}
		}
		catch ( final IllegalArgumentException e )
		{
			Log.logError( "Unable to get Item Details for Bit.", e );
		}

		if ( target == null || target.getItem() == null )
		{
			return null;
		}

		try
		{
			final ITextComponent myName = target.getDisplayName();
            if (!(myName instanceof IFormattableTextComponent))
                return myName;

            final IFormattableTextComponent formattableName = (IFormattableTextComponent) myName;

			final Set<String> extra = new HashSet<String>();
			if ( blk != null && state != null )
			{
				for ( final Property<?> p : state.getProperties() )
				{
					if ( p.getName().equals( "axis" ) || p.getName().equals( "facing" ) )
					{
						extra.add( DeprecationHelper.translateToLocal( "mod.chiselsandbits.pretty." + p.getName() + "-" + state.get( p ).toString() ) );
					}
				}
			}

			if ( extra.isEmpty() )
			{
				return myName;
			}

			for ( final String x : extra )
			{
				formattableName.appendString(" ").appendString( x );
			}

			return formattableName;
		}
		catch ( final Exception e )
		{
			return new StringTextComponent("Error");
		}
	}

	public static ITextComponent getBitTypeName(
			final ItemStack stack )
	{
		return getBitStateName( ModUtil.getStateById( ItemChiseledBit.getStackState( stack ) ) );
	}

	@Override
	public ITextComponent getDisplayName(
			final ItemStack stack )
	{
		final ITextComponent typeName = getBitTypeName( stack );

		if ( typeName == null )
		{
			return super.getDisplayName( stack );
		}

		final IFormattableTextComponent strComponent = new StringTextComponent("");
		return strComponent.append(super.getDisplayName( stack ))
          .appendString(" - ")
          .append(typeName);
	}



	@SuppressWarnings( "deprecation" )
	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return bitBagStackLimitHack ? ChiselsAndBits.getConfig().bagStackSize : super.getItemStackLimit(stack);
	}

    @Override
    public ActionResultType onItemUse(final ItemUseContext context)
    {
        return super.onItemUse(context);
    }

	public ActionResultType onItemUseInternal (
			final @Nonnull PlayerEntity player,
			final @Nonnull World world,
			final @Nonnull BlockPos usedBlock,
			final @Nonnull Hand hand,
            final @Nonnull BlockRayTraceResult rayTraceResult)
	{
		final ItemStack stack = player.getHeldItem( hand );

		if ( !player.canPlayerEdit( usedBlock, rayTraceResult.getFace(), stack ) )
		{
			return ActionResultType.FAIL;
		}

		// forward interactions to tank...
		final BlockState usedState = world.getBlockState( usedBlock );
		final Block blk = usedState.getBlock();
		if ( blk instanceof BlockBitTank )
		{
			if ( blk.onBlockActivated( usedState, world, usedBlock, player, hand, rayTraceResult) == ActionResultType.SUCCESS)
			{
				return ActionResultType.SUCCESS;
			}
			return ActionResultType.FAIL;
		}

		if ( world.isRemote )
		{
			final IToolMode mode = ChiselModeManager.getChiselMode( player, ClientSide.instance.getHeldToolType( hand ), hand );
			final BitLocation bitLocation = new BitLocation( rayTraceResult, false, getBitOperation( player, hand, stack ) );

			BlockState blkstate = world.getBlockState( bitLocation.blockPos );
			TileEntityBlockChiseled tebc = ModUtil.getChiseledTileEntity( world, bitLocation.blockPos, true );
			ReplaceWithChisledValue rv = null;
			if ( tebc == null && (rv=BlockChiseled.replaceWithChisled( world, bitLocation.blockPos, blkstate, ItemChiseledBit.getStackState( stack ), true )).success )
			{
				blkstate = world.getBlockState( bitLocation.blockPos );
				tebc = rv.te;
			}

			if ( tebc != null )
			{
				PacketChisel pc = null;
				if ( mode == ChiselMode.DRAWN_REGION )
				{
					if ( world.isRemote )
					{
						ClientSide.instance.pointAt( getBitOperation( player, hand, stack ).getToolType(), bitLocation, hand );
					}
					return ActionResultType.FAIL;
				}
				else
				{
					pc = new PacketChisel( getBitOperation( player, hand, stack ), bitLocation, rayTraceResult.getFace(), ChiselMode.castMode( mode ), hand );
				}

				final int result = pc.doAction( player );

				if ( result > 0 )
				{
				    ChiselsAndBits.getNetworkChannel().sendToServer(pc);
				}
			}
		}

		return ActionResultType.SUCCESS;

	}

    @Override
    public boolean canHarvestBlock(final ItemStack stack, final BlockState state)
    {
        return state.getBlock() instanceof BlockChiseled || super.canHarvestBlock( stack, state);
    }

	@Override
	public boolean canHarvestBlock(
			final BlockState blk )
	{
		return blk.getBlock() instanceof BlockChiseled || super.canHarvestBlock( blk );
	}

	public static BitOperation getBitOperation(
			final PlayerEntity player,
			final Hand hand,
			final ItemStack stack )
	{
		return ChiselsAndBits.getConfig().replaceingBits ? BitOperation.REPLACE : BitOperation.PLACE;
	}

	@Override
	public void clearCache()
	{
		bits = null;
	}

    @Override
    public void fillItemGroup(final ItemGroup tab, final NonNullList<ItemStack> items)
    {
        if ( !this.isInGroup( tab ) ) // is this my creative tab?
        {
            return;
        }

        if ( bits == null )
        {
            bits = new ArrayList<ItemStack>();

            final NonNullList<ItemStack> List = NonNullList.create();
            final BitSet used = new BitSet( 4096 );

            for ( final Object obj : ForgeRegistries.ITEMS)
            {
                if ( !( obj instanceof BlockItem ) )
                {
                    continue;
                }

                try
                {
                    Item it = (Item) obj;
                    final ItemGroup ctab = it.getGroup();

                    if ( ctab != null )
                    {
                        it.fillItemGroup( ctab, List );
                    }

                    for ( final ItemStack out : List )
                    {
                        it = out.getItem();

                        if ( !( it instanceof BlockItem ) )
                        {
                            continue;
                        }

                        final BlockState state = DeprecationHelper.getStateFromItem( out );
                        if ( state != null && BlockBitInfo.supportsBlock( state ) )
                        {
                            used.set( ModUtil.getStateId( state ) );
                            bits.add( ItemChiseledBit.createStack( ModUtil.getStateId( state ), 1, false ) );
                        }
                    }

                }
                catch ( final Throwable t )
                {
                    // a mod did something that isn't acceptable, let them crash
                    // in their own code...
                }

                List.clear();
            }

            for ( final Fluid o : ForgeRegistries.FLUIDS )
            {
                if ( used.get( Block.getStateId( o.getDefaultState().getBlockState() ) ) )
                {
                    continue;
                }

                bits.add( ItemChiseledBit.createStack( Block.getStateId( o.getDefaultState().getBlockState() ), 1, false ) );
            }
        }

        items.addAll( bits );
    }

	public static boolean sameBit(
			final ItemStack output,
			final int blk )
	{
		return output.hasTag() ? getStackState( output ) == blk : false;
	}

	public static @Nonnull ItemStack createStack(
			final int id,
			final int count,
			final boolean RequireStack )
	{
		if ( ChiselsAndBits.getItems().itemBlockBit == null )
		{
			if ( !RequireStack )
			{
				return ModUtil.getEmptyStack();
			}
		}

		final ItemStack out = new ItemStack( ChiselsAndBits.getItems().itemBlockBit, count );
		out.setTagInfo( "id", IntNBT.valueOf(id) );
		return out;
	}

	@Override
	public void scroll(
			final PlayerEntity player,
			final ItemStack stack,
			final int dwheel )
	{
		final IToolMode mode = ChiselModeManager.getChiselMode( player, ChiselToolType.BIT, Hand.MAIN_HAND );
		ChiselModeManager.scrollOption( ChiselToolType.BIT, mode, mode, dwheel );
	}

	public static int getStackState(
			final ItemStack inHand )
	{
		return inHand != null && inHand.hasTag() ? ModUtil.getTagCompound( inHand ).getInt( "id" ) : 0;
	}

	public static boolean placeBit(
			final IContinuousInventory bits,
			final ActingPlayer player,
			final VoxelBlob vb,
			final int x,
			final int y,
			final int z )
	{
		if ( vb.get( x, y, z ) == 0 )
		{
			final IItemInInventory slot = bits.getItem( 0 );
			final int stateID = ItemChiseledBit.getStackState( slot.getStack() );

			if ( slot.isValid() )
			{
				if ( !player.isCreative() )
				{
					if ( bits.useItem( stateID ) )
						vb.set( x, y, z, stateID );
				}
				else
					vb.set( x, y, z, stateID );
			}

			return true;
		}

		return false;
	}

	public static boolean hasBitSpace(
			final PlayerEntity player,
			final int blk )
	{
		final List<BagPos> bags = ItemBitBag.getBags( player.inventory );
		for ( final BagPos bp : bags )
		{
			for ( int x = 0; x < bp.inv.getSizeInventory(); x++ )
			{
				final ItemStack is = bp.inv.getStackInSlot( x );
				if( ( ItemChiseledBit.sameBit( is, blk ) && ModUtil.getStackSize( is ) < bp.inv.getInventoryStackLimit() ) || ModUtil.isEmpty( is ) )
				{
					return true;
				}
			}
		}
		for ( int x = 0; x < 36; x++ )
		{
			final ItemStack is = player.inventory.getStackInSlot( x );
			if( ( ItemChiseledBit.sameBit( is, blk ) && ModUtil.getStackSize( is ) < is.getMaxStackSize() ) || ModUtil.isEmpty( is ) )
			{
				return true;
			}
		}
		return false;
	}

	private static Stopwatch timer;

	public static boolean checkRequiredSpace(
			final PlayerEntity player,
			final BlockState blkstate) {
		if ( ChiselsAndBits.getConfig().requireBagSpace && !player.isCreative() )
		{
			//Cycle every item in any bag, if the player can't store the clicked block then
			//send them a message.
			final int stateId = ModUtil.getStateId( blkstate );
			if ( !ItemChiseledBit.hasBitSpace( player, stateId ) )
			{
				if( player.getEntityWorld().isRemote && ( timer == null || timer.elapsed( TimeUnit.MILLISECONDS ) > 1000 ) )
				{
					//Timer is client-sided so it doesn't have to be made player-specific
					timer = Stopwatch.createStarted();
					//Only client should handle messaging.
					player.sendMessage( new TranslationTextComponent( "mod.chiselsandbits.result.require_bag" ), Util.DUMMY_UUID );
				}
				return true;
			}
		}
		return false;
	}
}
