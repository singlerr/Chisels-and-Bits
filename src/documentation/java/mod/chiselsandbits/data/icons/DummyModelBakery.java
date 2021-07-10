package mod.chiselsandbits.data.icons;

import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.profiler.EmptyProfiler;
import net.minecraft.resources.IResourceManager;

import java.lang.reflect.Field;

public class DummyModelBakery extends ModelBakery
{
    protected DummyModelBakery(IResourceManager resourceManagerIn, BlockColors blockColorsIn)
    {
        super(resourceManagerIn, blockColorsIn, EmptyProfiler.INSTANCE, 0);
    }

    public void setSpriteMap(final SpriteMap spriteMap) {
        try
        {
            final Field spriteMapField = ModelBakery.class.getDeclaredField("spriteMap");
            spriteMapField.setAccessible(true);
            spriteMapField.set(this, spriteMap);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new IllegalStateException("Failed to update the sprite map data.");
        }
    }
}
