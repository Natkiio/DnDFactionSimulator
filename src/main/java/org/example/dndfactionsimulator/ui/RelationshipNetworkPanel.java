package org.example.dndfactionsimulator.ui;

import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.example.dndfactionsimulator.database.DatabaseManager;
import org.example.dndfactionsimulator.model.*;
import java.util.*;

public class RelationshipNetworkPanel extends VBox {

    private DatabaseManager db;
    private Canvas canvas;
    private Map<Integer, NodePosition> nodePositions;

    public RelationshipNetworkPanel(DatabaseManager db) {
        this.db = db;
        this.nodePositions = new HashMap<>();

        setPadding(new Insets(20));
        setSpacing(15);

        // Header
        Label header = new Label("🕸️ Relationship Network");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label subtitle = new Label("Visual graph of faction relationships");
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: gray;");

        // Controls
        HBox controls = new HBox(10);
        controls.setStyle("-fx-alignment: center-left;");

        Button refreshBtn = new Button("🔄 Refresh Network");
        refreshBtn.setOnAction(e -> drawNetwork());

        CheckBox showInactiveCheck = new CheckBox("Show Inactive Factions");
        showInactiveCheck.setOnAction(e -> drawNetwork());

        controls.getChildren().addAll(refreshBtn, showInactiveCheck);

        // Legend
        HBox legend = new HBox(20);
        legend.setStyle("-fx-alignment: center-left; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-border-radius: 5;");

        Label legendTitle = new Label("Legend:");
        legendTitle.setStyle("-fx-font-weight: bold;");

        Label alliedLabel = new Label("─── Allied");
        alliedLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");

        Label friendlyLabel = new Label("─── Friendly");
        friendlyLabel.setStyle("-fx-text-fill: #2ecc71;");

        Label neutralLabel = new Label("─── Neutral");
        neutralLabel.setStyle("-fx-text-fill: #95a5a6;");

        Label unfriendlyLabel = new Label("─── Unfriendly");
        unfriendlyLabel.setStyle("-fx-text-fill: #e67e22;");

        Label hostileLabel = new Label("─── Hostile");
        hostileLabel.setStyle("-fx-text-fill: #e74c3c;");

        Label warLabel = new Label("─── At War");
        warLabel.setStyle("-fx-text-fill: #c0392b; -fx-font-weight: bold;");

        legend.getChildren().addAll(legendTitle, alliedLabel, friendlyLabel,
                neutralLabel, unfriendlyLabel, hostileLabel, warLabel);

        // Canvas for drawing
        canvas = new Canvas(1100, 600);
        canvas.setStyle("-fx-border-color: #34495e; -fx-border-width: 2;");

        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        getChildren().addAll(header, subtitle, controls, legend, scrollPane);

        // Initial draw
        drawNetwork();
    }

    private void drawNetwork() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Clear canvas
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        List<Faction> factions = db.getActiveFactions();
        List<Relationship> relationships = db.getAllRelationships();

        if (factions.isEmpty()) {
            gc.setFill(Color.GRAY);
            gc.fillText("No active factions to display",
                    canvas.getWidth() / 2 - 80, canvas.getHeight() / 2);
            return;
        }

        // Calculate positions for factions in a circle
        calculateNodePositions(factions);

        // Draw relationships (lines) first so they're behind nodes
        drawRelationships(gc, relationships, factions);

        // Draw faction nodes
        drawFactionNodes(gc, factions);
    }

    private void calculateNodePositions(List<Faction> factions) {
        nodePositions.clear();

        double centerX = canvas.getWidth() / 2;
        double centerY = canvas.getHeight() / 2;
        double radius = Math.min(centerX, centerY) - 100;

        int count = factions.size();
        for (int i = 0; i < count; i++) {
            double angle = (2 * Math.PI * i) / count - Math.PI / 2;
            double x = centerX + radius * Math.cos(angle);
            double y = centerY + radius * Math.sin(angle);

            nodePositions.put(factions.get(i).getId(), new NodePosition(x, y));
        }
    }

    private void drawRelationships(GraphicsContext gc, List<Relationship> relationships, List<Faction> factions) {
        Set<Integer> activeFactionIds = new HashSet<>();
        for (Faction f : factions) {
            activeFactionIds.add(f.getId());
        }

        for (Relationship rel : relationships) {
            // Only draw if both factions are active
            if (!activeFactionIds.contains(rel.getFaction1Id()) ||
                    !activeFactionIds.contains(rel.getFaction2Id())) {
                continue;
            }

            NodePosition pos1 = nodePositions.get(rel.getFaction1Id());
            NodePosition pos2 = nodePositions.get(rel.getFaction2Id());

            if (pos1 == null || pos2 == null) continue;

            // Set color based on relationship type
            Color lineColor = getRelationshipColor(rel.getType());
            gc.setStroke(lineColor);

            // Set line width based on strength
            double lineWidth = Math.abs(rel.getStrength()) / 25.0 + 1;
            gc.setLineWidth(lineWidth);

            // Draw line
            gc.strokeLine(pos1.x, pos1.y, pos2.x, pos2.y);
        }
    }

    private void drawFactionNodes(GraphicsContext gc, List<Faction> factions) {
        for (Faction faction : factions) {
            NodePosition pos = nodePositions.get(faction.getId());
            if (pos == null) continue;

            double nodeSize = 40;

            // Draw node circle
            gc.setFill(getFactionColor(faction));
            gc.fillOval(pos.x - nodeSize/2, pos.y - nodeSize/2, nodeSize, nodeSize);

            // Draw border
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(2);
            gc.strokeOval(pos.x - nodeSize/2, pos.y - nodeSize/2, nodeSize, nodeSize);

            // Draw faction strength inside circle
            gc.setFill(Color.WHITE);
            gc.fillText(String.valueOf(faction.getStrength()),
                    pos.x - 10, pos.y + 5);

            // Draw faction name below
            gc.setFill(Color.BLACK);
            String name = faction.getName();
            if (name.length() > 15) {
                name = name.substring(0, 12) + "...";
            }
            gc.fillText(name, pos.x - 30, pos.y + nodeSize/2 + 15);
        }
    }

    private Color getRelationshipColor(RelationshipType type) {
        return switch (type) {
            case ALLIED -> Color.rgb(39, 174, 96);      // Green
            case FRIENDLY -> Color.rgb(46, 204, 113);   // Light green
            case NEUTRAL -> Color.rgb(149, 165, 166);   // Gray
            case UNFRIENDLY -> Color.rgb(230, 126, 34); // Orange
            case HOSTILE -> Color.rgb(231, 76, 60);     // Red
            case AT_WAR -> Color.rgb(192, 57, 43);      // Dark red
        };
    }

    private Color getFactionColor(Faction faction) {
        // Color based on alignment
        if (faction.getAlignment().name().contains("GOOD")) {
            return Color.rgb(52, 152, 219); // Blue
        } else if (faction.getAlignment().name().contains("EVIL")) {
            return Color.rgb(155, 89, 182); // Purple
        } else {
            return Color.rgb(241, 196, 15); // Yellow
        }
    }

    // Helper class for node positions
    private static class NodePosition {
        double x, y;

        NodePosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}