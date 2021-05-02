package mod.chiselsandbits.client.model.baked.bit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import mod.chiselsandbits.api.item.bit.IBitItem;
import mod.chiselsandbits.client.events.TickHandler;
import mod.chiselsandbits.client.model.baked.simple.NullBakedModel;
import mod.chiselsandbits.registrars.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class BitBlockBakedModelManager
{
    private static final Logger                         LOGGER            = LogManager.getLogger();
    private static final BitBlockBakedModelManager      INSTANCE          = new BitBlockBakedModelManager();
    private final        Cache<BlockState, IBakedModel> modelCache        = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    private final        Cache<BlockState, IBakedModel> largeModelCache   = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
    private final        NonNullList<ItemStack>         alternativeStacks = NonNullList.create();

    private BitBlockBakedModelManager()
    {
    }

    public static BitBlockBakedModelManager getInstance()
    {
        return INSTANCE;
    }

    public IBakedModel get(
      ItemStack stack
    )
    {
        return get(
          stack,
          (!Minecraft.getInstance().gameSettings.keyBindSneak.isInvalid() && Minecraft.getInstance().gameSettings.keyBindSneak.isKeyDown()) || Screen.hasShiftDown()
        );
    }

    public IBakedModel get(
      ItemStack stack,
      final boolean large)
    {
        if (!(stack.getItem() instanceof IBitItem))
        {
            LOGGER.warn("Tried to get bit item model for non bit item");
            return NullBakedModel.instance;
        }

        return get(
          ((IBitItem) stack.getItem()).getBitState(stack),
          large
        );
    }

    public IBakedModel get(
      BlockState state,
      final boolean large)
    {
        if (state == Blocks.AIR.getDefaultState() || state == null)
        {
            //We are running an empty bit, for display purposes.
            //Lets loop:
            if (alternativeStacks.isEmpty())
            {
                ModItems.ITEM_BLOCK_BIT.get().fillItemGroup(Objects.requireNonNull(ModItems.ITEM_BLOCK_BIT.get().getGroup()), alternativeStacks);
            }

            final int alternativeIndex = (int) ((Math.floor(TickHandler.getClientTicks() / 20d)) % alternativeStacks.size());

            final ItemStack stack = this.alternativeStacks.get(alternativeIndex);
            if (!(stack.getItem() instanceof IBitItem))
            {
                throw new IllegalStateException("BitItem returned none bit item stack!");
            }

            state = ((IBitItem) stack.getItem()).getBitState(stack);
        }

        final Cache<BlockState, IBakedModel> target = large ? largeModelCache : modelCache;
        final BlockState workingState = state;
        try
        {
            return target.get(state, () -> {
                if (large)
                {
                    BlockState lookupState = workingState;
                    if (workingState.getBlock() instanceof FlowingFluidBlock)
                    {
                        lookupState = workingState.with(FlowingFluidBlock.LEVEL, 15);
                    }
                    return Minecraft.getInstance().getBlockRendererDispatcher().getModelForState(lookupState);
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
