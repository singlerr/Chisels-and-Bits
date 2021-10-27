package mod.chiselsandbits.api.config;

import com.google.common.collect.Sets;
import mod.chiselsandbits.api.util.DeprecationHelper;
import mod.chiselsandbits.api.util.constants.Constants;
import net.minecraftforge.common.ForgeConfigSpec.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public abstract class AbstractConfiguration
{
    public static final Set<String> LANG_KEYS = Sets.newLinkedHashSet();

    private static       String              currentCategory      = "";
    private static final LinkedList<String> categoryStack = new LinkedList<>();
    private static final LinkedList<Integer> additionalDepthCount = new LinkedList<>();

    public AbstractConfiguration()
    {
        categoryStack.push("");
    }

    protected static void createCategory(final Builder builder, String key)
    {
        final String originalKey = key;
        if (!currentCategory.isEmpty())
            key = currentCategory + "." + key;

        final String[] keySections = key.split("\\.");
        if (keySections.length > 1) {
            String workingKey = "";
            for (int i = 0; i < keySections.length - 1; i++)
            {
                if (!workingKey.isEmpty())
                    workingKey += ".";

                workingKey += keySections[i];
                final String translation = DeprecationHelper.translateToLocal(commentTKey(workingKey));
                builder.comment(translation == null || translation.isEmpty() ? workingKey : translation).push(keySections[i]);
            }
        }

        final String translation = DeprecationHelper.translateToLocal(commentTKey(key));
        builder.comment(translation == null || translation.isEmpty() ? key : translation).push(keySections[keySections.length - 1]);
        currentCategory = key;
        additionalDepthCount.addFirst(originalKey.split("\\.").length);
        categoryStack.push(key);
    }

    protected static void swapToCategory(final Builder builder, final String key)
    {
        finishCategory(builder);
        createCategory(builder, key);
    }

    protected static void finishCategory(final Builder builder)
    {
        if (currentCategory.isEmpty())
            return;

        final int popCount = additionalDepthCount.removeFirst();
        for (int i = 0; i < popCount; i++)
        {
            builder.pop();
        }
        categoryStack.removeFirst();
        currentCategory = categoryStack.peekFirst();
    }

    private static String nameTKey(final String key)
    {
        return currentCategory.isEmpty() ? String.format("mod.%s.config.%s", Constants.MOD_ID, key) : String.format("mod.%s.config.%s.%s", Constants.MOD_ID, currentCategory, key);
    }

    private static String commentTKey(final String key)
    {
        final String tComKey = String.format("%s.comment", nameTKey(key));
        LANG_KEYS.add(tComKey);
        return tComKey;
    }

    private static Builder buildBase(final Builder builder, final String key)
    {
        final String translation = DeprecationHelper.translateToLocal(commentTKey(key));
        return  builder.comment(translation == null || translation.isEmpty() ? key : translation).translation(nameTKey(key));
    }

    protected static BooleanValue defineBoolean(final Builder builder, final String key, final boolean defaultValue)
    {
        return buildBase(builder, key).define(key, defaultValue);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue)
    {
        return defineInteger(builder, key, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    protected static IntValue defineInteger(final Builder builder, final String key, final int defaultValue, final int min, final int max)
    {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue)
    {
        return defineLong(builder, key, defaultValue, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    protected static LongValue defineLong(final Builder builder, final String key, final long defaultValue, final long min, final long max)
    {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue)
    {
        return defineDouble(builder, key, defaultValue, Double.MIN_VALUE, Double.MAX_VALUE);
    }

    protected static DoubleValue defineDouble(final Builder builder, final String key, final double defaultValue, final double min, final double max)
    {
        return buildBase(builder, key).defineInRange(key, defaultValue, min, max);
    }

    protected static <T> ConfigValue<List<? extends T>> defineList(
        final Builder builder,
        final String key,
        final List<? extends T> defaultValue,
        final Predicate<Object> elementValidator)
    {
        return buildBase(builder, key).defineList(key, defaultValue, elementValidator);
    }

    protected static <V extends Enum<V>> EnumValue<V> defineEnum(final Builder builder, final String key, final V defaultValue)
    {
        return buildBase(builder, key).defineEnum(key, defaultValue);
    }

    protected static ConfigValue<String> defineString(final Builder builder, final String key, final String defaultValue) {
        return buildBase(builder, key).define(key, defaultValue);
    }
}
