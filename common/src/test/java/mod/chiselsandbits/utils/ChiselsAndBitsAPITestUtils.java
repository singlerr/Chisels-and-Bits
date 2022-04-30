package mod.chiselsandbits.utils;

import mod.chiselsandbits.api.IChiselsAndBitsAPI;
import mod.chiselsandbits.api.block.state.id.IBlockStateIdManager;
import mod.chiselsandbits.api.blockinformation.BlockInformation;
import mod.chiselsandbits.api.multistate.StateEntrySize;
import mod.chiselsandbits.api.multistate.accessor.IStateEntryInfo;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChiselsAndBitsAPITestUtils
{

    private ChiselsAndBitsAPITestUtils()
    {
        throw new IllegalStateException("Can not instantiate an instance of: ChiselsAndBitsAPITestUtils. This is a utility class");
    }

    @SafeVarargs
    public static void setupApi(Consumer<IChiselsAndBitsAPI>... configurator) {
        final IChiselsAndBitsAPI mock = mock(IChiselsAndBitsAPI.class);

        for (final Consumer<IChiselsAndBitsAPI> consumer : configurator)
        {
            consumer.accept(mock);
        }

        IChiselsAndBitsAPI.Holder.setInstance(mock);
    }

    public static void mockBlockStateIdManager(final IChiselsAndBitsAPI api) {
        final IBlockStateIdManager mock = mock(IBlockStateIdManager.class);

        final List<BlockState> stateIdList = new ArrayList<>();

        when(mock.getIdFrom(any())).thenAnswer(invocation -> {
           final BlockState state = invocation.getArgumentAt(0, BlockState.class);
           if (stateIdList.contains(state)) {
               return stateIdList.indexOf(state);
           }

           stateIdList.add(state);
           return stateIdList.size() - 1;
        });

        when(api.getBlockStateIdManager()).thenReturn(mock);
    }

    public static void mockDefaultStateEntrySize(final IChiselsAndBitsAPI api) {
        when(api.getStateEntrySize()).thenReturn(StateEntrySize.ONE_SIXTEENTH);
    }
}
