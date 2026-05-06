package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.level.ServerPlayer;

public record GameruleAudience(String type, int permissionLevel, Identifier dimension) {
    public static final GameruleAudience ALL = new GameruleAudience("all", 0, null);

    public static GameruleAudience fromJson(JsonObject obj, String ruleRef) {
        if (obj == null || !obj.has("audience")) {
            return ALL;
        }
        if (obj.get("audience").isJsonPrimitive()) {
            String audience = obj.getAsJsonPrimitive("audience").getAsString();
            return switch (audience) {
                case "ops" -> new GameruleAudience("permission_level", 2, null);
                case "all" -> ALL;
                default -> {
                    GameruleActionDispatcher.logWarning("Rule " + ruleRef + " has unknown audience '" + audience + "'. Falling back to 'all'.");
                    yield ALL;
                }
            };
        }
        if (!obj.get("audience").isJsonObject()) {
            GameruleActionDispatcher.logWarning("Rule " + ruleRef + " has non-object audience value. Falling back to 'all'.");
            return ALL;
        }

        JsonObject audience = obj.getAsJsonObject("audience");
        String type = audience.has("type") && audience.get("type").isJsonPrimitive()
                ? audience.getAsJsonPrimitive("type").getAsString()
                : "all";

        if ("permission_level".equals(type)) {
            int level = audience.has("level") && audience.get("level").isJsonPrimitive()
                    ? audience.getAsJsonPrimitive("level").getAsInt()
                    : 2;
            if (level > 2) {
                GameruleActionDispatcher.logWarning(
                        "Rule " + ruleRef + " requests audience permission_level=" + level
                                + ", but this build safely supports up to 2. This rule will only match level <= 2."
                );
            }
            return new GameruleAudience(type, level, null);
        }
        if ("dimension".equals(type) && audience.has("dimension") && audience.get("dimension").isJsonPrimitive()) {
            String rawDimension = audience.getAsJsonPrimitive("dimension").getAsString();
            try {
                return new GameruleAudience(type, 0, Identifier.parse(rawDimension));
            } catch (RuntimeException ex) {
                GameruleActionDispatcher.logWarning(
                        "Rule " + ruleRef + " has invalid audience dimension '" + rawDimension + "'. Falling back to 'all'."
                );
                return ALL;
            }
        }
        GameruleActionDispatcher.logWarning("Rule " + ruleRef + " has unknown audience type '" + type + "'. Falling back to 'all'.");
        return ALL;
    }

    public boolean matches(ServerPlayer player) {
        if ("permission_level".equals(type)) {
            if (player.level().getServer() == null) {
                return false;
            }
            if (permissionLevel <= 0) {
                return true;
            }
            boolean isOp = player.level().getServer().getPlayerList().isOp(new NameAndId(player.getGameProfile()));
            if (!isOp) {
                return false;
            }
            // Current API exposes op membership directly and profile permission sets via a non-int type.
            // Treat op as permission level >= 2 and reject higher thresholds conservatively.
            return permissionLevel <= 2;
        }
        if ("dimension".equals(type) && dimension != null) {
            return dimension.toString().equals(player.level().dimension().toString());
        }
        return true;
    }
}
