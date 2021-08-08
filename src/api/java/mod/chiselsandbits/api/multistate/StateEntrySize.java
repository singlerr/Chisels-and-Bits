package mod.chiselsandbits.api.multistate;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;

/**
 * The size of state entries in the current instance.
 */
public enum StateEntrySize
{
    /**
     * 16 Bits per block.
     */
    ONE_SIXTEENTH(16),

    /**
     * 8 Bits per block.
     */
    ONE_EIGHT(8),

    /**
     * 4 Bits per block.
     */
    ONE_QUARTER(4),

    /**
     * 2 Bits per block.
     */
    ONE_HALF(2);

    public static StateEntrySize current() {
        if (IChiselsAndBitsAPI.getInstance() == null)
            return StateEntrySize.ONE_SIXTEENTH;

        return IChiselsAndBitsAPI.getInstance().getStateEntrySize();
    }

    private final int bitsPerBlockSide;
    private final int   bitsPerBlock;
    private final int   bitsPerLayer;
    private final float sizePerBit;
    private final float sizePerHalfBit;

    StateEntrySize(final int bitsPerBlockSide)
    {
        this.bitsPerBlockSide = bitsPerBlockSide;
        this.bitsPerBlock = this.bitsPerBlockSide * this.bitsPerBlockSide * this.bitsPerBlockSide;
        this.bitsPerLayer = this.bitsPerBlockSide * this.bitsPerBlockSide;
        this.sizePerBit = 1 / ((float) bitsPerBlockSide);
        this.sizePerHalfBit = this.sizePerBit / 2f;
    }

    /**
     * The amount of bits in a single layer per side of the block.
     *
     * @return The amount of bits in a layer on a single side of the block.
     */
    public int getBitsPerBlockSide()
    {
        return bitsPerBlockSide;
    }

    /**
     * The total amount of bits per block.
     * This is {@link #getBitsPerBlockSide()} ^ 3.
     *
     * @return The total amount of bits in a block.
     */
    public int getBitsPerBlock()
    {
        return bitsPerBlock;
    }


    /**
     * The total amount of bits per layer.
     * This is {@link #getBitsPerBlockSide()} ^ 2.
     *
     * @return The total amount of bits in a layer.
     */
    public int getBitsPerLayer()
    {
        return bitsPerLayer;
    }


    /**
     * The size of a single bit if a block is a single unit of length.
     * Is always 1 / {@link #getBitsPerBlockSide()}.
     *
     * @return The size of a bit.
     */
    public float getSizePerBit()
    {
        return sizePerBit;
    }

    /**
     * The size of half a bit if a block is a single unit of length.
     * Is always {@link #getSizePerBit()} / 2.
     *
     * @return The size of half a single bit.
     */
    public float getSizePerHalfBit()
    {
        return sizePerHalfBit;
    }
}
