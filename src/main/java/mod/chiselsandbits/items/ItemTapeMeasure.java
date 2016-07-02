package mod.chiselsandbits.items;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.core.ChiselsAndBits;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ChiselModeManager;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.LocalStrings;
import mod.chiselsandbits.helpers.ModUtil;
import mod.chiselsandbits.interfaces.IChiselModeItem;
import mod.chiselsandbits.interfaces.IItemScrollWheel;
import mod.chiselsandbits.modes.IToolMode;
import mod.chiselsandbits.modes.TapeMeasureModes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemTapeMeasure extends Item implements IChiselModeItem, IItemScrollWheel
{
	@Override
	public void addInformation(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final List<String> tooltip,
			final boolean advanced )
	{
		super.addInformation( stack, playerIn, tooltip, advanced );
		ChiselsAndBits.getConfig().helpText( LocalStrings.HelpTapeMeasure, tooltip, ClientSide.instance.getModeKey() );
	}

	@Override
	public EnumActionResult onItemUse(
			final ItemStack stack,
			final EntityPlayer playerIn,
			final World worldIn,
			final BlockPos pos,
			final EnumHand hand,
			final EnumFacing facing,
			final float hitX,
			final float hitY,
			final float hitZ )
	{
		if ( worldIn.isRemote )
		{
			if ( playerIn.isSneaking() )
			{
				ClientSide.instance.tapeMeasures.clear();
				return EnumActionResult.SUCCESS;
			}

			final Pair<Vec3d, Vec3d> PlayerRay = ModUtil.getPlayerRay( playerIn );
			final Vec3d ray_from = PlayerRay.getLeft();
			final Vec3d ray_to = PlayerRay.getRight();

			final RayTraceResult mop = playerIn.worldObj.getBlockState( pos ).getBlock().collisionRayTrace( playerIn.getEntityWorld().getBlockState( pos ), playerIn.worldObj, pos, ray_from, ray_to );
			if ( mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK )
			{
				final BitLocation loc = new BitLocation( mop, true, ChiselToolType.CHISEL );
				ClientSide.instance.pointAt( ChiselToolType.TAPEMEASURE, loc, hand );
			}
		}

		return EnumActionResult.SUCCESS;
	}

	@Override
	public String getHighlightTip(
			final ItemStack item,
			final String displayName )
	{
		if ( ChiselsAndBits.getConfig().itemNameModeDisplay )
		{
			return displayName + " - " + TapeMeasureModes.getMode( item ).string.getLocal();
		}

		return displayName;
	}

	@Override
	public void scroll(
			final EntityPlayer player,
			final ItemStack stack,
			final int dwheel )
	{
		final IToolMode mode = ChiselModeManager.getChiselMode( player, ChiselToolType.CHISEL, EnumHand.MAIN_HAND );
		ChiselModeManager.scrollOption( ChiselToolType.CHISEL, mode, mode, dwheel );
	}

}
