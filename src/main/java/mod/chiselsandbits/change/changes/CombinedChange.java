package mod.chiselsandbits.change.changes;

import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class CombinedChange implements IChange
{
    private final Collection<IChange> changes;

    public CombinedChange(final Collection<IChange> changes) {this.changes = changes;}

    public CombinedChange(final Tag tag)
    {
        Validate.isInstanceOf(CompoundTag.class, tag);
        this.changes = new ArrayList<>();
        this.deserializeNBT((CompoundTag) tag);
    }

    @Override
    public boolean canUndo(final Player player)
    {
        for (IChange change : changes)
        {
            if (!change.canUndo(player))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean canRedo(final Player player)
    {
        for (IChange change : changes)
        {
            if (!change.canRedo(player))
            {
                return false;
            }
        }
        return true;
    }

    @Override
    public void undo(final Player player) throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.undo(player);
        }
    }

    @Override
    public void redo(final Player player) throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.redo(player);
        }
    }

    @Override
    public CompoundTag serializeNBT()
    {
        final CompoundTag tag = new CompoundTag();

        tag.put("changes", this.changes.stream().map(INBTSerializable::serializeNBT).collect(Collectors.toCollection(ListTag::new)));

        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundTag nbt)
    {
        final ListTag tag = nbt.getList("changes", Constants.NBT.TAG_COMPOUND);

        this.changes.clear();
        this.changes.addAll(tag.stream().map(BitChange::new).collect(Collectors.toList()));
    }
}
