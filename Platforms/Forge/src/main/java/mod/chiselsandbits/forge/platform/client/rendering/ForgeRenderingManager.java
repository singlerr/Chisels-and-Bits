package mod.chiselsandbits.forge.platform.client.rendering;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mod.chiselsandbits.platforms.core.client.rendering.IRenderingManager;
import mod.chiselsandbits.platforms.core.client.rendering.type.IRenderTypeManager;
import mod.chiselsandbits.platforms.core.fluid.FluidInformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

public class ForgeRenderingManager implements IRenderingManager
{
    private static final ForgeRenderingManager INSTANCE = new ForgeRenderingManager();

    public static ForgeRenderingManager getInstance()
    {
        return INSTANCE;
    }

    private final Map<Item, BlockEntityWithoutLevelRenderer> bewlrs = Maps.newConcurrentMap();

    private ForgeRenderingManager()
    {
    }

    @Override
    public void renderModel(
      final PoseStack.Pose last,
      final VertexConsumer buffer,
      final BlockState defaultBlockState,
      final BakedModel model,
      final float r,
      final float g,
      final float b,
      final int combinedLight,
      final int combinedOverlay)
    {
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(last, buffer, defaultBlockState, model, r, g, b, combinedLight, combinedOverlay,
          EmptyModelData.INSTANCE);
    }

    @Override
    public ResourceLocation getFlowingFluidTexture(final FluidInformation fluidInformation)
    {
        return fluidInformation.fluid().getAttributes().getFlowingTexture(buildFluidStack(fluidInformation));
    }

    @Override
    public ResourceLocation getFlowingFluidTexture(final Fluid fluid)
    {
        return fluid.getAttributes().getFlowingTexture();
    }

    @Override
    public ResourceLocation getStillFluidTexture(final FluidInformation fluidInformation)
    {
        return fluidInformation.fluid().getAttributes().getStillTexture(buildFluidStack(fluidInformation));
    }

    @Override
    public ResourceLocation getStillFluidTexture(final Fluid fluid)
    {
        return fluid.getAttributes().getStillTexture();
    }

    @Override
    public @NotNull IRenderTypeManager getRenderTypeManager()
    {
        return ForgeRenderTypeManager.getInstance();
    }

    @Override
    public void registerISTER(final Item item, final BlockEntityWithoutLevelRenderer renderer)
    {
        this.bewlrs.put(item, renderer);
    }

    public Optional<BlockEntityWithoutLevelRenderer> getRenderer(final Item item) {
        return Optional.ofNullable(this.bewlrs.get(item));
    }

    @NotNull
    private FluidStack buildFluidStack(final FluidInformation fluid)
    {
        if (fluid.data() == null)
            return new FluidStack(fluid.fluid(), (int) fluid.amount());

        return new FluidStack(fluid.fluid(), (int) fluid.amount(), fluid.data());
    }
}
