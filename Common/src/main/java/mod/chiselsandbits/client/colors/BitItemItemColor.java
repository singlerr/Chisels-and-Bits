package mod.chiselsandbits.client.colors;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.item.bit.BitItem;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class BitItemItemColor implements ItemColor
{
    private static final int TINT_MASK = 0xff;
    private static final int TINT_BITS = 8;

    @Override
    public int getColor(
      final ItemStack stack,
      final int tint)
    {
        if (!(stack.getItem() instanceof BitItem))
            return 0xffffff;

        final BlockState state = ((BitItem) stack.getItem()).getBitState(stack);
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

        return Minecraft.getInstance().getItemColors().getColor(workingStack, tint);
    }
}
