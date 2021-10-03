package mod.chiselsandbits.api.util;

public class ArrayUtils
{

    private ArrayUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ArrayUtils. This is a utility class");
    }

    public static float[] multiply(final float[] input, final float count) {
        final float[] result = new float[input.length];
        for (int i = 0; i < input.length; i++)
        {
            result[i] = input[i] * count;
        }

        return result;
    }
}
