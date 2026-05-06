package com.mervyn.gameruleevents.gameruleevents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

public final class GameruleWhenParser {
    private GameruleWhenParser() {
    }

    public static GameruleWhenPredicate parse(JsonObject when) {
        if (when == null) {
            return GameruleWhenPredicate.ANY;
        }

        List<GameruleWhenPredicate> checks = new ArrayList<>();
        boolean any = when.has("any") && when.get("any").isJsonPrimitive() && when.getAsJsonPrimitive("any").getAsBoolean();

        if (when.has("equals")) {
            JsonPrimitive primitive = when.getAsJsonPrimitive("equals");
            if (primitive != null) {
                String expected = primitive.getAsString();
                checks.add(context -> expected.equals(context.newValue()));
            }
        }
        if (when.has("not_equals")) {
            JsonPrimitive primitive = when.getAsJsonPrimitive("not_equals");
            if (primitive != null) {
                String expected = primitive.getAsString();
                checks.add(context -> !expected.equals(context.newValue()));
            }
        }
        if (when.has("from_equals")) {
            JsonPrimitive primitive = when.getAsJsonPrimitive("from_equals");
            if (primitive != null) {
                String expected = primitive.getAsString();
                checks.add(context -> context.oldValue() != null && expected.equals(context.oldValue()));
            }
        }
        if (when.has("to_equals")) {
            JsonPrimitive primitive = when.getAsJsonPrimitive("to_equals");
            if (primitive != null) {
                String expected = primitive.getAsString();
                checks.add(context -> expected.equals(context.newValue()));
            }
        }
        if (when.has("changed") && when.get("changed").isJsonPrimitive()) {
            boolean mustChange = when.getAsJsonPrimitive("changed").getAsBoolean();
            checks.add(context -> {
                boolean changed = context.oldValue() != null && !context.oldValue().equals(context.newValue());
                return mustChange == changed;
            });
        }

        Set<String> allowedSet = parseStringSet(when.get("in"));
        if (allowedSet != null) {
            checks.add(context -> allowedSet.contains(context.newValue()));
        }
        Set<String> oneOfSet = parseStringSet(when.get("one_of"));
        if (oneOfSet != null) {
            checks.add(context -> oneOfSet.contains(context.newValue()));
        }

        Pattern regex = parsePattern(when.get("matches"));
        if (regex != null) {
            checks.add(context -> regex.matcher(context.newValue()).matches());
        }

        addNumericCheck(checks, when, "gt", (actual, target) -> actual > target);
        addNumericCheck(checks, when, "gte", (actual, target) -> actual >= target);
        addNumericCheck(checks, when, "lt", (actual, target) -> actual < target);
        addNumericCheck(checks, when, "lte", (actual, target) -> actual <= target);

        if (checks.isEmpty()) {
            return any ? GameruleWhenPredicate.ANY : context -> false;
        }

        return context -> {
            for (GameruleWhenPredicate check : checks) {
                if (!check.matches(context)) {
                    return false;
                }
            }
            return true;
        };
    }

    private static Set<String> parseStringSet(JsonElement element) {
        if (element == null || !element.isJsonArray()) {
            return null;
        }
        JsonArray array = element.getAsJsonArray();
        Set<String> values = new HashSet<>();
        for (JsonElement value : array) {
            if (!value.isJsonPrimitive()) {
                continue;
            }
            values.add(value.getAsString());
        }
        return values;
    }

    private static Pattern parsePattern(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) {
            return null;
        }
        try {
            return Pattern.compile(element.getAsString());
        } catch (PatternSyntaxException e) {
            GameruleActionDispatcher.logError("Invalid regex in when.matches: " + e.getMessage());
            return null;
        }
    }

    private static void addNumericCheck(
            List<GameruleWhenPredicate> checks,
            JsonObject when,
            String key,
            NumericComparison comparison
    ) {
        if (!when.has(key) || !when.get(key).isJsonPrimitive()) {
            return;
        }
        double target = when.getAsJsonPrimitive(key).getAsDouble();
        checks.add(context -> {
            try {
                double actual = Double.parseDouble(context.newValue());
                return comparison.compare(actual, target);
            } catch (NumberFormatException e) {
                return false;
            }
        });
    }

    @FunctionalInterface
    private interface NumericComparison {
        boolean compare(double actual, double target);
    }
}
