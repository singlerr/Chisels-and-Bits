package mod.chiselsandbits.api.chiseling;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.multistate.accessor.IAreaAccessor;
import mod.chiselsandbits.api.registries.IRegistryManager;
import mod.chiselsandbits.api.util.IWithDisplayName;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Optional;

public interface IChiselMode extends IWithDisplayName, IForgeRegistryEntry<IChiselMode>, IRenderableMode
{

    static IChiselMode getDefaultMode() {
        return IChiselsAndBitsAPI.getInstance().getDefaultChiselMode();
    }

    static IForgeRegistry<IChiselMode> getRegistry() {
        return IRegistryManager.getInstance().getChiselModeRegistry();
    }

    ClickProcessingState onLeftClickBy(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    void onStoppedLeftClicking(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    ClickProcessingState onRightClickBy(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    void onStoppedRightClicking(
      final PlayerEntity playerEntity,
      final IChiselingContext context
    );

    Optional<IAreaAccessor> getCurrentAccessor(final IChiselingContext context);
}
