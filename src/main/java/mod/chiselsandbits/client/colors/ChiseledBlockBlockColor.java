package mod.chiselsandbits.client.colors;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChiseledBlockBlockColor implements IBlockColor
{
    private static final int TINT_MASK = 0xff;
    private static final int TINT_BITS = 8;

    @Override
    public int getColor(
      @NotNull final BlockState state, @Nullable final IBlockDisplayReader displayReader, @Nullable final BlockPos pos, final int color)
    {
        final BlockState containedState = IBlockStateIdManager.getInstance().getBlockStateFrom(color >> TINT_BITS);
        int tintValue = color & TINT_MASK;
        return Minecraft.getInstance().getBlockColors().getColor(containedState, displayReader, pos, tintValue);
    }
}
