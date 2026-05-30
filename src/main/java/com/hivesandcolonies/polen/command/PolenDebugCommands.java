package com.hivesandcolonies.polen.command;

import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class PolenDebugCommands {

    private PolenDebugCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
                Commands.literal("polen")
                        .requires(source -> source.hasPermission(2))

                        .then(Commands.literal("affinity")
                                .then(Commands.literal("get")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int affinity = PolenAffinityManager.getAffinity(player);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Polen affinity: " + affinity),
                                                    false
                                            );

                                            return 1;
                                        }))

                                .then(Commands.literal("set")
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 100))
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                    int value = IntegerArgumentType.getInteger(context, "value");

                                                    PolenAffinityManager.setAffinity(player, value);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Polen affinity set to: " + value),
                                                            false
                                                    );

                                                    return 1;
                                                })))

                                .then(Commands.literal("add")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                                    int amount = IntegerArgumentType.getInteger(context, "amount");

                                                    PolenAffinityManager.addAffinity(player, amount);

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal(
                                                                    "Polen affinity is now: "
                                                                            + PolenAffinityManager.getAffinity(player)
                                                            ),
                                                            false
                                                    );

                                                    return 1;
                                                })))

                                .then(Commands.literal("reset")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();

                                            PolenAffinityManager.resetAffinity(player);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Polen affinity reset."),
                                                    false
                                            );

                                            return 1;
                                        })))

                        .then(Commands.literal("flag")
                                .then(Commands.literal("set")
                                        .then(Commands.literal("name_revealed")
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                                    PolenStoryFlagsManager.setFlag(
                                                            player,
                                                            PolenStoryFlag.NAME_REVEALED
                                                    );

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Flag set: NAME_REVEALED"),
                                                            false
                                                    );

                                                    return 1;
                                                })))

                                .then(Commands.literal("clear")
                                        .then(Commands.literal("name_revealed")
                                                .executes(context -> {
                                                    ServerPlayer player = context.getSource().getPlayerOrException();

                                                    PolenStoryFlagsManager.clearFlag(
                                                            player,
                                                            PolenStoryFlag.NAME_REVEALED
                                                    );

                                                    context.getSource().sendSuccess(
                                                            () -> Component.literal("Flag cleared: NAME_REVEALED"),
                                                            false
                                                    );

                                                    return 1;
                                                })))

                                .then(Commands.literal("reset")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();

                                            PolenStoryFlagsManager.resetFlags(player);

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Polen story flags reset."),
                                                    false
                                            );

                                            return 1;
                                        })))
                        .then(Commands.literal("chapter")

        .then(Commands.literal("get")
                .executes(context -> {

                    ServerPlayer player =
                            context.getSource().getPlayerOrException();

                    int chapter =
                            PolenChapterManager.getCurrentChapter(player);

                    context.getSource().sendSuccess(
                            () -> Component.literal(
                                    "Current chapter: " + chapter
                            ),
                            false
                    );

                    return 1;
                }))

        .then(Commands.literal("set")
                .then(Commands.argument(
                                "chapter",
                                IntegerArgumentType.integer(
                                        0,
                                        PolenChapterManager.NEW_BEGINNING
                                ))
                        .executes(context -> {

                            ServerPlayer player =
                                    context.getSource().getPlayerOrException();

                            int chapter =
                                    IntegerArgumentType.getInteger(
                                            context,
                                            "chapter"
                                    );

                            PolenChapterManager.setCurrentChapter(
                                    player,
                                    chapter
                            );

                            context.getSource().sendSuccess(
                                    () -> Component.literal(
                                            "Chapter set to: " + chapter
                                    ),
                                    false
                            );

                            return 1;
                        })))

        .then(Commands.literal("reset")
                .executes(context -> {

                    ServerPlayer player =
                            context.getSource().getPlayerOrException();

                    PolenChapterManager.resetChapter(player);

                    context.getSource().sendSuccess(
                            () -> Component.literal(
                                    "Chapter reset."
                            ),
                            false
                    );

                    return 1;
                })))
        );
    }
}