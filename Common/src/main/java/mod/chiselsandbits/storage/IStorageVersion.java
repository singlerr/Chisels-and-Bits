package mod.chiselsandbits.storage;

import mod.chiselsandbits.api.util.INBTSerializable;
import net.minecraft.nbt.CompoundTag;

public interface IStorageVersion extends INBTSerializable<CompoundTag>
{

    boolean matchesVersion(final CompoundTag tagToCheck);
}
