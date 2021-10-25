package mod.chiselsandbits.client.chiseling.preview.render;

import com.google.common.collect.Maps;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRenderer;
import mod.chiselsandbits.api.client.chiseling.preview.render.IChiselContextPreviewRendererRegistry;
import mod.chiselsandbits.api.config.Configuration;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ChiselContextPreviewRendererRegistry implements IChiselContextPreviewRendererRegistry
{
    private static final ChiselContextPreviewRendererRegistry INSTANCE = new ChiselContextPreviewRendererRegistry();

    public static ChiselContextPreviewRendererRegistry getInstance()
    {
        return INSTANCE;
    }

    private final Map<ResourceLocation, IChiselContextPreviewRenderer> rendererMap = Maps.newConcurrentMap();

    private ChiselContextPreviewRendererRegistry()
    {
        this.register(
          new ConfigurableColoredVoxelShapeChiselContextPreviewRenderer(),
          new NoopChiselContextPreviewRenderer()
        );
    }

    @Override
    public IChiselContextPreviewRenderer getCurrent()
    {
        return rendererMap.getOrDefault(new ResourceLocation(Configuration.getInstance().getClient().previewRenderer.get()),
          rendererMap.get(ConfigurableColoredVoxelShapeChiselContextPreviewRenderer.ID));
    }

    @Override
    public IChiselContextPreviewRendererRegistry register(final IChiselContextPreviewRenderer... renderers)
    {
        for (final IChiselContextPreviewRenderer renderer : renderers)
        {
            if (rendererMap.put(renderer.getId(), renderer) != null)
                throw new IllegalArgumentException("The renderer id: " + renderer.getId() + " is already in use!");
        }
        return this;
    }
}
