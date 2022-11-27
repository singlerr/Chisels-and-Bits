package mod.chiselsandbits.storage;

import net.minecraft.nbt.CompoundTag;

public interface IThreadAwareStorageEngine extends IStorageEngine {

    record HandlerWithData(IThreadAwareStorageHandler<?> handler, CompoundTag data) {}

    HandlerWithData getThreadAwareStorageHandler(CompoundTag tag);
}
