package mod.chiselsandbits.api.util;

/**
 * Utility class for manipulating arrays.
 */
public class ArrayUtils
{

    private ArrayUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ArrayUtils. This is a utility class");
    }

    /**
     * Multiplies the given array by the given factor.
     *
     * @param input The input array.
     * @param count The factor to multiply the arrays values by.
     * @return A new array with the values of the old array multiplied by the given factor.
     */
    public static float[] multiply(final float[] input, final float count) {
        final float[] result = new float[input.length];
        for (int i = 0; i < input.length; i++)
        {
            result[i] = input[i] * count;
        }

        return result;
    }
}
