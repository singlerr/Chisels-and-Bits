package mod.chiselsandbits.events;

import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EntityItemPickupEventHandler
{

    @SubscribeEvent
    public static void pickupItems(
      final EntityItemPickupEvent event)
    {
        final ItemEntity entityItem = event.getItem();
        if (entityItem != null)
        {
            final ItemStack itemStack = entityItem.getItem();
            final Player player = event.getPlayer();
            if (!itemStack.isEmpty() && itemStack.getItem() instanceof final IBitItem bitItem)
            {
                final IBitInventory playerInventory = IBitInventoryManager.getInstance().create(player);
                final BlockState containedState = bitItem.getBitState(itemStack);
                final int insertionCount = Math.min(itemStack.getCount(), playerInventory.getMaxInsertAmount(containedState));

                playerInventory.insert(containedState, insertionCount);

                itemStack.setCount(itemStack.getCount() - insertionCount);

                if (itemStack.isEmpty())
                {
                    entityItem.remove(Entity.RemovalReason.DISCARDED);
                }

                event.setCanceled(true);
            }
        }
    }
}