package mod.chiselsandbits.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClassUtils
{
    private static final Logger LOGGER = LogManager.getLogger();

    private ClassUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ClassUtils. This is a utility class");
    }

    public static Class<?> getDeclaringClass(
      final Class<?> blkClass,
      final String methodName,
      final Class<?>... args)
    {
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
        catch (final NoClassDefFoundError e)
        {
            LOGGER.warn("Unable to determine blocks eligibility for chiseling, " + blkClass.getName() + " attempted to load " + e.getMessage()
                          + " missing @OnlyIn( Dist.CLIENT ) or @Optional?");
            return blkClass;
        }
        catch (final Throwable t)
        {
            return blkClass;
        }

        return getDeclaringClass(
          blkClass.getSuperclass(),
          methodName,
          args);
    }
}
