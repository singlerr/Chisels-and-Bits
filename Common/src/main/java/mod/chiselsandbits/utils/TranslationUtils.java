package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.platforms.core.util.constants.Constants;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class TranslationUtils
{

    private TranslationUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: TranslationUtils. This is a utility class");
    }

    public static MutableComponent build(final String keySuffix, final Object... args) {
        return new TranslatableComponent(String.format("mod.%s.%s", Constants.MOD_ID, keySuffix), args);
    }

    public static MutableComponent build(final LocalStrings chiselSupportTagBlackListed, final Object... args)
    {
        return new TranslatableComponent(chiselSupportTagBlackListed.toString(), args);
    }
}
