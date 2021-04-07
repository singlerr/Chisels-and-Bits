package mod.chiselsandbits.api.item.multistate;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * The statistics of a multi state itemstack.
 */
public interface IStatistics extends INBTSerializable<CompoundNBT>
{

    /**
     * The primary state of the mutli state itemstacks this statistics object
     * belongs to.
     *
     * @return The primary blockstate.
     */
    BlockState getPrimaryState();
}
