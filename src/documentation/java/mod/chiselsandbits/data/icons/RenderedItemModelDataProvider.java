package mod.chiselsandbits.data.icons;

import mod.chiselsandbits.api.item.documentation.IDocumentableItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.data.init.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;

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

    private RenderedItemModelDataProvider(final DataGenerator generator, ExistingFileHelper helper)
    {
        this.generator = generator;
        this.helper = helper;
    }

    @Override
    public void run(@Nonnull DirectoryCache cache) throws IOException
    {
        GameInitializationManager.getInstance().initialize(helper);

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

        ForgeRegistries.ITEMS.forEach(item -> {
            ModelResourceLocation modelLocation = new ModelResourceLocation(
              item.getRegistryName().toString(), "inventory"
            );

            final IBakedModel model = Minecraft.getInstance().getModelManager().getModel(modelLocation);
            if (item instanceof IDocumentableItem) {
                final IDocumentableItem documentableItem = (IDocumentableItem) item;
                documentableItem.getDocumentableInstances(item)
                  .forEach((name, stack) -> itemRenderer.renderModel(model, item.getRegistryName().getNamespace() + "/" + name + ".png", stack));
            }
            else {
                itemRenderer.renderModel(model, item.getRegistryName().getNamespace() + "/" + item.getRegistryName().getPath() + ".png", new ItemStack(item));
            }
        });
        itemRenderer.exportAtlas(((ExtendedModelManager) Minecraft.getInstance().getModelManager()).getTextureMap());
    }

    @Nonnull
    @Override
    public String getName()
    {
        return "Item Renderer";
    }
}
