# Gamerule Events

Gamerule Events is a Minecraft mod for NeoForge that allows you to trigger custom actions when game rules change. Actions are defined via data packs.

## Features

- **Trigger Actions on GameRule Change**: Execute titles, subtitles, and sounds when a specific game rule is modified.
- **Data Pack Driven**: Define your own rules and actions in `data/<namespace>/gamerule_events/` JSON files.
- **Multiple Actions**: Supports setting title text, subtitles, animations, and playing sounds.

## Example Configuration

Create a file at `data/mymod/gamerule_events/my_rules.json`:

```json
{
  "rules": [
    {
      "gamerule": "doDaylightCycle",
      "when": {
        "equals": "false"
      },
      "title": {
        "title": {"text": "Daylight Cycle Stopped", "color": "red"},
        "stay": 60
      },
      "sounds": [
        {
          "sound": "minecraft:block.note_block.pling",
          "volume": 1.0,
          "pitch": 0.5
        }
      ]
    }
  ]
}
```

## Installation

Gamerule Events requires NeoForge 26.1 (Minecraft 1.21.2+) or later.

## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details.
