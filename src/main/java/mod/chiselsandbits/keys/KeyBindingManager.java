package mod.chiselsandbits.keys;

import com.google.common.collect.Lists;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.screens.RadialToolModeSelectionScreen;
import mod.chiselsandbits.keys.contexts.HoldsSpecificItemInHandKeyConflictContext;
import mod.chiselsandbits.keys.contexts.HoldsWithToolItemInHandKeyConflictContext;
import mod.chiselsandbits.keys.contexts.SpecificScreenOpenKeyConflictContext;
import mod.chiselsandbits.network.packets.HeldToolModeChangedPacket;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBindingManager
{
    private KeyBinding openToolMenuKeybinding = null;
    private KeyBinding cycleToolMenuLeftKeybinding = null;
    private KeyBinding cycleToolMenuRightKeybinding = null;
    private KeyBinding resetMeasuringTapeKeyBinding = null;

    private boolean toolMenuKeyWasDown = false;
    private int toolModeSelectionPlusCoolDown = 15;
    private int toolModeSelectionMinusCoolDown = 15;


    private static final KeyBindingManager INSTANCE = new KeyBindingManager();

    public static KeyBindingManager getInstance()
    {
        return INSTANCE;
    }

    private KeyBindingManager()
    {
    }

    public void onModInitialization() {
        ClientRegistry.registerKeyBinding(openToolMenuKeybinding =
                                            new KeyBinding("key.modded-tool.open",
                                              HoldsWithToolItemInHandKeyConflictContext.getInstance(),
                                              InputMappings.Type.KEYSYM,
                                              GLFW.GLFW_KEY_R,
                                              "key.chiselsandbits.category"));

        ClientRegistry.registerKeyBinding(cycleToolMenuLeftKeybinding =
                                            new KeyBinding("key.modded-tool.cycle.left",
                                              SpecificScreenOpenKeyConflictContext.RADIAL_TOOL_MENU,
                                              InputMappings.Type.KEYSYM,
                                              InputMappings.INPUT_INVALID.getKeyCode(),
                                              "key.chiselsandbits.category"));

        ClientRegistry.registerKeyBinding(cycleToolMenuRightKeybinding =
                                            new KeyBinding("key.modded-tool.cycle.right",
                                              SpecificScreenOpenKeyConflictContext.RADIAL_TOOL_MENU,
                                              InputMappings.Type.KEYSYM,
                                              InputMappings.INPUT_INVALID.getKeyCode(),
                                              "key.chiselsandbits.category"));

        ClientRegistry.registerKeyBinding(resetMeasuringTapeKeyBinding =
                                            new KeyBinding("key.measuring-tape.reset",
                                              HoldsSpecificItemInHandKeyConflictContext.MEASURING_TAPE,
                                              KeyModifier.SHIFT, InputMappings.Type.MOUSE, 1,
                                              "key.chiselsandbits.category"));

    }

    @SubscribeEvent
    public static void handleClientTickEvent(TickEvent.ClientTickEvent ev)
    {
        if (ev.phase != TickEvent.Phase.START)
            return;

        getInstance().handleKeyPresses();
    }

    @SuppressWarnings("unchecked")
    public void handleKeyPresses() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.currentScreen == null)
        {
            boolean toolMenuKeyIsDown = isOpenToolMenuKeyPressed();
            if (toolMenuKeyIsDown && !toolMenuKeyWasDown)
            {
                if (isOpenToolMenuKeyPressed())
                {
                    if (mc.currentScreen == null)
                    {
                        ItemStack inHand = ItemStackUtils.getModeItemStackFromPlayer(mc.player);
                        if (!inHand.isEmpty() && inHand.getItem() instanceof IWithModeItem)
                        {
                            try {
                                final IWithModeItem<? extends IRenderableMode> withModeItem = (IWithModeItem<? extends IRenderableMode>) inHand.getItem();
                                mc.displayGuiScreen(RadialToolModeSelectionScreen.create(withModeItem, inHand));
                            } catch (ClassCastException ignored) {
                            }
                        }
                    }
                }
            }
            toolMenuKeyWasDown = toolMenuKeyIsDown;
        }
        else
        {
            toolMenuKeyWasDown = true;
        }

        if (mc.currentScreen instanceof RadialToolModeSelectionScreen) {
            final RadialToolModeSelectionScreen<?> radialToolMenuScreen = (RadialToolModeSelectionScreen<?>) mc.currentScreen;

            if (toolModeSelectionPlusCoolDown == 0 && isCycleToolMenuRightKeyPressed())
                radialToolMenuScreen.onMoveSelectionToTheRight();

            if (toolModeSelectionMinusCoolDown == 0 && isCycleToolMenuLeftKeyPressed())
                radialToolMenuScreen.onMoveSelectionToTheLeft();
        }
        else if (ItemStackUtils.getModeItemStackFromPlayer(Minecraft.getInstance().player).getItem() instanceof IWithModeItem)
        {
            final ItemStack stack = ItemStackUtils.getModeItemStackFromPlayer(Minecraft.getInstance().player);
            final IWithModeItem<?> withModeItem = (IWithModeItem<?>) stack.getItem();

            final List<Object> candidates = Lists.newArrayList(withModeItem.getPossibleModes());
            final Object cur = withModeItem.getMode(stack);
            int workingIndex = candidates.indexOf(cur);

            if (toolModeSelectionPlusCoolDown == 15 && isCycleToolMenuRightKeyPressed()) {
                workingIndex++;
                if (workingIndex >= candidates.size()) {
                    workingIndex = 0;
                }

                ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new HeldToolModeChangedPacket(workingIndex));
                toolModeSelectionPlusCoolDown = 0;
            }
            else if (isCycleToolMenuRightKeyPressed() && toolModeSelectionPlusCoolDown <= 14) {
                toolModeSelectionPlusCoolDown++;
            }
            else if (toolModeSelectionMinusCoolDown == 15 && isCycleToolMenuLeftKeyPressed()) {
                workingIndex--;
                if (workingIndex < 0) {
                    workingIndex = candidates.size() - 1;
                }

                ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new HeldToolModeChangedPacket(workingIndex));
                toolModeSelectionMinusCoolDown = 0;
            }
            else if (isCycleToolMenuLeftKeyPressed() && toolModeSelectionMinusCoolDown <= 14) {
                toolModeSelectionMinusCoolDown++;
            }
        }

        if (!isCycleToolMenuRightKeyPressed())
            toolModeSelectionPlusCoolDown = 15;

        if (!isCycleToolMenuLeftKeyPressed())
            toolModeSelectionMinusCoolDown = 15;
    }

    public boolean isKeyDown(KeyBinding keybinding)
    {
        if (keybinding.isInvalid())
            return false;

        boolean isDown = false;
        switch (keybinding.getKey().getType())
        {
            case KEYSYM:
                isDown = InputMappings.isKeyDown(Minecraft.getInstance().getMainWindow().getHandle(), keybinding.getKey().getKeyCode());
                break;
            case MOUSE:
                isDown = GLFW.glfwGetMouseButton(Minecraft.getInstance().getMainWindow().getHandle(), keybinding.getKey().getKeyCode()) == GLFW.GLFW_PRESS;
                break;
        }
        return isDown && keybinding.getKeyConflictContext().isActive() && keybinding.getKeyModifier().isActive(keybinding.getKeyConflictContext());
    }

    public boolean areBindingsInitialized() {
        return resetMeasuringTapeKeyBinding != null;
    }

    public KeyBinding getResetMeasuringTapeKeyBinding()
    {
        if (resetMeasuringTapeKeyBinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return resetMeasuringTapeKeyBinding;
    }

    public KeyBinding getOpenToolMenuKeybinding()
    {
        if (openToolMenuKeybinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return openToolMenuKeybinding;
    }

    public KeyBinding getCycleToolMenuLeftKeybinding()
    {
        if (cycleToolMenuLeftKeybinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return cycleToolMenuLeftKeybinding;
    }

    public KeyBinding getCycleToolMenuRightKeybinding()
    {
        if (cycleToolMenuRightKeybinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return cycleToolMenuRightKeybinding;
    }

    public boolean isResetMeasuringTapeKeyPressed() {return isKeyDown(getResetMeasuringTapeKeyBinding()); }

    public boolean isOpenToolMenuKeyPressed() {
        return isKeyDown(getOpenToolMenuKeybinding());
    }

    public boolean isCycleToolMenuLeftKeyPressed() {
        return isKeyDown(getCycleToolMenuLeftKeybinding());
    }

    public boolean isCycleToolMenuRightKeyPressed() {
        return isKeyDown(getCycleToolMenuRightKeybinding());
    }
}
