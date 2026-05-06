package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;
public record GameruleRuleEntry(
        String id,
        String gameruleId,
        int priority,
        boolean stopAfter,
        int cooldownTicks,
        GameruleWhenPredicate when,
        GameruleAudience audience,
        GameruleRuleActions actions
) {
    public static GameruleRuleEntry fromJson(JsonObject obj) {
        if (obj == null) {
            GameruleActionDispatcher.logWarning("Skipping null gamerule rule entry.");
            return null;
        }

        String gameruleId = null;
        if (obj.has("gamerule") && obj.get("gamerule").isJsonPrimitive()) {
            gameruleId = obj.getAsJsonPrimitive("gamerule").getAsString();
        }

        if (gameruleId == null || gameruleId.isEmpty()) {
            GameruleActionDispatcher.logWarning("Skipping gamerule rule entry with missing/empty 'gamerule' field.");
            return null;
        }

        String id = obj.has("id") && obj.get("id").isJsonPrimitive() ? obj.getAsJsonPrimitive("id").getAsString() : gameruleId;
        int priority = obj.has("priority") && obj.get("priority").isJsonPrimitive() ? obj.getAsJsonPrimitive("priority").getAsInt() : 0;
        boolean stopAfter = obj.has("stop_after") && obj.get("stop_after").isJsonPrimitive()
                && obj.getAsJsonPrimitive("stop_after").getAsBoolean();
        int cooldownTicks = obj.has("cooldown_ticks") && obj.get("cooldown_ticks").isJsonPrimitive()
                ? Math.max(0, obj.getAsJsonPrimitive("cooldown_ticks").getAsInt())
                : 0;
        GameruleWhenPredicate when = obj.has("when") && obj.get("when").isJsonObject()
                ? GameruleWhenParser.parse(obj.getAsJsonObject("when"))
                : GameruleWhenPredicate.ANY;
        GameruleAudience audience = GameruleAudience.fromJson(obj, "'" + id + "' (" + gameruleId + ")");

        GameruleRuleActions actions = GameruleRuleActions.fromJson(obj, "'" + id + "' (" + gameruleId + ")");
        if (actions == null) {
            GameruleActionDispatcher.logWarning("Skipping rule '" + id + "' (" + gameruleId + ") because it has no valid actions.");
            return null;
        }

        return new GameruleRuleEntry(id, gameruleId, priority, stopAfter, cooldownTicks, when, audience, actions);
    }

    public boolean matches(GameruleMatchContext context) {
        return when.matches(context);
    }
}

