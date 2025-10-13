package org.example.dndfactionsimulator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.dndfactionsimulator.database.*;
import org.example.dndfactionsimulator.ui.*;

public class Main extends Application {
    private DatabaseManager db;
    private FactionOverviewPanel factionPanel;
    private EventLogPanel eventLogPanel;
    private Label turnLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize database
        db = DatabaseManager.getInstance();

        // Create main layout
        BorderPane root = new BorderPane();

        // Top bar with title and turn counter
        VBox topBar = new VBox(5);
        topBar.setStyle("-fx-padding: 10; -fx-background-color: #2c3e50;");

        Label titleLabel = new Label("🎲 D&D Faction Simulator");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox turnBox = new HBox(15);
        turnBox.setStyle("-fx-alignment: center-left;");

        turnLabel = new Label("Turn: " + db.getCurrentTurn());
        turnLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #ecf0f1;");

        Button advanceTurnBtn = new Button("⏭ Advance Turn");
        advanceTurnBtn.setStyle("-fx-font-size: 14px;");
        advanceTurnBtn.setOnAction(e -> advanceTurn());

        turnBox.getChildren().addAll(turnLabel, advanceTurnBtn);
        topBar.getChildren().addAll(titleLabel, turnBox);

        root.setTop(topBar);

        // Create tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Dashboard Tab (simple stats for now)
        Tab dashboardTab = new Tab("📊 Dashboard");
        dashboardTab.setContent(createDashboard());

        // Faction Management Tab
        Tab factionsTab = new Tab("👥 Factions");
        factionPanel = new FactionOverviewPanel(db);
        factionsTab.setContent(factionPanel);

        // Event Log Tab
        Tab eventsTab = new Tab("📜 Event Log");
        eventLogPanel = new EventLogPanel(db);
        eventsTab.setContent(eventLogPanel);

        // Testing Tab (keep old testing features)
        Tab testingTab = new Tab("🧪 Testing");
        testingTab.setContent(createTestingPanel());

        tabPane.getTabs().addAll(dashboardTab, factionsTab, eventsTab, testingTab);

        root.setCenter(tabPane);

        // Menu bar
        MenuBar menuBar = createMenuBar();
        VBox topContainer = new VBox(menuBar, topBar);
        root.setTop(topContainer);

        // Create and show scene
        Scene scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("D&D Faction Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("✅ Application started successfully!");
    }

    private VBox createDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("📊 World Statistics");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextArea statsArea = new TextArea();
        statsArea.setEditable(false);
        statsArea.setPrefHeight(500);
        statsArea.setWrapText(true);

        Button refreshBtn = new Button("🔄 Refresh Stats");
        refreshBtn.setOnAction(e -> {
            statsArea.clear();
            statsArea.appendText("=== WORLD OVERVIEW ===\n\n");

            statsArea.appendText(String.format("Current Turn: %d\n\n", db.getCurrentTurn()));

            var factions = db.getAllFactions();
            var activeFactions = db.getActiveFactions();
            var relationships = db.getAllRelationships();
            var events = db.getAllEvents();

            statsArea.appendText(String.format("Total Factions: %d\n", factions.size()));
            statsArea.appendText(String.format("Active Factions: %d\n", activeFactions.size()));
            statsArea.appendText(String.format("Total Relationships: %d\n", relationships.size()));
            statsArea.appendText(String.format("Total Events Logged: %d\n\n", events.size()));

            if (!activeFactions.isEmpty()) {
                statsArea.appendText("=== FACTION STRENGTH RANKINGS ===\n\n");
                activeFactions.stream()
                        .sorted((f1, f2) -> Integer.compare(f2.getStrength(), f1.getStrength()))
                        .forEach(f -> statsArea.appendText(String.format("%d. %s - Strength: %d\n",
                                activeFactions.indexOf(f) + 1, f.getName(), f.getStrength())));
            }

            statsArea.appendText("\n=== RELATIONSHIP BREAKDOWN ===\n\n");
            for (var type : org.example.dndfactionsimulator.model.RelationshipType.values()) {
                long count = relationships.stream()
                        .filter(r -> r.getType() == type)
                        .count();
                statsArea.appendText(String.format("%s: %d\n", type.getDisplayName(), count));
            }
        });

        // Auto-refresh on load
        refreshBtn.fire();

        dashboard.getChildren().addAll(header, refreshBtn, statsArea);
        return dashboard;
    }

    private VBox createTestingPanel() {
        VBox testPanel = new VBox(15);
        testPanel.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("🧪 Quick Testing Tools");
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(10);
        buttonBox.setStyle("-fx-alignment: center;");

        Button addRandomFactionBtn = new Button("Add Random Faction");
        Button addRandomRelationshipBtn = new Button("Add Random Relationship");
        Button logRandomEventBtn = new Button("Log Random Event");

        buttonBox.getChildren().addAll(addRandomFactionBtn, addRandomRelationshipBtn, logRandomEventBtn);

        TextArea outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(500);
        outputArea.setWrapText(true);

        addRandomFactionBtn.setOnAction(e -> {
            var faction = new org.example.dndfactionsimulator.model.Faction(
                    "Faction " + System.currentTimeMillis() % 1000,
                    org.example.dndfactionsimulator.model.FactionType.values()[
                            (int)(Math.random() * org.example.dndfactionsimulator.model.FactionType.values().length)],
                    org.example.dndfactionsimulator.model.Alignment.values()[
                            (int)(Math.random() * org.example.dndfactionsimulator.model.Alignment.values().length)]
            );

            if (db.addFaction(faction)) {
                outputArea.appendText("✅ Added: " + faction + "\n\n");
                factionPanel.refreshFactions();
            }
        });

        addRandomRelationshipBtn.setOnAction(e -> {
            var factions = db.getAllFactions();
            if (factions.size() < 2) {
                outputArea.appendText("❌ Need at least 2 factions!\n\n");
                return;
            }

            var f1 = factions.get((int)(Math.random() * factions.size()));
            var f2 = factions.get((int)(Math.random() * factions.size()));
            while (f1.getId() == f2.getId()) {
                f2 = factions.get((int)(Math.random() * factions.size()));
            }

            var types = org.example.dndfactionsimulator.model.RelationshipType.values();
            var rel = new org.example.dndfactionsimulator.model.Relationship(
                    f1.getId(), f2.getId(),
                    types[(int)(Math.random() * types.length)],
                    (int)(Math.random() * 201) - 100
            );

            if (db.addRelationship(rel)) {
                outputArea.appendText(String.format("✅ %s <-> %s: %s\n\n",
                        f1.getName(), f2.getName(), rel.getType()));
            }
        });

        logRandomEventBtn.setOnAction(e -> {
            var factions = db.getActiveFactions();
            if (factions.isEmpty()) {
                outputArea.appendText("❌ Need at least 1 active faction!\n\n");
                return;
            }

            var faction = factions.get((int)(Math.random() * factions.size()));
            var actions = org.example.dndfactionsimulator.model.FactionAction.values();
            var action = actions[(int)(Math.random() * actions.length)];

            var event = new org.example.dndfactionsimulator.model.WorldEvent(
                    db.getCurrentTurn(),
                    faction.getId(),
                    action,
                    faction.getName() + " performed " + action.getDisplayName()
            );

            if (db.addWorldEvent(event)) {
                outputArea.appendText("✅ " + event + "\n\n");
                eventLogPanel.refreshEvents();
            }
        });

        testPanel.getChildren().addAll(header, buttonBox, outputArea);
        return testPanel;
    }

    private void advanceTurn() {
        if (db.advanceTurn()) {
            turnLabel.setText("Turn: " + db.getCurrentTurn());
            eventLogPanel.refreshEvents();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Turn Advanced");
            alert.setHeaderText(null);
            alert.setContentText("Advanced to Turn " + db.getCurrentTurn());
            alert.showAndWait();
        }
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // File menu
        Menu fileMenu = new Menu("File");
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(e -> {
            db.close();
            System.exit(0);
        });
        fileMenu.getItems().add(exitItem);

        // View menu
        Menu viewMenu = new Menu("View");
        MenuItem refreshAllItem = new MenuItem("Refresh All");
        refreshAllItem.setOnAction(e -> {
            factionPanel.refreshFactions();
            eventLogPanel.refreshEvents();
        });
        viewMenu.getItems().add(refreshAllItem);

        menuBar.getMenus().addAll(fileMenu, viewMenu);
        return menuBar;
    }

    @Override
    public void stop() {
        db.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}