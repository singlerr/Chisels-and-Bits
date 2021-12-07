package mod.chiselsandbits.fabric.platform.chiseling.eligibilty;

import mod.chiselsandbits.platforms.core.chiseling.eligibility.IPlatformEligibilityOptions;

public final class FabricPlatformEligibilityOptions implements IPlatformEligibilityOptions
{
    private static final FabricPlatformEligibilityOptions INSTANCE = new FabricPlatformEligibilityOptions();

    public static FabricPlatformEligibilityOptions getInstance()
    {
        return INSTANCE;
    }

    private FabricPlatformEligibilityOptions()
    {
    }

    @Override
    public boolean isValidExplosionDefinitionClass(final Class<?> explosionDefinitionClass)
    {
        return false;
    }

}
