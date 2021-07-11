package mod.chiselsandbits.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.SpriteMap;
import net.minecraft.util.ResourceLocation;

import java.lang.reflect.Field;
import java.util.Map;

public class ReflectionUtils
{

    private ReflectionUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ReflectionUtils. This is a utility class");
    }

    public static void setField(final Object targetObject, final String fieldName, final Object value) {
        try
        {
            Field f = targetObject.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(targetObject, value);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new IllegalStateException("Failed to set value!");
        }
    }

    public static Object getField(final Object target, final String name)
    {
        try
        {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new IllegalStateException("Failed to get value!");
        }
    }
}
