package mod.chiselsandbits.change.changes;

import mod.chiselsandbits.api.change.changes.IChange;
import mod.chiselsandbits.api.change.changes.IllegalChangeAttempt;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
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

    public CombinedChange(final INBT tag)
    {
        Validate.isInstanceOf(CompoundNBT.class, tag);
        this.changes = new ArrayList<>();
        this.deserializeNBT((CompoundNBT) tag);
    }

    @Override
    public boolean canUndo(final PlayerEntity player)
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
    public boolean canRedo(final PlayerEntity player)
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
    public void undo(final PlayerEntity player) throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.undo(player);
        }
    }

    @Override
    public void redo(final PlayerEntity player) throws IllegalChangeAttempt
    {
        for (IChange change : changes)
        {
            change.redo(player);
        }
    }

    @Override
    public CompoundNBT serializeNBT()
    {
        final CompoundNBT tag = new CompoundNBT();

        tag.put("changes", this.changes.stream().map(INBTSerializable::serializeNBT).collect(Collectors.toCollection(ListNBT::new)));

        return tag;
    }

    @Override
    public void deserializeNBT(final CompoundNBT nbt)
    {
        final ListNBT tag = nbt.getList("changes", Constants.NBT.TAG_COMPOUND);

        this.changes.clear();
        this.changes.addAll(tag.stream().map(BitChange::new).collect(Collectors.toList()));
    }
}
