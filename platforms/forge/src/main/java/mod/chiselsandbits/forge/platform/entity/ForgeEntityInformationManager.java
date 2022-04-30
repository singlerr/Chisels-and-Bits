package mod.chiselsandbits.forge.platform.entity;

import mod.chiselsandbits.platforms.core.entity.IEntityInformationManager;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;

public class ForgeEntityInformationManager implements IEntityInformationManager
{
    private static final ForgeEntityInformationManager INSTANCE = new ForgeEntityInformationManager();

    public static ForgeEntityInformationManager getInstance()
    {
        return INSTANCE;
    }


    private ForgeEntityInformationManager()
    {
    }


    @Override
    public double getReachDistance(final Player player)
    {
        final AttributeInstance reachAttribute = player.getAttribute(ForgeMod.REACH_DISTANCE.get());
        if (reachAttribute == null)
        {
            return player.isCreative() ? 5f : 4.5f;
        }

        final double reachAttributeValue = reachAttribute.getValue();
        return player.isCreative() ? reachAttributeValue : reachAttributeValue - 0.5D;
    }
}
