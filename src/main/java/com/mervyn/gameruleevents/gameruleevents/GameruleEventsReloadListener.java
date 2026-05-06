package com.mervyn.gameruleevents.gameruleevents;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import java.io.Reader;

public class GameruleEventsReloadListener extends SimplePreparableReloadListener<Map<Identifier, JsonElement>> {

    public static final GameruleEventsReloadListener INSTANCE = new GameruleEventsReloadListener();

    private volatile Map<String, List<GameruleRuleEntry>> rulesByGamerule = Map.of();

    private GameruleEventsReloadListener() {
    }

    @Override
    protected Map<Identifier, JsonElement> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<Identifier, JsonElement> map = new HashMap<>();
        net.minecraft.resources.FileToIdConverter converter = net.minecraft.resources.FileToIdConverter.json("gamerule_events");
        for (Map.Entry<Identifier, net.minecraft.server.packs.resources.Resource> entry : converter.listMatchingResources(resourceManager).entrySet()) {
            Identifier id = entry.getKey();
            Identifier identifier = converter.fileToId(id);
            try (Reader reader = entry.getValue().openAsReader()) {
                JsonElement json = com.google.gson.JsonParser.parseReader(reader);
                map.put(identifier, json);
            } catch (Exception e) {
                GameruleActionDispatcher.logError("Failed to parse gamerule_events file " + id + ": " + e.getMessage());
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, List<GameruleRuleEntry>> byRule = new HashMap<>();

        for (Map.Entry<Identifier, JsonElement> entry : jsonMap.entrySet()) {
            Identifier id = entry.getKey();
            JsonElement root = entry.getValue();
            try {
                if (!root.isJsonObject()) {
                    continue;
                }

                JsonObject obj = root.getAsJsonObject();
                JsonElement rulesElement = obj.get("rules");
                if (rulesElement == null || !rulesElement.isJsonArray()) {
                    continue;
                }

                for (JsonElement ruleElement : rulesElement.getAsJsonArray()) {
                    if (!ruleElement.isJsonObject()) {
                        continue;
                    }

                    GameruleRuleEntry rule = GameruleRuleEntry.fromJson(ruleElement.getAsJsonObject());
                    if (rule == null) {
                        continue;
                    }

                    byRule.computeIfAbsent(rule.gameruleId(), k -> new ArrayList<>()).add(rule);
                }
            } catch (JsonParseException e) {
                // Logged through the dispatcher
                GameruleActionDispatcher.logError("Failed to parse gamerule_events file " + id + ": " + e.getMessage());
            }
        }

        this.rulesByGamerule = Map.copyOf(byRule);
        GameruleActionDispatcher.updateRulesIndex(this.rulesByGamerule);
    }
}

