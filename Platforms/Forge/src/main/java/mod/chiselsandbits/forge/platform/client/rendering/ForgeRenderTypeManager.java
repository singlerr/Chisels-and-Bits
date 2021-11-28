package mod.chiselsandbits.forge.platform.client.rendering;

import mod.chiselsandbits.platforms.core.client.rendering.type.IRenderTypeManager;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.MinecraftForgeClient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class ForgeRenderTypeManager implements IRenderTypeManager
{
    private static final ForgeRenderTypeManager INSTANCE = new ForgeRenderTypeManager();

    public static ForgeRenderTypeManager getInstance()
    {
        return INSTANCE;
    }

    private ForgeRenderTypeManager()
    {
    }

    @Override
    public @NotNull Optional<RenderType> getCurrentRenderType()
    {
        return Optional.ofNullable(MinecraftForgeClient.getRenderLayer());
    }

    @Override
    public void setCurrentRenderType(final RenderType renderType)
    {
        ForgeHooksClient.setRenderLayer(renderType);
    }

    @Override
    public boolean canRenderInType(final BlockState blockState, final RenderType renderType)
    {
        return ItemBlockRenderTypes.canRenderInLayer(blockState, renderType);
    }

    @Override
    public boolean canRenderInType(final FluidState fluidState, final RenderType renderType)
    {
        return ItemBlockRenderTypes.canRenderInLayer(fluidState, renderType);
    }

    @Override
    public void setPossibleRenderTypesFor(
      final Block block, final RenderType defaultRenderType, final Predicate<RenderType> dynamicSelector)
    {
        ItemBlockRenderTypes.setRenderLayer(block, dynamicSelector);
    }
}
