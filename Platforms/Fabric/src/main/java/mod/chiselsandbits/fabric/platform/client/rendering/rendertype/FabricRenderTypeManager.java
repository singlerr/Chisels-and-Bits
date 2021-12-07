package mod.chiselsandbits.fabric.platform.client.rendering.rendertype;

import mod.chiselsandbits.platforms.core.client.rendering.type.IRenderTypeManager;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;

public class FabricRenderTypeManager implements IRenderTypeManager
{
    private static final FabricRenderTypeManager INSTANCE = new FabricRenderTypeManager();

    public static FabricRenderTypeManager getInstance()
    {
        return INSTANCE;
    }

    private FabricRenderTypeManager()
    {
    }

    @Override
    public @NotNull Optional<RenderType> getCurrentRenderType()
    {
        return Optional.empty();
    }

    @Override
    public void setCurrentRenderType(final RenderType renderType)
    {
    }

    @Override
    public boolean canRenderInType(final BlockState blockState, final RenderType renderType)
    {
        return ItemBlockRenderTypes.getChunkRenderType(blockState) == renderType;
    }

    @Override
    public boolean canRenderInType(final FluidState fluidState, final RenderType renderType)
    {
        return ItemBlockRenderTypes.getRenderLayer(fluidState) == renderType;
    }

    @Override
    public void setPossibleRenderTypesFor(
      final Block block, final RenderType defaultRenderType, final Predicate<RenderType> dynamicSelector)
    {
        ItemBlockRenderTypes.TYPE_BY_BLOCK.put(block, defaultRenderType);
    }
}
