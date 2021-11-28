package mod.chiselsandbits.forge.platform.eligibility;

import mod.chiselsandbits.platforms.core.chiseling.eligibility.IPlatformEligibilityOptions;
import net.minecraftforge.common.extensions.IForgeBlock;

public class ForgeEligibilityOptions implements IPlatformEligibilityOptions
{
    private static final ForgeEligibilityOptions INSTANCE = new ForgeEligibilityOptions();

    public static ForgeEligibilityOptions getInstance()
    {
        return INSTANCE;
    }

    private ForgeEligibilityOptions()
    {
    }

    @Override
    public boolean isValidExplosionDefinitionClass(final Class<?> explosionDefinitionClass)
    {
        return IForgeBlock.class == explosionDefinitionClass;
    }
}
