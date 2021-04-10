package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TranslationUtils
{

    private TranslationUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: TranslationUtils. This is a utility class");
    }

    public static IFormattableTextComponent build(final String keySuffix) {
        return new TranslationTextComponent(String.format("mod.%s.%s", Constants.MOD_ID, keySuffix));
    }

    public static IFormattableTextComponent build(final LocalStrings chiselSupportTagBlackListed)
    {
        return new TranslationTextComponent(chiselSupportTagBlackListed.toString());
    }
}
