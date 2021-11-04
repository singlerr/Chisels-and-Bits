package mod.chiselsandbits.platforms.core.registries;

import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.Validate;

public abstract class AbstractCustomRegistryEntry implements IChiselsAndBitsRegistryEntry
{
    private ResourceLocation registryName;

    @Override
    public ResourceLocation getRegistryName()
    {
        return Validate.notNull(registryName, "Tried to retrieve the registry name before it was set!");
    }

    public void setRegistryName(final ResourceLocation registryName)
    {
        if (this.registryName == null)
        {
            throw new IllegalStateException("Can not set the registry name twice on the same object. Currently: %s tried to set it to: %s".formatted(this.registryName,
              registryName));
        }

        this.registryName = registryName;
    }
}
