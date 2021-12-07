package mod.chiselsandbits.fabric.platform.event;

import mod.chiselsandbits.platforms.core.event.IEventFirer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class FabricEventFirer implements IEventFirer
{
    private static final FabricEventFirer INSTANCE = new FabricEventFirer();

    public static FabricEventFirer getInstance()
    {
        return INSTANCE;
    }

    private FabricEventFirer()
    {
    }

    @Override
    public boolean canBreakBlock(final Level level, final BlockPos position, final Player playerEntity)
    {
        return true;
    }
}
