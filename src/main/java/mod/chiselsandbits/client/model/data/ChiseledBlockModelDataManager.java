package mod.chiselsandbits.client.model.data;

import mod.chiselsandbits.block.entities.ChiseledBlockEntity;

public class ChiseledBlockModelDataManager
{
    private static final Runnable NOOP = () -> {};
    private static final ChiseledBlockModelDataManager INSTANCE = new ChiseledBlockModelDataManager();

    private ChiseledBlockModelDataManager()
    {
    }

    public static ChiseledBlockModelDataManager getInstance()
    {
        return INSTANCE;
    }

    public void updateModelData(
      final ChiseledBlockEntity tileEntity
    ) {
        this.updateModelData(tileEntity, NOOP, false);
    }

    public void updateModelData(
      final ChiseledBlockEntity tileEntity,
      final Runnable onCompleteCallback,
      final boolean force
    )
    {
        if (!force)
        {
            if (!tileEntity.hasLevel() || !tileEntity.getLevel().isClientSide() || tileEntity == null)
            {
                return;
            }
        }

        ChiseledBlockModelDataExecutor.updateModelDataCore(tileEntity, onCompleteCallback);
    }
}
