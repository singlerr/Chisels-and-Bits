package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.item.bit.IBitItemManager;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class BitInventoryUtils
{

    private BitInventoryUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: BitInventoryUtils. This is a utility class");
    }

    public static void insertIntoOrSpawn(final PlayerEntity playerEntity, final BlockState blockState, final int count) {
        if (playerEntity == null || playerEntity.getCommandSenderWorld().isClientSide() || count <= 0)
            return;

        final IBitInventory inventory = IBitInventoryManager.getInstance().create(playerEntity);

        if (playerEntity.isCreative()) {
            if (inventory.canExtractOne(blockState))
                return;

            if (inventory.canInsertOne(blockState)) {
                inventory.insertOne(blockState);
            }
            return;
        }

        final int maxInsertionCount = inventory.getMaxInsertAmount(blockState);

        final int insertionCount = Math.min(maxInsertionCount, count);
        inventory.insert(blockState, insertionCount);

        int leftOverCount = count - insertionCount;
        if (leftOverCount <= 0)
            return;

        while(leftOverCount > 0) {
            final int spawnCount = Math.min(IBitItemManager.getInstance().getMaxStackSize(), leftOverCount);
            if (spawnCount <= 0)
                break;

            leftOverCount -= spawnCount;

            final ItemStack spawnStack = IBitItemManager.getInstance().create(blockState, spawnCount);
            playerEntity.drop(spawnStack, true, true);
        }
    }
}
