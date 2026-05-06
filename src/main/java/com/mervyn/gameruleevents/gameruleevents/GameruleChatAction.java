package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public record GameruleChatAction(JsonObject messageTemplate) {
    public static GameruleChatAction fromJson(JsonObject obj) {
        if (obj == null || !obj.has("message") || !obj.get("message").isJsonObject()) {
            return null;
        }
        return new GameruleChatAction(obj.getAsJsonObject("message").deepCopy());
    }

    public void send(ServerPlayer player, GameruleMatchContext context) {
        Component message = PlaceholderUtil.componentFromTemplate(messageTemplate, context, player);
        if (message != null) {
            player.sendSystemMessage(message);
        }
    }
}
