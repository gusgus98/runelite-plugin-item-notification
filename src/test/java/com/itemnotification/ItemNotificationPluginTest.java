package com.itemnotification;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class ItemNotificationPluginTest
{
	private final ItemNotificationPlugin plugin = new ItemNotificationPlugin();

	@Test
	public void testItemMatching()
	{
		// Basic match
		assertTrue(plugin.isItemHighlighted("Bones", "Bones"));
		
		// Case insensitive
		assertTrue(plugin.isItemHighlighted("bones", "Bones"));
		assertTrue(plugin.isItemHighlighted("Bones", "bones"));

		// List matching
		assertTrue(plugin.isItemHighlighted("Ruby", "Bones, Ruby, Coin"));
		
		// Whitespace handling
		assertTrue(plugin.isItemHighlighted("Ruby", "Bones ,  Ruby  , Coin"));

		// Partial match should FAIL (we want exact matches only)
		assertFalse(plugin.isItemHighlighted("Ruby ring", "Ruby"));
		
		// Empty config
		assertFalse(plugin.isItemHighlighted("Anything", ""));
		assertFalse(plugin.isItemHighlighted("Anything", null));
	}
}
