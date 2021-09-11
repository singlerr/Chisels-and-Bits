package mod.chiselsandbits.client.model.baked.chiseled;

import mod.chiselsandbits.api.block.entity.IMultiStateBlockEntity;
import mod.chiselsandbits.api.item.multistate.IMultiStateItem;
import mod.chiselsandbits.api.item.multistate.IMultiStateItemStack;
import mod.chiselsandbits.api.profiling.IProfilerSection;
import mod.chiselsandbits.client.model.baked.base.BaseSmartModel;
import mod.chiselsandbits.client.model.baked.simple.CombinedModel;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.profiling.ProfilingManager;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;
import java.util.Random;

import static net.minecraftforge.client.MinecraftForgeClient.getRenderLayer;

public class DataAwareChiseledBlockBakedModel extends BaseSmartModel
{
    private final ModelProperty<IBakedModel> MODEL_PROP = new ModelProperty<>();

    @Override
    public boolean isSideLit()
    {
        return true;
    }

    @Override
    public IBakedModel handleBlockState(final BlockState state, final Random random, final IModelData modelData)
    {
        if (!modelData.hasProperty(MODEL_PROP))
            return NullBakedModel.instance;

        return modelData.getData(MODEL_PROP);
    }

    @NotNull
    @Override
    public IModelData getModelData(
      @NotNull final IBlockDisplayReader world, @NotNull final BlockPos pos, @NotNull final BlockState state, @NotNull final IModelData modelData)
    {
        try(IProfilerSection ignored1 = ProfilingManager.getInstance().withSection("Extract model data from data"))
        {
            if (world.getTileEntity(pos) == null)
            {
                return new ModelDataMap.Builder().build();
            }

            final RenderType layer = getRenderLayer();

            if (layer == null)
            {
                try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Unknown render layer model building"))
                {
                    final ChiseledBlockBakedModel[] models = new ChiseledBlockBakedModel[ChiselRenderType.values().length];
                    int o = 0;

                    final TileEntity tileEntity = world.getTileEntity(pos);
                    if (tileEntity instanceof IMultiStateBlockEntity) {

                        try(IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Individual render types building"))
                        {
                            for (final ChiselRenderType chiselRenderType : ChiselRenderType.values())
                            {
                                try(IProfilerSection ignored4 = ProfilingManager.getInstance().withSection(chiselRenderType.name()))
                                {
                                    final ChiseledBlockBakedModel model = ChiseledBlockBakedModelManager.getInstance().get(
                                      ((IMultiStateBlockEntity) tileEntity),
                                      ((IMultiStateBlockEntity) tileEntity).getStatistics().getPrimaryState(),
                                      chiselRenderType,
                                      world,
                                      pos
                                    );
                                    models[o++] = model;
                                }
                            }
                        }

                        try(IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Combining model"))
                        {
                            return new ModelDataMap.Builder().withInitial(MODEL_PROP, new CombinedModel(models)).build();
                        }
                    }
                    else
                    {
                        return new ModelDataMap.Builder().withInitial(MODEL_PROP, NullBakedModel.instance).build();
                    }
                }
            }

            try(IProfilerSection ignored2 = ProfilingManager.getInstance().withSection("Known render layer model building"))
            {

                final TileEntity tileEntity = world.getTileEntity(pos);
                if (!(tileEntity instanceof IMultiStateBlockEntity))
                    return new ModelDataMap.Builder().withInitial(MODEL_PROP, NullBakedModel.instance).build();

                final IMultiStateBlockEntity multiStateBlockEntity = (IMultiStateBlockEntity) tileEntity;
                if (multiStateBlockEntity.getStatistics().getStateCounts().isEmpty() ||
                      (multiStateBlockEntity.getStatistics().getStateCounts().size() == 1 && multiStateBlockEntity.getStatistics().getStateCounts().containsKey(Blocks.AIR.getDefaultState()))) {
                    return new ModelDataMap.Builder().withInitial(MODEL_PROP, NullBakedModel.instance).build();
                }

                IBakedModel baked;
                if (RenderType.getBlockRenderTypes().contains(layer) && FluidRenderingManager.getInstance().isFluidRenderType(layer))
                {
                    try(IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Solid and fluid model building"))
                    {

                        final ChiseledBlockBakedModel solidModel;
                        try(IProfilerSection ignored4 = ProfilingManager.getInstance().withSection("Solid"))
                        {
                            solidModel = ChiseledBlockBakedModelManager.getInstance().get(
                              ((IMultiStateBlockEntity) tileEntity),
                              ((IMultiStateBlockEntity) tileEntity).getStatistics().getPrimaryState(),
                              ChiselRenderType.fromLayer(layer, false),
                              world,
                              pos
                            );
                        }

                        final ChiseledBlockBakedModel fluidModel;
                        try(IProfilerSection ignored4 = ProfilingManager.getInstance().withSection("Fluid"))
                        {
                            fluidModel = ChiseledBlockBakedModelManager.getInstance().get(
                              ((IMultiStateBlockEntity) tileEntity),
                              ((IMultiStateBlockEntity) tileEntity).getStatistics().getPrimaryState(),
                              ChiselRenderType.fromLayer(layer, true),
                              world,
                              pos
                            );
                        }

                        try(IProfilerSection ignored4 = ProfilingManager.getInstance().withSection("Model combining"))
                        {
                            if (solidModel.isEmpty())
                            {
                                baked = fluidModel;
                            }
                            else if (fluidModel.isEmpty())
                            {
                                baked = solidModel;
                            }
                            else
                            {
                                baked = new CombinedModel(solidModel, fluidModel);
                            }
                        }

                    }
                }
                else
                {
                    try(IProfilerSection ignored3 = ProfilingManager.getInstance().withSection("Solid or fluid model building"))
                    {
                        baked = ChiseledBlockBakedModelManager.getInstance().get(
                          ((IMultiStateBlockEntity) tileEntity),
                          ((IMultiStateBlockEntity) tileEntity).getStatistics().getPrimaryState(),
                          ChiselRenderType.fromLayer(layer, false),
                          world,
                          pos
                        );
                    }
                }

                return new ModelDataMap.Builder().withInitial(MODEL_PROP, baked).build();
            }

        }
    }

    @Override
    public IBakedModel func_239290_a_(
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
