package mod.chiselsandbits.fabric.platform.client.rendering;

import mod.chiselsandbits.fabric.mixin.platform.client.MinecraftAccessor;
import mod.chiselsandbits.platforms.core.client.rendering.IColorManager;
import net.minecraft.client.Minecraft;

import java.util.function.Consumer;

public final class FabricColorManager implements IColorManager
{
    private static final FabricColorManager INSTANCE = new FabricColorManager();

    public static FabricColorManager getInstance()
    {
        return INSTANCE;
    }

    private FabricColorManager()
    {
    }

    @Override
    public void setupBlockColors(final Consumer<IBlockColorSetter> configurator)
    {
        final IBlockColorSetter setter = (colorManager, blocks) -> Minecraft.getInstance().getBlockColors().register(colorManager, blocks);

        configurator.accept(setter);
    }

    @Override
    public void setupItemColors(final Consumer<IItemColorSetter> configurator)
    {
        final IItemColorSetter setter = (colorManager, items) -> ((MinecraftAccessor) Minecraft.getInstance()).getItemColors().register(colorManager, items);

        configurator.accept(setter);
    }

}
