package mod.chiselsandbits.client.model.baked.chiseled;

import mod.chiselsandbits.api.config.IClientConfiguration;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.model.baked.base.BaseSmartModel;
import mod.chiselsandbits.client.model.baked.simple.CombinedModel;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.platforms.core.client.IClientManager;
import mod.chiselsandbits.platforms.core.client.models.data.IBlockModelData;
import mod.chiselsandbits.platforms.core.client.rendering.type.IRenderTypeManager;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.registrars.ModModelProperties;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

@SuppressWarnings("ConstantConditions")
public class DataAwareChiseledBlockBakedModel extends BaseSmartModel
{
    private static final SimpleMaxSizedCache<CompoundTag, BakedModel> STACK_MODEL_CACHE = new SimpleMaxSizedCache<>(
      IClientConfiguration.getInstance().getStackModelCacheSize()::get
    );

    @Override
    public boolean useAmbientOcclusion()
    {
        return true;
    }

    @Override
    public boolean usesBlockLight()
    {
        return true;
    }

    @Override
    public BakedModel handleBlockState(final BlockState state, final Random random, final IBlockModelData modelData)
    {
        if (!modelData.hasProperty(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY) && !modelData.hasProperty(ModModelProperties.UNKNOWN_LAYER_MODEL_PROPERTY))
            return NullBakedModel.instance;

        if (!modelData.hasProperty(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY) || IRenderTypeManager.getInstance().getCurrentRenderType().isEmpty())
            return modelData.getData(ModModelProperties.UNKNOWN_LAYER_MODEL_PROPERTY);

        return modelData.getData(ModModelProperties.KNOWN_LAYER_MODEL_PROPERTY).getOrDefault(
          IRenderTypeManager.getInstance().getCurrentRenderType().get(),
          NullBakedModel.instance
        );
    }

    @Override
    public BakedModel resolve(
      final BakedModel originalModel, final ItemStack stack, final Level world, final LivingEntity entity)
    {
        final Item item = stack.getItem();
        if (!(item instanceof final IMultiStateItem multiStateItem))
            return NullBakedModel.instance;

        final CompoundTag cacheKey = stack.save(new CompoundTag());
        return STACK_MODEL_CACHE.get(cacheKey, () -> {
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
        });
    }
}
