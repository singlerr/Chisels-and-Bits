package mod.chiselsandbits.client.model.baked.bit;

import mod.chiselsandbits.client.model.baked.base.BaseSmartModel;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class DataAwareBitBlockBakedModel extends BaseSmartModel
{
    @Override
    public boolean isSideLit()
    {
        return true;
    }

    @Override
    public IBakedModel func_239290_a_(
      final IBakedModel originalModel, final ItemStack stack, final World world, final LivingEntity entity)
    {
        return BitBlockBakedModelManager.getInstance().get(stack);
    }
}
