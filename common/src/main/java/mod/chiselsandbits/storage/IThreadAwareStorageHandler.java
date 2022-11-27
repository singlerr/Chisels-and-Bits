package mod.chiselsandbits.storage;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import net.minecraft.nbt.CompoundTag;

public interface IThreadAwareStorageHandler<P> extends IStorageHandler {
    P deserializeNBTOffThread(CompoundTag nbt);

    void savePayload(P payload);
}
