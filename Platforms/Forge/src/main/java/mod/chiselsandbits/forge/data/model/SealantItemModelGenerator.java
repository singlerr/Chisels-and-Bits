package mod.chiselsandbits.forge.data.model;

import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SealantItemModelGenerator extends AbstractInteractableItemModelGenerator
{

    @SubscribeEvent
    public static void dataGeneratorSetup(final GatherDataEvent event)
    {
        event.getGenerator().addProvider(new SealantItemModelGenerator(event.getGenerator(), event.getExistingFileHelper()));
    }

    public SealantItemModelGenerator(final DataGenerator generator, final ExistingFileHelper existingFileHelper)
    {
        super(generator, existingFileHelper, ModItems.SEALANT_ITEM);
    }

    @Override
    public @NotNull String getName()
    {
        return "Sealant item model generator";
    }
}
