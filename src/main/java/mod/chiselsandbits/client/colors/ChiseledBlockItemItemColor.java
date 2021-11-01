package mod.chiselsandbits.client.colors;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.item.bit.BitItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChiseledBlockItemItemColor implements ItemColor
{
    private static final int TINT_MASK = 0xff;
    private static final int TINT_BITS = 8;

    @Override
    public int getColor(
      @NotNull final ItemStack stack,
      final int tint )
    {
        final BlockState state = IBlockStateIdManager.getInstance().getBlockStateFrom( tint >> TINT_BITS );
        if(state.getBlock() instanceof LiquidBlock) {
            return ((LiquidBlock) state.getBlock()).getFluid().getAttributes().getColor();
        }

        if ((!Minecraft.getInstance().options.keyShift.isUnbound() && Minecraft.getInstance().options.keyShift.isDown()) || (Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown()))
        {
            final Block block = state.getBlock();
            final Item item = block.asItem();
            int tintValue = tint & TINT_MASK;

            if (item != Items.AIR)
            {
                return Minecraft.getInstance().getItemColors().getColor(new ItemStack(item, 1), tintValue);
            }

            return 0xffffff;
        }

        if (!IEligibilityManager.getInstance().canBeChiseled(state))
        {
            return 0xffffff;
        }

        final ItemStack workingStack = new ItemStack(state.getBlock(), 1);
        if (workingStack.getItem() instanceof AirItem)
            return 0xffffff;

        final Block block = state.getBlock();
        final Item itemFromBlock = block.asItem();
        int tintValue = tint & TINT_MASK;
        return Minecraft.getInstance().getItemColors().getColor( new ItemStack(itemFromBlock, 1), tintValue );
    }
}
