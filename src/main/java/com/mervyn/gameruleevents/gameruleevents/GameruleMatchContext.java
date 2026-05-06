package com.mervyn.gameruleevents.gameruleevents;

import net.minecraft.server.MinecraftServer;

public record GameruleMatchContext(
        MinecraftServer server,
        String gameruleId,
        String oldValue,
        String newValue
) {
}
