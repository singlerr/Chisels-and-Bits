package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.config.ICommonConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassUtils
{
    private static final Logger LOGGER = LogManager.getLogger();

    private record Key(Class<?> sourceClass, String methodName, Class<?>... args) {}

    private static final SimpleMaxSizedCache<Key, Class<?>> CACHE = new SimpleMaxSizedCache<>(ICommonConfiguration.getInstance().getClassMetadataCacheSize()::get);

    private ClassUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ClassUtils. This is a utility class");
    }

    public static Class<?> getDeclaringClass(
      final Class<?> blkClass,
      final String methodName,
      final Class<?>... args)
    {
        return CACHE.get(
                new Key(blkClass, methodName, args),
                () -> {
                    try
                    {
                        return blkClass.getMethod(methodName, args).getDeclaringClass();
                    }
                    catch (final NoSuchMethodException e)
                    {
                        // nothing here...
                    }
                    catch (final SecurityException e)
                    {
                        // nothing here..
                    }
                    catch (final Throwable e)
                    {
                        return blkClass;
                    }

                    return getDeclaringClass(
                            blkClass.getSuperclass(),
                            methodName,
                            args);
                });
    }
}
