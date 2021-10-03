package mod.chiselsandbits.keys.contexts;

import mod.chiselsandbits.api.item.change.IChangeTrackingItem;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;

import java.util.function.Predicate;

public enum HoldsSpecificItemInHandKeyConflictContext implements IKeyConflictContext
{
    MEASURING_TAPE(item -> ModItems.MEASURING_TAPE.get() == item),
    CHANGE_TRACKING_ITEM(item -> item instanceof IChangeTrackingItem);

    private final Predicate<Item> item;

    HoldsSpecificItemInHandKeyConflictContext(final Predicate<Item> item) {this.item = item;}

    @Override
    public boolean isActive()
    {
        return Minecraft.getInstance().player != null && (item.test(Minecraft.getInstance().player.getMainHandItem().getItem()) ||
          item.test(Minecraft.getInstance().player.getOffhandItem().getItem())) && !KeyConflictContext.GUI.isActive();
    }

    @Override
    public boolean conflicts(final IKeyConflictContext other)
    {
        return this == other;
    }
}
