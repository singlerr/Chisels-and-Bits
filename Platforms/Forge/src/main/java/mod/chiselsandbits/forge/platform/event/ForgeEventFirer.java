package mod.chiselsandbits.forge.platform.event;

import mod.chiselsandbits.platforms.core.event.IEventFirer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

public class ForgeEventFirer implements IEventFirer
{
    private static final ForgeEventFirer INSTANCE = new ForgeEventFirer();

    public static ForgeEventFirer getInstance()
    {
        return INSTANCE;
    }

    private ForgeEventFirer()
    {
    }

    @Override
    public boolean canBreakBlock(final Level level, final BlockPos position, final Player playerEntity)
    {
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, position, level.getBlockState(position), playerEntity);
        MinecraftForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}
