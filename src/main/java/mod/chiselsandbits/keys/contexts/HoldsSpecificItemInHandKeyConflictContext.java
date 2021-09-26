package mod.chiselsandbits.keys.contexts;

import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

import java.util.function.Supplier;

public enum HoldsSpecificItemInHandKeyConflictContext implements IKeyConflictContext
{
    MEASURING_TAPE(ModItems.MEASURING_TAPE::get);

    private final Supplier<Item> item;

    HoldsSpecificItemInHandKeyConflictContext(final Supplier<Item> item) {this.item = item;}

    @Override
    public boolean isActive()
    {
        return (Minecraft.getInstance().player.getMainHandItem().getItem() == item.get() ||
          Minecraft.getInstance().player.getOffhandItem().getItem() == item.get()) && !KeyConflictContext.GUI.isActive();
    }

    @Override
    public boolean conflicts(final IKeyConflictContext other)
    {
        return this == other;
    }
}
