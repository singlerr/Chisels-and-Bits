package mod.chiselsandbits.items;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselMode;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
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
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class ItemChisel extends ItemTool implements IItemScrollWheel, IChiselModeItem
{
	final private static float one_16th = 1.0f / 16.0f;

	public ItemChisel(
			final ToolMaterial material )
	{
		super( 0.1F, material, new HashSet<Block>() );

		// chisels are scaled up so that 1 stone chisel can mine one block.
		final long uses = material.getMaxUses() * ChiselsAndBits.getConfig().availableUsesMultiplier;
		setMaxDamage( ChiselsAndBits.getConfig().damageTools ? (int) Math.max( 0, uses ) : 0 );
	}

	public ToolMaterial whatMaterial()
	{
		return toolMaterial;
	}

	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List<String> tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpChisel, tooltip, ClientSide.instance.getModeKey() );
	}

	private static Stopwatch timer;

	public static void resetDelay()
	{
		timer = null;
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
		return ItemChisel.fromBreakToChisel( ChiselModeManager.getChiselMode( ChiselToolType.CHISEL ), itemstack, pos, player );
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
			if ( mode == ChiselMode.DRAWN_REGION )
			{
				final Pair<Vec3, Vec3> PlayerRay = ModUtil.getPlayerRay( player );
				final Vec3 a = PlayerRay.getLeft();
				final Vec3 b = PlayerRay.getRight();

				final MovingObjectPosition mop = player.worldObj.getBlockState( pos ).getBlock().collisionRayTrace( player.worldObj, pos, a, b );
				if ( mop != null && mop.typeOfHit == MovingObjectType.BLOCK )
				{
					final BitLocation loc = new BitLocation( mop, true, ChiselToolType.CHISEL );
					ClientSide.instance.pointAt( ChiselToolType.CHISEL, loc );
					return true;
				}

				return true;
			}

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
				useChisel( mode, player, player.worldObj, pos, mop.sideHit, (float) ( mop.hitVec.xCoord - pos.getX() ), (float) ( mop.hitVec.yCoord - pos.getY() ), (float) ( mop.hitVec.zCoord - pos.getZ() ) );
			}
		}

		return true;
	}

	@Override
	public String getHighlightTip(
			final ItemStack item,
			final String displayName )
	{
		if ( ChiselsAndBits.getConfig().itemNameModeDisplay )
		{
			if ( ChiselsAndBits.getConfig().perChiselMode )
			{
				return displayName + " - " + ChiselMode.getMode( item ).string.getLocal();
			}
			else
			{
				return displayName + " - " + ChiselModeManager.getChiselMode( ChiselToolType.CHISEL ).string.getLocal();
			}
		}

		return displayName;
	}

	@Override
	public ItemStack onItemRightClick(
			final ItemStack itemStackIn,
			final World worldIn,
			final EntityPlayer playerIn )
	{
		if ( worldIn.isRemote )
		{
			final ChiselMode mode = ChiselModeManager.getChiselMode( ChiselToolType.CHISEL );
			ChiselModeManager.scrollOption( ChiselToolType.CHISEL, mode, mode, playerIn.isSneaking() ? -1 : 1 );
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
	 * uses a chisel, this is called from onBlockStartBreak converts block, and
	 * handles everything short of modifying the voxel data.
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
		final BitLocation location = new BitLocation( new MovingObjectPosition( MovingObjectType.BLOCK, new Vec3( hitX, hitY, hitZ ), side, pos ), false, ChiselToolType.CHISEL );

		final PacketChisel pc = new PacketChisel( false, location, side, mode );

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
	 * @param selected
	 *
	 * @param player
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
			final IContinuousInventory selected,
			final EntityPlayer player,
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
		final boolean isCreative = player.capabilities.isCreativeMode;

		final int blk = vb.get( x, y, z );
		if ( blk == 0 )
		{
			return output;
		}

		if ( !canMine( selected, Block.getStateById( blk ), player, world, pos ) )
		{
			return output;
		}

		selected.useItem( blk );

		final boolean spawnBit = ChiselsAndBits.getItems().itemBlockBit != null;
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
				output = ItemChiseledBit.createStack( blk, 1, true );// new
				// ItemStack(
				// srcItem,
				// 1,
				// blk
				// );

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
			output = ItemChiseledBit.createStack( blk, 1, true );
		}

		vb.clear( x, y, z );
		return output;
	}

	public static int getStackState(
			final ItemStack inHand )
	{
		return inHand != null && inHand.hasTagCompound() ? inHand.getTagCompound().getInteger( "id" ) : 0;
	}

	private static boolean testingChisel = false;

	public static boolean canMine(
			final IContinuousInventory chiselInv,
			final IBlockState state,
			final EntityPlayer player,
			final World world,
			final BlockPos pos )
	{
		final int targetState = Block.getStateId( state );
		ItemStackSlot chiselSlot = chiselInv.getItem( targetState );
		ItemStack chisel = chiselSlot.getStack();

		if ( player.capabilities.isCreativeMode )
		{
			return true;
		}

		if ( chisel == null )
		{
			return false;
		}

		if ( ChiselsAndBits.getConfig().enableChiselToolHarvestCheck )
		{
			// this is the earily check.
			if ( state.getBlock() instanceof BlockChiseled )
			{
				return true;
			}

			do
			{

				final Block blk = world.getBlockState( pos ).getBlock();
				BlockChiseled.setActingAs( state );
				testingChisel = true;
				chiselSlot.swapWithWeapon();
				final boolean canHarvest = blk.canHarvestBlock( world, pos, player );
				chiselSlot.swapWithWeapon();
				testingChisel = false;
				BlockChiseled.setActingAs( null );

				if ( canHarvest )
				{
					return true;
				}

				chiselInv.fail( targetState );

				chiselSlot = chiselInv.getItem( targetState );
				chisel = chiselSlot.getStack();
			}
			while ( chisel != null );

			return false;
		}

		return true;
	}

	private static final String DAMAGE_KEY = "damage";

	@Override
	public int getDamage(
			final ItemStack stack )
	{
		return Math.max( getMetadata( stack ), getNBT( stack ).getInteger( DAMAGE_KEY ) );
	}

	@Override
	public boolean isDamaged(
			final ItemStack stack )
	{
		return getDamage( stack ) > 0;
	}

	@Override
	public void setDamage(
			final ItemStack stack,
			int damage )
	{
		if ( damage < 0 )
		{
			damage = 0;
		}

		getNBT( stack ).setInteger( DAMAGE_KEY, damage );
	}

	private NBTTagCompound getNBT(
			final ItemStack stack )
	{
		if ( !stack.hasTagCompound() )
		{
			stack.setTagCompound( new NBTTagCompound() );
		}

		return stack.getTagCompound();
	}

	@Override
	public boolean canHarvestBlock(
			final Block blk )
	{
		Item it;

		switch ( getToolMaterial() )
		{
			case EMERALD:
				it = Items.diamond_pickaxe;
				break;
			case GOLD:
				it = Items.golden_pickaxe;
				break;
			case IRON:
				it = Items.iron_pickaxe;
				break;
			default:
			case STONE:
				it = Items.stone_pickaxe;
				break;
			case WOOD:
				it = Items.wooden_pickaxe;
				break;
		}

		return blk instanceof BlockChiseled || it.canHarvestBlock( blk );
	}

	@Override
	public int getHarvestLevel(
			final ItemStack stack,
			final String toolClass )
	{
		if ( testingChisel && stack.getItem() instanceof ItemChisel )
		{
			final String pattern = "(^|,)" + Pattern.quote( toolClass ) + "(,|$)";

			final Pattern p = Pattern.compile( pattern );
			final Matcher m = p.matcher( ChiselsAndBits.getConfig().enableChiselToolHarvestCheckTools );

			if ( m.find() )
			{
				final ItemChisel ic = (ItemChisel) stack.getItem();
				return ic.toolMaterial.getHarvestLevel();
			}
		}

		return super.getHarvestLevel( stack, toolClass );
	}

	@Override
	public void scroll(
			final EntityPlayer player,
			final ItemStack stack,
			final int dwheel )
	{
		final ChiselMode mode = ChiselModeManager.getChiselMode( ChiselToolType.CHISEL );
		ChiselModeManager.scrollOption( ChiselToolType.CHISEL, mode, mode, dwheel );
	}

}
