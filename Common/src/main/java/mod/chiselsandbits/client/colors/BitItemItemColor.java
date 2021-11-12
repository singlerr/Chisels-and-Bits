package mod.chiselsandbits.client.colors;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.item.bit.BitItem;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

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
            return IFluidManager.getInstance().getFluidColor(new FluidInformation(
                    state.getFluidState().getType(),
                    1
            ));
        }

        if ((!Minecraft.getInstance().options.keyShift.isUnbound() && Minecraft.getInstance().options.keyShift.isDown()) || (Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown()))
        {
            final Block block = state.getBlock();
            final Item item = block.asItem();
            int tintValue = tint & TINT_MASK;

            if (item != Items.AIR)
            {
                return Minecraft.getInstance().itemColors.getColor(new ItemStack(item, 1), tintValue);
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

        return Minecraft.getInstance().itemColors.getColor(workingStack, tint);
    }
}
