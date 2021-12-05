package mod.chiselsandbits.forge.platform.client.color;

import com.google.common.collect.Lists;
import mod.chiselsandbits.platforms.core.client.rendering.IColorManager;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeColorManager implements IColorManager
{
    private static final ForgeColorManager INSTANCE = new ForgeColorManager();

    public static ForgeColorManager getInstance()
    {
        return INSTANCE;
    }
    
    private final List<Consumer<IBlockColorSetter>> blockColorSetters = Lists.newArrayList();
    private final List<Consumer<IItemColorSetter>> itemColorSetters = Lists.newArrayList();

    private ForgeColorManager()
    {
    }

    @Override
    public void setupBlockColors(final Consumer<IBlockColorSetter> configurator)
    {
        blockColorSetters.add(configurator);
    }

    @Override
    public void setupItemColors(final Consumer<IItemColorSetter> configurator)
    {
        itemColorSetters.add(configurator);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBlockColorHandler(final ColorHandlerEvent.Block event)
    {
        ForgeColorManager.getInstance().blockColorSetters.forEach(
            c -> c.accept(event.getBlockColors()::register)
        );
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onItemColorHandler(final ColorHandlerEvent.Item event)
    {
        ForgeColorManager.getInstance().itemColorSetters.forEach(
          c -> c.accept(event.getItemColors()::register)
        );
    }
}
