package org.example.dndfactionsimulator;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.dndfactionsimulator.database.*;
import org.example.dndfactionsimulator.ui.*;
import org.example.dndfactionsimulator.simulation.*;
import org.example.dndfactionsimulator.model.*;
import java.util.List;

public class Main extends Application {
    private DatabaseManager db;
    private SimulationEngine simulationEngine;
    private FactionOverviewPanel factionPanel;
    private EventLogPanel eventLogPanel;
    private PlayerInfluencePanel playerInfluencePanel;
    private AnalyticsPanel analyticsPanel;
    private RelationshipNetworkPanel relationshipNetworkPanel;
    private Label turnLabel;

    @Override
    public void start(Stage primaryStage) {
        // Initialize database and simulation engine
        db = DatabaseManager.getInstance();
        simulationEngine = new SimulationEngine(db);

        // Create main layout
        BorderPane root = new BorderPane();

        // Top bar with title and turn counter
        VBox topBar = new VBox(5);
        topBar.getStyleClass().add("top-bar");

        Label titleLabel = new Label("🎲 D&D Faction Simulator");
        titleLabel.getStyleClass().add("title-label");

        HBox turnBox = new HBox(15);
        turnBox.setStyle("-fx-alignment: center-left;");

        turnLabel = new Label("Turn: " + db.getCurrentTurn());
        turnLabel.getStyleClass().add("turn-label");

        Button advanceTurnBtn = new Button("⏭ Advance Turn");
        advanceTurnBtn.getStyleClass().add("advance-turn-button");
        advanceTurnBtn.setOnAction(e -> advanceTurn());

        turnBox.getChildren().addAll(turnLabel, advanceTurnBtn);
        topBar.getChildren().addAll(titleLabel, turnBox);

        root.setTop(topBar);

        // Create tab pane
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Dashboard Tab
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

        // Analytics Tab
        Tab analyticsTab = new Tab("📈 Analytics");
        analyticsPanel = new AnalyticsPanel(db);
        analyticsTab.setContent(analyticsPanel);

        // Relationship Network Tab
        Tab networkTab = new Tab("🕸️ Network");
        relationshipNetworkPanel = new RelationshipNetworkPanel(db);
        networkTab.setContent(relationshipNetworkPanel);

        // Player Influence Tab
        Tab playerInfluenceTab = new Tab("🧙 Player Influence");
        playerInfluencePanel = new PlayerInfluencePanel(db);
        playerInfluenceTab.setContent(playerInfluencePanel);

        // Testing Tab
        Tab testingTab = new Tab("🧪 Testing");
        testingTab.setContent(createTestingPanel());

        tabPane.getTabs().addAll(dashboardTab, factionsTab, eventsTab, analyticsTab,
                networkTab, playerInfluenceTab, testingTab);

        root.setCenter(tabPane);

        // Menu bar
        MenuBar menuBar = createMenuBar();
        VBox topContainer = new VBox(menuBar, topBar);
        root.setTop(topContainer);

        // Create and show scene with medieval theme
        Scene scene = new Scene(root, 1200, 800);

        // Apply medieval CSS theme
        try {
            scene.getStylesheets().add(
                    getClass().getResource("/styles/medieval-theme.css").toExternalForm()
            );
            System.out.println("✅ Medieval theme loaded successfully!");
        } catch (Exception e) {
            System.err.println("⚠️ Could not load medieval theme: " + e.getMessage());
        }

        primaryStage.setTitle("D&D Faction Simulator");
        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("✅ Application started successfully!");
    }

    private VBox createDashboard() {
        VBox dashboard = new VBox(20);
        dashboard.getStyleClass().add("dashboard-panel");
        dashboard.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("📊 World Statistics");
        header.getStyleClass().add("header-label");

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
                var sortedFactions = activeFactions.stream()
                        .sorted((f1, f2) -> Integer.compare(f2.getStrength(), f1.getStrength()))
                        .toList();

                for (int i = 0; i < sortedFactions.size(); i++) {
                    Faction f = sortedFactions.get(i);
                    statsArea.appendText(String.format("%d. %s - Strength: %d\n", i + 1, f.getName(), f.getStrength()));
                }
            }

            statsArea.appendText("\n=== RELATIONSHIP BREAKDOWN ===\n\n");
            for (var type : RelationshipType.values()) {
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
        testPanel.getStyleClass().add("panel-container");
        testPanel.setStyle("-fx-padding: 20; -fx-alignment: center;");

        Label header = new Label("🧪 Quick Testing Tools");
        header.getStyleClass().add("header-label");

        Label subtitle = new Label("Generate random data for testing");
        subtitle.getStyleClass().add("subtitle-label");

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
            var faction = new Faction(
                    "Faction " + System.currentTimeMillis() % 1000,
                    FactionType.values()[(int)(Math.random() * FactionType.values().length)],
                    Alignment.values()[(int)(Math.random() * Alignment.values().length)]
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

            var types = RelationshipType.values();
            var rel = new Relationship(
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
            var actions = FactionAction.values();
            var action = actions[(int)(Math.random() * actions.length)];

            var event = new WorldEvent(
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

        testPanel.getChildren().addAll(header, subtitle, buttonBox, outputArea);
        return testPanel;
    }

    private void advanceTurn() {
        // Check if there are active factions
        var activeFactions = db.getActiveFactions();
        if (activeFactions.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Active Factions");
            alert.setHeaderText("Cannot Simulate Turn");
            alert.setContentText("You need at least one active faction to run a simulation.");
            alert.showAndWait();
            return;
        }

        // Show simulation running dialog
        Alert simulating = new Alert(Alert.AlertType.INFORMATION);
        simulating.setTitle("Simulating...");
        simulating.setHeaderText("Running Turn Simulation");
        simulating.setContentText("Processing " + activeFactions.size() + " faction actions...");
        simulating.show();

        // Run the simulation
        List<WorldEvent> events = simulationEngine.runTurn();
        simulating.close();

        // Update UI
        int newTurn = db.getCurrentTurn();
        turnLabel.setText("Turn: " + newTurn);
        eventLogPanel.refreshEvents();
        factionPanel.refreshFactions();

        // Show results
        Alert results = new Alert(Alert.AlertType.INFORMATION);
        results.setTitle("Turn Complete!");
        results.setHeaderText("Turn " + newTurn + " Simulation Complete");

        StringBuilder summary = new StringBuilder();
        summary.append("Events this turn: ").append(events.size()).append("\n\n");
        summary.append("Summary:\n");

        for (WorldEvent event : events.stream().limit(5).toList()) {
            summary.append("• ").append(event.getAction().getDisplayName())
                    .append(": ").append(event.getDescription().substring(0,
                            Math.min(60, event.getDescription().length())))
                    .append("...\n");
        }

        if (events.size() > 5) {
            summary.append("\n... and ").append(events.size() - 5).append(" more events.");
        }

        results.setContentText(summary.toString());
        results.showAndWait();
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