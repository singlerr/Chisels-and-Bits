package mod.chiselsandbits.clipboard;

import mod.chiselsandbits.api.clipboard.ICreativeClipboardManager;
import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;

public final class CreativeClipboardUtils
{

    private CreativeClipboardUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: CreativeClipboardUtils. This is a utility class");
    }

    public static void addPickedBlock(final IMultiStateItemStack multiStateItemStack) {
        if (IClientConfiguration.getInstance().getShouldPickedBlocksBeAddedToClipboard().get()) {
            ICreativeClipboardManager.getInstance().addEntry(multiStateItemStack);
        }
    }

    public static void addBrokenBlock(final IMultiStateItemStack multiStateItemStack) {
        if (IClientConfiguration.getInstance().getShouldBrokenBlocksBeAddedToClipboard().get()) {
            ICreativeClipboardManager.getInstance().addEntry(multiStateItemStack);
        }
    }
}
