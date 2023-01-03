package mod.chiselsandbits.client.model.baked.bit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.blockinformation.IBlockInformation;
import mod.chiselsandbits.blockinformation.BlockInformation;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.api.variant.state.IStateVariantManager;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.client.time.TickHandler;
import mod.chiselsandbits.registrars.ModCreativeTabs;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BitBlockBakedModelManager
{
    private static final Logger                         LOGGER            = LogManager.getLogger();
    private static final BitBlockBakedModelManager      INSTANCE          = new BitBlockBakedModelManager();
    private final        Cache<IBlockInformation, BakedModel> modelCache        = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    private final        Cache<IBlockInformation, BakedModel> largeModelCache   = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    private final        NonNullList<ItemStack>         alternativeStacks = NonNullList.create();

    private BitBlockBakedModelManager()
    {
    }

    public static BitBlockBakedModelManager getInstance()
    {
        return INSTANCE;
    }

    public void clearCache() {
        modelCache.asMap().clear();
        largeModelCache.asMap().clear();
    }

    public BakedModel get(
      ItemStack stack,
      final Level world,
      final LivingEntity entity)
    {
        return get(
          stack,
          world,
          entity,
          (!Minecraft.getInstance().options.keyShift.isUnbound() && Minecraft.getInstance().options.keyShift.isDown()) || (Minecraft.getInstance().getWindow() != null && Screen.hasShiftDown())
        );
    }

    public BakedModel get(
      ItemStack stack,
      final Level world,
      final LivingEntity entity,
      final boolean large
    )
    {
        if (!(stack.getItem() instanceof IBitItem))
        {
            LOGGER.warn("Tried to get bit item model for non bit item");
            return NullBakedModel.instance;
        }

        final BakedModel model = get(
          large,
          ((IBitItem) stack.getItem()).getBlockInformation(stack),
          world,
          entity
        );

        return model.getOverrides().resolve(
          model,
          stack,
          null,
          null,
          0
        );
    }

    public BakedModel get(
      final boolean large,
      IBlockInformation blockInformation,
      final Level level,
      final LivingEntity entity)
    {
        if (blockInformation.isAir() || blockInformation == null)
        {
            if (alternativeStacks.isEmpty()) {
                ModCreativeTabs.BITS.get().buildContents(FeatureFlags.VANILLA_SET, false);
                this.alternativeStacks.addAll(ModCreativeTabs.BITS.get().getDisplayItems());
            }

            final int alternativeIndex = (int) ((Math.floor(TickHandler.getClientTicks() / 20d)) % alternativeStacks.size());

            final ItemStack alternativeStack = this.alternativeStacks.get(alternativeIndex);
            if (!(alternativeStack.getItem() instanceof IBitItem))
            {
                throw new IllegalStateException("BitItem returned none bit item stack!");
            }

            blockInformation = ((IBitItem) alternativeStack.getItem()).getBlockInformation(alternativeStack);
        }

        final Cache<IBlockInformation, BakedModel> target = large ? largeModelCache : modelCache;
        final IBlockInformation workingState = blockInformation;
        try
        {
            return target.get(blockInformation, () -> {
                if (large)
                {
                    ItemStack lookupStack = IStateVariantManager.getInstance().getItemStack(workingState).orElseGet(
                      () -> new ItemStack(workingState.getBlockState().getBlock())
                    );
                    if (workingState.getBlockState().getBlock() instanceof LiquidBlock)
                    {
                        lookupStack = new ItemStack(workingState.getBlockState().getFluidState().getType().getBucket());
                    }
                    return Minecraft.getInstance().getItemRenderer().getModel(
                      lookupStack,
                      level,
                      entity,
                      0
                    );
                }
                else
                {
                    return new BitBlockBakedModel(workingState);
                }
            });
        }
        catch (ExecutionException e)
        {
            LOGGER.warn("Failed to get a model for a bit: " + blockInformation + " the model calculation got aborted.", e);
            return NullBakedModel.instance;
        }
    }
}
