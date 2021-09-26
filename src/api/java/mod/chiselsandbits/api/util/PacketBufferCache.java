package mod.chiselsandbits.api.util;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;

public class PacketBufferCache
{
    private static final PacketBufferCache INSTANCE = new PacketBufferCache();

    public static PacketBufferCache getInstance()
    {
        return INSTANCE;
    }

    private ThreadLocal<FriendlyByteBuf> cache = new ThreadLocal<FriendlyByteBuf>();

    private PacketBufferCache()
    {
    }

    public FriendlyByteBuf get()
    {
        FriendlyByteBuf bb = cache.get();

        if ( bb == null )
        {
            bb = new FriendlyByteBuf( Unpooled.buffer() );
            cache.set( bb );
        }

        bb.resetReaderIndex();
        bb.resetWriterIndex();

        return bb;
    }
}
