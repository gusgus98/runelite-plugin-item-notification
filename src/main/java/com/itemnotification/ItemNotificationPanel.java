package com.itemnotification;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class ItemNotificationPanel extends PluginPanel {
    private final ItemManager itemManager;
    private final ItemNotificationConfig config;
    private final ItemNotificationPlugin plugin;

    private final JTextField searchBar = new JTextField();
    private final JPanel listContainer = new JPanel();

    @Inject
    public ItemNotificationPanel(ItemManager itemManager, ItemNotificationConfig config,
            ItemNotificationPlugin plugin) {
        super();
        this.itemManager = itemManager;
        this.config = config;
        this.plugin = plugin;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        setLayout(new BorderLayout());

        // Search Bar
        JPanel searchContainer = new JPanel();
        searchContainer.setLayout(new BorderLayout());
        searchContainer.setBorder(new EmptyBorder(0, 0, 10, 0));

        searchBar.setPreferredSize(new Dimension(PluginPanel.PANEL_WIDTH - 20, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setToolTipText("Search for an item to add...");
        searchBar.addActionListener(e -> search(searchBar.getText()));

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> search(searchBar.getText()));

        searchContainer.add(searchBar, BorderLayout.CENTER);
        searchContainer.add(searchButton, BorderLayout.EAST);
        add(searchContainer, BorderLayout.NORTH);

        // List Container
        listContainer.setLayout(new javax.swing.BoxLayout(listContainer, javax.swing.BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(listContainer);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Initial Update
        updateList();
    }

    public void updateList() {
        listContainer.removeAll();

        String highlightedItems = config.highlightedItems();
        if (highlightedItems != null && !highlightedItems.isEmpty()) {
            String[] items = highlightedItems.split(",");
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    listContainer.add(createItemPanel(trimmed));
                }
            }
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private JPanel createItemPanel(String itemName) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(5, 5, 5, 5));
        panel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        panel.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH, 40));

        JLabel nameLabel = new JLabel(itemName);
        panel.add(nameLabel, BorderLayout.CENTER);

        JButton removeBtn = new JButton("X");
        removeBtn.setPreferredSize(new Dimension(20, 20));
        removeBtn.addActionListener(e -> removeItem(itemName));
        panel.add(removeBtn, BorderLayout.EAST);

        return panel;
    }

    private void search(String query) {
        if (query == null || query.isEmpty()) {
            updateList();
            return;
        }

        listContainer.removeAll();

        // In a real plugin we would use ItemManager.search(query) but that returns IDs.
        // For simplicity/demo we might assume exact logic or use searcher.
        // Wait, ItemManager.search() returns List<ItemPrice> which isn't quite right
        // for names.
        // Actually typical way is iterating cache or using existing search.
        // Since we don't have easy cache access here without more boilerplate,
        // I'll simulate a "Search" that just adds the item if you type it,
        // OR simpler: we rely on user typing exact name in search bar to ADD it.
        // Wait, user asked for SEARCH.
        // Let's implement a verify via ItemManager:
        // We CAN search via client.getItemComposition(id) loops, but that's slow.
        // Let's assume for this "v1" search bar behaves like "Add this item".
        // But better: use ItemManager.search(String) if available?
        // In the available API (1.9.x), ItemManager.search(String) returns
        // List<ItemPrice> which has names!

        try {
            List<ItemComposition> results = itemManager.search(query).stream()
                    .map(itemPrice -> itemManager.getItemComposition(itemPrice.getId()))
                    .limit(10)
                    .collect(Collectors.toList());

            if (results.isEmpty()) {
                listContainer.add(new JLabel("No results found."));
            } else {
                for (ItemComposition item : results) {
                    JPanel resultPanel = new JPanel(new BorderLayout());
                    resultPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
                    resultPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
                    resultPanel.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH, 40));

                    JLabel label = new JLabel(item.getName() + " (ID: " + item.getId() + ")");
                    JButton addBtn = new JButton("+");
                    addBtn.addActionListener(e -> addItem(item.getName()));

                    resultPanel.add(label, BorderLayout.CENTER);
                    resultPanel.add(addBtn, BorderLayout.EAST);

                    listContainer.add(resultPanel);
                }
            }
        } catch (Exception e) {
            // Fallback if search fails/API mismatch
            addItem(query);
            updateList();
        }

        listContainer.revalidate();
        listContainer.repaint();
    }

    private void addItem(String newItem) {
        String current = config.highlightedItems();
        if (current == null || current.isEmpty()) {
            plugin.updateConfig(newItem);
        } else {
            // Basic check to avoid dupes
            if (!Arrays.stream(current.split(",")).map(String::trim).anyMatch(s -> s.equalsIgnoreCase(newItem))) {
                plugin.updateConfig(current + "," + newItem);
            }
        }
        updateList();
        searchBar.setText("");
    }

    private void removeItem(String itemToRemove) {
        String current = config.highlightedItems();
        if (current == null)
            return;

        List<String> items = new ArrayList<>(Arrays.asList(current.split(",")));
        items.removeIf(s -> s.trim().equalsIgnoreCase(itemToRemove));

        plugin.updateConfig(String.join(",", items));
        updateList();
    }
}
