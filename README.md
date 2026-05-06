# Gamerule Events

Gamerule Events is a Minecraft mod for NeoForge that allows you to trigger custom actions when game rules change. Actions are defined via data packs.

## Features

- **Trigger Actions on GameRule Change**: Execute titles, subtitles, and sounds when a specific game rule is modified.
- **Data Pack Driven**: Define your own rules and actions in `data/<namespace>/gamerule_events/` JSON files.
- **Multiple Actions**: Supports setting title/subtitle (with fade timings) and playing sounds.

## Example Configuration

Create a file at `data/<namespace>/gamerule_events/<name>.json`:

```json
{
  "rules": [
    {
      "gamerule": "minecraft:doDaylightCycle",
      "when": {
        "equals": "false"
      },
      "actions": [
        {
          "type": "broadcast_title",
          "title": { "text": "Daylight Cycle Stopped", "color": "red" },
          "subtitle": { "text": "Gamerule change detected", "color": "yellow" },
          "fadeIn": 10,
          "stay": 60,
          "fadeOut": 10
        },
        {
          "type": "broadcast_sound",
          "sound": "minecraft:block.note_block.pling",
          "volume": 1.0,
          "pitch": 1.0
        }
      ]
    }
  ]
}
```

Notes:
- `gamerule` must match the in-game gamerule identifier (usually namespaced like `minecraft:doDaylightCycle`).
- `when.equals` is compared to the serialized new gamerule value; booleans should be `"true"` / `"false"`.
- After editing JSON, run `/reload` to apply changes.

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

## Installation

Gamerule Events targets NeoForge **26.1** (Minecraft **26.1**) or later.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.
