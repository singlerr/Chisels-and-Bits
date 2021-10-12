package mod.chiselsandbits.chiseling.eligibility;

import mod.chiselsandbits.api.IgnoreBlockLogic;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityAnalysisResult;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.config.Configuration;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.block.ChiseledBlock;
import mod.chiselsandbits.materials.MaterialManager;
import mod.chiselsandbits.registrars.ModBlocks;
import mod.chiselsandbits.registrars.ModTags;
import mod.chiselsandbits.utils.ClassUtils;
import mod.chiselsandbits.utils.ReflectionHelperBlock;
import mod.chiselsandbits.utils.SimpleMaxSizedCache;
import mod.chiselsandbits.utils.TranslationUtils;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("ConstantConditions")
public class EligibilityManager implements IEligibilityManager
{
    private static final EligibilityManager INSTANCE = new EligibilityManager();

    private static final SimpleMaxSizedCache<BlockState, IEligibilityAnalysisResult> cache =
        new SimpleMaxSizedCache<>(() -> GameData.getBlockStateIDMap().size() == 0 ? 1000 : GameData.getBlockStateIDMap().size());

    private EligibilityManager()
    {
    }

    public static EligibilityManager getInstance()
    {
        return INSTANCE;
    }

    /**
     * Performs a chiselability analysis on the given blockstate.
     *
     * @param state The blockstate to analyze.
     * @return The analysis result.
     */
    @SuppressWarnings("deprecation")
    @Override
    public IEligibilityAnalysisResult analyse(@NotNull final BlockState state)
    {
        return cache.get(state, () -> {
            if (state.getBlock() instanceof ChiseledBlock)
            {
                return new EligibilityAnalysisResult(
                  false,
                  true,
                  TranslationUtils.build("chiseling.is-already-chiseled")
                );
            }

            final Block blk = state.getBlock();

            if (blk.is(ModTags.Blocks.BLOCKED_CHISELABLE))
            {
                return new EligibilityAnalysisResult(
                  false,
                  false,
                  TranslationUtils.build(LocalStrings.ChiselSupportTagBlackListed)
                );
            }

            if (blk.is(ModTags.Blocks.FORCED_CHISELABLE))
            {
                return new EligibilityAnalysisResult(
                  true,
                  false,
                  TranslationUtils.build(LocalStrings.ChiselSupportTagWhitelisted)
                );
            }

            try
            {
                // require basic hardness behavior...
                final ReflectionHelperBlock pb = new ReflectionHelperBlock();
                final Class<? extends Block> blkClass = blk.getClass();

                // custom dropping behavior?
                pb.getDrops(state, null);
                final Class<?> wc = ClassUtils.getDeclaringClass(blkClass, pb.MethodName, BlockState.class, LootContext.Builder.class);
                final boolean quantityDroppedTest = wc == Block.class || wc == AbstractBlock.class || wc == FlowingFluidBlock.class;

                final boolean isNotSlab = Item.byBlock(blk) != Items.AIR || state.getBlock() instanceof FlowingFluidBlock;
                boolean itemExistsOrNotSpecialDrops = quantityDroppedTest || isNotSlab;

                // ignore blocks with custom collision.
                pb.getShape(null, null, null, null);
                Class<?> collisionClass = ClassUtils.getDeclaringClass(blkClass, pb.MethodName, BlockState.class, IBlockReader.class, BlockPos.class, ISelectionContext.class);
                boolean noCustomCollision = collisionClass == Block.class || collisionClass == AbstractBlock.class || blk.getClass() == SlimeBlock.class || collisionClass == FlowingFluidBlock.class;

                // full cube specifically is tied to lighting... so for glass
                // Compatibility use isFullBlock which can be true for glass.
                boolean isFullBlock = state.canOcclude() || blk instanceof AbstractGlassBlock || blk instanceof FlowingFluidBlock;
                final BlockEligibilityAnalysisData info = BlockEligibilityAnalysisData.createFromState(state);

                final boolean tickingBehavior = blk.isRandomlyTicking(state) && Configuration.getInstance().getServer().blackListRandomTickingBlocks.get();
                boolean hasBehavior = (blk.hasTileEntity(state) || tickingBehavior);

                final Material remappedMaterial = MaterialManager.getInstance().remapMaterialIfNeeded(
                  state.getMaterial()
                );
                final boolean supportedMaterial = ModBlocks.MATERIAL_TO_BLOCK_CONVERSIONS.containsKey(remappedMaterial);

                if (!supportedMaterial) {
                    return new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportGenericNotSupported));
                }

                if (blkClass.isAnnotationPresent(IgnoreBlockLogic.class))
                {
                    isFullBlock = true;
                    noCustomCollision = true;
                    hasBehavior = false;
                    itemExistsOrNotSpecialDrops = true;
                }

                if (info.isCompatible() && noCustomCollision && info.getHardness() >= -0.01f && isFullBlock && supportedMaterial && !hasBehavior && itemExistsOrNotSpecialDrops)
                {
                    return new EligibilityAnalysisResult(
                      true,
                      false,
                      TranslationUtils.build ((blkClass.isAnnotationPresent(IgnoreBlockLogic.class))
                                                ? LocalStrings.ChiselSupportLogicIgnored
                                                : LocalStrings.ChiselSupportGenericSupported
                      ));
                }

                if (!state.getFluidState().isEmpty())
                {
                    return new EligibilityAnalysisResult(
                      true,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportGenericFluidSupport)
                    );
                }

                EligibilityAnalysisResult result = null;
                if (!info.isCompatible())
                {
                    result = new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportCompatDeactivated)
                    );
                }
                else if (!noCustomCollision)
                {
                    result = new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportCustomCollision)
                    );
                }
                else if (info.getHardness() < -0.01f)
                {
                    result = new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportNoHardness)
                    );
                }
                else if (!isNotSlab)
                {
                    result = new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportIsSlab)
                    );
                }
                else if (!isFullBlock)
                {
                    result = new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportNotFullBlock)
                    );
                }
                else if (hasBehavior)
                {
                    result = new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportHasBehaviour)
                    );
                }
                else if (!quantityDroppedTest)
                {
                    result = new EligibilityAnalysisResult(
                      false,
                      false,
                      TranslationUtils.build(LocalStrings.ChiselSupportHasCustomDrops)
                    );
                }
                return result;
            }
            catch (final Throwable t)
            {
                return new EligibilityAnalysisResult(
                  false,
                  false,
                  TranslationUtils.build(LocalStrings.ChiselSupportFailureToAnalyze)
                );
            }
        });
    }

    /**
     * Performs a chiselability analysis on the given {@link IItemProvider}.
     *
     * @param provider The {@link IItemProvider} to analyze.
     * @return The analysis result.
     */
    @Override
    public IEligibilityAnalysisResult analyse(@NotNull final IItemProvider provider)
    {
        final Item item = provider.asItem();
        if (item instanceof BlockItem) {
            return analyse(((BlockItem) item).getBlock());
        }

        return new EligibilityAnalysisResult(
          false,
          false,
          TranslationUtils.build(LocalStrings.ChiselSupportGenericNotSupported)
        );
    }
}
