package mod.chiselsandbits.forge.data.model;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.model.generators.CustomLoaderBuilder;
import net.minecraftforge.client.model.generators.ItemModelBuilder;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.BiFunction;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class QuillItemModelGenerator extends AbstractInteractableItemModelGenerator
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new QuillItemModelGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    public QuillItemModelGenerator(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, existingFileHelper, ModItems.QUILL);
    }

    @Override
    public @NotNull String getName()
    {
        return "Quill item model generator";
    }
}
