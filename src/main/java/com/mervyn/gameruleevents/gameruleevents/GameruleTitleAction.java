package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;

public record GameruleTitleAction(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
    public static GameruleTitleAction fromJson(JsonObject obj) {
        if (obj == null) {
            return null;
        }

        Component title = null;
        Component subtitle = null;

        if (obj.has("title") && obj.get("title").isJsonObject()) {
            title = ComponentSerialization.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, obj.getAsJsonObject("title")).result().orElse(null);
        }

        if (obj.has("subtitle") && obj.get("subtitle").isJsonObject()) {
            subtitle = ComponentSerialization.CODEC.parse(com.mojang.serialization.JsonOps.INSTANCE, obj.getAsJsonObject("subtitle")).result().orElse(null);
        }

        if (title == null && subtitle == null) {
            return null;
        }

        int fadeIn = obj.has("fadeIn") ? obj.get("fadeIn").getAsInt() : 10;
        int stay = obj.has("stay") ? obj.get("stay").getAsInt() : 40;
        int fadeOut = obj.has("fadeOut") ? obj.get("fadeOut").getAsInt() : 10;

        return new GameruleTitleAction(title, subtitle, fadeIn, stay, fadeOut);
    }

    public void send(ServerPlayer player) {
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

