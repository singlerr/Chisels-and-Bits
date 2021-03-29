package mod.chiselsandbits.api.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;

public class PacketBufferCache
{
    private static final PacketBufferCache INSTANCE = new PacketBufferCache();

    public static PacketBufferCache getInstance()
    {
        return INSTANCE;
    }

    private ThreadLocal<PacketBuffer> cache = new ThreadLocal<PacketBuffer>();

    private PacketBufferCache()
    {
    }

    public PacketBuffer get()
    {
        PacketBuffer bb = cache.get();

        if ( bb == null )
        {
            bb = new PacketBuffer( Unpooled.buffer() );
            cache.set( bb );
        }

        bb.resetReaderIndex();
        bb.resetWriterIndex();

        return bb;
    }
}
