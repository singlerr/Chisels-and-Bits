package mod.chiselsandbits.legacy.serialization.blob;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.legacy.LegacyLoadManager;
import mod.chiselsandbits.utils.PaletteUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.IdMapper;
import net.minecraft.world.level.chunk.HashMapPalette;
import net.minecraft.world.level.chunk.PaletteResize;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class NbtBasedPalettedBlobSerializer extends BlobSerializer implements PaletteResize<BlockState>
{
    private final IdMapper<BlockState> registry = GameData.getBlockStateIDMap();
    private final HashMapPalette<BlockState>       palette  = new HashMapPalette<>(this.registry, 16, this, NbtUtils::readBlockState, NbtUtils::writeBlockState);

    public NbtBasedPalettedBlobSerializer(final FriendlyByteBuf toInflate)
    {
        super();

        final CompoundTag wrapper = toInflate.readNbt();
        final ListTag paletteNBT = Objects.requireNonNull(wrapper).getList("data", Constants.NBT.TAG_COMPOUND);
        this.palette.read(paletteNBT);
    }


    @Override
    public void write(final FriendlyByteBuf to)
    {
        final ListTag paletteNBT = new ListTag();
        this.palette.write(paletteNBT);

        final CompoundTag wrapper = new CompoundTag();
        wrapper.put("data", paletteNBT);

        to.writeNbt(wrapper);
    }

    @Override
    protected int readStateID(final FriendlyByteBuf buffer)
    {
        //Not needed because of different palette system.
        return 0;
    }

    @Override
    protected void writeStateID(final FriendlyByteBuf buffer, final int key)
    {
        //Noop
    }

    @Override
    protected int getIndex(final int stateID)
    {
        return this.palette.idFor(IBlockStateIdManager.getInstance().getBlockStateFrom(stateID));
    }

    @Override
    protected int getStateID(final int indexID)
    {
        return IBlockStateIdManager.getInstance().getIdFrom(this.palette.valueFor(indexID));
    }

    @Override
    public int getVersion()
    {
        return LegacyLoadManager.VERSION_COMPACT_PALLETED;
    }

    @Override
    public int onResize(final int newBitSize, @NotNull final BlockState violatingBlockState)
    {
        final List<BlockState> ids = PaletteUtils.getOrderedListInPalette(this.palette);
        ids.forEach(this.palette::idFor);

        return this.palette.idFor(violatingBlockState);
    }
}
