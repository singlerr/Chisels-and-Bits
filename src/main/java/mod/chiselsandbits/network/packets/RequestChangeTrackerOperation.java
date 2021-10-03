package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.api.change.IChangeTracker;
import mod.chiselsandbits.api.change.IChangeTrackerManager;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import mod.chiselsandbits.api.util.LocalStrings;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextFormatting;

public class RequestChangeTrackerOperation extends ModPacket
{
    private boolean redo;

    public RequestChangeTrackerOperation(PacketBuffer byteBuf)
    {
        this.readPayload(byteBuf);
    }

    public RequestChangeTrackerOperation(final boolean redo)
    {
        this.redo = redo;
    }

    @Override
    public void writePayload(final PacketBuffer buffer)
    {
        buffer.writeBoolean(this.redo);
    }

    @Override
    public void readPayload(final PacketBuffer buffer)
    {
        this.redo = buffer.readBoolean();
    }

    @Override
    public void server(final ServerPlayerEntity playerEntity)
    {
        final IChangeTracker tracker = IChangeTrackerManager.getInstance().getChangeTracker(playerEntity);
        if (redo) {
            if (!tracker.canRedo(playerEntity)) {
                playerEntity.sendMessage(LocalStrings.CanNotRedo.getText().withStyle(TextFormatting.RED), Util.NIL_UUID);
                return;
            }

            try
            {
                tracker.redo(playerEntity);
                playerEntity.sendMessage(LocalStrings.RedoSuccessful.getText().withStyle(TextFormatting.GREEN), Util.NIL_UUID);
            }
            catch (IllegalChangeAttempt e)
            {
                playerEntity.sendMessage(LocalStrings.CanNotRedo.getText().withStyle(TextFormatting.RED), Util.NIL_UUID);
            }

            return;
        }

        if (!tracker.canUndo(playerEntity)) {
            playerEntity.sendMessage(LocalStrings.CanNotUndo.getText().withStyle(TextFormatting.RED), Util.NIL_UUID);
            return;
        }

        try
        {
            tracker.undo(playerEntity);
            playerEntity.sendMessage(LocalStrings.UndoSuccessful.getText().withStyle(TextFormatting.GREEN), Util.NIL_UUID);
        }
        catch (IllegalChangeAttempt e)
        {
            playerEntity.sendMessage(LocalStrings.CanNotUndo.getText().withStyle(TextFormatting.RED), Util.NIL_UUID);
        }
    }
}
