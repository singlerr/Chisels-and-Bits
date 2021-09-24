package mod.chiselsandbits.chiseling.metadata;

import mod.chiselsandbits.api.chiseling.metadata.IMetadataKey;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class SimpleMetadataKey<T> extends ForgeRegistryEntry<IMetadataKey<?>> implements IMetadataKey<T>
{
}
