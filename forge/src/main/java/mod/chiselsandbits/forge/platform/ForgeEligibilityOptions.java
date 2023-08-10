package mod.chiselsandbits.forge.platform;

import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityOptions;
import net.minecraftforge.common.extensions.IForgeBlock;

public final class ForgeEligibilityOptions implements IEligibilityOptions {
    private static final ForgeEligibilityOptions INSTANCE = new ForgeEligibilityOptions();

    public static ForgeEligibilityOptions getInstance() {
        return INSTANCE;
    }

    private ForgeEligibilityOptions() {
    }

    @Override
    public boolean isValidExplosionDefinitionClass(Class<?> explosionDefinitionClass) {
        return IForgeBlock.class == explosionDefinitionClass;
    }

}
