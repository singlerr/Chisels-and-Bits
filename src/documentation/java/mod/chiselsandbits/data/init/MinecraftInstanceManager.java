package mod.chiselsandbits.data.init;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.util.ReflectionUtils;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderTypeBuffers;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.NetworkTagManager;
import net.minecraft.tags.TagRegistryManager;
import net.minecraft.util.Timer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.model.animation.CapabilityAnimation;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import sun.misc.Unsafe;

import java.io.File;
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

    void initialize(final ExistingFileHelper helper) {
        if (isInitialized)
            return;

        isInitialized = true;

        IResourceManager resourceManager = (IResourceManager) ReflectionUtils.getField(helper, "clientResources");

        createMinecraft();
        initializeTimer();
        initializeRenderSystem();
        initializeResourceManager(resourceManager);
        initializeTextureManager(resourceManager);
        initializeBlockColors();
        initializeItemColors();
        initializeModelManager();
        initializeItemRenderer();
        initializeBlockRenderDispatcher();
        initializeGameRenderer(resourceManager);
        initializeDataFixer();
        initializeGameSettings();

        initializeForge();

        initializeTags(helper);
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

    private void initializeTimer() {
        final Timer timer = new Timer(20,0L);
        ReflectionUtils.setField(Minecraft.getInstance(), "timer", timer);
    }

    private void initializeRenderSystem()
    {
        RenderSystem.initRenderThread();
        RenderSystem.initGameThread(false);
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
        ReflectionUtils.setField(Minecraft.getInstance(), "blockColors", BlockColors.createDefault());
    }

    private void initializeItemColors() {
        ReflectionUtils.setField(Minecraft.getInstance(), "itemColors", ItemColors.createDefault(Minecraft.getInstance().getBlockColors()));
    }

    private void initializeModelManager() {
        final ExtendedModelManager modelManager = new ExtendedModelManager(Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getBlockColors(), 0);
        ReflectionUtils.setField(Minecraft.getInstance(), "modelManager", modelManager);
    }

    private void initializeItemRenderer()
    {
        final ItemRenderer itemRenderer = new ItemRenderer(
          Minecraft.getInstance().getTextureManager(),
          Minecraft.getInstance().getModelManager(),
          Minecraft.getInstance().getItemColors()
        );
        ReflectionUtils.setField(Minecraft.getInstance(), "itemRenderer", itemRenderer);
    }

    private void initializeBlockRenderDispatcher() {
        final BlockRendererDispatcher blockRendererDispatcher = new BlockRendererDispatcher(Minecraft.getInstance().getModelManager().getBlockModelShaper(), Minecraft.getInstance().getBlockColors());
        ReflectionUtils.setField(Minecraft.getInstance(), "blockRenderer", blockRendererDispatcher);
    }

    private void initializeGameRenderer(final IResourceManager resourceManager) {
        final GameRenderer gameRenderer = new GameRenderer(Minecraft.getInstance(), resourceManager, new RenderTypeBuffers());
        ReflectionUtils.setField(Minecraft.getInstance(), "gameRenderer", gameRenderer);
    }

    private void initializeDataFixer()
    {
        ReflectionUtils.setField(Minecraft.getInstance(), "fixerUpper", DataFixesManager.getDataFixer());
    }

    private void initializeGameSettings()
    {
        final GameSettings gameSettings = new GameSettings(Minecraft.getInstance(), new File("./"));
        ReflectionUtils.setField(Minecraft.getInstance(), "options", gameSettings);
    }

    private void initializeForge()
    {
        initializeCapabilities();
    }

    private void initializeCapabilities()
    {
        CapabilityItemHandler.register();
        CapabilityFluidHandler.register();
        CapabilityAnimation.register();
        CapabilityEnergy.register();
    }

    private void initializeTags(ExistingFileHelper existingFileHelper)
    {
        final IResourceManager resourceManager = (IResourceManager) ReflectionUtils.getField(existingFileHelper, "serverData");
        final NetworkTagManager networkTagManager = new NetworkTagManager();
        AsyncReloadManager.getInstance().reload(resourceManager, networkTagManager);
        TagRegistryManager.resetAll(networkTagManager.getTags());
        TagRegistryManager.fetchCustomTagTypes(networkTagManager.getTags());
    }
}
