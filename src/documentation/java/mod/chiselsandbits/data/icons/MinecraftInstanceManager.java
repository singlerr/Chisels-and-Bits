package mod.chiselsandbits.data.icons;

import mod.chiselsandbits.api.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IResourceManager;
import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class MinecraftInstanceManager
{
    private static final MinecraftInstanceManager INSTANCE = new MinecraftInstanceManager();

    public static MinecraftInstanceManager getInstance()
    {
        return INSTANCE;
    }

    private boolean isInitialized = false;
    private Unsafe internalUnsafe = null;

    private MinecraftInstanceManager()
    {
    }

    public void initialize(final IResourceManager resourceManager) {
        if (isInitialized)
            return;

        isInitialized = true;

        createMinecraft();
        initializeResourceManager(resourceManager);
        initializeTextureManager(resourceManager);
        initializeBlockColors();
        initializeItemColors();
        initializeModelManager();
        initializeBlockRenderDispatcher();
        initializeGameRenderer(resourceManager);
    }

    private Unsafe unsafe() {
        if (internalUnsafe != null)
            return internalUnsafe;

        try
        {
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            internalUnsafe = (Unsafe) f.get(null);
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new IllegalStateException("Missing unsafe!");
        }

        return internalUnsafe;
    }

    private void createMinecraft() {
        try
        {
            final Minecraft testingMinecraft = (Minecraft) unsafe().allocateInstance(Minecraft.class);

            Field f = Minecraft.class.getDeclaredField("instance");
            f.setAccessible(true);
            f.set(null, testingMinecraft);

        }
        catch (InstantiationException | NoSuchFieldException | IllegalAccessException e)
        {
            throw new IllegalStateException("Failed to load minecraft!");
        }
    }

    private void initializeResourceManager(final IResourceManager resourceManager)
    {
        ReflectionUtils.setField(Minecraft.getInstance(), "resourceManager", resourceManager);
    }

    private void initializeTextureManager(final IResourceManager resourceManager)
    {
        final TextureManager textureManager = new TextureManager(resourceManager);
        ReflectionUtils.setField(Minecraft.getInstance(), "textureManager", textureManager);
    }

    private void initializeBlockColors() {
        ReflectionUtils.setField(Minecraft.getInstance(), "blockColors", BlockColors.init());
    }

    private void initializeItemColors() {
        ReflectionUtils.setField(Minecraft.getInstance(), "itemColors", ItemColors.init(Minecraft.getInstance().getBlockColors()));
    }

    private void initializeModelManager() {
        final ModelManager modelManager = new ModelManager(Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getBlockColors(), 0);
        ReflectionUtils.setField(Minecraft.getInstance(), "modelManager", modelManager);
    }

    private void initializeBlockRenderDispatcher() {
        final BlockRendererDispatcher blockRendererDispatcher = new BlockRendererDispatcher(Minecraft.getInstance().getModelManager().getBlockModelShapes(), Minecraft.getInstance().getBlockColors());
        ReflectionUtils.setField(Minecraft.getInstance(), "blockRenderDispatcher", blockRendererDispatcher);
    }

    private void initializeGameRenderer(final IResourceManager resourceManager) {
        final GameRenderer gameRenderer = new GameRenderer(Minecraft.getInstance(), resourceManager, new RenderTypeBuffers());
        ReflectionUtils.setField(Minecraft.getInstance(), "gameRenderer", gameRenderer);
    }
}
