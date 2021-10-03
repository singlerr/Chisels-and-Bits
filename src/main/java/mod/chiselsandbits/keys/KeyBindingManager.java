package mod.chiselsandbits.keys;

import com.google.common.collect.Lists;
import mod.chiselsandbits.ChiselsAndBits;
import mod.chiselsandbits.api.item.withmode.IRenderableMode;
import mod.chiselsandbits.api.item.withmode.IWithModeItem;
import mod.chiselsandbits.api.util.constants.Constants;
import mod.chiselsandbits.client.events.TickHandler;
import mod.chiselsandbits.client.screens.RadialToolModeSelectionScreen;
import mod.chiselsandbits.keys.contexts.HoldsSpecificItemInHandKeyConflictContext;
import mod.chiselsandbits.keys.contexts.HoldsWithToolItemInHandKeyConflictContext;
import mod.chiselsandbits.keys.contexts.SpecificScreenOpenKeyConflictContext;
import mod.chiselsandbits.network.packets.HeldToolModeChangedPacket;
import mod.chiselsandbits.network.packets.RequestChangeTrackerOperation;
import mod.chiselsandbits.utils.ItemStackUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fmlclient.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class KeyBindingManager
{
    private KeyMapping openToolMenuKeybinding = null;
    private KeyMapping cycleToolMenuLeftKeybinding = null;
    private KeyMapping cycleToolMenuRightKeybinding = null;
    private KeyMapping resetMeasuringTapeKeyBinding = null;
    private KeyMapping undoOperationKeyBinding = null;
    private KeyMapping redoOperationKeyBinding = null;

    private boolean toolMenuKeyWasDown = false;
    private int toolModeSelectionPlusCoolDown = 15;
    private int toolModeSelectionMinusCoolDown = 15;

    private long lastChangeTime = -10;

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
                                            new KeyMapping("mod.chiselsandbits.keys.key.modded-tool.open",
                                              HoldsWithToolItemInHandKeyConflictContext.getInstance(),
                                              InputConstants.Type.KEYSYM,
                                              GLFW.GLFW_KEY_R,
                                              "mod.chiselsandbits.keys.category"));

        ClientRegistry.registerKeyBinding(cycleToolMenuLeftKeybinding =
                                            new KeyMapping("mod.chiselsandbits.keys.key.modded-tool.cycle.left",
                                              SpecificScreenOpenKeyConflictContext.RADIAL_TOOL_MENU,
                                              InputConstants.Type.KEYSYM,
                                              InputConstants.UNKNOWN.getValue(),
                                              "mod.chiselsandbits.keys.category"));

        ClientRegistry.registerKeyBinding(cycleToolMenuRightKeybinding =
                                            new KeyMapping("mod.chiselsandbits.keys.key.modded-tool.cycle.right",
                                              SpecificScreenOpenKeyConflictContext.RADIAL_TOOL_MENU,
                                              InputConstants.Type.KEYSYM,
                                              InputConstants.UNKNOWN.getValue(),
                                              "mod.chiselsandbits.keys.category"));

        ClientRegistry.registerKeyBinding(resetMeasuringTapeKeyBinding =
                                            new KeyMapping("mod.chiselsandbits.keys.key.measuring-tape.reset",
                                              HoldsSpecificItemInHandKeyConflictContext.MEASURING_TAPE,
                                              KeyModifier.SHIFT, InputConstants.Type.MOUSE, 1,
                                              "mod.chiselsandbits.keys.category"));

        ClientRegistry.registerKeyBinding(undoOperationKeyBinding =
                                           new KeyMapping("mod.chiselsandbits.keys.key.undo",
                                             HoldsSpecificItemInHandKeyConflictContext.CHISELABLE_ITEM,
                                             KeyModifier.CONTROL, InputConstants.Type.KEYSYM, InputConstants.KEY_Z,
                                             "mod.chiselsandbits.keys.category"));

        ClientRegistry.registerKeyBinding(redoOperationKeyBinding =
                                            new KeyMapping("mod.chiselsandbits.keys.key.redo",
                                              HoldsSpecificItemInHandKeyConflictContext.CHISELABLE_ITEM,
                                              KeyModifier.CONTROL, InputConstants.Type.KEYSYM, InputConstants.KEY_Y,
                                              "mod.chiselsandbits.keys.category"));
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

        if (mc.screen == null)
        {
            boolean toolMenuKeyIsDown = isOpenToolMenuKeyPressed();
            if (toolMenuKeyIsDown && !toolMenuKeyWasDown)
            {
                if (isOpenToolMenuKeyPressed())
                {
                    if (mc.screen == null)
                    {
                        ItemStack inHand = ItemStackUtils.getModeItemStackFromPlayer(mc.player);
                        if (!inHand.isEmpty() && inHand.getItem() instanceof IWithModeItem)
                        {
                            try {
                                final IWithModeItem<? extends IRenderableMode> withModeItem = (IWithModeItem<? extends IRenderableMode>) inHand.getItem();
                                mc.setScreen(RadialToolModeSelectionScreen.create(withModeItem, inHand));
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

        if (mc.screen instanceof final RadialToolModeSelectionScreen<?> radialToolMenuScreen) {

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

        if (isUndoOperationKeyPressed() && (TickHandler.getClientTicks() - lastChangeTime) > 10) {
            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new RequestChangeTrackerOperation(false));
            lastChangeTime = TickHandler.getClientTicks();
        }

        if (isRedoOperationKeyPressed() && (TickHandler.getClientTicks() - lastChangeTime) > 10) {
            ChiselsAndBits.getInstance().getNetworkChannel().sendToServer(new RequestChangeTrackerOperation(true));
            lastChangeTime = TickHandler.getClientTicks();
        }
    }

    public boolean isKeyDown(KeyMapping keybinding)
    {
        if (keybinding.isUnbound())
            return false;

        boolean isDown = switch (keybinding.getKey().getType())
                           {
                               case KEYSYM -> InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybinding.getKey().getValue());
                               case MOUSE -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybinding.getKey().getValue()) == GLFW.GLFW_PRESS;
                               default -> false;
                           };
        return isDown && keybinding.getKeyConflictContext().isActive() && keybinding.getKeyModifier().isActive(keybinding.getKeyConflictContext());
    }

    public boolean areBindingsInitialized() {
        return resetMeasuringTapeKeyBinding != null;
    }

    public KeyMapping getResetMeasuringTapeKeyBinding()
    {
        if (resetMeasuringTapeKeyBinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return resetMeasuringTapeKeyBinding;
    }

    public KeyMapping getOpenToolMenuKeybinding()
    {
        if (openToolMenuKeybinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return openToolMenuKeybinding;
    }

    public KeyMapping getCycleToolMenuLeftKeybinding()
    {
        if (cycleToolMenuLeftKeybinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return cycleToolMenuLeftKeybinding;
    }

    public KeyMapping getCycleToolMenuRightKeybinding()
    {
        if (cycleToolMenuRightKeybinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return cycleToolMenuRightKeybinding;
    }

    public KeyMapping getUndoOperationKeyBinding()
    {
        if (undoOperationKeyBinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return undoOperationKeyBinding;
    }

    public KeyMapping getRedoOperationKeyBinding()
    {
        if (redoOperationKeyBinding == null)
            throw new IllegalStateException("Keybindings have not been initialized.");

        return redoOperationKeyBinding;
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

    public boolean isUndoOperationKeyPressed() {
        return isKeyDown(getUndoOperationKeyBinding());
    }

    public boolean isRedoOperationKeyPressed() {
        return isKeyDown(getRedoOperationKeyBinding());
    }
}
