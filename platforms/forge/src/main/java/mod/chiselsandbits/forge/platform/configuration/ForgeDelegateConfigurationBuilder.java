package mod.chiselsandbits.forge.platform.configuration;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.math.Vector4f;
import mod.chiselsandbits.utils.LanguageHandler;
import mod.chiselsandbits.platforms.core.config.IConfigurationBuilder;
import mod.chiselsandbits.platforms.core.dist.DistExecutor;
import net.minecraft.locale.Language;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ForgeDelegateConfigurationBuilder implements IConfigurationBuilder
{

    private final Consumer<ForgeConfigSpec> specConsumer;
    private final Consumer<String> keyConsumer;
    private final ForgeConfigSpec.Builder builder;

    private final Set<String> keys = Sets.newConcurrentHashSet();

    public ForgeDelegateConfigurationBuilder(final Consumer<ForgeConfigSpec> specConsumer, final Consumer<String> keyConsumer) {
        this.specConsumer = specConsumer;
        this.keyConsumer = keyConsumer;
        builder = new ForgeConfigSpec.Builder();
    }

    @Override
    public Supplier<Boolean> defineBoolean(final String key, final boolean defaultValue)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal( "mod.chiselsandbits.config." + key + ".comment"));
        return builder.define(key, defaultValue)::get;
    }

    @Override
    public <T> Supplier<List<? extends T>> defineList(final String key, final List<T> defaultValue, final Class<T> containedType)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal(key + ".comment"));
        return builder.defineList(key, defaultValue, t -> true)::get;
    }

    @Override
    public Supplier<Vector4f> defineVector4f(final String key, final Vector4f defaultValue)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal(key + ".comment"));
        final ArrayList<Double> defaultValueList = Lists.newArrayList(
                (double) defaultValue.x(),
                (double) defaultValue.y(),
                (double) defaultValue.z(),
                (double) defaultValue.w());
        final ForgeConfigSpec.ConfigValue<List<? extends Double>> doubleListValue = builder.defineList(key, defaultValueList, t -> t instanceof Double);
        return () -> {
            final List<? extends Double> list = doubleListValue.get();
            return new Vector4f(
                    list.get(0).floatValue(),
                    list.get(1).floatValue(),
                    list.get(2).floatValue(),
                    list.get(3).floatValue());
        };
    }

    @Override
    public Supplier<String> defineString(final String key, final String defaultValue)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal(key + ".comment"));
        return builder.define(key, defaultValue)::get;
    }

    @Override
    public Supplier<Long> defineLong(final String key, final long defaultValue, final long minValue, final long maxValue)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal(key + ".comment"));
        return builder.defineInRange(key, defaultValue, minValue, maxValue)::get;
    }

    @Override
    public Supplier<Integer> defineInteger(final String key, final int defaultValue, final int minValue, final int maxValue)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal(key + ".comment"));
        return builder.defineInRange(key, defaultValue, minValue, maxValue)::get;
    }

    @Override
    public Supplier<Double> defineDouble(final String key, final double defaultValue, final double minValue, final double maxValue)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal(key + ".comment"));
        return builder.defineInRange(key, defaultValue, minValue, maxValue)::get;
    }

    @Override
    public <T extends Enum<T>> Supplier<T> defineEnum(final String key, final T defaultValue)
    {
        keys.add(key + ".comment");
        builder.comment(translateToLocal(key + ".comment"));
        return builder.defineEnum(key, defaultValue)::get;
    }

    @Override
    public void setup()
    {
        keys.forEach(keyConsumer);
        specConsumer.accept(builder.build());
    }

    public static String translateToLocal(
      final String string )
    {
        return DistExecutor.unsafeRunForDist(
          () -> () -> {
              final String translated = Language.getInstance().getOrDefault(string);
              if (translated.equals(string))
                  return LanguageHandler.translateKey(string);

              return translated;
          },
          () -> () -> LanguageHandler.translateKey(string)
        );
    }
}
