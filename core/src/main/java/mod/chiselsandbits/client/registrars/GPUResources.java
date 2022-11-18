package mod.chiselsandbits.client.registrars;

import com.communi.suggestu.scena.core.client.event.IClientEvents;
import com.communi.suggestu.scena.core.client.event.IResourceRegistrationEvent;
import mod.chiselsandbits.client.icon.IconManager;
import mod.chiselsandbits.client.reloading.ClientResourceReloadingManager;
import net.minecraft.client.Minecraft;

public class GPUResources {

    public static void onClientConstruction() {
        IClientEvents.getInstance().getResourceRegistrationEvent().register(() -> {
            //noinspection ConstantConditions -> This is null during datagen!
            if (Minecraft.getInstance() != null) {
                ClientResourceReloadingManager.setup();
                IconManager.getInstance().initialize();
            }
        });
    }
}
