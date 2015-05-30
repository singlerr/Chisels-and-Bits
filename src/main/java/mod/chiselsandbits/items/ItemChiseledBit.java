
package mod.chiselsandbits.items;

import java.util.ArrayList;
import java.util.List;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.bitbag.BagInventory;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;


public class ItemChiseledBit extends Item
{
	public ItemChiseledBit()
	{
		setCreativeTab( ChiselsAndBits.creativeTab );
		setHasSubtypes( true );
	}

	@SuppressWarnings( { "rawtypes" } )
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.instance.config.helpText( "mod.chiselsandbits.help.bit", tooltip );
	}

	@Override
	/**
	 *  alter digging behavior to chisel, uses packets to enable server to stay in-sync.
	 */
	public boolean onBlockStartBreak(
			final ItemStack itemstack,
			final BlockPos pos,
			final EntityPlayer player )
	{
		return ItemChisel.fromBreakToChisel( ChiselMode.SINGLE, itemstack, pos, player );
	}

	@Override
	public String getItemStackDisplayName(
			final ItemStack stack )
	{
		final IBlockState state = Block.getStateById( ItemChisel.getStackState( stack ) );
		final Block blk = state.getBlock();

		final ItemStack target = new ItemStack( blk, 1, blk.getMetaFromState( state ) );

		if ( target.getItem() == null )
			return super.getItemStackDisplayName( stack );

		return new StringBuilder().append( super.getItemStackDisplayName( stack ) ).append( " - " ).append( target.getDisplayName() ).toString();
	}

	final private static float HALF_16th = 0.5f / 16.0f;

	@Override
	public boolean onItemUseFirst(
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
			return false;

		IBlockState blkstate = world.getBlockState( pos );
		Block blkObj = blkstate.getBlock();

		hitX += side.getFrontOffsetX() * HALF_16th;
		hitY += side.getFrontOffsetY() * HALF_16th;
		hitZ += side.getFrontOffsetZ() * HALF_16th;

		if ( !( blkObj instanceof BlockChiseled ) || hitX < -0.001 || hitY < -0.001 || hitZ < -0.001 || hitX > 1.001 || hitY > 1.001 || hitZ > 1.001 )
		{
			pos = pos.offset( side );
			hitX -= side.getFrontOffsetX();
			hitY -= side.getFrontOffsetY();
			hitZ -= side.getFrontOffsetZ();

			blkstate = world.getBlockState( pos );
			blkObj = blkstate.getBlock();
		}

		if ( BlockChiseled.replaceWithChisled( world, pos, blkstate, ItemChisel.getStackState( stack ) ) )
		{
			blkstate = world.getBlockState( pos );
			blkObj = blkstate.getBlock();
		}

		if ( blkObj instanceof BlockChiseled )
		{
			final TileEntity te = world.getTileEntity( pos );
			if ( te instanceof TileEntityBlockChiseled )
			{
				final TileEntityBlockChiseled tec = ( TileEntityBlockChiseled ) te;

				// adjust voxel state...
				final VoxelBlob vb = tec.getBlob();

				final int x = Math.min( 15, Math.max( 0, ( int ) ( vb.detail * hitX ) ) );
				final int y = Math.min( 15, Math.max( 0, ( int ) ( vb.detail * hitY ) ) );
				final int z = Math.min( 15, Math.max( 0, ( int ) ( vb.detail * hitZ ) ) );

				if ( vb.get( x, y, z ) == 0 )
				{
					final int stateID = ItemChisel.getStackState( stack );

					if ( world.isRemote )
						ClientSide.placeSound( world, pos, stateID );

					vb.set( x, y, z, stateID );
					tec.setBlob( vb );

					if ( !player.capabilities.isCreativeMode )
						stack.stackSize--;

					final IInventory inv = player.inventory;
					for ( int zz = 0; zz < inv.getSizeInventory(); zz++ )
					{
						final ItemStack which = inv.getStackInSlot( zz );
						if ( which != null && which.getItem() instanceof ItemBitBag )
							new BagInventory( which ).restockItem( stack );
					}

					return false;
				}
			}
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
					continue;

				try
				{
					Item it = ( Item ) obj;
					it.getSubItems( it, it.getCreativeTab(), List );

					for ( final ItemStack out : List )
					{
						it = out.getItem();

						if ( !( it instanceof ItemBlock ) )
							continue;

						final ItemBlock ib = ( ItemBlock ) it;
						final IBlockState state = ib.block.getStateFromMeta( ItemChisel.getStackState( out ) );

						if ( state != null && BlockChiseled.supportsBlock( state ) )
							bits.add( ItemChiseledBit.createStack( Block.getStateId( state ), 1 ) );
					}

				}
				catch ( final Throwable t )
				{
					// a mod did something that isn't acceptable, let them crash in their own code...
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
			final int count )
	{
		final ItemStack out = new ItemStack( ChiselsAndBits.instance.itemBlockBit, count );
		out.setTagInfo( "id", new NBTTagInt( id ) );
		return out;
	}
}
