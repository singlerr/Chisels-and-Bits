package mod.chiselsandbits.client.tool.mode.icon;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chiselsandbits.api.client.tool.mode.icon.ISelectedToolModeIconRenderer;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public class NoopSelectedToolModeIconRenderer implements ISelectedToolModeIconRenderer
{
    static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "noop");

    @Override
    public ResourceLocation getId()
    {
        return ID;
    }

    @Override
    public void render(final PoseStack matrixStack, final ItemStack stack)
    {
        //Noop
    }
}
