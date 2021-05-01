package mod.chiselsandbits.legacy.serialization.blob;

import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.utils.PaletteUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.palette.*;
import net.minecraftforge.registries.GameData;

import java.util.List;

public class PalettedBlobSerializer extends BlobSerializer implements IResizeCallback<BlockState>
{
    private final ObjectIntIdentityMap<BlockState> registry        = GameData.getBlockStateIDMap();
    private final IPalette<BlockState>             registryPalette = new IdentityPalette<>(GameData.getBlockStateIDMap(), Blocks.AIR.getDefaultState());
    private       IPalette<BlockState>             palette         = new IdentityPalette<>(GameData.getBlockStateIDMap(), Blocks.AIR.getDefaultState());
    private int bits = 0;

    public PalettedBlobSerializer(final PacketBuffer toInflate)
    {
        super();
        this.setBits(4);

        //Setup the palette ids.
        this.setBits(toInflate.readVarInt());
        PaletteUtils.read(this.palette, toInflate);
    }

    private void setBits(int bitsIn) {
        setBits(bitsIn, false);
    }

    private void setBits(int bitsIn, boolean forceBits) {
        if (bitsIn != this.bits) {
            this.bitsPerInt = bitsIn;
            this.bitsPerIntMinus1 = bitsIn - 1;

            this.bits = bitsIn;
            if (this.bits <= 8) {
                this.bits = 4;
                this.palette = new ArrayPalette<>(this.registry, this.bits, this, NBTUtil::readBlockState);
            } else if (this.bits < 17) {
                this.palette = new HashMapPalette<>(this.registry, this.bits, this, NBTUtil::readBlockState, NBTUtil::writeBlockState);
            } else {
                this.palette = this.registryPalette;
                this.bits = MathHelper.log2DeBruijn(this.registry.size());
                if (forceBits)
                    this.bits = bitsIn;
            }

            this.palette.idFor(Blocks.AIR.getDefaultState());
        }
    }


    @Override
    public void write(final PacketBuffer to)
    {
        to.writeVarInt(this.bits);
        this.palette.write(to);
    }

    @Override
    protected int readStateID(final PacketBuffer buffer)
    {
        //Not needed because of different palette system.
        return 0;
    }

    @Override
    protected void writeStateID(final PacketBuffer buffer, final int key)
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
        return IBlockStateIdManager.getInstance().getIdFrom(this.palette.get(indexID));
    }

    @Override
    public int getVersion()
    {
        return 3;
    }

    @Override
    public int onResize(final int newBitSize, final BlockState violatingBlockState)
    {
        final IPalette<BlockState> currentPalette = this.palette;
        this.setBits(newBitSize);

        final List<BlockState> ids = PaletteUtils.getOrderedListInPalette(currentPalette);
        ids.forEach(this.palette::idFor);

        return this.palette.idFor(violatingBlockState);
    }
}
