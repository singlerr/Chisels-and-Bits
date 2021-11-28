package mod.chiselsandbits.forge.platform.network;

import mod.chiselsandbits.platforms.core.network.INetworkChannel;
import mod.chiselsandbits.platforms.core.network.INetworkChannelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ForgeNetworkChannelManager implements INetworkChannelManager
{
    private static final ForgeNetworkChannelManager INSTANCE = new ForgeNetworkChannelManager();

    public static ForgeNetworkChannelManager getInstance()
    {
        return INSTANCE;
    }

    private ForgeNetworkChannelManager()
    {
    }

    @Override
    public INetworkChannel create(
      final ResourceLocation name, final Supplier<String> networkProtocolVersion, final Predicate<String> clientAcceptedVersions, final Predicate<String> serverAcceptedVersions)
    {
        return new ForgeSimpleChannelPlatformDelegate(NetworkRegistry.newSimpleChannel(name, networkProtocolVersion, clientAcceptedVersions, serverAcceptedVersions));
    }
}
