package com.itemnotification;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("itemnotification")
public interface ItemNotificationConfig extends Config {
	@ConfigItem(keyName = "highlightedItems", name = "Highlighted Items", description = "A list of items to notify on drop, comma separated")
	default String highlightedItems() {
		return "";
	}

	@ConfigItem(keyName = "soundType", name = "Notification Sound", description = "The sound to play when a highlighted item is spawned")
	default SoundType soundType() {
		return SoundType.BOOP;
	}

	@net.runelite.client.config.Range(min = 0, max = 200)
	@ConfigItem(keyName = "soundVolume", name = "Sound Volume", description = "Volume of the custom sound (0-200%)")
	@net.runelite.client.config.Units(net.runelite.client.config.Units.PERCENT)
	default int soundVolume() {
		return 100;
	}
}
