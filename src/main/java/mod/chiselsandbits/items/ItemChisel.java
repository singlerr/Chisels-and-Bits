
package mod.chiselsandbits.items;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.ClientSide;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.network.NetworkRouter;
import mod.chiselsandbits.network.packets.ChiselPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Stopwatch;


public class ItemChisel extends ItemTool
{
	final private static float one_16th = 1.0f / 16.0f;
	private static ChiselMode clientChiselMode = ChiselMode.SINGLE;

	public ItemChisel(
			final ToolMaterial material )
	{
		super( 0.1F, material, Collections.emptySet() );
		setCreativeTab( ChiselsAndBits.creativeTab );

		// chisels are scaled up so that 1 stone chisel can mine one block.
		final long uses = material.getMaxUses() * ChiselsAndBits.instance.config.availableUsesMultiplier;
		setMaxDamage( ChiselsAndBits.instance.config.damageTools ? ( int ) Math.max( 0, Math.min( Short.MAX_VALUE, uses ) ) : 0 );
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
		ChiselsAndBits.instance.config.helpText( LocalStrings.HelpChisel, tooltip );
	}

	private static Stopwatch timer;

	public static void resetDelay()
	{
		timer = null;
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
		return ItemChisel.fromBreakToChisel( clientChiselMode, itemstack, pos, player );
	}

	static public boolean fromBreakToChisel(
			final ChiselMode mode,
			final ItemStack itemstack,
			final BlockPos pos,
			final EntityPlayer player )
	{
		if ( itemstack != null && ( timer == null || timer.elapsed( TimeUnit.MILLISECONDS ) > 100 ) )
		{
			timer = Stopwatch.createStarted();

			if ( !player.worldObj.isRemote )
			{
				return true;
			}

			final Pair<Vec3, Vec3> PlayerRay = ModUtil.getPlayerRay( player );
			final Vec3 a = PlayerRay.getLeft();
			final Vec3 b = PlayerRay.getRight();

			final MovingObjectPosition mop = player.worldObj.getBlockState( pos ).getBlock().collisionRayTrace( player.worldObj, pos, a, b );
			if ( mop != null && mop.typeOfHit == MovingObjectType.BLOCK )
			{
				useChisel( mode, player, player.worldObj, pos, mop.sideHit, ( float ) mop.hitVec.xCoord - pos.getX(), ( float ) mop.hitVec.yCoord - pos.getY(), ( float ) mop.hitVec.zCoord - pos.getZ() );
			}
		}

		return true;
	}

	public static void scrollOption(
			final ChiselMode originalMode,
			final int dwheel )
	{
		int offset = clientChiselMode.ordinal() + ( dwheel < 0 ? -1 : 1 );

		if ( offset >= ChiselMode.values().length )
		{
			offset = 0;
		}

		if ( offset < 0 )
		{
			offset = ChiselMode.values().length - 1;
		}

		clientChiselMode = ChiselMode.values()[offset];

		if ( clientChiselMode.isDisabled )
		{
			scrollOption( originalMode, dwheel );
		}
		else if ( originalMode != clientChiselMode )
		{
			Minecraft.getMinecraft().thePlayer.addChatComponentMessage( new ChatComponentTranslation( clientChiselMode.string.toString() ) );
		}
	}

	@Override
	public ItemStack onItemRightClick(
			final ItemStack itemStackIn,
			final World worldIn,
			final EntityPlayer playerIn )
	{
		if ( worldIn.isRemote )
		{
			scrollOption( getChiselMode(), playerIn.isSneaking() ? -1 : 1 );
		}

		return super.onItemRightClick( itemStackIn, worldIn, playerIn );
	}

	@Override
	/**
	 * switch chisel modes.
	 */
	public boolean onItemUseFirst(
			final ItemStack stack,
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if ( world.isRemote )
		{
			onItemRightClick( stack, world, player );
		}

		return true;
	}

	/**
	 * uses a chisel, this is called from onBlockStartBreak converts block, and handles everything short of modifying
	 * the voxel data.
	 *
	 * @param stack
	 * @param player
	 * @param world
	 * @param pos
	 * @param side
	 * @param hitX
	 * @param hitY
	 * @param hitZ
	 */
	static void useChisel(
			final ChiselMode mode,
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		final float One32ndf = 0.5f / VoxelBlob.dim;

		final int x = Math.min( 15, Math.max( 0, ( int ) ( VoxelBlob.dim * ( hitX - One32ndf * side.getFrontOffsetX() ) ) ) );
		final int y = Math.min( 15, Math.max( 0, ( int ) ( VoxelBlob.dim * ( hitY - One32ndf * side.getFrontOffsetY() ) ) ) );
		final int z = Math.min( 15, Math.max( 0, ( int ) ( VoxelBlob.dim * ( hitZ - One32ndf * side.getFrontOffsetZ() ) ) ) );

		final ChiselPacket pc = new ChiselPacket( pos, x, y, z, side, mode );

		final int extractedState = pc.doAction( player );
		if ( extractedState != 0 )
		{
			ClientSide.breakSound( world, pos, extractedState );

			NetworkRouter.instance.sendToServer( pc );
		}
	}

	/**
	 * Modifies VoxelData of TileEntityChiseled
	 *
	 * @param isCreative
	 * @param vb
	 * @param world
	 * @param pos
	 * @param side
	 * @param x
	 * @param y
	 * @param z
	 * @param output
	 * @return
	 */
	static public ItemStack chiselBlock(
			final boolean isCreative,
			final VoxelBlob vb,
			final World world,
			final BlockPos pos,
			final EnumFacing side,
			final int x,
			final int y,
			final int z,
			ItemStack output,
			final List<EntityItem> spawnlist )
	{
		final int blk = vb.get( x, y, z );
		if ( blk == 0 )
		{
			return output;
		}

		final boolean spawnBit = ChiselsAndBits.instance.itemBlockBit != null;
		if ( !world.isRemote && !isCreative )
		{
			double hitX = x * one_16th;
			double hitY = y * one_16th;
			double hitZ = z * one_16th;

			final double offset = 0.5;
			hitX += side.getFrontOffsetX() * offset;
			hitY += side.getFrontOffsetY() * offset;
			hitZ += side.getFrontOffsetZ() * offset;

			if ( output == null || !ItemChiseledBit.sameBit( output, blk ) || output.stackSize == 64 )
			{
				output = ItemChiseledBit.createStack( blk, 1 );// new ItemStack( srcItem, 1, blk );
				if ( spawnBit )
				{
					spawnlist.add( new EntityItem( world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, output ) );
				}
			}
			else
			{
				output.stackSize++;
			}
		}
		else
		{
			// return value...
			output = ItemChiseledBit.createStack( blk, 1 );
		}

		vb.clear( x, y, z );
		return output;
	}

	public static ChiselMode getChiselMode()
	{
		return clientChiselMode;
	}

	public static int getStackState(
			final ItemStack inHand )
	{
		return inHand != null && inHand.hasTagCompound() ? inHand.getTagCompound().getInteger( "id" ) : 0;
	}

}
