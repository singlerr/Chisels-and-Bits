package mod.chiselsandbits.client.model.baked.bit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.client.time.TickHandler;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BitBlockBakedModelManager
{
    private static final Logger                         LOGGER            = LogManager.getLogger();
    private static final BitBlockBakedModelManager      INSTANCE          = new BitBlockBakedModelManager();
    private final        Cache<BlockState, BakedModel> modelCache        = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    private final        Cache<BlockState, BakedModel> largeModelCache   = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
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
          stack,
          large,
          ((IBitItem) stack.getItem()).getBitState(stack),
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
      final ItemStack stack,
      final boolean large,
      BlockState state,
      final Level level,
      final LivingEntity entity)
    {
        if (state == Blocks.AIR.defaultBlockState() || state == null)
        {
            //We are running an empty bit, for display purposes.
            //Lets loop:
            if (alternativeStacks.isEmpty())
            {
                ModItems.ITEM_BLOCK_BIT.get().fillItemCategory(Objects.requireNonNull(ModItems.ITEM_BLOCK_BIT.get().getItemCategory()), alternativeStacks);
            }

            final int alternativeIndex = (int) ((Math.floor(TickHandler.getClientTicks() / 20d)) % alternativeStacks.size());

            final ItemStack alternativeStack = this.alternativeStacks.get(alternativeIndex);
            if (!(alternativeStack.getItem() instanceof IBitItem))
            {
                throw new IllegalStateException("BitItem returned none bit item stack!");
            }

            state = ((IBitItem) alternativeStack.getItem()).getBitState(alternativeStack);
        }

        final Cache<BlockState, BakedModel> target = large ? largeModelCache : modelCache;
        final BlockState workingState = state;
        try
        {
            return target.get(state, () -> {
                if (large)
                {
                    ItemStack lookupStack = new ItemStack(workingState.getBlock());
                    if (workingState.getBlock() instanceof LiquidBlock)
                    {
                        lookupStack = new ItemStack(workingState.getFluidState().getType().getBucket());
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
            LOGGER.warn("Failed to get a model for a bit: " + state + " the model calculation got aborted.", e);
            return NullBakedModel.instance;
        }
    }
}
