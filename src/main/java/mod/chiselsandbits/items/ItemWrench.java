package mod.chiselsandbits.items;

import java.util.List;

import mod.chiselsandbits.blueprints.EntityBlueprint;
import mod.chiselsandbits.chiseledblock.BlockChiseled;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ChiselModeManager;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.integration.mcmultipart.MCMultipartProxy;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.WrenchModes;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWrench extends Item implements IItemScrollWheel, IChiselModeItem
{

	public ItemWrench()
	{
		setMaxStackSize( 1 );

		final long uses = ChiselsAndBits.getConfig().wrenchUses;
		setMaxDamage( ChiselsAndBits.getConfig().damageTools ? (int) Math.max( 0, Math.min( Short.MAX_VALUE, uses ) ) : 0 );
	}

	@Override
	public String getHighlightTip(
			final ItemStack item,
			final String displayName )
	{
		if ( ChiselsAndBits.getConfig().itemNameModeDisplay )
		{
			return displayName + " - " + WrenchModes.getMode( item ).string.getLocal();
		}

		return displayName;
	}

	@Override
	@SideOnly( Side.CLIENT )
	public void addInformation(
			final ItemStack stack,
			final World worldIn,
			final List<String> tooltip,
			final ITooltipFlag advanced )
	{
		super.addInformation( stack, worldIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpWrench, tooltip,
				ClientSide.instance.getKeyName( Minecraft.getMinecraft().gameSettings.keyBindUseItem ) );
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(
			World world,
			EntityPlayer player,
			EnumHand hand )
	{
		if ( findAndInteractWithBlueprint( player, world, player.getHeldItem( hand ) ) )
		{
			return new ActionResult<ItemStack>( EnumActionResult.SUCCESS, player.getHeldItem( hand ) );
		}

		return super.onItemRightClick( world, player, hand );
	}

	@Override
	public EnumActionResult onItemUse(
			final EntityPlayer player,
			final World world,
			final BlockPos pos,
			final EnumHand hand,
			final EnumFacing side,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if ( findAndInteractWithBlueprint( player, world, player.getHeldItem( hand ) ) )
		{
			return EnumActionResult.SUCCESS;
		}

		final ItemStack stack = player.getHeldItem( hand );

		if ( !player.canPlayerEdit( pos, side, stack ) || !world.isBlockModifiable( player, pos ) )
		{
			return EnumActionResult.FAIL;
		}

		final IBlockState state = world.getBlockState( pos );
		if ( state != null )
		{
			final WrenchModes mode = WrenchModes.getMode( stack );

			switch ( mode )
			{
				case NUDGE_BIT:
					return nudgeBit( player, hand, state, stack, world, pos, side );
				case NUDGE_BLOCK:
					return nudgeBlock( player, hand, state, stack, world, pos, side );
				case ROTATE:
					if ( !player.isSneaking() )
					{
						return rotate( player, hand, state, stack, world, pos, side );
					}

			}
		}
		return EnumActionResult.FAIL;
	}

	private boolean findAndInteractWithBlueprint(
			EntityPlayer player,
			World world,
			ItemStack stack )
	{
		org.apache.commons.lang3.tuple.Pair<Vec3d, Vec3d> ray = ModUtil.getPlayerRay( player );

		boolean found = false;

		for ( EntityBlueprint x : world.getEntitiesWithinAABB( EntityBlueprint.class, player.getEntityBoundingBox().expand( 128, 128, 128 ) ) )
		{
			AxisAlignedBB bb = x.getBlueprintBox();
			RayTraceResult rtay = bb.calculateIntercept( ray.getLeft(), ray.getRight() );
			if ( rtay != null && rtay.sideHit != null )
			{
				found = true;
				x.hitWithTool( player, stack );
			}
		}

		return found;
	}

	private EnumActionResult nudgeBit(
			final EntityPlayer player,
			final EnumHand hand,
			final IBlockState state,
			final ItemStack stack,
			final World world,
			final BlockPos pos,
			EnumFacing side )
	{
		if ( player.isSneaking() )
		{
			side = side.getOpposite();
		}

		if ( state.getBlockHardness( world, pos ) == -1.0F || state.getMobilityFlag() == EnumPushReaction.BLOCK )
		{
			return EnumActionResult.FAIL;
		}

		if ( state.getMobilityFlag() == EnumPushReaction.DESTROY )
		{
			return EnumActionResult.FAIL;
		}

		if ( ChiselsAndBits.getConfig().enableSurvivalWrenchBlockNudging || player.isCreative() )
		{
			// TODO IMPLEMENT BIT NUDGE.
		}
		else
		{
			if ( !world.isRemote )
			{
				player.addChatMessage( new TextComponentTranslation( LocalStrings.WrenchOnlyGhosts.toString() ) );
			}
		}

		return EnumActionResult.FAIL;
	}

	private EnumActionResult nudgeBlock(
			final EntityPlayer player,
			final EnumHand hand,
			final IBlockState state,
			final ItemStack stack,
			final World world,
			final BlockPos pos,
			EnumFacing side )
	{
		if ( player.isSneaking() )
		{
			side = side.getOpposite();
		}

		if ( state.getBlockHardness( world, pos ) == -1.0F || state.getMobilityFlag() == EnumPushReaction.BLOCK )
		{
			return EnumActionResult.FAIL;
		}

		if ( state.getMobilityFlag() == EnumPushReaction.DESTROY )
		{
			return EnumActionResult.FAIL;
		}

		if ( ChiselsAndBits.getConfig().enableSurvivalWrenchBlockNudging || player.isCreative() )
		{
			final BlockPos target = pos.offset( side.getOpposite() );
			final IBlockState targetState = world.getBlockState( target );
			if ( targetState.getBlock().isAir( targetState, world, target ) || targetState.getBlock().isReplaceable( world, target ) )
			{
				if ( !state.getBlock().hasTileEntity( state ) )
				{
					stack.damageItem( 1, player );
					world.setBlockState( target, state );
					world.setBlockToAir( pos );
					return EnumActionResult.SUCCESS;
				}
				else if ( state.getBlock() instanceof BlockChiseled )
				{
					stack.damageItem( 1, player );
					world.setBlockState( target, state );
					final TileEntity dest = world.getTileEntity( target );
					final TileEntity src = world.getTileEntity( pos );
					final NBTTagCompound tag = new NBTTagCompound();
					src.writeToNBT( tag );
					dest.readFromNBT( tag );
					world.setBlockToAir( pos );
					return EnumActionResult.SUCCESS;
				}
			}
		}
		else
		{
			if ( !world.isRemote )
			{
				player.addChatMessage( new TextComponentTranslation( LocalStrings.WrenchOnlyGhosts.toString() ) );
			}
		}

		return EnumActionResult.FAIL;
	}

	private EnumActionResult rotate(
			final EntityPlayer player,
			final EnumHand hand,
			final IBlockState state,
			final ItemStack stack,
			final World world,
			final BlockPos pos,
			final EnumFacing side )
	{
		if ( MCMultipartProxy.proxyMCMultiPart.isMultiPartTileEntity( world, pos ) )
		{
			if ( MCMultipartProxy.proxyMCMultiPart.rotate( world, pos, player ) )
			{
				stack.damageItem( 1, player );
				world.notifyNeighborsOfStateChange( pos, state.getBlock(), false );
				player.swingArm( hand );
				return EnumActionResult.SUCCESS;
			}
		}
		else if ( state.getBlock().rotateBlock( world, pos, side ) )
		{
			stack.damageItem( 1, player );
			world.notifyNeighborsOfStateChange( pos, state.getBlock(), false );
			player.swingArm( hand );
			return EnumActionResult.SUCCESS;
		}

		return EnumActionResult.FAIL;
	}

	@Override
	public void scroll(
			final EntityPlayer player,
			final ItemStack stack,
			final int dwheel )
	{
		final IToolMode mode = ChiselModeManager.getChiselMode( player, ChiselToolType.WRENCH, EnumHand.MAIN_HAND );
		ChiselModeManager.scrollOption( ChiselToolType.WRENCH, mode, mode, dwheel );
	}
}