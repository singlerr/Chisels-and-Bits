package mod.chiselsandbits.client;

import mod.chiselsandbits.helpers.ModUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.Nullable;

public class BlockColorChisled implements IBlockColor
{

	public static final int TINT_MASK = 0xff;
	public static final int TINT_BITS = 8;

    @Override
    public int getColor(
      final BlockState p_getColor_1_, @Nullable final IBlockDisplayReader p_getColor_2_, @Nullable final BlockPos p_getColor_3_, final int p_getColor_4_)
    {

        final BlockState tstate = ModUtil.getStateById( p_getColor_4_ >> TINT_BITS );
        int tintValue = p_getColor_4_ & TINT_MASK;
        return Minecraft.getInstance().getBlockColors().getColor( tstate, p_getColor_2_, p_getColor_3_, tintValue );
    }
}
