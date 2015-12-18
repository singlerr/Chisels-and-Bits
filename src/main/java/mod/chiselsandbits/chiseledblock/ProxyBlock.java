
package mod.chiselsandbits.chiseledblock;


import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;


class ProxyBlock extends Block
{
	public String MethodName;

	private void markMethod()
	{
		MethodName = new Throwable().fillInStackTrace().getStackTrace()[1].getMethodName();
	}

	protected ProxyBlock()
	{
		super( Material.air );
	}

	@Override
	public float getBlockHardness(
			final World worldIn,
			final BlockPos pos )
	{
		markMethod();
		return 0;
	}

	@SuppressWarnings( "rawtypes" )
	@Override
	public void addCollisionBoxesToList(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final AxisAlignedBB mask,
			final List list,
			final Entity collidingEntity )
	{
		markMethod();
	}

	@Override
	public float getPlayerRelativeBlockHardness(
			final EntityPlayer playerIn,
			final World worldIn,
			final BlockPos pos )
	{
		markMethod();
		return 0;
	}

	@Override
	public float getExplosionResistance( Entity exploder )
	{
		markMethod();
		return 0;
	}

	@Override
	public float getExplosionResistance(
			final World world,
			final BlockPos pos,
			final Entity exploder,
			final Explosion explosion )
	{
		markMethod();
		return 0;
	}

	@Override
	public int quantityDropped(
			final IBlockState state,
			final int fortune,
			final Random random )
	{

		markMethod();
		return 0;
	}

	@Override
	public int quantityDropped(
			final Random random )
	{
		markMethod();
		return 0;
	}

	@Override
	public int quantityDroppedWithBonus(
			final int fortune,
			final Random random )
	{
		markMethod();
		return 0;
	}

	@Override
	public void onEntityCollidedWithBlock(
			final World worldIn,
			final BlockPos pos,
			final Entity entityIn )
	{
		markMethod();
	}

	@Override
	public void onEntityCollidedWithBlock(
			final World worldIn,
			final BlockPos pos,
			final IBlockState state,
			final Entity entityIn )
	{

		markMethod();
	}
}