package mod.chiselsandbits.api.client.tool.mode.icon;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/**
 * Used to render the icons on the hot bar for the selected tool mode.
 */
public interface ISelectedToolModeIconRenderer
{
    /**
     * The id of the renderer.
     * Used to give the player a selection option for the preview renderer.
     *
     * @return The id of the preview renderer.
     */
    ResourceLocation getId();

    /**
     * Invoked to render the stacks tool mode icon in the given itemstack.
     * This is invoked already translated to the top left pixel of the slot in question.
     *
     * @param matrixStack The matrix stack.
     * @param stack The stack.
     */
    void render(MatrixStack matrixStack, ItemStack stack);
}
