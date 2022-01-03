package mod.chiselsandbits.utils;

import java.util.BitSet;

public class ByteArrayUtils
{

    private ByteArrayUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ByteArrayUtils. This is a utility class");
    }

    public static byte[] fill(final int value, final int bitWidth, final int count) {
        final int valueMask = BitUtils.getBitMask(bitWidth);

        final int totalBitCount = bitWidth * count;
        final int byteCount = BitUtils.getByteCount(totalBitCount);

        final byte[] result = new byte[byteCount];

        for (int insertionIndex = 0; insertionIndex < count; insertionIndex++)
        {
            final int bitOffset = insertionIndex * bitWidth;
            setValue(result, value, valueMask, bitOffset);
        }

        return result;
    }

    public static void setValueAt(final byte[] target, final int value, final int bitWidth, final int index) {
        setValue(target, value, bitWidth, index * bitWidth);
    }

    public static void setValueWith(final byte[] target, final int value, final int bitMask, final int index) {
        final int maskWidth = BitUtils.getMaskWidth(bitMask);
        setValue(target, value, bitMask, index * maskWidth);
    }

    public static void setValue(final byte[] target, final int value, final int bitMask, final int bitOffset) {
        final BitSet bitSet = BitSet.valueOf(target);

        final int maskWidth = BitUtils.getMaskWidth(bitMask);
        bitSet.clear(bitOffset, bitOffset + maskWidth);

        for (int i = 0; i < maskWidth; i++)
        {
            final int inValueOffset = maskWidth - i - 1;

            final boolean isSet = ((value >> i) & 1) != 0;
            bitSet.set(bitOffset + i, isSet);
        }

        final byte[] newTarget = bitSet.toByteArray();
        System.arraycopy(newTarget, 0, target, 0, newTarget.length);
    }

    public static int getValueAt(final byte[] target, final int bitWidth, final int index) {
        return getValue(target, BitUtils.getBitMask(bitWidth), index * bitWidth);
    }

    public static int getValue(final byte[] target, final int bitMask, final int bitOffset) {
        final BitSet bitSet = BitSet.valueOf(target);

        final int maskWidth = BitUtils.getMaskWidth(bitMask);

        int result = 0;
        for (int i = 0; i < maskWidth; i++)
        {
            final boolean isSet = bitSet.get(bitOffset + i);
            result |= (isSet ? 1 : 0) << i;
        }

        return result;
    }
}
