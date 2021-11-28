package mod.chiselsandbits.forge.client.events;

import mod.chiselsandbits.client.registrars.ModISTER;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import mod.chiselsandbits.client.screens.BitBagScreen;
import mod.chiselsandbits.client.screens.ChiseledPrinterScreen;
import mod.chiselsandbits.client.screens.ModificationTableScreen;
import mod.chiselsandbits.registrars.ModContainerTypes;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientInitHandler
{

    @SubscribeEvent
    public static void onFMLClientSetup(final FMLClientSetupEvent event)
    {
        event.enqueueWork(() -> {
            MenuScreens.register(
              ModContainerTypes.BIT_BAG.get(),
              BitBagScreen::new
            );
            MenuScreens.register(
              ModContainerTypes.MODIFICATION_TABLE.get(),
              ModificationTableScreen::new
            );
            MenuScreens.register(
              ModContainerTypes.CHISELED_PRINTER_CONTAINER.get(),
              ChiseledPrinterScreen::new
            );
        });

        event.enqueueWork(() -> {
            ItemProperties.register(ModItems.MEASURING_TAPE.get(), new ResourceLocation(Constants.MOD_ID, "is_measuring"), (stack, clientWorld, livingEntity, value) -> {
                if (stack.getItem() != ModItems.MEASURING_TAPE.get())
                    return 0;

                return ModItems.MEASURING_TAPE.get().getStart(stack).isPresent() ? 1 : 0;
            });
        });

        ModISTER.onClientInit();
    }
}
