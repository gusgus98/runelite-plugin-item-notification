package com.itemnotification;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name = "Ground Item Notification")
public class ItemNotificationPlugin extends Plugin {

	@Inject
	private ItemNotificationConfig config;

	@Inject
	private ItemManager itemManager;

	@Inject
	private ConfigManager configManager;

	public void updateConfig(String newItems) {
		configManager.setConfiguration("itemnotification", "highlightedItems", newItems);
	}

	@Inject
	private net.runelite.client.audio.AudioPlayer audioPlayer;

	@Inject
	private net.runelite.client.ui.ClientToolbar clientToolbar;

	private ItemNotificationPanel panel;
	private net.runelite.client.ui.NavigationButton navButton;

	@Override
	protected void startUp() throws Exception {
		panel = new ItemNotificationPanel(config, this);

		// Use a standard icon (using a placeholder buffered image properly would need
		// ImageUtil but let's use a dummy runnable or load one)
		// For simplicity, we'll try to load the Ruby Item ID image.
		@SuppressWarnings("deprecation")
		final java.awt.image.BufferedImage icon = itemManager.getImage(net.runelite.api.ItemID.RUBY);

		navButton = net.runelite.client.ui.NavigationButton.builder()
				.tooltip("Item Notifications")
				.icon(icon)
				.priority(5)
				.panel(panel)
				.build();

		clientToolbar.addNavigation(navButton);

		log.info("Item Notification started!");
	}

	@Override
	protected void shutDown() throws Exception {
		clientToolbar.removeNavigation(navButton);
		log.info("Item Notification stopped!");
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned itemSpawned) {
		final int itemId = itemSpawned.getItem().getId();
		final ItemComposition itemComposition = itemManager.getItemComposition(itemId);
		final String itemName = itemComposition.getName();

		if (isItemHighlighted(itemName, config.highlightedItems())) {
			log.info("Highlighted item spawned: {}", itemName);
			playSound(config.soundType());
		}
	}

	// Visible for testing
	boolean isItemHighlighted(String itemName, String highlightedItems) {
		if (highlightedItems == null || highlightedItems.isEmpty()) {
			return false;
		}

		List<String> itemsToNotify = Arrays.stream(highlightedItems.split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.collect(Collectors.toList());

		return itemsToNotify.contains(itemName.toLowerCase());
	}

	private void playSound(SoundType soundType) {
		if (soundType.getResourcePath() != null) {
			// Convert percentage to decibels
			// 100% = 0dB, 200% = ~6dB, 50% = ~-6dB
			float volume = config.soundVolume();
			// Prevent log(0) which is -infinity
			if (volume <= 0) {
				volume = 0.0001f;
			}
			float dB = (float) (20.0 * Math.log10(volume / 100.0));

			// Using RuneLite's AudioPlayer to avoid javax.sound usage
			try {
				audioPlayer.play(getClass(), soundType.getResourcePath(), dB);
			} catch (Exception e) {
				log.warn("Failed to play custom sound", e);
			}
		}
	}

	@Subscribe
	public void onConfigChanged(net.runelite.client.events.ConfigChanged event) {
		if (event.getGroup().equals("itemnotification")) {
			// Update the panel if the highlighted items config changes
			if (event.getKey().equals("highlightedItems")) {
				panel.updateList();
			}
		}
	}

	@Provides
	ItemNotificationConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(ItemNotificationConfig.class);
	}
}
