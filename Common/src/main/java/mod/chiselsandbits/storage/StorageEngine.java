package mod.chiselsandbits.storage;

import mod.chiselsandbits.api.util.INBTSerializable;
import mod.chiselsandbits.api.util.IPacketBufferSerializable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;

public class StorageEngine implements IPacketBufferSerializable, INBTSerializable<CompoundTag>
{

    private final LinkedList<IStorageVersion> versions;

    public StorageEngine(

    ) {}

    @Override
    public CompoundTag serializeNBT()
    {
        return null;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {

    }

    @Override
    public void serializeInto(final @NotNull FriendlyByteBuf packetBuffer)
    {

    }

    @Override
    public void deserializeFrom(final @NotNull FriendlyByteBuf packetBuffer)
    {

    }
}
