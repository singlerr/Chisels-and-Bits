package mod.chiselsandbits.client;

import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BlockColorChisled implements IBlockColor
{

	public static final int TINT_MASK = 0xff;
	public static final int TINT_BITS = 8;

	@Override
	public int colorMultiplier(
			final BlockState state,
			final IBlockAccess worldIn,
			final BlockPos pos,
			final int tint )
	{
		final BlockState tstate = ModUtil.getStateById( tint >> TINT_BITS );
		int tintValue = tint & TINT_MASK;
		return Minecraft.getMinecraft().getBlockColors().colorMultiplier( tstate, worldIn, pos, tintValue );
	}

}
