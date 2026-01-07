# Ground Item Notification

A RuneLite plugin that notifies you when specific items of your choice are dropped or spawned on the ground.

## Features
- **Watchlist**: Easily add items to your watchlist using the side panel or by typing them manually in the config.
- **Instant Search**: Type in the plugin panel to find items or add new ones to your list instantly.
- **Sound Notifications**: Plays a unique, distinctive jingle when items are detected ("Unique Jingle").
- **Volume Control**: Adjust notification volume independently of game sounds (up to 200%).
- **Ironman Mode**: Toggle "Only My Drops" to filter notifications to items from NPCs you killed — perfect for Ironman accounts!

## Setup
1. **Enable** the plugin in RuneLite (search for "Ground Item Notification").
2. **Open the Side Panel** (Red Ruby Icon).
3. **Add Items**:
    - Type an item name (e.g., "Dragon bones") in the search bar.
    - Click **"Add 'Dragon bones'"** to track it.
4. **Configure**:
    - Open the Plugin Configuration (Cog icon).
    - Select your preferred **Notification Sound**.
    - Adjust **Sound Volume** if needed.
    - **Ironman?** Enable **"Only My Drops"** to only get notified for your own kills.

## Configuration
| Setting | Description |
| :--- | :--- |
| **Only My Drops** | When enabled, only notifies for items dropped by NPCs you killed. Ideal for Ironman accounts who can only pick up their own loot. |
| **Highlighted Items** | A comma-separated list of item names to track (e.g., `Bones, Coins, Twisted bow`). |
| **Notification Sound** | The sound to play when a tracked item appears. |
| **Sound Volume** | Volume for custom sounds (0-200%). |

## Troubleshooting
- **No Sound?** Ensure your "Sound Volume" is not 0% and that you have valid items in your list.
- **Spelling**: Item names must match the game name exactly (case insensitive).
- **"Only My Drops" not working?** This feature relies on RuneLite's loot tracking. Make sure you're not in a situation where loot ownership is ambiguous (e.g., some group boss scenarios).

## License
BSD 2-Clause License
