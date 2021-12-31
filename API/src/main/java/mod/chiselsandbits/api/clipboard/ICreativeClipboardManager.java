package mod.chiselsandbits.api.clipboard;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;

import java.util.List;

public interface ICreativeClipboardManager
{

    static ICreativeClipboardManager getInstance() {
        return IChiselsAndBitsAPI.getInstance().getCreativeClipboardManager();
    }

    /**
     * The clipboard contents.
     *
     * @return The clipboard contents.
     */
    List<IMultiStateItemStack> getClipboard();

    /**
     * Adds an entry to the clipboard.
     *
     * @param multiStateItemStack The multi-state item stack to add.
     */
    void addEntry(IMultiStateItemStack multiStateItemStack);
}
