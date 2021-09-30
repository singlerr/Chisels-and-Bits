package mod.chiselsandbits.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import mod.chiselsandbits.api.chiseling.eligibility.IEligibilityManager;
import mod.chiselsandbits.api.exceptions.SpaceOccupiedException;
import mod.chiselsandbits.api.inventory.bit.IBitInventory;
import mod.chiselsandbits.api.inventory.management.IBitInventoryManager;
import mod.chiselsandbits.api.multistate.mutator.IMutableStateEntryInfo;
import mod.chiselsandbits.api.multistate.mutator.IMutatorFactory;
import mod.chiselsandbits.api.multistate.mutator.batched.IBatchMutation;
import mod.chiselsandbits.api.multistate.mutator.world.IWorldAreaMutator;
import mod.chiselsandbits.api.profiling.IProfiler;
import mod.chiselsandbits.api.profiling.IProfilerResult;
import mod.chiselsandbits.api.profiling.IProfilingManager;
import mod.chiselsandbits.api.util.BlockStateUtils;
import mod.chiselsandbits.api.util.LocalStrings;
import mod.chiselsandbits.profiling.ProfilingManager;
import mod.chiselsandbits.utils.CommandUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandManager
{
    private static final CommandManager INSTANCE = new CommandManager();
    private static final Logger         LOGGER   = LogManager.getLogger();

    private CommandManager()
    {
    }

    private static final SimpleCommandExceptionType GIVE_NOT_CHISELABLE_EXCEPTION = new SimpleCommandExceptionType(new TranslatableComponent(LocalStrings.CommandGiveErrorBlockStateNotChiselable
                                                                                                                                     .toString()));

    public static CommandManager getInstance()
    {
        return INSTANCE;
    }

    public void register(final CommandDispatcher<CommandSourceStack> dispatcher)
    {
        dispatcher.register(
          Commands.literal("candb")
            .requires(source -> source.hasPermission(2))
            .then(Commands.literal("fill")
                    .then(Commands.argument("start", Vec3Argument.vec3(false))
                            .then(Commands.argument("end", Vec3Argument.vec3(false))
                                    .then(Commands.argument("state", BlockStateArgument.block())
                                            .executes(this::runFillCommand)
                                    )
                                    .then(Commands.literal("random")
                                            .executes(this::runFillCommand)
                                    )
                            )
                    )
            )
            .then(Commands.literal("clear")
                    .then(Commands.argument("start", Vec3Argument.vec3(false))
                            .then(Commands.argument("end", Vec3Argument.vec3(false))
                                    .executes(this::runClearCommand)
                            )
                    )
            )
            .then(Commands.literal("stats")
                    .then(Commands.argument("start", Vec3Argument.vec3(false))
                            .then(Commands.argument("end", Vec3Argument.vec3(false))
                                    .executes(this::runStatsCommand)
                            )
                    )
            )
            .then(Commands.literal("give")
                    .then(Commands.argument("target", EntityArgument.player())
                            .then(Commands.argument("state", BlockStateArgument.block())
                                    .then(Commands.argument("count", IntegerArgumentType.integer(0, 64 * 64))
                                            .executes(this::runGiveCommand)
                                    )
                                    .executes(this::runGiveCommand)
                            )
                    )
            )
          .then(Commands.literal("profiling")
                    .then(Commands.literal("start")
                            .executes(this::startProfiling)
                    )
                    .then(Commands.literal("stop")
                            .executes(this::stopProfiling)
                    )
          )
        );
    }

    private int runFillCommand(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final Vec3 start = Vec3Argument.getVec3(context, "start");
        final Vec3 end = Vec3Argument.getVec3(context, "end");

        final IWorldAreaMutator mutator = IMutatorFactory.getInstance().covering(
          context.getSource().getLevel(),
          start,
          end
        );

        if (CommandUtils.hasArgument(context, "state"))
        {
            final BlockState state = BlockStateArgument.getBlock(context, "state").getState();
            try (final IBatchMutation ignored = mutator.batch())
            {
                mutator.mutableStream().forEach(
                  entry -> {
                      try
                      {
                          entry.clear();
                          entry.setState(state);
                      }
                      catch (SpaceOccupiedException e)
                      {
                          LOGGER.warn("Executing the command: " + context.getInput() + " failed to clear and set the state for entry: " + entry);
                      }
                  }
                );
            }
        }
        else
        {
            try (final IBatchMutation ignored = mutator.batch())
            {
                mutator.mutableStream().forEach(
                  entry -> {
                      try
                      {
                          entry.clear();
                          entry.setState(BlockStateUtils.getRandomSupportedDefaultState(context.getSource().getLevel().getRandom()));
                      }
                      catch (SpaceOccupiedException e)
                      {
                          LOGGER.warn("Executing the command: " + context.getInput() + " failed to clear and set the state for entry: " + entry);
                      }
                  }
                );
            }
        }

        context.getSource().sendSuccess(new TranslatableComponent(LocalStrings.CommandFillCompleted.toString()), true);
        return 0;
    }

    private int runClearCommand(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final Vec3 start = Vec3Argument.getVec3(context, "start");
        final Vec3 end = Vec3Argument.getVec3(context, "end");

        final IWorldAreaMutator mutator = IMutatorFactory.getInstance().covering(
          context.getSource().getLevel(),
          start,
          end
        );

        try (final IBatchMutation ignored = mutator.batch())
        {
            mutator.mutableStream().forEach(
              IMutableStateEntryInfo::clear
            );
        }

        return 0;
    }

    private int runStatsCommand(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final Vec3 start = Vec3Argument.getVec3(context, "start");
        final Vec3 end = Vec3Argument.getVec3(context, "end");

        final IWorldAreaMutator mutator = IMutatorFactory.getInstance().covering(
          context.getSource().getLevel(),
          start,
          end
        );

        context.getSource().sendSuccess(new TextComponent("Collected the following statistics for the requested area:"), true);
        context.getSource().sendSuccess(new TextComponent("----------------------------------------------------------"), true);
        context.getSource().sendSuccess(new TextComponent("BlockStates:"), true);
        context.getSource().sendSuccess(new TextComponent("############"), true);
        mutator.createSnapshot().getStatics()
          .getStateCounts().forEach((state, count) -> {
            context.getSource().sendSuccess(new TextComponent(" > ").append(state.getBlock().getName()).append(new TextComponent(": " + count)), true);
        });

        return 0;
    }

    private int runGiveCommand(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        final Player target = EntityArgument.getPlayer(context, "target");
        final BlockState state = BlockStateArgument.getBlock(context, "state").getState();
        if (!IEligibilityManager.getInstance().canBeChiseled(state))
            throw GIVE_NOT_CHISELABLE_EXCEPTION.create();

        final int count = CommandUtils.hasArgument(context, "count") ? IntegerArgumentType.getInteger(context, "count") : 4096;

        final IBitInventory inventory = IBitInventoryManager.getInstance().create(target);

        final int insertionCount = Math.min(inventory.getMaxInsertAmount(state), count);

        inventory.insertOrDiscard(state, insertionCount);

        return 0;
    }

    private int startProfiling(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        if (ProfilingManager.getInstance().hasProfiler()) {
            context.getSource().sendFailure(new TextComponent("Already profiling!"));
            return 1;
        }

        ProfilingManager.getInstance().setProfiler(
          IProfilingManager.getInstance().startProfiling()
        );

        return 0;
    }

    private int stopProfiling(final CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
        if (!ProfilingManager.getInstance().hasProfiler()) {
            context.getSource().sendFailure(new TextComponent("Not yet profiling!"));
            return 1;
        }

        final IProfilerResult result = IProfilingManager.getInstance().endProfiling(ProfilingManager.getInstance().getProfiler());
        result.writeAsResponse(line -> context.getSource().sendSuccess(new TextComponent(line), true));

        ProfilingManager.getInstance().setProfiler(null);

        return 0;
    }
}
