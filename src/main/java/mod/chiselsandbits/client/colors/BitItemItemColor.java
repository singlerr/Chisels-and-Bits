package mod.chiselsandbits.client.colors;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.item.bit.BitItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

public class BitItemItemColor implements IItemColor
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
        if(state.getBlock() instanceof FlowingFluidBlock) {
            return ((FlowingFluidBlock) state.getBlock()).getFluid().getAttributes().getColor();
        }

        if ((!Minecraft.getInstance().gameSettings.keyBindSneak.isInvalid() && Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown()) || Screen.hasShiftDown())
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

        return Minecraft.getInstance().getItemColors().getColor(new ItemStack(state.getBlock(), 1), tint);
    }
}
