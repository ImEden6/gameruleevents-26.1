package com.mervyn.gameruleevents.gameruleevents;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class GameruleEventsCommands {
    private GameruleEventsCommands() {
    }

    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        dispatcher.register(Commands.literal("gameruleevents")
                .then(Commands.literal("list")
                        .executes(context -> {
                            var counts = GameruleActionDispatcher.getRuleCountByGamerule();
                            if (counts.isEmpty()) {
                                context.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("No gamerule event rules loaded."), false);
                                return 0;
                            }
                            context.getSource().sendSuccess(
                                    () -> net.minecraft.network.chat.Component.literal("Loaded " + counts.size() + " gamerule groups."),
                                    false
                            );
                            counts.forEach((ruleId, count) -> context.getSource().sendSuccess(
                                    () -> net.minecraft.network.chat.Component.literal("- " + ruleId + ": " + count + " rule(s)"),
                                    false
                            ));
                            return counts.size();
                        }))
                .then(Commands.literal("validate")
                        .executes(context -> {
                            GameruleActionDispatcher.ReloadDiagnostics diagnostics = GameruleActionDispatcher.getLastReloadDiagnostics();
                            context.getSource().sendSuccess(
                                    () -> net.minecraft.network.chat.Component.literal(
                                            "Last reload diagnostics: "
                                                    + diagnostics.warnings() + " warning(s), "
                                                    + diagnostics.errors() + " error(s)."
                                    ),
                                    false
                            );
                            context.getSource().sendSuccess(
                                    () -> net.minecraft.network.chat.Component.literal(
                                            "Tip: run /reload after datapack edits to refresh diagnostics."
                                    ),
                                    false
                            );
                            return diagnostics.warnings() + diagnostics.errors();
                        })));
    }
}
