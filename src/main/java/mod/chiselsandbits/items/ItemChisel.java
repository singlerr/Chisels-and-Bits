package mod.chiselsandbits.items;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.item.IItemTier;
import net.minecraft.item.ToolItem;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Stopwatch;

import mod.chiselsandbits.chiseledblock.BlockBitInfo;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.chiseledblock.data.VoxelBlob;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ActingPlayer;
import mod.chiselsandbits.helpers.BitOperation;
import mod.chiselsandbits.helpers.ChiselModeManager;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.IContinuousInventory;
import mod.chiselsandbits.helpers.IItemInInventory;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.integration.mcmultipart.MCMultipartProxy;
import mod.chiselsandbits.integration.mods.LittleTiles;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.modes.ChiselMode;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.network.packets.PacketChisel;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.item.ItemTier.*;

public class ItemChisel extends ToolItem implements IItemScrollWheel, IChiselModeItem
{
	final private static float one_16th = 1.0f / 16.0f;

	public ItemChisel(
			final IItemTier material,
            final Item.Properties properties)
	{
		super( 0.1F, -2.8F, material, new HashSet<Block>(), properties );

		long uses = 1;
        if (DIAMOND.equals(material))
        {
            uses = ChiselsAndBits.getConfig().diamondChiselUses;
        }
        else if (GOLD.equals(material))
        {
            uses = ChiselsAndBits.getConfig().goldChiselUses;
        }
        else if (IRON.equals(material))
        {
            uses = ChiselsAndBits.getConfig().ironChiselUses;
        }
        else if (STONE.equals(material))
        {
            uses = ChiselsAndBits.getConfig().stoneChiselUses;
        }

        properties.maxDamage(ChiselsAndBits.getConfig().damageTools ? (int) Math.max( 0, uses ) : 0);
	}

    @Override
    public void addInformation(
      final ItemStack stack, @Nullable final World worldIn, final List<ITextComponent> tooltip, final ITooltipFlag flagIn)
    {
        super.addInformation( stack, worldIn, tooltip, flagIn );
        ChiselsAndBits.getConfig().helpText( LocalStrings.HelpChisel, tooltip,
          ClientSide.instance.getKeyName( Minecraft.getInstance().gameSettings.keyBindAttack ),
          ClientSide.instance.getModeKey() );
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
			final PlayerEntity player )
	{
		return ItemChisel.fromBreakToChisel( ChiselMode.castMode( ChiselModeManager.getChiselMode( player, ChiselToolType.CHISEL, Hand.MAIN_HAND ) ), itemstack, pos, player, Hand.MAIN_HAND );
	}

	static public boolean fromBreakToChisel(
			final ChiselMode mode,
			final ItemStack itemstack,
			final @Nonnull BlockPos pos,
			final PlayerEntity player,
			final Hand hand )
	{
		final BlockState state = player.getEntityWorld().getBlockState( pos );
		if ( ItemChiseledBit.checkRequiredSpace( player, state ) )
		{
			return false;
		}
		if ( BlockBitInfo.canChisel( state ) || MCMultipartProxy.proxyMCMultiPart.isMultiPartTileEntity( player.getEntityWorld(), pos ) || LittleTiles.isLittleTilesBlock( player.getEntityWorld().getTileEntity( pos ) ) )
		{
			if ( itemstack != null && ( timer == null || timer.elapsed( TimeUnit.MILLISECONDS ) > 150 ) )
			{
				timer = Stopwatch.createStarted();
				if ( mode == ChiselMode.DRAWN_REGION )
				{
					final Pair<Vector3d, Vector3d> PlayerRay = ModUtil.getPlayerRay( player );
					final Vector3d ray_from = PlayerRay.getLeft();
					final Vector3d ray_to = PlayerRay.getRight();

					final RayTraceResult mop = player.worldObj.getBlockState( pos ).getBlock().collisionRayTrace( player.getEntityWorld().getBlockState( pos ), player.worldObj, pos, ray_from, ray_to );
					if ( mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK )
					{
						final BitLocation loc = new BitLocation( mop, true, BitOperation.CHISEL );
						ClientSide.instance.pointAt( ChiselToolType.CHISEL, loc, hand );
						return true;
					}

					return true;
				}

				if ( !player.worldObj.isRemote )
				{
					return true;
				}

				final Pair<Vector3d, Vector3d> PlayerRay = ModUtil.getPlayerRay( player );
				final Vector3d ray_from = PlayerRay.getLeft();
				final Vector3d ray_to = PlayerRay.getRight();

				final RayTraceResult mop = player.worldObj.getBlockState( pos ).getBlock().collisionRayTrace( player.worldObj.getBlockState( pos ), player.worldObj, pos, ray_from, ray_to );
				if ( mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK )
				{
					useChisel( mode, player, player.worldObj, pos, mop.sideHit, (float) ( mop.hitVec.xCoord - pos.getX() ), (float) ( mop.hitVec.yCoord - pos.getY() ), (float) ( mop.hitVec.zCoord - pos.getZ() ), hand );
				}
			}

			return true;
		}

		if ( player.getEntityWorld() != null && player.getEntityWorld().isRemote )
		{
			if ( ClientSide.instance.getStartPos() != null )
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public String getHighlightTip(
			final ItemStack item,
			final String displayName )
	{
		if ( ChiselsAndBits.getConfig().itemNameModeDisplay )
		{
			if ( ChiselsAndBits.getConfig().perChiselMode || FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER )
			{
				return displayName + " - " + ChiselMode.getMode( item ).string.getLocal();
			}
			else
			{
				return displayName + " - " + ChiselModeManager.getChiselMode( ClientSide.instance.getPlayer(), ChiselToolType.CHISEL, Hand.MAIN_HAND ).getName().getLocal();
			}
		}

		return displayName;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(
			final World worldIn,
			final PlayerEntity playerIn,
			final Hand hand )
	{
		final ItemStack itemStackIn = playerIn.getHeldItem( hand );

		if ( worldIn.isRemote && ChiselsAndBits.getConfig().enableRightClickModeChange )
		{
			final IToolMode mode = ChiselModeManager.getChiselMode( playerIn, ChiselToolType.CHISEL, hand );
			ChiselModeManager.scrollOption( ChiselToolType.CHISEL, mode, mode, playerIn.isSneaking() ? -1 : 1 );
			return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, itemStackIn );
		}

		return super.onItemRightClick( worldIn, playerIn, hand );
	}

	@Override
	public EnumActionResult onItemUseFirst(
			final PlayerEntity player,
			final World world,
			final BlockPos pos,
			final Direction side,
			final float hitX,
			final float hitY,
			final float hitZ,
			final Hand hand )
	{
		if ( world.isRemote && ChiselsAndBits.getConfig().enableRightClickModeChange )
		{
			onItemRightClick( world, player, hand );
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.FAIL;
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
			final PlayerEntity player,
			final World world,
			final @Nonnull BlockPos pos,
			final Direction side,
			final float hitX,
			final float hitY,
			final float hitZ,
			final Hand hand )
	{
		final BitLocation location = new BitLocation( new RayTraceResult( RayTraceResult.Type.BLOCK, new Vector3d( hitX, hitY, hitZ ), side, pos ), false, BitOperation.CHISEL );

		final PacketChisel pc = new PacketChisel( BitOperation.CHISEL, location, side, mode, hand );

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
			final ActingPlayer player,
			final VoxelBlob vb,
			final World world,
			final BlockPos pos,
			final Direction side,
			final int x,
			final int y,
			final int z,
			ItemStack output,
			final List<EntityItem> spawnlist )
	{
		final boolean isCreative = player.isCreative();

		final int blk = vb.get( x, y, z );
		if ( blk == 0 )
		{
			return output;
		}

		if ( !canMine( selected, ModUtil.getStateById( blk ), player.getPlayer(), world, pos ) )
		{
			return output;
		}

		if ( !selected.useItem( blk ) )
		{
			return output;
		}

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

			if ( output == null || !ItemChiseledBit.sameBit( output, blk ) || ModUtil.getStackSize( output ) == 64 )
			{
				output = ItemChiseledBit.createStack( blk, 1, true );

				if ( spawnBit )
				{
					spawnlist.add( new EntityItem( world, pos.getX() + hitX, pos.getY() + hitY, pos.getZ() + hitZ, output ) );
				}
			}
			else
			{
				ModUtil.adjustStackSize( output, 1 );
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

	private static boolean testingChisel = false;

	public static boolean canMine(
			final IContinuousInventory chiselInv,
			final BlockState state,
			final PlayerEntity player,
			final World world,
			final @Nonnull BlockPos pos )
	{
		final int targetState = ModUtil.getStateId( state );
		IItemInInventory chiselSlot = chiselInv.getItem( targetState );
		ItemStack chisel = chiselSlot.getStack();

		if ( player.capabilities.isCreativeMode )
		{
			return world.isBlockModifiable( player, pos );
		}

		if ( ModUtil.isEmpty( chisel ) )
		{
			return false;
		}

		if ( ChiselsAndBits.getConfig().enableChiselToolHarvestCheck )
		{
			// this is the earily check.
			if ( state.getBlock() instanceof BlockChiseled )
			{
				return ( (BlockChiseled) state.getBlock() ).basicHarvestBlockTest( world, pos, player );
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
			while ( !ModUtil.isEmpty( chisel ) );

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
			final BlockState blk )
	{
		Item it;

		switch ( getToolMaterialName() )
		{
			case "DIAMOND":
				it = Items.DIAMOND_PICKAXE;
				break;
			case "GOLD":
				it = Items.GOLDEN_PICKAXE;
				break;
			case "IRON":
				it = Items.IRON_PICKAXE;
				break;
			default:
			case "STONE":
				it = Items.STONE_PICKAXE;
				break;
			case "WOOD":
				it = Items.WOODEN_PICKAXE;
				break;
		}

		return blk.getBlock() instanceof BlockChiseled || it.canHarvestBlock( blk );
	}

	@Override
	public int getHarvestLevel(
			final ItemStack stack,
			final String toolClass,
			final PlayerEntity player,
			final BlockState blockState )
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

		return super.getHarvestLevel( stack, toolClass, player, blockState );
	}

	@Override
	public void scroll(
			final PlayerEntity player,
			final ItemStack stack,
			final int dwheel )
	{
		final IToolMode mode = ChiselModeManager.getChiselMode( player, ChiselToolType.CHISEL, Hand.MAIN_HAND );
		ChiselModeManager.scrollOption( ChiselToolType.CHISEL, mode, mode, dwheel );
	}

}
