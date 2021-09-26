package mod.chiselsandbits.data.init;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.util.ReflectionUtils;
import net.minecraft.client.Options;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagManager;
import net.minecraft.tags.StaticTags;
import net.minecraft.client.Timer;
import net.minecraft.util.datafix.DataFixers;
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

        ResourceManager resourceManager = (ResourceManager) ReflectionUtils.getField(helper, "clientResources");

        createMinecraft();
        initializeTimer();
        initializeRenderSystem();
        initializeResourceManager(resourceManager);
        initializeTextureManager(resourceManager);
        initializeDataFixer();
        initializeGameSettings();

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

    private void initializeResourceManager(final ResourceManager resourceManager)
    {
        ReflectionUtils.setField(Minecraft.getInstance(), "resourceManager", resourceManager);
    }

    private void initializeTextureManager(final ResourceManager resourceManager)
    {
        final TextureManager textureManager = new TextureManager(resourceManager);
        ReflectionUtils.setField(Minecraft.getInstance(), "textureManager", textureManager);
    }

    private void initializeDataFixer()
    {
        ReflectionUtils.setField(Minecraft.getInstance(), "dataFixer", DataFixers.getDataFixer());
    }

    private void initializeGameSettings()
    {
        final Options gameSettings = new Options(Minecraft.getInstance(), new File("./"));
        ReflectionUtils.setField(Minecraft.getInstance(), "gameSettings", gameSettings);
    }

    private void initializeTags(ExistingFileHelper existingFileHelper)
    {
        final ResourceManager resourceManager = (ResourceManager) ReflectionUtils.getField(existingFileHelper, "serverData");
        final TagManager networkTagManager = new TagManager(new RegistryAccess.RegistryHolder());
        AsyncReloadManager.getInstance().reload(resourceManager, networkTagManager);
        StaticTags.resetAll(networkTagManager.getTags());
    }
}
