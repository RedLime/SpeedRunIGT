package com.redlimerl.speedrunigt.instance;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.RunCategory;
import com.redlimerl.speedrunigt.timer.category.RunCategoryArgumentType;
import com.redlimerl.speedrunigt.timer.running.RunType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.function.Function;

public class TimerCommand {

    private enum TimerAccessType {
        TICK(time -> (int) (time / 50)),
        MILLISECOND(Long::intValue),
        SECOND(time -> (int) (time / 1000));

        private final Function<Long, Integer> timeConvert;

        TimerAccessType(Function<Long, Integer> timeConvert) {
            this.timeConvert = timeConvert;
        }

        int getValue(long time) {
            return this.timeConvert.apply(time);
        }
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("speedrunigt")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.literal("get")
                        .then(CommandManager.literal("rta")
                                .then(CommandManager.literal("tick")
                                        .executes(context -> TimerCommand.getTime(context.getSource(), false, TimerAccessType.TICK))
                                )
                                .then(CommandManager.literal("millis")
                                        .executes(context -> TimerCommand.getTime(context.getSource(), false, TimerAccessType.MILLISECOND))
                                )
                                .then(CommandManager.literal("second")
                                        .executes(context -> TimerCommand.getTime(context.getSource(), false, TimerAccessType.SECOND))
                                )
                        )
                        .then(CommandManager.literal("igt")
                                .then(CommandManager.literal("tick")
                                        .executes(context -> TimerCommand.getTime(context.getSource(), true, TimerAccessType.TICK))
                                )
                                .then(CommandManager.literal("millis")
                                        .executes(context -> TimerCommand.getTime(context.getSource(), true, TimerAccessType.MILLISECOND))
                                )
                                .then(CommandManager.literal("second")
                                        .executes(context -> TimerCommand.getTime(context.getSource(), true, TimerAccessType.SECOND))
                                )
                        )
                )
                .then(CommandManager.literal("visible")
                        .then(CommandManager.literal("on")
                                .executes(context -> TimerCommand.setVisible(context.getSource(), true))
                        )
                        .then(CommandManager.literal("off")
                                .executes(context -> TimerCommand.setVisible(context.getSource(), false))
                        )
                )
                .then(CommandManager.literal("stop")
                        .executes(context -> TimerCommand.stopTimer(context.getSource()))
                )
                .then(CommandManager.literal("start")
                        .executes(context -> TimerCommand.startTimer(context.getSource(), RunCategories.CUSTOM, true))
                        .then(CommandManager.argument("startInstantly", BoolArgumentType.bool())
                                .executes(context -> TimerCommand.startTimer(context.getSource(), RunCategories.CUSTOM, BoolArgumentType.getBool(context, "startInstantly")))
                                .then(CommandManager.argument("category", new RunCategoryArgumentType())
                                        .executes(context -> TimerCommand.startTimer(context.getSource(), context.getArgument("category", RunCategory.class), BoolArgumentType.getBool(context, "startInstantly")))))
                );
        dispatcher.register(command);
    }

    private static int getTime(ServerCommandSource source, boolean isIGT, TimerAccessType timerAccessType) {
        if (InGameTimer.getInstance().isStopped()) {
            source.sendFeedback(new LiteralText("Timer is not running"), true);
            return 0;
        }
        int value = timerAccessType.getValue(isIGT ? InGameTimer.getInstance().getInGameTime() : InGameTimer.getInstance().getRealTimeAttack());
        source.sendFeedback(new LiteralText(String.format("IGT by %s: %s", timerAccessType.name().toLowerCase(), value)), true);
        return value;
    }

    private static int setVisible(ServerCommandSource source, boolean visible) {
        if (InGameTimer.getInstance().isStopped()) {
            source.sendFeedback(new LiteralText("Timer is not running"), true);
            return 0;
        }

        if (InGameTimer.getInstance().isInvisible() != visible) {
            source.sendFeedback(new LiteralText("Nothing has changed"), true);
            return 0;
        }

        InGameTimer.getInstance().setInvisible(!visible);
        source.sendFeedback(new LiteralText("Timer is now " + (visible ? "visible" : "invisible")), true);
        return 1;
    }

    private static int startTimer(ServerCommandSource source, RunCategory runCategory, boolean instantStart) {
        InGameTimer.start(InGameTimer.getInstance().getWorldName(), RunType.OLD_WORLD);
        InGameTimer.getInstance().setCategory(runCategory, true);
        if (instantStart) InGameTimer.getInstance().setPause(false, "instant start");
        source.sendFeedback(new LiteralText("Timer is started" + (instantStart ? " instantly" : "") + " with " + runCategory.getText().getString() + " category"), true);
        return 1;
    }

    private static int stopTimer(ServerCommandSource source) {
        if (InGameTimer.getInstance().isStopped()) {
            source.sendFeedback(new LiteralText("Timer is not running"), true);
            return 0;
        }

        InGameTimer.complete();
        source.sendFeedback(new LiteralText("Timer is stopped"), true);
        return 1;
    }
}
