package mod.chiselsandbits.api.block.entity;

import mod.chiselsandbits.api.block.IMultiStateBlock;
import mod.chiselsandbits.api.multistate.accessor.world.IWorldAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Represents the block entity with the state data, which under-ly the information
 * provided by the {@link IMultiStateBlock} blocks.
 */
public interface IMultiStateBlockEntity extends IWorldAreaAccessor,
                                                          IWorldAreaMutator,
                                                          INBTSerializable<CompoundNBT>,
                                                          IPacketBufferSerializable
{

    /**
     * The statistics of this block.
     *
     * @return The statistics.
     */
    IMultiStateBlockStatistics getStatistics();
}
