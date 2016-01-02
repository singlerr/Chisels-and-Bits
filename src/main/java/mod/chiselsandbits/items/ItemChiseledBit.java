package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.ChiselTypeIterator;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitColors;
import mod.chiselsandbits.chiseledblock.data.IntegerBox;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.ChiselModeManager;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.helpers.ModUtil.ItemStackSlot;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.PacketChisel;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class ItemChiseledBit extends Item implements IItemScrollWheel, IChiselModeItem
{
	public ItemChiseledBit()
	{
		setHasSubtypes( true );
	}

	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List<String> tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpBit, tooltip );
	}

	@Override
	// 1.8.8 only hook.
	public String getHighlightTip(
			final ItemStack item,
			final String displayName )
	{
		if ( ChiselsAndBits.getConfig().itemNameModeDisplay )
		{
			return displayName + " - " + ChiselModeManager.getChiselMode( ChiselToolType.BIT ).string.getLocal();
		}

		return displayName;
	}

	@Override
	/**
	 * alter digging behavior to chisel, uses packets to enable server to stay
	 * in-sync.
	 */
	public boolean onBlockStartBreak(
			final ItemStack itemstack,
			final BlockPos pos,
			final EntityPlayer player )
	{
		return ItemChisel.fromBreakToChisel( ChiselModeManager.getChiselMode( ChiselToolType.BIT ), itemstack, pos, player );
	}

	@Override
	public String getItemStackDisplayName(
			final ItemStack stack )
	{
		ItemStack target = null;

		try
		{
			// for an unknown reason its possible to generate mod blocks without
			// proper state here...
			final IBlockState state = Block.getStateById( ItemChisel.getStackState( stack ) );
			final Block blk = state.getBlock();
			target = new ItemStack( blk, 1, blk.getMetaFromState( state ) );
		}
		catch ( final IllegalArgumentException e )
		{
		}

		if ( target == null || target.getItem() == null )
		{
			return super.getItemStackDisplayName( stack );
		}

		return new StringBuilder().append( super.getItemStackDisplayName( stack ) ).append( " - " ).append( target.getDisplayName() ).toString();
	}

	@Override
	public int getColorFromItemStack(
			final ItemStack stack,
			final int renderPass )
	{
		final IBlockState state = Block.getStateById( ItemChisel.getStackState( stack ) );
		return BitColors.getColorFor( state, renderPass );
	}

	final private static float HALF_16th = 0.5f / 16.0f;

	@Override
	public boolean onItemUse(
			final ItemStack stack,
			final EntityPlayer player,
			final World world,
			BlockPos pos,
			final EnumFacing side,
			float hitX,
			float hitY,
			float hitZ )
	{
		if ( !player.canPlayerEdit( pos, side, stack ) )
		{
			return false;
		}

		IBlockState blkstate = world.getBlockState( pos );

		final ChiselMode mode = ChiselModeManager.getChiselMode( ClientSide.instance.getHeldToolType() );

		TileEntityBlockChiseled tebc = ModUtil.getChiseledTileEntity( world, pos, true );
		ChiselTypeIterator connectedPlaneIterator = null;
		IntegerBox connectedBox = null;

		if ( tebc != null )
		{
			final VoxelBlob vx = tebc.getBlob();

			final int x = ItemChisel.getX( hitX, side );
			final int y = ItemChisel.getY( hitY, side );
			final int z = ItemChisel.getZ( hitZ, side );

			connectedPlaneIterator = new ChiselTypeIterator( VoxelBlob.dim, x, y, z, vx, mode, side );
			connectedBox = connectedPlaneIterator.getVoxelBox( vx );
		}
		else
		{
			final VoxelBlob vx = new VoxelBlob();
			vx.fill( Block.getStateId( blkstate ) );

			final int x = ItemChisel.getX( hitX, side );
			final int y = ItemChisel.getY( hitY, side );
			final int z = ItemChisel.getZ( hitZ, side );

			connectedPlaneIterator = new ChiselTypeIterator( VoxelBlob.dim, x, y, z, vx, mode, side );
			connectedBox = connectedPlaneIterator.getVoxelBox( vx );
		}

		hitX += side.getFrontOffsetX() * HALF_16th;
		hitY += side.getFrontOffsetY() * HALF_16th;
		hitZ += side.getFrontOffsetZ() * HALF_16th;

		if ( tebc == null || hitX < -0.001 || hitY < -0.001 || hitZ < -0.001 || hitX > 1.001 || hitY > 1.001 || hitZ > 1.001 )
		{
			pos = pos.offset( side );
			hitX -= side.getFrontOffsetX();
			hitY -= side.getFrontOffsetY();
			hitZ -= side.getFrontOffsetZ();

			if ( connectedBox != null )
			{
				connectedBox.minX -= side.getFrontOffsetX() * 16;
				connectedBox.maxX -= side.getFrontOffsetX() * 16;
				connectedBox.minY -= side.getFrontOffsetY() * 16;
				connectedBox.maxY -= side.getFrontOffsetY() * 16;
				connectedBox.minZ -= side.getFrontOffsetZ() * 16;
				connectedBox.maxZ -= side.getFrontOffsetZ() * 16;
			}

			blkstate = world.getBlockState( pos );
			tebc = ModUtil.getChiseledTileEntity( world, pos, true );
		}

		if ( tebc == null && BlockChiseled.replaceWithChisled( world, pos, blkstate, ItemChisel.getStackState( stack ) ) )
		{
			blkstate = world.getBlockState( pos );
			tebc = ModUtil.getChiseledTileEntity( world, pos, true );
		}

		if ( tebc != null )
		{
			if ( world.isRemote )
			{
				final int stateID = ItemChisel.getStackState( stack );

				// adjust voxel state...
				final VoxelBlob vb = tebc.getBlob();

				final int x = Math.min( 15, Math.max( 0, (int) ( vb.detail * hitX ) ) );
				final int y = Math.min( 15, Math.max( 0, (int) ( vb.detail * hitY ) ) );
				final int z = Math.min( 15, Math.max( 0, (int) ( vb.detail * hitZ ) ) );

				PacketChisel pc = null;

				switch ( mode )
				{
					case CONNECTED_PLANE:
						connectedBox.minX += side.getFrontOffsetX();
						connectedBox.maxX += side.getFrontOffsetX();
						connectedBox.minY += side.getFrontOffsetY();
						connectedBox.maxY += side.getFrontOffsetY();
						connectedBox.minZ += side.getFrontOffsetZ();
						connectedBox.maxZ += side.getFrontOffsetZ();
						pc = new PacketChisel( true, pos, connectedBox.minX, connectedBox.minY, connectedBox.minZ, connectedBox.maxX, connectedBox.maxY, connectedBox.maxZ, side, ChiselMode.DRAWN_REGION );
						break;
					default:
						pc = new PacketChisel( true, pos, x, y, z, side, mode );
						break;
				}

				final int result = pc.doAction( player );

				if ( result > 0 )
				{
					NetworkRouter.instance.sendToServer( pc );
					ClientSide.placeSound( world, pos, stateID );
				}
			}
			return false;
		}

		return true;
	}

	private ArrayList<ItemStack> bits;

	@SuppressWarnings( { "rawtypes", "unchecked" } )
	@Override
	public void getSubItems(
			final Item itemIn,
			final CreativeTabs tab,
			final List subItems )
	{
		if ( bits == null )
		{
			bits = new ArrayList<ItemStack>();

			final ArrayList<ItemStack> List = new ArrayList<ItemStack>();

			for ( final Object obj : Item.itemRegistry )
			{
				if ( !( obj instanceof ItemBlock ) )
				{
					continue;
				}

				try
				{
					Item it = (Item) obj;
					it.getSubItems( it, it.getCreativeTab(), List );

					for ( final ItemStack out : List )
					{
						it = out.getItem();

						if ( !( it instanceof ItemBlock ) )
						{
							continue;
						}

						final ItemBlock ib = (ItemBlock) it;
						final IBlockState state = ib.block.getStateFromMeta( out.getMetadata() );

						if ( state != null && BlockBitInfo.supportsBlock( state ) )
						{
							bits.add( ItemChiseledBit.createStack( Block.getStateId( state ), 1, false ) );
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
		}

		subItems.addAll( bits );
	}

	public static boolean sameBit(
			final ItemStack output,
			final int blk )
	{
		return output.hasTagCompound() ? output.getTagCompound().getInteger( "id" ) == blk : false;
	}

	public static ItemStack createStack(
			final int id,
			final int count,
			final boolean RequireStack )
	{
		if ( ChiselsAndBits.getItems().itemBlockBit == null )
		{
			if ( !RequireStack )
			{
				return null;
			}
		}

		final ItemStack out = new ItemStack( ChiselsAndBits.getItems().itemBlockBit, count );
		out.setTagInfo( "id", new NBTTagInt( id ) );
		return out;
	}

	@Override
	public void scroll(
			final EntityPlayer player,
			final ItemStack stack,
			final int dwheel )
	{
		final ChiselMode mode = ChiselModeManager.getChiselMode( ChiselToolType.BIT );
		ChiselModeManager.scrollOption( ChiselToolType.BIT, mode, mode, dwheel );
	}

	public static boolean placeBit(
			final IContinuousInventory bits,
			final EntityPlayer player,
			final VoxelBlob vb,
			final int x,
			final int y,
			final int z )
	{
		if ( vb.get( x, y, z ) == 0 )
		{
			final ItemStackSlot slot = bits.getItem( 0 );
			final int stateID = ItemChisel.getStackState( slot.getStack() );

			vb.set( x, y, z, stateID );

			if ( !player.capabilities.isCreativeMode )
			{
				bits.useItem( stateID );
			}

			return true;
		}

		return false;
	}
}
