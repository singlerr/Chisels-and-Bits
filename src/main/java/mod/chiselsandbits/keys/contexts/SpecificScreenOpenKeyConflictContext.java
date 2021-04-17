package mod.chiselsandbits.keys.contexts;

import mod.chiselsandbits.client.screens.RadialToolModeSelectionScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;

public enum SpecificScreenOpenKeyConflictContext implements IKeyConflictContext
{

    RADIAL_TOOL_MENU(RadialToolModeSelectionScreen.class);

    private final Class<? extends Screen> guiScreenClass;

    SpecificScreenOpenKeyConflictContext(final Class<? extends Screen> guiScreenClass) {this.guiScreenClass = guiScreenClass;}

    /**
     * @return true if conditions are met to activate {@link KeyBinding}s with this context
     */
    @Override
    public boolean isActive()
    {
        return Minecraft.getInstance().currentScreen != null && Minecraft.getInstance().currentScreen.getClass() == guiScreenClass;
    }

    /**
     * @param other The other conflict context.
     * @return true if the other context can have {@link KeyBinding} conflicts with this one. This will be called on both contexts to check for conflicts.
     */
    @Override
    public boolean conflicts(final IKeyConflictContext other)
    {
        return this == other;
    }
}
