package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public record GameruleCommandAction(String command, String as) {
    public static GameruleCommandAction fromJson(JsonObject obj) {
        if (obj == null || !obj.has("command") || !obj.get("command").isJsonPrimitive()) {
            return null;
        }
        String as = obj.has("as") && obj.get("as").isJsonPrimitive() ? obj.getAsJsonPrimitive("as").getAsString() : "server";
        return new GameruleCommandAction(obj.getAsJsonPrimitive("command").getAsString(), as);
    }

    public void executeServer(MinecraftServer server, GameruleMatchContext context) {
        String resolved = PlaceholderUtil.replace(command, context, null);
        CommandSourceStack source = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, resolved);
    }

    public void executeAsPlayer(ServerPlayer player, GameruleMatchContext context) {
        String resolved = PlaceholderUtil.replace(command, context, player);
        CommandSourceStack source = player.createCommandSourceStack();
        if (player.level().getServer() != null) {
            player.level().getServer().getCommands().performPrefixedCommand(source, resolved);
        }
    }

    public boolean runsAsPlayer() {
        return "player".equalsIgnoreCase(as);
    }
}
