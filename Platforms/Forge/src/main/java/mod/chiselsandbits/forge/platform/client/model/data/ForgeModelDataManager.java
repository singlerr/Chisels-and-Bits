package mod.chiselsandbits.forge.platform.client.model.data;

import mod.chiselsandbits.platforms.core.client.models.data.IModelDataManager;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.model.ModelDataManager;

public class ForgeModelDataManager implements IModelDataManager
{
    private static final ForgeModelDataManager INSTANCE = new ForgeModelDataManager();

    public static ForgeModelDataManager getInstance()
    {
        return INSTANCE;
    }

    private ForgeModelDataManager()
    {
    }

    @Override
    public void requestModelDataRefresh(final BlockEntity blockEntity)
    {
        ModelDataManager.requestModelDataRefresh(blockEntity);
    }
}
