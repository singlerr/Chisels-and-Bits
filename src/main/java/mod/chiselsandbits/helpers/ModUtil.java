package mod.chiselsandbits.helpers;

import org.apache.commons.lang3.tuple.Pair;

import mod.chiselsandbits.ChiselMode;
import mod.chiselsandbits.chiseledblock.ItemBlockChiseled;
import mod.chiselsandbits.chiseledblock.TileEntityBlockChiseled;
import mod.chiselsandbits.chiseledblock.data.IntegerBox;
import mod.chiselsandbits.integration.Integration;
import mod.chiselsandbits.items.ItemChisel;
import mod.chiselsandbits.items.ItemChiseledBit;
import mod.chiselsandbits.items.ItemNegativePrint;
import mod.chiselsandbits.items.ItemPositivePrint;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ModUtil
{

	static public EnumFacing getPlaceFace(
			final EntityLivingBase placer )
	{
		return EnumFacing.getHorizontal( MathHelper.floor_double( placer.rotationYaw * 4.0F / 360.0F + 0.5D ) & 3 ).getOpposite();
	}

	static public Pair<Vec3, Vec3> getPlayerRay(
			final EntityPlayer playerIn )
	{
		double reachDistance = 5.0d;

		final double x = playerIn.prevPosX + ( playerIn.posX - playerIn.prevPosX );
		final double y = playerIn.prevPosY + ( playerIn.posY - playerIn.prevPosY ) + playerIn.getEyeHeight();
		final double z = playerIn.prevPosZ + ( playerIn.posZ - playerIn.prevPosZ );

		final float playerPitch = playerIn.prevRotationPitch + ( playerIn.rotationPitch - playerIn.prevRotationPitch );
		final float playerYaw = playerIn.prevRotationYaw + ( playerIn.rotationYaw - playerIn.prevRotationYaw );

		final float yawRayX = MathHelper.sin( -playerYaw * 0.017453292f - (float) Math.PI );
		final float yawRayZ = MathHelper.cos( -playerYaw * 0.017453292f - (float) Math.PI );

		final float pitchMultiplier = -MathHelper.cos( -playerPitch * 0.017453292F );
		final float eyeRayY = MathHelper.sin( -playerPitch * 0.017453292F );
		final float eyeRayX = yawRayX * pitchMultiplier;
		final float eyeRayZ = yawRayZ * pitchMultiplier;

		if ( playerIn instanceof EntityPlayerMP )
		{
			reachDistance = ( (EntityPlayerMP) playerIn ).theItemInWorldManager.getBlockReachDistance();
		}

		final Vec3 from = new Vec3( x, y, z );
		final Vec3 to = from.addVector( eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance );

		return Pair.of( from, to );
	}

	static public class ItemStackSlot
	{
		private final IInventory inv;
		private final int slot;
		private final ItemStack stack;
		private final boolean isCreative;
		private final int toolSlot;

		ItemStackSlot(
				final IInventory i,
				final int s,
				final ItemStack st,
				final EntityPlayer player )
		{
			inv = i;
			slot = s;
			stack = st;
			toolSlot = player.inventory.currentItem;
			isCreative = player.capabilities.isCreativeMode;
		}

		public boolean isValid()
		{
			return isCreative || stack != null && stack.stackSize > 0;
		}

		public void damage(
				final EntityPlayer who )
		{
			if ( isCreative )
			{
				return;
			}

			stack.damageItem( 1, who );
			if ( stack.stackSize <= 0 )
			{
				net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem( who, stack );
				inv.setInventorySlotContents( slot, null );
			}
		}

		public void consume()
		{
			if ( isCreative )
			{
				return;
			}

			stack.stackSize--;
			if ( stack.stackSize <= 0 )
			{
				inv.setInventorySlotContents( slot, null );
			}
		}

		public ItemStack getStack()
		{
			return stack;
		}

		public void swapWithWeapon()
		{
			final ItemStack it = inv.getStackInSlot( toolSlot );
			inv.setInventorySlotContents( toolSlot, inv.getStackInSlot( slot ) );
			inv.setInventorySlotContents( slot, it );
		}
	};

	static public ItemStackSlot findBit(
			final EntityPlayer who,
			final int StateID )
	{
		final ItemStack inHand = who.getCurrentEquippedItem();
		if ( inHand != null && inHand.stackSize > 0 && inHand.getItem() instanceof ItemChiseledBit && ItemChisel.getStackState( inHand ) == StateID )
		{
			return new ItemStackSlot( who.inventory, who.inventory.currentItem, inHand, who );
		}

		for ( int x = 0; x < who.inventory.getSizeInventory(); x++ )
		{
			final ItemStack is = who.inventory.getStackInSlot( x );
			if ( is != null && is.stackSize > 0 && is.getItem() instanceof ItemChiseledBit && ItemChiseledBit.sameBit( is, StateID ) )
			{
				return new ItemStackSlot( who.inventory, x, is, who );
			}
		}

		return new ItemStackSlot( null, -1, null, who );
	}

	public static ChiselMode isHoldingChiselTool(
			final EntityPlayer player )
	{
		final ItemStack inHand = player.getCurrentEquippedItem();

		if ( inHand != null && inHand.getItem() instanceof ItemChiseledBit )
		{
			return ChiselMode.SINGLE;
		}

		if ( inHand != null && inHand.getItem() instanceof ItemChisel )
		{
			return ItemChisel.getChiselMode();
		}

		return null;
	}

	public static boolean isHoldingPattern(
			final EntityPlayer player )
	{
		final ItemStack inHand = player.getCurrentEquippedItem();

		if ( inHand != null && inHand.getItem() instanceof ItemPositivePrint )
		{
			return true;
		}

		if ( inHand != null && inHand.getItem() instanceof ItemNegativePrint )
		{
			return true;
		}

		return false;
	}

	public static boolean isHoldingChiseledBlock(
			final EntityPlayer player )
	{
		final ItemStack inHand = player.getCurrentEquippedItem();

		if ( inHand != null && inHand.getItem() instanceof ItemBlockChiseled )
		{
			return true;
		}

		return false;
	}

	public static int getRotationIndex(
			final EnumFacing face )
	{
		return face.getHorizontalIndex();
	}

	public static int getRotations(
			final EntityLivingBase placer,
			final byte side )
	{
		final EnumFacing newFace = ModUtil.getPlaceFace( placer );
		final EnumFacing oldYaw = EnumFacing.VALUES[side];

		int rotations = getRotationIndex( newFace ) - getRotationIndex( oldYaw );

		// work out the rotation math...
		while ( rotations < 0 )
		{
			rotations = 4 + rotations;
		}
		while ( rotations > 4 )
		{
			rotations = rotations - 4;
		}

		return 4 - rotations;
	}

	public static BlockPos getPartialOffset(
			final EnumFacing side,
			final BlockPos partial,
			final IntegerBox modelBounds )
	{
		int offset_x = modelBounds.minX;
		int offset_y = modelBounds.minY;
		int offset_z = modelBounds.minZ;

		final int partial_x = partial.getX();
		final int partial_y = partial.getY();
		final int partial_z = partial.getZ();

		int middle_x = ( modelBounds.maxX - modelBounds.minX ) / -2;
		int middle_y = ( modelBounds.maxY - modelBounds.minY ) / -2;
		int middle_z = ( modelBounds.maxZ - modelBounds.minZ ) / -2;

		switch ( side )
		{
			case DOWN:
				offset_y = modelBounds.maxY;
				middle_y = 0;
				break;
			case EAST:
				offset_x = modelBounds.minX;
				middle_x = 0;
				break;
			case NORTH:
				offset_z = modelBounds.maxZ;
				middle_z = 0;
				break;
			case SOUTH:
				offset_z = modelBounds.minZ;
				middle_z = 0;
				break;
			case UP:
				offset_y = modelBounds.minY;
				middle_y = 0;
				break;
			case WEST:
				offset_x = modelBounds.maxX;
				middle_x = 0;
				break;
			default:
				throw new NullPointerException();
		}

		final int t_x = -offset_x + middle_x + partial_x;
		final int t_y = -offset_y + middle_y + partial_y;
		final int t_z = -offset_z + middle_z + partial_z;

		return new BlockPos( t_x, t_y, t_z );
	}

	static public <T> T firstNonNull(
			final T... options )
	{
		for ( final T i : options )
		{
			if ( i != null )
			{
				return i;
			}
		}

		throw new NullPointerException( "Unable to find a non null item." );
	}

	public static TileEntityBlockChiseled getChiseledTileEntity(
			final World world,
			final BlockPos pos )
	{
		final TileEntity te = world.getTileEntity( pos );

		if ( te instanceof TileEntityBlockChiseled )
		{
			return (TileEntityBlockChiseled) te;
		}

		return Integration.mcmp.getChiseledTileEntity( te );
	}

	public static void removeChisledBlock(
			final World world,
			final BlockPos pos )
	{
		final TileEntity te = world.getTileEntity( pos );

		if ( te instanceof TileEntityBlockChiseled )
		{
			world.setBlockToAir( pos ); // no physical matter left...
			return;
		}

		Integration.mcmp.removeChisledBlock( te );
	}

}
