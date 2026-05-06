package com.mervyn.gameruleevents.gameruleevents;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public record GameruleRuleActions(
        List<GameruleTitleAction> titleActions,
        List<GameruleSoundAction> soundActions,
        List<GameruleChatAction> chatActions,
        List<GameruleActionbarAction> actionbarActions,
        List<GameruleCommandAction> commandActions
) {
    public static GameruleRuleActions fromJson(JsonObject obj, String ruleRef) {
        if (obj == null) {
            return null;
        }

        List<GameruleTitleAction> titles = new ArrayList<>();
        List<GameruleSoundAction> sounds = new ArrayList<>();
        List<GameruleChatAction> chats = new ArrayList<>();
        List<GameruleActionbarAction> actionbars = new ArrayList<>();
        List<GameruleCommandAction> commands = new ArrayList<>();

        if (obj.has("actions") && obj.get("actions").isJsonArray()) {
            JsonArray array = obj.getAsJsonArray("actions");
            for (JsonElement element : array) {
                if (!element.isJsonObject()) {
                    GameruleActionDispatcher.logWarning("Rule " + ruleRef + " has non-object action entry. Skipping it.");
                    continue;
                }
                JsonObject action = element.getAsJsonObject();
                String type = action.has("type") && action.get("type").isJsonPrimitive()
                        ? action.getAsJsonPrimitive("type").getAsString()
                        : "";

                switch (type) {
                    case "broadcast_title" -> {
                        GameruleTitleAction title = GameruleTitleAction.fromJson(action);
                        if (title != null) {
                            titles.add(title);
                        }
                    }
                    case "broadcast_sound" -> {
                        GameruleSoundAction sound = GameruleSoundAction.fromJson(action);
                        if (sound != null) {
                            sounds.add(sound);
                        }
                    }
                    case "broadcast_chat" -> {
                        GameruleChatAction chat = GameruleChatAction.fromJson(action);
                        if (chat != null) {
                            chats.add(chat);
                        }
                    }
                    case "broadcast_actionbar" -> {
                        GameruleActionbarAction actionbar = GameruleActionbarAction.fromJson(action);
                        if (actionbar != null) {
                            actionbars.add(actionbar);
                        }
                    }
                    case "run_command" -> {
                        GameruleCommandAction command = GameruleCommandAction.fromJson(action);
                        if (command != null) {
                            commands.add(command);
                        }
                    }
                    default -> {
                        GameruleActionDispatcher.logWarning("Rule " + ruleRef + " has unknown action type '" + type + "'. Skipping it.");
                    }
                }
            }
        } else {
            GameruleActionDispatcher.logWarning("Rule " + ruleRef + " is missing 'actions' array. Skipping it.");
        }

        if (titles.isEmpty() && sounds.isEmpty() && chats.isEmpty() && actionbars.isEmpty() && commands.isEmpty()) {
            return null;
        }

        return new GameruleRuleActions(
                List.copyOf(titles),
                List.copyOf(sounds),
                List.copyOf(chats),
                List.copyOf(actionbars),
                List.copyOf(commands)
        );
    }

    public boolean isEmpty() {
        return titleActions.isEmpty()
                && soundActions.isEmpty()
                && chatActions.isEmpty()
                && actionbarActions.isEmpty()
                && commandActions.isEmpty();
    }
}

