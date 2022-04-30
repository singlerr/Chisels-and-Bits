package mod.chiselsandbits.forge.platform.client.key;

import net.minecraftforge.client.settings.IKeyConflictContext;

public class PlatformKeyConflictContextForgeDelegate implements IKeyConflictContext
{
    private final mod.chiselsandbits.platforms.core.client.key.IKeyConflictContext delegate;

    public PlatformKeyConflictContextForgeDelegate(final mod.chiselsandbits.platforms.core.client.key.IKeyConflictContext delegate) {this.delegate = delegate;}

    @Override
    public boolean isActive()
    {
        return delegate.isActive();
    }

    @Override
    public boolean conflicts(final IKeyConflictContext other)
    {
        return delegate.conflicts(new ForgeKeyConflictContextPlatformDelegate(other));
    }
}
