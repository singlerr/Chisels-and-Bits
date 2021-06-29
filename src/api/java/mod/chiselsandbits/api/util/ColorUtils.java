package mod.chiselsandbits.api.util;

import org.lwjgl.system.CallbackI;

public class ColorUtils
{

    public static final int FULL_CHANNEL = 255;
    public static final int EMPTY_CHANNEL = 0;

    private ColorUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ColorUtils. This is a utility class");
    }

    public static int pack(final int c) {
        return pack(c, c, c);
    }

    public static int pack(final int r, final int g, final int b) {
        return pack(r, g, b, FULL_CHANNEL);
    }

    @SuppressWarnings("PointlessBitwiseExpression")
    public static int pack(final int r, final int g, final int b, final int a) {
        int color = 0;

        color |= (a & FULL_CHANNEL) << 24;
        color |= (r & FULL_CHANNEL) << 16;
        color |= (g & FULL_CHANNEL) << 8;
        color |= (b & FULL_CHANNEL) << 0;

        return color;
    }
}
