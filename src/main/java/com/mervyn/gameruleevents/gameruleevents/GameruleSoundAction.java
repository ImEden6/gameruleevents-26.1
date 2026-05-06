package com.mervyn.gameruleevents.gameruleevents;

import com.google.gson.JsonObject;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.Level;

public record GameruleSoundAction(Identifier soundId, float volume, float pitch) {
    public static GameruleSoundAction fromJson(JsonObject obj) {
        if (obj == null) {
            return null;
        }

        String sound = obj.has("sound") ? obj.get("sound").getAsString() : "";
        if (sound.isEmpty()) {
            return null;
        }

        float volume = obj.has("volume") ? obj.get("volume").getAsFloat() : 1.0F;
        float pitch = obj.has("pitch") ? obj.get("pitch").getAsFloat() : 1.0F;

        return new GameruleSoundAction(Identifier.parse(sound), volume, pitch);
    }

    public void play(ServerPlayer player) {
        Level level = player.level();
        SoundEvent sound = level.registryAccess()
                .lookupOrThrow(net.minecraft.core.registries.Registries.SOUND_EVENT)
                .get(soundId).map(net.minecraft.core.Holder::value).orElse(null);
        if (sound == null) {
            sound = SoundEvents.NOTE_BLOCK_PLING.value();
        }
        player.playSound(sound, volume, pitch);
    }
}

