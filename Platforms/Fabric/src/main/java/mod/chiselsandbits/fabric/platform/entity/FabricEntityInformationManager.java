package mod.chiselsandbits.fabric.platform.entity;

import mod.chiselsandbits.platforms.core.entity.IEntityInformationManager;
import net.minecraft.world.entity.player.Player;

public class FabricEntityInformationManager implements IEntityInformationManager
{
    private static final FabricEntityInformationManager INSTANCE = new FabricEntityInformationManager();

    public static FabricEntityInformationManager getInstance()
    {
        return INSTANCE;
    }

    @Override
    public double getReachDistance(final Player player)
    {
        return 5d;
    }

    private FabricEntityInformationManager()
    {
    }
}
