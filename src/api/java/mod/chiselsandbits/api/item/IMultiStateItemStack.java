package mod.chiselsandbits.api.item;

import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.multistate.mutator.IAreaMutator;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.block.AirBlock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

/**
 * The itemstack sensitive version of the multistate item.
 */
public interface IMultiStateItemStack extends IAreaAccessor,
                                                IAreaMutator,
                                                INBTSerializable<CompoundNBT>,
                                                IPacketBufferSerializable
{
}
