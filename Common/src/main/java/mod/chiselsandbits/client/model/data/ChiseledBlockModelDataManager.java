package mod.chiselsandbits.client.model.data;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ChiseledBlockModelDataManager
{
    private static final ChiseledBlockModelDataManager INSTANCE = new ChiseledBlockModelDataManager();

    public static ChiseledBlockModelDataManager getInstance()
    {
        return INSTANCE;
    }


    private ChiseledBlockModelDataManager()
    {
    }

    public void updateModelData(@Nullable final ChiseledBlockEntity tileEntity) {
        if (tileEntity == null || !tileEntity.hasLevel() || !Objects.requireNonNull(tileEntity.getLevel()).isClientSide())
            return;

        ChiseledBlockModelDataExecutor.updateModelDataCore(tileEntity);
    }

}
