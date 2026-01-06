package com.itemnotification;

import com.google.inject.Provides;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.ItemComposition;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

@Slf4j
@PluginDescriptor(name = "Item Notification")
public class ItemNotificationPlugin extends Plugin {
	@Inject
	private Client client;

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
	private net.runelite.client.ui.ClientToolbar clientToolbar;

	private ItemNotificationPanel panel;
	private net.runelite.client.ui.NavigationButton navButton;

	@Override
	protected void startUp() throws Exception {
		panel = new ItemNotificationPanel(itemManager, config, this);

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

		final String highlightedItems = config.highlightedItems();
		if (highlightedItems == null || highlightedItems.isEmpty()) {
			return;
		}

		// Split by comma and trim
		List<String> itemsToNotify = Arrays.stream(highlightedItems.split(","))
				.map(String::trim)
				.map(String::toLowerCase)
				.collect(Collectors.toList());

		if (itemsToNotify.contains(itemName.toLowerCase())) {
			log.info("Highlighted item spawned: {}", itemName);
			playSound(config.soundType());
		}
	}

	private void playSound(SoundType soundType) {
		if (soundType.getResourcePath() != null) {
			try (java.io.InputStream s = getClass().getResourceAsStream(soundType.getResourcePath())) {
				if (s == null) {
					log.warn("Sound file not found: {}", soundType.getResourcePath());
					return;
				}
				try (java.io.InputStream bufferedIn = new java.io.BufferedInputStream(s);
						javax.sound.sampled.AudioInputStream audioStream = javax.sound.sampled.AudioSystem
								.getAudioInputStream(bufferedIn)) {
					javax.sound.sampled.Clip clip = javax.sound.sampled.AudioSystem.getClip();
					clip.open(audioStream);

					// Apply volume if control is supported
					if (clip.isControlSupported(javax.sound.sampled.FloatControl.Type.MASTER_GAIN)) {
						javax.sound.sampled.FloatControl gainControl = (javax.sound.sampled.FloatControl) clip
								.getControl(javax.sound.sampled.FloatControl.Type.MASTER_GAIN);

						// Convert percentage to decibels
						// 100% = 0dB, 200% = ~6dB, 50% = ~-6dB
						float volume = config.soundVolume();
						// Prevent log(0) which is -infinity
						if (volume <= 0) {
							volume = 0.0001f;
						}
						float dB = (float) (20.0 * Math.log10(volume / 100.0));

						// Clamp to valid range
						dB = Math.min(dB, gainControl.getMaximum());
						dB = Math.max(dB, gainControl.getMinimum());

						gainControl.setValue(dB);
					}

					clip.start();
				}
			} catch (Exception e) {
				log.warn("Failed to play custom sound", e);
			}
		} else {
			client.playSoundEffect(soundType.getValidSoundId());
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
