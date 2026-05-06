# Gamerule Events

Gamerule Events is a Minecraft mod for NeoForge that allows you to trigger custom actions when game rules change. Actions are defined via data packs.

## Features

- **Trigger Actions on GameRule Change**: Execute actions when a specific game rule is modified.
- **Data Pack Driven**: Define your own rules and actions in `data/<namespace>/gamerule_events/` JSON files.
- **Richer Conditions**: `equals`, `not_equals`, `in`, `matches`, `gt/gte/lt/lte`, `from_equals`, `to_equals`, and `changed`.
- **Rule Controls**: `priority`, `cooldown_ticks`, `stop_after`, `id`.
- **Audience Filters**: Broadcast to all, ops/permission-level, or players in a specific dimension.
- **Multiple Actions**: `broadcast_title`, `broadcast_sound`, `broadcast_chat`, `broadcast_actionbar`, and optional `run_command`.

## Example Configuration

Create a file at `data/<namespace>/gamerule_events/<name>.json`:

```json
{
  "rules": [
    {
      "gamerule": "minecraft:doDaylightCycle",
      "id": "daylight-off-notify",
      "priority": 100,
      "cooldown_ticks": 40,
      "stop_after": true,
      "audience": "all",
      "when": {
        "equals": "false",
        "changed": true
      },
      "actions": [
        {
          "type": "broadcast_title",
          "title": { "text": "Daylight Cycle Stopped", "color": "red" },
          "subtitle": { "text": "Gamerule change detected: {old} -> {new}", "color": "yellow" },
          "fadeIn": 10,
          "stay": 60,
          "fadeOut": 10
        },
        {
          "type": "broadcast_sound",
          "sound": "minecraft:block.note_block.pling",
          "volume": 1.0,
          "pitch": 1.0
        },
        {
          "type": "broadcast_chat",
          "message": { "text": "[GameruleEvents] {gamerule} -> {new}" }
        },
        {
          "type": "broadcast_actionbar",
          "message": { "text": "Rule updated: {gamerule}" }
        },
        {
          "type": "run_command",
          "as": "server",
          "command": "say gamerule {gamerule} is now {new}"
        }
      ]
    }
  ]
}
```

Notes:
- `gamerule` must match the in-game gamerule identifier (usually namespaced like `minecraft:doDaylightCycle`).
- `when` compares against the serialized new value; booleans should be `"true"` / `"false"`.
- Placeholders currently supported in string values: `{gamerule}`, `{old}`, `{new}`, `{player}`.
- `run_command` is disabled by default. Enable it in config with `allowCommandActions=true`.
- After editing JSON, run `/reload` to apply changes.
- Use `/gameruleevents list` (op level 2) to inspect loaded rule groups.

## Supported Actions

The datapack schema uses an `actions[]` array. Supported action `type`s:

- `broadcast_title`
  - `title` (Component JSON) optional
  - `subtitle` (Component JSON) optional
  - `fadeIn` (int, default `10`)
  - `stay` (int, default `40`)
  - `fadeOut` (int, default `10`)

- `broadcast_sound`
  - `sound` (string, resource location like `minecraft:block.note_block.pling`)
  - `volume` (float, default `1.0`)
  - `pitch` (float, default `1.0`)

- `broadcast_chat`
  - `message` (Component JSON) required

- `broadcast_actionbar`
  - `message` (Component JSON) required

- `run_command`
  - `command` (string) required
  - `as` (`"server"` or `"player"`, default `"server"`)

## Conditions

- `when.any` (boolean)
- `when.equals` / `when.not_equals` (string)
- `when.in` / `when.one_of` (array of strings)
- `when.matches` (regex)
- `when.gt`, `when.gte`, `when.lt`, `when.lte` (numeric compare)
- `when.from_equals`, `when.to_equals` (string transition checks)
- `when.changed` (boolean, compares old/new known values)

## Rule-Level Optional Fields

- `id` (string, used for cooldown state key; defaults to gamerule id)
- `priority` (int, higher runs first)
- `cooldown_ticks` (int)
- `stop_after` (boolean)
- `audience`:
  - `"all"` (default) or `"ops"`
  - or object, e.g. `{ "type": "permission_level", "level": 2 }`
  - or object, e.g. `{ "type": "dimension", "dimension": "minecraft:overworld" }`

## Installation

Gamerule Events targets NeoForge **26.1** (Minecraft **26.1**) or later.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.
