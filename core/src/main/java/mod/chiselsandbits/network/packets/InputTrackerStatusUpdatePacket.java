package mod.chiselsandbits.network.packets;

import mod.chiselsandbits.input.ProcessingInputTracker;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class InputTrackerStatusUpdatePacket extends ModPacket
{

    private boolean isLeftMouse;
    private boolean started;

    public InputTrackerStatusUpdatePacket(final FriendlyByteBuf buffer)
    {
        readPayload(buffer);
    }

    public InputTrackerStatusUpdatePacket(final boolean isLeftMouse, final boolean started) {
        this.isLeftMouse = isLeftMouse;
        this.started = started;
    }

    @Override
    public void writePayload(final FriendlyByteBuf buffer)
    {
        buffer.writeBoolean(this.isLeftMouse);
        buffer.writeBoolean(this.started);
    }

    @Override
    public void readPayload(final FriendlyByteBuf buffer)
    {
        this.isLeftMouse = buffer.readBoolean();
        this.started = buffer.readBoolean();
    }

    @Override
    public void server(final ServerPlayer playerEntity)
    {
        if (isLeftMouse) {
            if (started) {
                ProcessingInputTracker.getInstance().onStartedLeftClicking(playerEntity);
            }
            else {
                ProcessingInputTracker.getInstance().onStoppedLeftClicking(playerEntity);
            }
        } else {
            if (started) {
                ProcessingInputTracker.getInstance().onStartedRightClicking(playerEntity);
            }
            else {
                ProcessingInputTracker.getInstance().onStoppedRightClicking(playerEntity);
            }
        }
    }
}
