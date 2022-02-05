package mod.chiselsandbits.api.multistate;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraft.world.phys.Vec3;

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
    ONE_HALF(2),

    /**
     * 1 Bit per block side.
     * Generally only used for testing.
     */
    ONE(1);

    private static StateEntrySize _current = null;

    public static StateEntrySize current() {
        if (_current == null) {
            if (IChiselsAndBitsAPI.getInstance() == null)
                _current = StateEntrySize.ONE_SIXTEENTH;
            else
                _current = IChiselsAndBitsAPI.getInstance().getStateEntrySize();
        }

        return _current;
    }

    private final int   bitsPerBlockSide;
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
     * Returns the vector used to scale down another vector with the size of a single bit.
     * Useful for passing to {@link net.minecraft.world.phys.Vec3#multiply(Vec3)}
     *
     * @return The scaling vector.
     */
    public Vec3 getSizePerBitScalingVector()
    {
        return new Vec3(sizePerBit, sizePerBit, sizePerBit);
    }

    /**
     * Returns the vector used to scale down another vector with the size of half a bit.
     * Useful for passing to {@link net.minecraft.world.phys.Vec3#multiply(Vec3)}
     *
     * @return The scaling vector.
     */
    public Vec3 getSizePerHalfBitScalingVector()
    {
        return new Vec3(sizePerHalfBit, sizePerHalfBit, sizePerHalfBit);
    }

    /**
     * Returns the vector used to scale up another vector with the amount of bits on a given side.
     * Useful for passing to {@link Vec3#multiply(Vec3)}
     *
     * @return The scaling vector.
     */
    public Vec3 getBitsPerBlockSideScalingVector()
    {
        return new Vec3(bitsPerBlockSide, bitsPerBlockSide, bitsPerBlockSide);
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

    /**
     * The y coordinate of the upper of the block.
     *
     * @return The y coordinate.
     */
    public float upperLevelY() {
        return getBitsPerLayer() - 1;
    }
}
