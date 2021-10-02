package mod.chiselsandbits.client.model.baked.chiseled;

import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.model.baked.base.BaseSmartModel;
import mod.chiselsandbits.client.model.baked.simple.CombinedModel;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.registrars.ModModelProperties;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

public class DataAwareChiseledBlockBakedModel extends BaseSmartModel
{
    @Override
    public boolean usesBlockLight()
    {
        return true;
    }

    @Override
    public IBakedModel handleBlockState(final BlockState state, final Random random, final IModelData modelData)
    {
        if (!modelData.hasProperty(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY) && !modelData.hasProperty(ModModelProperties.UNKNOWN_LAYER_MODEL_PROPERTY))
            return NullBakedModel.instance;

        if (!modelData.hasProperty(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY) || MinecraftForgeClient.getRenderLayer() == null)
            return modelData.getData(ModModelProperties.UNKNOWN_LAYER_MODEL_PROPERTY);

        return modelData.getData(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY).getOrDefault(
          MinecraftForgeClient.getRenderLayer(),
          NullBakedModel.instance
        );
    }

    @Override
    public IBakedModel resolve(
      final IBakedModel originalModel, final ItemStack stack, final World world, final LivingEntity entity)
    {
        final Item item = stack.getItem();
        if (!(item instanceof IMultiStateItem))
            return NullBakedModel.instance;

        final IMultiStateItem multiStateItem = (IMultiStateItem) item;
        final IMultiStateItemStack multiStateItemStack = multiStateItem.createItemStack(stack);

        final IBakedModel[] typedModels;
        try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Building individual render type models"))
        {
            typedModels = Arrays.stream(VoxelType.values())
                            .map(ChiselRenderType::getRenderTypes)
                            .filter(types -> !types.isEmpty())
                            .map(types -> {
                                final IBakedModel[] models = types.stream()
                                                               .map(type ->  ChiseledBlockBakedModelManager.getInstance().get(multiStateItemStack, type))
                                                               .filter(Optional::isPresent)
                                                               .map(Optional::get)
                                                               .filter(model -> !model.isEmpty())
                                                               .toArray(IBakedModel[]::new);
                                if (models.length == 0)
                                    return ChiseledBlockBakedModel.EMPTY;

                                return new CombinedModel(models);
                            })
                            .toArray(IBakedModel[]::new);
        }

        if (typedModels.length == 0)
            return ChiseledBlockBakedModel.EMPTY;

        try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Combining model data"))
        {
            return new CombinedModel(typedModels);
        }
    }
}
