package com.mervyn.gameruleevents.gameruleevents;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.mervyn.gameruleevents.GameruleEvents;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class GameruleActionDispatcher {
    private static volatile Map<String, List<GameruleRuleEntry>> rulesByGamerule = Collections.emptyMap();
    private static final Map<String, String> lastValuesByRule = new ConcurrentHashMap<>();
    private static final Map<String, Long> cooldownState = new ConcurrentHashMap<>();
    private static final AtomicInteger reloadWarnings = new AtomicInteger();
    private static final AtomicInteger reloadErrors = new AtomicInteger();
    private static volatile ReloadDiagnostics lastReloadDiagnostics = new ReloadDiagnostics(0, 0);

    static void updateRulesIndex(Map<String, List<GameruleRuleEntry>> index) {
        rulesByGamerule = index != null ? index : Collections.emptyMap();
        GameruleEvents.LOGGER.info("Loaded {} gamerule event rule groups", rulesByGamerule.size());
    }

    public static void onServerStarting() {
        lastValuesByRule.clear();
        cooldownState.clear();
        reloadWarnings.set(0);
        reloadErrors.set(0);
        lastReloadDiagnostics = new ReloadDiagnostics(0, 0);
    }

    static void logError(String message) {
        reloadErrors.incrementAndGet();
        GameruleEvents.LOGGER.error("[GameruleEvents] {}", message);
    }

    static void logWarning(String message) {
        reloadWarnings.incrementAndGet();
        GameruleEvents.LOGGER.warn("[GameruleEvents] {}", message);
    }

    static void beginReloadDiagnostics() {
        reloadWarnings.set(0);
        reloadErrors.set(0);
    }

    static void finishReloadDiagnostics() {
        lastReloadDiagnostics = new ReloadDiagnostics(reloadWarnings.get(), reloadErrors.get());
    }

    public static void onGameRuleChanged(MinecraftServer server, String gameruleId, String serializedValue) {
        if (server == null || gameruleId == null) {
            return;
        }

        List<GameruleRuleEntry> rules = rulesByGamerule.get(gameruleId);
        if (rules == null || rules.isEmpty()) {
            return;
        }

        String oldValue = lastValuesByRule.get(gameruleId);
        GameruleMatchContext context = new GameruleMatchContext(server, gameruleId, oldValue, serializedValue);
        long nowTick = server.getTickCount();
        List<ServerPlayer> players = server.getPlayerList().getPlayers();

        for (GameruleRuleEntry rule : rules) {
            if (!rule.matches(context)) {
                continue;
            }
            if (isOnCooldown(rule, nowTick)) {
                continue;
            }

            GameruleRuleActions actions = rule.actions();
            if (actions == null || actions.isEmpty()) {
                continue;
            }

            List<ServerPlayer> audiencePlayers = players.stream().filter(rule.audience()::matches).toList();
            for (ServerPlayer player : players) {
                if (!audiencePlayers.contains(player)) {
                    continue;
                }
                for (GameruleTitleAction title : actions.titleActions()) {
                    title.send(player, context);
                }
                for (GameruleSoundAction sound : actions.soundActions()) {
                    sound.play(player, context);
                }
                for (GameruleChatAction chat : actions.chatActions()) {
                    chat.send(player, context);
                }
                for (GameruleActionbarAction actionbar : actions.actionbarActions()) {
                    actionbar.send(player, context);
                }
            }
            if (com.mervyn.gameruleevents.Config.ALLOW_COMMAND_ACTIONS.getAsBoolean()) {
                for (GameruleCommandAction command : actions.commandActions()) {
                    if (command.runsAsPlayer()) {
                        for (ServerPlayer player : audiencePlayers) {
                            command.executeAsPlayer(player, context);
                        }
                    } else {
                        command.executeServer(server, context);
                    }
                }
            }
            markCooldown(rule, nowTick);
            if (rule.stopAfter()) {
                break;
            }
        }
        lastValuesByRule.put(gameruleId, serializedValue);
    }

    private static boolean isOnCooldown(GameruleRuleEntry rule, long nowTick) {
        if (rule.cooldownTicks() <= 0) {
            return false;
        }
        String key = cooldownKey(rule);
        Long lastTick = cooldownState.get(key);
        return lastTick != null && nowTick - lastTick < rule.cooldownTicks();
    }

    private static void markCooldown(GameruleRuleEntry rule, long nowTick) {
        if (rule.cooldownTicks() <= 0) {
            return;
        }
        cooldownState.put(cooldownKey(rule), nowTick);
    }

    private static String cooldownKey(GameruleRuleEntry rule) {
        return rule.gameruleId() + "|" + rule.id();
    }

    public static Map<String, Integer> getRuleCountByGamerule() {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, List<GameruleRuleEntry>> entry : rulesByGamerule.entrySet()) {
            counts.put(entry.getKey(), entry.getValue().size());
        }
        return counts;
    }

    public static ReloadDiagnostics getLastReloadDiagnostics() {
        return lastReloadDiagnostics;
    }

    public record ReloadDiagnostics(int warnings, int errors) {
    }
}

