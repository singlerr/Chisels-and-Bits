package mod.chiselsandbits.forge.platform;

import mod.chiselsandbits.forge.platform.client.ForgeClientManager;
import mod.chiselsandbits.forge.platform.configuration.ForgeConfigurationManager;
import mod.chiselsandbits.forge.platform.creativetab.ForgeCreativeTabManager;
import mod.chiselsandbits.forge.platform.distribution.ForgeDistributionManager;
import mod.chiselsandbits.forge.platform.eligibility.ForgeEligibilityOptions;
import mod.chiselsandbits.forge.platform.entity.ForgeEntityInformationManager;
import mod.chiselsandbits.forge.platform.entity.ForgePlayerInventoryManager;
import mod.chiselsandbits.forge.platform.fluid.ForgeFluidManager;
import mod.chiselsandbits.forge.platform.inventory.bit.ForgeAdaptingBitInventoryManager;
import mod.chiselsandbits.forge.platform.item.DyeItemHelper;
import mod.chiselsandbits.forge.platform.item.ForgeItemComparisonHelper;
import mod.chiselsandbits.forge.platform.level.ForgeLevelBasedPropertyAccessor;
import mod.chiselsandbits.forge.platform.network.ForgeNetworkChannelManager;
import mod.chiselsandbits.forge.platform.plugin.ForgePluginManager;
import mod.chiselsandbits.forge.platform.registry.ForgeRegistryManager;
import mod.chiselsandbits.platforms.core.IChiselsAndBitsPlatformCore;
import mod.chiselsandbits.platforms.core.blockstate.ILevelBasedPropertyAccessor;
import mod.chiselsandbits.platforms.core.chiseling.eligibility.IPlatformEligibilityOptions;
import mod.chiselsandbits.platforms.core.client.IClientManager;
import mod.chiselsandbits.platforms.core.config.IConfigurationManager;
import mod.chiselsandbits.platforms.core.creativetab.ICreativeTabManager;
import mod.chiselsandbits.platforms.core.dist.IDistributionManager;
import mod.chiselsandbits.platforms.core.entity.IEntityInformationManager;
import mod.chiselsandbits.platforms.core.entity.IPlayerInventoryManager;
import mod.chiselsandbits.platforms.core.fluid.IFluidManager;
import mod.chiselsandbits.platforms.core.inventory.bit.IAdaptingBitInventoryManager;
import mod.chiselsandbits.platforms.core.item.IDyeItemHelper;
import mod.chiselsandbits.platforms.core.item.IItemComparisonHelper;
import mod.chiselsandbits.platforms.core.network.INetworkChannelManager;
import mod.chiselsandbits.platforms.core.plugin.IPlatformPluginManager;
import mod.chiselsandbits.platforms.core.registries.IPlatformRegistryManager;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;

public class ForgeChiselsAndBitsPlatform implements IChiselsAndBitsPlatformCore
{
    @Override
    public @NotNull IPlatformRegistryManager getPlatformRegistryManager()
    {
        return ForgeRegistryManager.getInstance();
    }

    @Override
    public @NotNull IEntityInformationManager getEntityInformationManager()
    {
        return ForgeEntityInformationManager.getInstance();
    }

    @Override
    public @NotNull IFluidManager getFluidManager()
    {
        return ForgeFluidManager.getInstance();
    }

    @Override
    public @NotNull IClientManager getClientManager()
    {
        return ForgeClientManager.getInstance();
    }

    @Override
    public @NotNull ILevelBasedPropertyAccessor getLevelBasedPropertyAccessor()
    {
        return ForgeLevelBasedPropertyAccessor.getInstance();
    }

    @Override
    public @NotNull IItemComparisonHelper getItemComparisonHelper()
    {
        return ForgeItemComparisonHelper.getInstance();
    }

    @Override
    public @NotNull IPlatformEligibilityOptions getPlatformEligibilityOptions()
    {
        return ForgeEligibilityOptions.getInstance();
    }

    @Override
    public @NotNull IPlayerInventoryManager getPlayerInventoryManager()
    {
        return ForgePlayerInventoryManager.getInstance();
    }

    @Override
    public @NotNull IDistributionManager getDistributionManager()
    {
        return ForgeDistributionManager.getInstance();
    }

    @Override
    public @NotNull INetworkChannelManager getNetworkChannelManager()
    {
        return ForgeNetworkChannelManager.getInstance();
    }

    @Override
    public @NotNull IPlatformPluginManager getPlatformPluginManager()
    {
        return ForgePluginManager.getInstance();
    }

    @Override
    public @NotNull IDyeItemHelper getDyeItemHelper()
    {
        return DyeItemHelper.getInstance();
    }

    @Override
    public @NotNull IConfigurationManager getConfigurationManager()
    {
        return ForgeConfigurationManager.getInstance();
    }

    @Override
    public @NotNull IAdaptingBitInventoryManager getAdaptingBitInventoryManager()
    {
        return ForgeAdaptingBitInventoryManager.getInstance();
    }

    @Override
    public @NotNull MinecraftServer getCurrentServer()
    {
        return ServerLifecycleHooks.getCurrentServer();
    }

    @Override
    public @NotNull ICreativeTabManager getCreativeTabManager()
    {
        return ForgeCreativeTabManager.getInstance();
    }
}
