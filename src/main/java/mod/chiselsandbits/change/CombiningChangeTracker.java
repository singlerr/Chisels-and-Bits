package mod.chiselsandbits.change;

import mod.chiselsandbits.api.change.ICombiningChangeTracker;
import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.change.changes.CombinedChange;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class CombiningChangeTracker extends ChangeTracker implements ICombiningChangeTracker
{
    private final Consumer<IChange> onSubmitCallback;

    public CombiningChangeTracker(final Player player, final Consumer<IChange> onSubmitCallback)
    {
        super(player);
        this.onSubmitCallback = onSubmitCallback;
    }

    @Override
    public void close()
    {
        this.onSubmitCallback.accept(new CombinedChange(
          getChanges()
        ));
    }
}
