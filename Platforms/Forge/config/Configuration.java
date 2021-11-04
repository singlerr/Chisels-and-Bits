package mod.chiselsandbits.api.config;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Mod root configuration.
 */
public class Configuration
{

    public static final Configuration getInstance() {
        return IChiselsAndBitsAPI.getInstance().getConfiguration();
    }

    private final ClientConfiguration clientConfig;
    private final ServerConfiguration serverConfig;
    private final CommonConfiguration commonConfig;

    private final ForgeConfigSpec clientConfigSpec;
    private final ForgeConfigSpec commonConfigSpec;
    private final ForgeConfigSpec serverConfigSpec;

    /**
     * Builds configuration tree.
     *
     * @param modContainer from event
     */
    public Configuration(final ModContainer modContainer)
    {
        final Pair<ClientConfiguration, ForgeConfigSpec> cli = new ForgeConfigSpec.Builder().configure(ClientConfiguration::new);
        final Pair<ServerConfiguration, ForgeConfigSpec> ser = new ForgeConfigSpec.Builder().configure(ServerConfiguration::new);
        final Pair<CommonConfiguration, ForgeConfigSpec> com = new ForgeConfigSpec.Builder().configure(CommonConfiguration::new);

        final ModConfig client = new ModConfig(ModConfig.Type.CLIENT, cli.getRight(), modContainer);
        final ModConfig server = new ModConfig(ModConfig.Type.SERVER, ser.getRight(), modContainer);
        final ModConfig common = new ModConfig(ModConfig.Type.COMMON, com.getRight(), modContainer);

        clientConfig = cli.getLeft();
        serverConfig = ser.getLeft();
        commonConfig = com.getLeft();

        clientConfigSpec = cli.getRight();
        serverConfigSpec = ser.getRight();
        commonConfigSpec = com.getRight();

        modContainer.addConfig(client);
        modContainer.addConfig(server);
        modContainer.addConfig(common);
    }

    public ClientConfiguration getClient()
    {
        return clientConfig;
    }

    public ServerConfiguration getServer()
    {
        return serverConfig;
    }

    public CommonConfiguration getCommon()
    {
        return commonConfig;
    }
}