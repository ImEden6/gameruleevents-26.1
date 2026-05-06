package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public record GameruleActionbarAction(JsonObject messageTemplate) {
    public static GameruleActionbarAction fromJson(JsonObject obj) {
        if (obj == null || !obj.has("message") || !obj.get("message").isJsonObject()) {
            return null;
        }
        return new GameruleActionbarAction(obj.getAsJsonObject("message").deepCopy());
    }

    public void send(ServerPlayer player, GameruleMatchContext context) {
        Component message = PlaceholderUtil.componentFromTemplate(messageTemplate, context, player);
        if (message == null) {
            return;
        }
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket(message));
    }
}
