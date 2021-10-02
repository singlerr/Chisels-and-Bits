package mod.chiselsandbits.client.model.baked.chiseled;

import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.model.baked.base.BaseSmartModel;
import mod.chiselsandbits.client.model.baked.simple.CombinedModel;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.registrars.ModModelProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

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
    public BakedModel handleBlockState(final BlockState state, final Random random, final IModelData modelData)
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
    public BakedModel resolve(
      final BakedModel originalModel, final ItemStack stack, final Level world, final LivingEntity entity)
    {
        final Item item = stack.getItem();
        if (!(item instanceof IMultiStateItem))
            return NullBakedModel.instance;

        final IMultiStateItem multiStateItem = (IMultiStateItem) item;
        final IMultiStateItemStack multiStateItemStack = multiStateItem.createItemStack(stack);

        final BakedModel[] typedModels;
        try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Building individual render type models"))
        {
            typedModels = Arrays.stream(VoxelType.values())
                            .map(ChiselRenderType::getRenderTypes)
                            .filter(types -> !types.isEmpty())
                            .map(types -> {
                                final BakedModel[] models = types.stream()
                                                               .map(type ->  ChiseledBlockBakedModelManager.getInstance().get(multiStateItemStack, type))
                                                               .filter(Optional::isPresent)
                                                               .map(Optional::get)
                                                               .filter(model -> !model.isEmpty())
                                                               .toArray(BakedModel[]::new);
                                if (models.length == 0)
                                    return ChiseledBlockBakedModel.EMPTY;

                                return new CombinedModel(models);
                            })
                            .toArray(BakedModel[]::new);
        }

        if (typedModels.length == 0)
            return ChiseledBlockBakedModel.EMPTY;

        try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Combining model data"))
        {
            return new CombinedModel(typedModels);
        }
    }
}
