package mod.chiselsandbits.items;

import org.apache.commons.lang3.tuple.Pair;

import mod.chiselsandbits.chiseledblock.data.BitLocation;
import mod.chiselsandbits.core.ClientSide;
import mod.chiselsandbits.helpers.ChiselToolType;
import mod.chiselsandbits.helpers.ModUtil;
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

public class ItemTapeMeasure extends Item
{
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

}
