package com.mervyn.gameruleevents.gameruleevents;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mervyn.gameruleevents.GameruleEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class GameruleActionDispatcher {
    private static volatile Map<String, List<GameruleRuleEntry>> rulesByGamerule = Collections.emptyMap();

    static void updateRulesIndex(Map<String, List<GameruleRuleEntry>> index) {
        rulesByGamerule = index != null ? index : Collections.emptyMap();
        GameruleEvents.LOGGER.info("Loaded {} gamerule event rule groups", rulesByGamerule.size());
    }

    static void logError(String message) {
        GameruleEvents.LOGGER.error("[GameruleEvents] {}", message);
    }

    public static void onGameRuleChanged(MinecraftServer server, String gameruleId, String serializedValue) {
        if (server == null || gameruleId == null) {
            return;
        }

        List<GameruleRuleEntry> rules = rulesByGamerule.get(gameruleId);
        if (rules == null || rules.isEmpty()) {
            return;
        }

        List<ServerPlayer> players = server.getPlayerList().getPlayers();
        if (players.isEmpty()) {
            return;
        }

        for (GameruleRuleEntry rule : rules) {
            if (!rule.matches(serializedValue)) {
                continue;
            }

            GameruleRuleActions actions = rule.actions();
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            for (ServerPlayer player : players) {
                for (GameruleTitleAction title : actions.titleActions()) {
                    title.send(player);
                }
                for (GameruleSoundAction sound : actions.soundActions()) {
                    sound.play(player);
                }
            }
        }
    }
}

