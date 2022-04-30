package mod.chiselsandbits.fabric.platform.network;

import mod.chiselsandbits.platforms.core.network.INetworkChannel;
import mod.chiselsandbits.platforms.core.network.INetworkChannelManager;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class FabricNetworkChannelManager implements INetworkChannelManager
{
    private static final FabricNetworkChannelManager INSTANCE = new FabricNetworkChannelManager();

    public static FabricNetworkChannelManager getInstance()
    {
        return INSTANCE;
    }

    private FabricNetworkChannelManager()
    {
    }

    @Override
    public INetworkChannel create(
      final ResourceLocation name, final Supplier<String> networkProtocolVersion, final Predicate<String> clientAcceptedVersions, final Predicate<String> serverAcceptedVersions)
    {
        return new FabricNetworkChannel(
          name
        );
    }
}
