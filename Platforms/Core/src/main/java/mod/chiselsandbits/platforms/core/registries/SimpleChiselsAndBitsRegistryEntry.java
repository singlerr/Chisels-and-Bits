package mod.chiselsandbits.platforms.core.registries;

import net.minecraft.resources.ResourceLocation;

public class SimpleChiselsAndBitsRegistryEntry implements IChiselsAndBitsRegistryEntry
{
    private ResourceLocation name;

    @Override
    public ResourceLocation getRegistryName()
    {
        return name;
    }

    public void setRegistryName(final ResourceLocation name)
    {
        this.name = name;
    }
}
