package mod.chiselsandbits.fabric.platform;

import mod.chiselsandbits.fabric.platform.chiseling.eligibilty.FabricPlatformEligibilityOptions;
import mod.chiselsandbits.fabric.platform.client.FabricClientManager;
import mod.chiselsandbits.fabric.platform.configuration.FabricConfigurationManager;
import mod.chiselsandbits.fabric.platform.creativetab.FabricCreativeTabManager;
import mod.chiselsandbits.fabric.platform.dist.FabricDistributionManager;
import mod.chiselsandbits.fabric.platform.entity.FabricEntityInformationManager;
import mod.chiselsandbits.fabric.platform.event.FabricEventFirer;
import mod.chiselsandbits.fabric.platform.fluid.FabricFluidManager;
import mod.chiselsandbits.fabric.platform.inventory.FabricAdaptingBitInventoryManager;
import mod.chiselsandbits.fabric.platform.inventory.FabricPlayerInventoryManager;
import mod.chiselsandbits.fabric.platform.item.FabricDyeItemHelper;
import mod.chiselsandbits.fabric.platform.item.FabricItemComparisonHelper;
import mod.chiselsandbits.fabric.platform.level.FabricLevelBasedPropertyAccessor;
import mod.chiselsandbits.fabric.platform.network.FabricNetworkChannelManager;
import mod.chiselsandbits.fabric.platform.plugin.FabricPluginManager;
import mod.chiselsandbits.fabric.platform.registry.FabricRegistryManager;
import mod.chiselsandbits.fabric.platform.server.FabricServerLifecycleManager;
import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.platforms.core.blockstate.ILevelBasedPropertyAccessor;
import mod.chiselsandbits.platforms.core.chiseling.eligibility.IPlatformEligibilityOptions;
import mod.chiselsandbits.platforms.core.client.IClientManager;
import mod.chiselsandbits.platforms.core.config.IConfigurationManager;
import mod.chiselsandbits.platforms.core.creativetab.ICreativeTabManager;
import mod.chiselsandbits.platforms.core.dist.IDistributionManager;
import mod.chiselsandbits.platforms.core.entity.IEntityInformationManager;
import mod.chiselsandbits.platforms.core.entity.IPlayerInventoryManager;
import mod.chiselsandbits.platforms.core.event.IEventFirer;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.platforms.core.inventory.bit.IAdaptingBitInventoryManager;
import mod.chiselsandbits.platforms.core.item.IDyeItemHelper;
import mod.chiselsandbits.platforms.core.item.IItemComparisonHelper;
import mod.chiselsandbits.platforms.core.network.INetworkChannelManager;
import mod.chiselsandbits.platforms.core.plugin.IPlatformPluginManager;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public final class FabricChiselsAndBitsPlatform implements IChiselsAndBitsPlatformCore
{
    private static final FabricChiselsAndBitsPlatform INSTANCE = new FabricChiselsAndBitsPlatform();

    public static FabricChiselsAndBitsPlatform getInstance()
    {
        return INSTANCE;
    }

    private FabricChiselsAndBitsPlatform()
    {
    }

    @Override
    public @NotNull IPlatformRegistryManager getPlatformRegistryManager()
    {
        return FabricRegistryManager.getInstance();
    }

    @Override
    public @NotNull IEntityInformationManager getEntityInformationManager()
    {
        return FabricEntityInformationManager.getInstance();
    }

    @Override
    public @NotNull IFluidManager getFluidManager()
    {
        return FabricFluidManager.getInstance();
    }

    @Override
    public @NotNull IClientManager getClientManager()
    {
        return FabricClientManager.getInstance();
    }

    @Override
    public @NotNull ILevelBasedPropertyAccessor getLevelBasedPropertyAccessor()
    {
        return FabricLevelBasedPropertyAccessor.getInstance();
    }

    @Override
    public @NotNull IItemComparisonHelper getItemComparisonHelper()
    {
        return FabricItemComparisonHelper.getInstance();
    }

    @Override
    public @NotNull IPlatformEligibilityOptions getPlatformEligibilityOptions()
    {
        return FabricPlatformEligibilityOptions.getInstance();
    }

    @Override
    public @NotNull IEventFirer getEventFirer()
    {
        return FabricEventFirer.getInstance();
    }

    @Override
    public @NotNull IPlayerInventoryManager getPlayerInventoryManager()
    {
        return FabricPlayerInventoryManager.getInstance();
    }

    @Override
    public @NotNull IDistributionManager getDistributionManager()
    {
        return FabricDistributionManager.getInstance();
    }

    @Override
    public @NotNull INetworkChannelManager getNetworkChannelManager()
    {
        return FabricNetworkChannelManager.getInstance();
    }

    @Override
    public @NotNull IPlatformPluginManager getPlatformPluginManager()
    {
        return FabricPluginManager.getInstance();
    }

    @Override
    public @NotNull IDyeItemHelper getDyeItemHelper()
    {
        return FabricDyeItemHelper.getInstance();
    }

    @Override
    public @NotNull IConfigurationManager getConfigurationManager()
    {
        return FabricConfigurationManager.getInstance();
    }

    @Override
    public @NotNull IAdaptingBitInventoryManager getAdaptingBitInventoryManager()
    {
        return  FabricAdaptingBitInventoryManager.getInstance();
    }

    @Override
    public @NotNull MinecraftServer getCurrentServer()
    {
        return FabricServerLifecycleManager.getInstance().getServer();
    }

    @Override
    public @NotNull ICreativeTabManager getCreativeTabManager()
    {
        return FabricCreativeTabManager.getInstance();
    }
}
