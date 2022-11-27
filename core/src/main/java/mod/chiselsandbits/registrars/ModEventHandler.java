package mod.chiselsandbits.registrars;

import com.communi.suggestu.scena.core.event.*;
import mod.chiselsandbits.api.item.click.ClickProcessingState;
import mod.chiselsandbits.logic.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public final class ModEventHandler {

    private static final Logger LOGGER = LogManager.getLogger();

    private ModEventHandler() {
        throw new IllegalStateException("Can not instantiate an instance of: ModEventHandler. This is a utility class");
    }

    public static void onModConstruction() {
        LOGGER.info("Registering event handlers");

        IGameEvents.getInstance().getItemEntityPickupEvent().register(BitStackPickupHandler::pickupItems);
        IGameEvents.getInstance().getPlayerLeftClickEvent().register(new IPlayerLeftClickBlockEvent() {
            @Override
            public Result handle(Player player, InteractionHand interactionHand, ItemStack itemStack, BlockPos blockPos, Direction direction, Result result) {
                final ClickProcessingState state = LeftClickInteractionHandler.leftClickOnBlock(
                        player,
                        interactionHand,
                        itemStack,
                        blockPos,
                        direction,
                        result.handled(),
                        mapResult(result)
                );

                return new Result(state.shouldCancel(), mapResult(state.getNextState()));
            }

            private static ClickProcessingState.ProcessingResult mapResult(
                    final IPlayerLeftClickBlockEvent.Result result)
            {
                return switch (result.result())
                        {
                            case DENY -> ClickProcessingState.ProcessingResult.DENY;
                            case DEFAULT -> ClickProcessingState.ProcessingResult.DEFAULT;
                            case ALLOW -> ClickProcessingState.ProcessingResult.ALLOW;
                        };
            }

            private static ProcessingResult mapResult(
                    final ClickProcessingState.ProcessingResult processingResult
            ) {
                return switch (processingResult)
                        {
                            case DENY -> ProcessingResult.DENY;
                            case DEFAULT -> ProcessingResult.DEFAULT;
                            case ALLOW -> ProcessingResult.ALLOW;
                        };
            }
        });
        IGameEvents.getInstance().getPlayerJoinedWorldEvent().register((player, level) -> ChiselingManagerCountDownResetHandler.doResetFor(player));
        IGameEvents.getInstance().getPlayerLoggedInEvent().register(player -> {
            MeasuringSynchronisationHandler.syncToAll();
            ChiselingManagerCountDownResetHandler.doResetFor(player);
        });
        IGameEvents.getInstance().getRegisterCommandsEvent().register(CommandRegistrationHandler::registerCommandsTo);
        IGameEvents.getInstance().getPlayerRightClickEvent().register(new IPlayerRightClickBlockEvent() {
            @Override
            public Result handle(Player player, InteractionHand interactionHand, ItemStack itemStack, BlockPos blockPos, Direction direction, Result result) {
                final ClickProcessingState state = RightClickInteractionHandler.rightClickOnBlock(
                        player.getLevel(),
                        player,
                        interactionHand,
                        itemStack,
                        blockPos,
                        direction,
                        result.handled(),
                        mapResult(result)
                );

                return new Result(state.shouldCancel(), mapResult(state.getNextState()), mapResult(state.getNextState()), mapResult(state.getNextState()));
            }

            private static ClickProcessingState.ProcessingResult mapResult(
                    final IPlayerRightClickBlockEvent.Result result)
            {
                return switch (result.result())
                        {
                            case DENY -> ClickProcessingState.ProcessingResult.DENY;
                            case DEFAULT -> ClickProcessingState.ProcessingResult.DEFAULT;
                            case ALLOW -> ClickProcessingState.ProcessingResult.ALLOW;
                        };
            }

            private static ProcessingResult mapResult(
                    final ClickProcessingState.ProcessingResult processingResult
            ) {
                return switch (processingResult)
                        {
                            case DENY -> ProcessingResult.DENY;
                            case DEFAULT -> ProcessingResult.DEFAULT;
                            case ALLOW -> ProcessingResult.ALLOW;
                        };
            }
        });
        IGameEvents.getInstance().getServerAboutToStartEvent().register(minecraftServer -> ServerStartHandler.onServerStart());
    }
}
