package mod.chiselsandbits.api.util;

/**
 * Utility class for processing colors.
 */
public class ColorUtils
{

    /**
     * The maximal value for a given color channel.
     */
    public static final int FULL_CHANNEL = 255;
    /**
     * The minimal value for a given color channel.
     */
    public static final int EMPTY_CHANNEL = 0;

    private ColorUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ColorUtils. This is a utility class");
    }

    /**
     * Packs all three color channels (plus a full alpha channel) with the same value into an integer.
     *
     * @param c The value of the color channels to pack.
     * @return A packed integer, representing the color with all three channels set to the same value.
     */
    public static int pack(final int c) {
        return pack(c, c, c);
    }

    /**
     * Packs all three color channels (plus a full alpha channel) with the values into an integer.
     *
     * @param r The value for the red channel.
     * @param g The value for the green channel.
     * @param b The value for the blue channel.
     *
     * @return A packed integer, representing the color.
     */
    public static int pack(final int r, final int g, final int b) {
        return pack(r, g, b, FULL_CHANNEL);
    }

    /**
     * Packs all four color channels with the values into an integer.
     *
     * @param r The value for the red channel.
     * @param g The value for the green channel.
     * @param b The value for the blue channel.
     * @param a The value for the alpha channel.
     * @return A packed integer, representing the color.
     */
    public static int pack(final int r, final int g, final int b, final int a) {
        int color = 0;

        color |= (a & FULL_CHANNEL) << 24;
        color |= (r & FULL_CHANNEL) << 16;
        color |= (g & FULL_CHANNEL) << 8;
        color |= (b & FULL_CHANNEL);

        return color;
    }
}
