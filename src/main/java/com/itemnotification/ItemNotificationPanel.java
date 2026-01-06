package com.itemnotification;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject; // Keep Inject
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class ItemNotificationPanel extends PluginPanel {
    private final ItemNotificationConfig config;
    private final ItemNotificationPlugin plugin;

    private final JTextField searchBar = new JTextField();
    private final JPanel listContainer = new JPanel();

    @Inject
    public ItemNotificationPanel(ItemNotificationConfig config, ItemNotificationPlugin plugin) {
        super();
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
        // Live search/filter
        searchBar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                search(searchBar.getText());
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                search(searchBar.getText());
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                search(searchBar.getText());
            }
        });
        searchBar.addActionListener(e -> addItem(searchBar.getText())); // Enter adds item

        JButton addButton = new JButton("Add");
        addButton.addActionListener(e -> addItem(searchBar.getText()));

        searchContainer.add(searchBar, BorderLayout.CENTER);
        searchContainer.add(addButton, BorderLayout.EAST);
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
        listContainer.removeAll();

        // 1. Show "Add New" button if we have text
        if (query != null && !query.isEmpty()) {
            JPanel addPanel = new JPanel(new BorderLayout());
            addPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
            addPanel.setBorder(new EmptyBorder(5, 5, 10, 5));
            addPanel.setMaximumSize(new Dimension(PluginPanel.PANEL_WIDTH, 40));

            JButton addBtn = new JButton("Add '" + query + "'");
            addBtn.addActionListener(e -> addItem(query));
            addPanel.add(addBtn, BorderLayout.CENTER);

            listContainer.add(addPanel);
        }

        // 2. Filter existing items
        String highlightedItems = config.highlightedItems();
        if (highlightedItems != null && !highlightedItems.isEmpty()) {
            String[] items = highlightedItems.split(",");

            for (String item : items) {
                String trimmed = item.trim();
                if (trimmed.isEmpty())
                    continue;

                // If query is empty, show all. If not, fuzzy match.
                if (query == null || query.isEmpty() || trimmed.toLowerCase().contains(query.toLowerCase())) {
                    listContainer.add(createItemPanel(trimmed));
                }
            }
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
