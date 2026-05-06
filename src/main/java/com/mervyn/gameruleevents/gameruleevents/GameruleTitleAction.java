package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public record GameruleTitleAction(JsonObject titleTemplate, JsonObject subtitleTemplate, int fadeIn, int stay, int fadeOut) {
    public static GameruleTitleAction fromJson(JsonObject obj) {
        if (obj == null) {
            return null;
        }

        JsonObject title = null;
        JsonObject subtitle = null;

        if (obj.has("title") && obj.get("title").isJsonObject()) {
            title = obj.getAsJsonObject("title").deepCopy();
        }

        if (obj.has("subtitle") && obj.get("subtitle").isJsonObject()) {
            subtitle = obj.getAsJsonObject("subtitle").deepCopy();
        }

        if (title == null && subtitle == null) {
            return null;
        }

        int fadeIn = obj.has("fadeIn") ? obj.get("fadeIn").getAsInt() : 10;
        int stay = obj.has("stay") ? obj.get("stay").getAsInt() : 40;
        int fadeOut = obj.has("fadeOut") ? obj.get("fadeOut").getAsInt() : 10;

        return new GameruleTitleAction(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void send(ServerPlayer player, GameruleMatchContext context) {
        Component title = PlaceholderUtil.componentFromTemplate(titleTemplate, context, player);
        Component subtitle = PlaceholderUtil.componentFromTemplate(subtitleTemplate, context, player);
        if (title != null || subtitle != null) {
            player.connection.send(
                    new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(title != null ? title : Component.empty())
            );
            if (subtitle != null) {
                player.connection.send(
                        new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle)
                );
            }
            player.connection.send(
                    new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut)
            );
        }
    }
}

