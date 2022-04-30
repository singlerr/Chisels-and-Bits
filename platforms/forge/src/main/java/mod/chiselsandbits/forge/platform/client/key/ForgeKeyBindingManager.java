package mod.chiselsandbits.forge.platform.client.key;

import com.mojang.blaze3d.platform.InputConstants;
import mod.chiselsandbits.platforms.core.client.key.IKeyBindingManager;
import mod.chiselsandbits.platforms.core.client.key.IKeyConflictContext;
import mod.chiselsandbits.platforms.core.client.key.KeyModifier;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;

public class ForgeKeyBindingManager implements IKeyBindingManager
{
    private static final ForgeKeyBindingManager INSTANCE = new ForgeKeyBindingManager();

    public static ForgeKeyBindingManager getInstance()
    {
        return INSTANCE;
    }

    private ForgeKeyBindingManager()
    {
    }

    @Override
    public void register(final KeyMapping mapping)
    {
        ClientRegistry.registerKeyBinding(mapping);
    }

    @Override
    public IKeyConflictContext getGuiKeyConflictContext()
    {
        return new ForgeKeyConflictContextPlatformDelegate(KeyConflictContext.GUI);
    }

    @Override
    public KeyMapping createNew(
      final String translationKey, final IKeyConflictContext keyConflictContext, final InputConstants.Type inputType, final int key, final String groupTranslationKey)
    {
        return new KeyMapping(
          translationKey,
          new PlatformKeyConflictContextForgeDelegate(keyConflictContext),
          inputType,
          key,
          groupTranslationKey
        );
    }

    @Override
    public KeyMapping createNew(
      final String translationKey,
      final IKeyConflictContext keyConflictContext,
      final KeyModifier keyModifier,
      final InputConstants.Type inputType,
      final int key,
      final String groupTranslationKey)
    {
        return new KeyMapping(
          translationKey,
          new PlatformKeyConflictContextForgeDelegate(keyConflictContext),
          makePlatformSpecific(keyModifier),
          inputType,
          key,
          groupTranslationKey
        );
    }

    @Override
    public boolean isKeyConflictOfActive(final KeyMapping keybinding)
    {
        return keybinding.getKeyConflictContext().isActive();
    }

    @Override
    public boolean isKeyModifierActive(final KeyMapping keybinding)
    {
        return keybinding.getKeyModifier().isActive(keybinding.getKeyConflictContext());
    }

    private static net.minecraftforge.client.settings.KeyModifier makePlatformSpecific(final KeyModifier keyModifier) {
        return switch (keyModifier)
                 {
                     case CONTROL -> net.minecraftforge.client.settings.KeyModifier.CONTROL;
                     case SHIFT -> net.minecraftforge.client.settings.KeyModifier.SHIFT;
                     case ALT -> net.minecraftforge.client.settings.KeyModifier.ALT;
                 };
    }
}
