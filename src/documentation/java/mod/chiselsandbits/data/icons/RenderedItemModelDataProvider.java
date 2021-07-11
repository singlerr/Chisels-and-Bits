package mod.chiselsandbits.data.icons;

import com.mojang.blaze3d.systems.RenderSystem;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11C.GL_FALSE;
import static org.lwjgl.system.MemoryUtil.NULL;

import static org.lwjgl.glfw.GLFW.glfwInit;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class RenderedItemModelDataProvider implements IDataProvider
{
    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new RenderedItemModelDataProvider(event.getGenerator(), event.getExistingFileHelper()));
    }

    private final DataGenerator generator;
    private final ExistingFileHelper helper;
    private final GLFWErrorCallback  loggingErrorCallback = GLFWErrorCallback.create((error, description) -> {
        System.err.println("Error "+error+": "+description);
    });

    private RenderedItemModelDataProvider(final DataGenerator generator, ExistingFileHelper helper)
    {
        this.generator = generator;
        this.helper = helper;
    }

    @Override
    public void act(@Nonnull DirectoryCache cache) throws IOException
    {

        // Hack together something that may work?
        if(!glfwInit())
            throw new RuntimeException("Failed to initialize GLFW???");
        RenderSystem.initRenderThread();
        glfwSetErrorCallback(loggingErrorCallback);
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        long window = glfwCreateWindow(512, 512, "Hello World!", NULL, NULL);
        glfwMakeContextCurrent(window);
        GL.createCapabilities();

        IResourceManager resourceManager = null;
        try
        {
            final Field clientResourcesField = helper.getClass().getDeclaredField("clientResources");
            clientResourcesField.setAccessible(true);
            resourceManager = (IResourceManager) clientResourcesField.get(helper);
        }
        catch (IllegalAccessException | NoSuchFieldException e)
        {
            throw new IllegalStateException("Failed to load the resourceManager for exporting icons.", e);
        }

        MinecraftInstanceManager.getInstance().initialize(resourceManager);

        ModelLoadingHandler.loadAndBake();

        final Path itemOutputDirectory = this.generator.getOutputFolder().resolve("icons/item");
        if (Files.exists(itemOutputDirectory)) {
            Stream<Path> files = Files.walk(itemOutputDirectory);
            // delete directory including files and sub-folders
            files.sorted(Comparator.reverseOrder())
              .map(Path::toFile)
              .forEach(File::delete);
            Files.deleteIfExists(this.generator.getOutputFolder().resolve("icons/item"));
        }
        ModelRenderer itemRenderer = new ModelRenderer(512, 512, itemOutputDirectory.toFile());

        /*ForgeRegistries.ITEMS.forEach(item -> {
            ModelResourceLocation modelLocation = new ModelResourceLocation(
              item.getRegistryName().toString(), "inventory"
            );
            itemRenderer.renderModel(Minecraft.getInstance().getModelManager().getModel(modelLocation), item.getRegistryName().getNamespace() + "/" + item.getRegistryName().getPath() + ".png", item);
        });*/

        final Item item = Items.AIR;
        ModelResourceLocation modelLocation = new ModelResourceLocation(
          item.getRegistryName().toString(), "inventory"
        );
        itemRenderer.renderModel(Minecraft.getInstance().getModelManager().getModel(modelLocation), item.getRegistryName().getNamespace() + "/" + item.getRegistryName().getPath() + ".png", item);

        itemRenderer.exportAtlas(((ExtendedModelManager) Minecraft.getInstance().getModelManager()).getTextureMap());
        glfwTerminate();
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "Item Renderer";
    }
}
