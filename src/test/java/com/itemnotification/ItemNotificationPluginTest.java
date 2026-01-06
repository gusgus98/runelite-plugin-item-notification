package com.itemnotification;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class ItemNotificationPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(ItemNotificationPlugin.class);
        RuneLite.main(args);
    }
}
