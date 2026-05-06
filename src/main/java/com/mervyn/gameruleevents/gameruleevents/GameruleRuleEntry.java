package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public record GameruleRuleEntry(String gameruleId, String equalsValue, boolean any, GameruleRuleActions actions) {
    public static GameruleRuleEntry fromJson(JsonObject obj) {
        if (obj == null) {
            return null;
        }

        String gameruleId = null;
        if (obj.has("gamerule") && obj.get("gamerule").isJsonPrimitive()) {
            gameruleId = obj.getAsJsonPrimitive("gamerule").getAsString();
        }

        if (gameruleId == null || gameruleId.isEmpty()) {
            return null;
        }

        String equalsValue = null;
        boolean any = true;

        if (obj.has("when") && obj.get("when").isJsonObject()) {
            JsonObject when = obj.getAsJsonObject("when");
            if (when.has("any") && when.get("any").isJsonPrimitive()) {
                any = when.getAsJsonPrimitive("any").getAsBoolean();
            }

            if (when.has("equals")) {
                JsonPrimitive primitive = when.getAsJsonPrimitive("equals");
                if (primitive != null) {
                    equalsValue = primitive.getAsString();
                    any = false;
                }
            }
        }

        GameruleRuleActions actions = GameruleRuleActions.fromJson(obj);
        if (actions == null) {
            return null;
        }

        return new GameruleRuleEntry(gameruleId, equalsValue, any, actions);
    }

    public boolean matches(String serializedValue) {
        if (any) {
            return true;
        }
        return equalsValue != null && equalsValue.equals(serializedValue);
    }
}

