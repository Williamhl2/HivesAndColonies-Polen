package com.hivesandcolonies.polen.command;

import com.hivesandcolonies.polen.entity.PolenEntity;
import com.hivesandcolonies.polen.entity.ai.debug.PolenAiDebugInspector;
import com.hivesandcolonies.polen.entity.ai.debug.PolenAiDebugSnapshot;
import com.hivesandcolonies.polen.progression.PolenAffinityManager;
import com.hivesandcolonies.polen.progression.PolenChapterManager;
import com.hivesandcolonies.polen.progression.PolenStoryFlag;
import com.hivesandcolonies.polen.progression.PolenStoryFlagsManager;
import com.hivesandcolonies.polen.progression.player.PolenPlayerRelationshipData;
import com.hivesandcolonies.polen.progression.player.PolenPlayerRelationshipManager;
import com.hivesandcolonies.polen.progression.world.PolenWorldStoryData;
import com.hivesandcolonies.polen.progression.world.PolenWorldStorySavedData;

import com.hivesandcolonies.polen.story.PolenMemoryManager;
import com.hivesandcolonies.polen.story.PolenMemoryType;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.EnumSet;

public final class PolenDebugCommands {

    private PolenDebugCommands() {}

    public static void register(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("polen")
                        .requires(source -> source.hasPermission(2))
                        .then(registerAffinityCommands())
                        .then(registerChapterCommands())
                        .then(registerFlagCommands())
                        .then(registerMemoryCommands())
                        .then(registerMoodCommands())
                        .then(Commands.literal("ai")
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PolenEntity polen = findNearbyPolen(player);

                                    if (polen == null) {
                                        context.getSource().sendFailure(Component.literal("No nearby Polen entity found."));
                                        return 0;
                                    }

                                    PolenAiDebugSnapshot snapshot = PolenAiDebugInspector.inspect(polen);
                                    context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Polen AI -> mood="
                                                            + snapshot.mood()
                                                            + ", moodReason="
                                                            + snapshot.moodReason()
                                                            + ", intent="
                                                            + snapshot.intent()
                                                            + ", intentReason="
                                                            + snapshot.intentReason()
                                                            + ", task="
                                                            + snapshot.task()
                                                            + ", desiredTask="
                                                            + snapshot.desiredTask()
                                                            + ", taskStatus="
                                                            + snapshot.taskStatus()
                                                            + ", taskReason="
                                                            + snapshot.taskReason()
                                                            + ", taskNote="
                                                            + snapshot.taskNote()
                                                            + ", recentFailedTask="
                                                            + snapshot.recentFailedTask()
                                                            + ", recentFailureCount="
                                                            + snapshot.recentFailureCount()
                                                            + ", taskRecoverUntil="
                                                            + snapshot.taskRecoverUntil()
                                                            + ", quietActivity="
                                                            + snapshot.quietActivity()
                                                            + ", equippedAffinityCharm="
                                                            + snapshot.equippedAffinityCharm()
                                                            + ", dominantNeed="
                                                            + snapshot.dominantNeed()
                                                            + ", safetyNeed="
                                                            + snapshot.safetyNeed()
                                                            + ", socialNeed="
                                                            + snapshot.socialNeed()
                                                            + ", curiosityNeed="
                                                            + snapshot.curiosityNeed()
                                                            + ", restNeed="
                                                            + snapshot.restNeed()
                                                            + ", magicNeed="
                                                            + snapshot.magicNeed()
                                                            + ", unsafeArea="
                                                            + snapshot.unsafeArea()
                                                            + ", shouldSeekSafety="
                                                            + snapshot.shouldSeekSafety()
                                                            + ", unsafeDialogue="
                                                            + snapshot.shouldUseUnsafeDialogue()
                                                            + ", searchType="
                                                            + snapshot.searchType()
                                                            + ", searchStatus="
                                                            + snapshot.searchStatus()
                                                            + ", searchNote="
                                                            + snapshot.searchNote()
                                                            + ", searchTarget="
                                                            + formatPos(snapshot.searchTarget())
                                                            + ", observedPos="
                                                            + formatPos(snapshot.observedPos())
                                                            + ", observationFocus="
                                                            + snapshot.observationFocus()
                                                            + ", observationDisposition="
                                                            + snapshot.observationDisposition()
                                                            + ", observationAffordance="
                                                            + snapshot.observationAffordanceType()
                                                            + ", observationContext="
                                                            + snapshot.observationContext()
                                                            + ", observationNote="
                                                            + snapshot.observationNote()
                                                            + ", observationFocusPos="
                                                            + formatPos(snapshot.observationFocusPos())
                                                            + ", observationUsePos="
                                                            + formatPos(snapshot.observationUsePos())
                                                            + ", nearRememberedInterest="
                                                            + snapshot.nearRememberedInterest()
                                                            + ", flowerSpot="
                                                            + formatPos(snapshot.flowerSpot())
                                                            + ", hiveSpot="
                                                            + formatPos(snapshot.hiveSpot())
                                                            + ", sourceSpot="
                                                            + formatPos(snapshot.sourceSpot())
                                                            + ", residenceAnchor="
                                                            + formatPos(snapshot.residenceAnchor())
                                                            + ", residenceUse="
                                                            + formatPos(snapshot.residenceUse())
                                                            + ", residenceContext="
                                                            + snapshot.residenceContext()
                                                            + ", residenceStage="
                                                            + snapshot.residenceStage()
                                                            + ", restingSpot="
                                                            + formatPos(snapshot.restingSpot())
                                                            + ", dangerousSpot="
                                                            + formatPos(snapshot.dangerousSpot())
                                            ),
                                            false
                                    );
                                    return 1;
                                })))
                        .then(Commands.literal("thoughts")
                                .then(Commands.literal("on")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            PolenEntity polen = findNearbyPolen(player);

                                            if (polen == null) {
                                                context.getSource().sendFailure(Component.literal("No nearby Polen entity found."));
                                                return 0;
                                            }

                                            polen.getAiState().setDebugThoughtsEnabled(true);
                                            polen.getAiState().setLastThoughtDebugSignature("");
                                            polen.getAiState().setLastThoughtDebugGameTime(0L);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Polen thought debug enabled."),
                                                    false
                                            );
                                            return 1;
                                        }))
                                .then(Commands.literal("off")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            PolenEntity polen = findNearbyPolen(player);

                                            if (polen == null) {
                                                context.getSource().sendFailure(Component.literal("No nearby Polen entity found."));
                                                return 0;
                                            }

                                            polen.getAiState().setDebugThoughtsEnabled(false);
                                            context.getSource().sendSuccess(
                                                    () -> Component.literal("Polen thought debug disabled."),
                                                    false
                                            );
                                            return 1;
                                        }))
                                .then(Commands.literal("get")
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            PolenEntity polen = findNearbyPolen(player);

                                            if (polen == null) {
                                                context.getSource().sendFailure(Component.literal("No nearby Polen entity found."));
                                                return 0;
                                            }

                                            context.getSource().sendSuccess(
                                                    () -> Component.literal(
                                                            "Polen thought debug: "
                                                                    + (polen.getAiState().isDebugThoughtsEnabled() ? "enabled" : "disabled")
                                                    ),
                                                    false
                                            );
                                            return 1;
                                        })))
                .then(Commands.literal("relationship")
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PolenPlayerRelationshipData relationship =
                                            PolenPlayerRelationshipManager.getRelationship(player);

                                    context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "Relationship -> affinity="
                                                            + relationship.getAffinity()
                                                            + ", interactions="
                                                            + relationship.getInteractionCount()
                                                            + ", tasksCompleted="
                                                            + relationship.getTasksCompletedForPolen()
                                                            + ", lastInteractionGameTime="
                                                            + relationship.getLastInteractionGameTime()
                                            ),
                                            false
                                    );
                                    return 1;
                                })))
                .then(Commands.literal("worlddata")
                        .then(Commands.literal("get")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    PolenWorldStoryData worldData =
                                            PolenWorldStorySavedData.get(player.serverLevel()).getData();

                                    context.getSource().sendSuccess(
                                            () -> Component.literal(
                                                    "World data -> chapter="
                                                            + worldData.getCurrentChapter()
                                                            + ", flags="
                                                            + worldData.getWorldFlags()
                                                            + ", polenEntityUuid="
                                                            + worldData.getPolenEntityUuid()
                                                            + ", polenSpawned="
                                                            + worldData.isPolenSpawned()
                                            ),
                                            false
                                    );
                                    return 1;
                                })));

        event.getDispatcher().register(root);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> registerAffinityCommands() {
        return Commands.literal("affinity")
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
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> registerChapterCommands() {
        return Commands.literal("chapter")
                .then(Commands.literal("get")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            int chapter = PolenChapterManager.getCurrentChapter(player.serverLevel());

                            context.getSource().sendSuccess(
                                    () -> Component.literal("Current world chapter: " + chapter),
                                    false
                            );

                            return 1;
                        }))
                .then(Commands.literal("set")
                        .then(Commands.argument(
                                        "chapter",
                                        IntegerArgumentType.integer(0, PolenChapterManager.NEW_BEGINNING)
                                )
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    int chapter = IntegerArgumentType.getInteger(context, "chapter");

                                    PolenChapterManager.setCurrentChapter(player.serverLevel(), chapter);

                                    context.getSource().sendSuccess(
                                            () -> Component.literal("World chapter set to: " + chapter),
                                            false
                                    );

                                    return 1;
                                })))
                .then(Commands.literal("reset")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PolenChapterManager.resetChapter(player.serverLevel());

                            context.getSource().sendSuccess(
                                    () -> Component.literal("World chapter reset."),
                                    false
                            );

                            return 1;
                        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> registerFlagCommands() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("flag")
                .then(Commands.literal("get")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            EnumSet<PolenStoryFlag> flags = PolenStoryFlagsManager.getFlags(player.serverLevel());

                            context.getSource().sendSuccess(
                                    () -> Component.literal("World flags: " + flags),
                                    false
                            );

                            return 1;
                        }))
                .then(Commands.literal("reset")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            PolenStoryFlagsManager.resetFlags(player.serverLevel());

                            context.getSource().sendSuccess(
                                    () -> Component.literal("Polen world story flags reset."),
                                    false
                            );

                            return 1;
                        }));

        LiteralArgumentBuilder<CommandSourceStack> set = Commands.literal("set");
        LiteralArgumentBuilder<CommandSourceStack> clear = Commands.literal("clear");

        for (PolenStoryFlag flag : PolenStoryFlag.values()) {
            String flagName = flag.name().toLowerCase();

            set.then(Commands.literal(flagName)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        PolenStoryFlagsManager.setFlag(player.serverLevel(), flag);

                        context.getSource().sendSuccess(
                                () -> Component.literal("World flag set: " + flag.name()),
                                false
                        );

                        return 1;
                    }));

            clear.then(Commands.literal(flagName)
                    .executes(context -> {
                        ServerPlayer player = context.getSource().getPlayerOrException();
                        PolenStoryFlagsManager.clearFlag(player.serverLevel(), flag);

                        context.getSource().sendSuccess(
                                () -> Component.literal("World flag cleared: " + flag.name()),
                                false
                        );

                        return 1;
                    }));
        }

        return root.then(set).then(clear);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> registerMemoryCommands() {
            LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("memory");

            for (PolenMemoryType memory : PolenMemoryType.values()) {
                String memoryName = memory.name().toLowerCase();

                root.then(Commands.literal(memoryName)
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();

                            PolenMemoryManager.unlockMemory(
                                    player.serverLevel(),
                                    memory,
                                    player.getX(),
                                    player.getY(),
                                    player.getZ()
                            );

                            context.getSource().sendSuccess(
                                    () -> Component.literal("Unlocked Polen memory: " + memory.name()),
                                    false
                            );

                            return 1;
                        }));
            }

            return root;
        }

        private static LiteralArgumentBuilder<CommandSourceStack> registerMoodCommands() {
            return Commands.literal("mood")
                    .then(Commands.literal("get")
                            .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();

                                PolenEntity polen = findNearbyPolen(player);
                                
                                if (polen == null) {
                                    context.getSource().sendFailure(
                                            Component.literal("No nearby Polen entity found.")
                                    );
                                    return 0;
                                }
                        
                                context.getSource().sendSuccess(
                                        () -> Component.literal("Polen mood: " + polen.getMood().name()),
                                        false
                                );
                        
                                return 1;
                            }));
        }

    private static PolenEntity findNearbyPolen(ServerPlayer player) {
        return player.serverLevel()
                .getEntitiesOfClass(
                        PolenEntity.class,
                        player.getBoundingBox().inflate(64.0D)
                )
                .stream()
                .findFirst()
                .orElse(null);
    }

    private static String formatPos(BlockPos pos) {
        if (pos == null) {
            return "null";
        }

        return "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
    }
}
