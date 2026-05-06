package com.mervyn.gameruleevents.gameruleevents;

import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;

public final class PlaceholderUtil {
    private PlaceholderUtil() {
    }

    public static String replace(String text, GameruleMatchContext context, ServerPlayer player) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        Map<String, String> placeholders = new LinkedHashMap<>();
        placeholders.put("{gamerule}", context.gameruleId());
        placeholders.put("{new}", context.newValue());
        placeholders.put("{old}", context.oldValue() != null ? context.oldValue() : "");
        placeholders.put("{player}", player != null ? player.getName().getString() : "server");
        String output = text;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            output = output.replace(entry.getKey(), entry.getValue());
        }
        return output;
    }

    public static Component componentFromTemplate(JsonObject template, GameruleMatchContext context, ServerPlayer player) {
        if (template == null) {
            return null;
        }
        JsonElement replaced = replaceInJson(template.deepCopy(), context, player);
        if (!replaced.isJsonObject()) {
            return null;
        }
        return ComponentSerialization.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, replaced.getAsJsonObject()).result().orElse(null);
    }

    private static JsonElement replaceInJson(JsonElement element, GameruleMatchContext context, ServerPlayer player) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                object.add(entry.getKey(), replaceInJson(entry.getValue(), context, player));
            }
            return object;
        }
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                array.set(i, replaceInJson(array.get(i), context, player));
            }
            return array;
        }
        if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
            return new JsonPrimitive(replace(element.getAsString(), context, player));
        }
        return element;
    }
}
