package mod.chiselsandbits.api.item.multistate;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * The statistics of a multi state itemstack.
 */
public interface IStatistics extends INBTSerializable<CompoundTag>
{

    /**
     * The primary state of the mutli state itemstacks this statistics object
     * belongs to.
     *
     * @return The primary blockstate.
     */
    BlockState getPrimaryState();

    boolean isEmpty();
}
