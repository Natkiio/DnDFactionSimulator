package org.example.dndfactionsimulator.ui;

import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import org.example.dndfactionsimulator.database.DatabaseManager;
import org.example.dndfactionsimulator.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class AnalyticsPanel extends VBox {

    private DatabaseManager db;

    public AnalyticsPanel(DatabaseManager db) {
        this.db = db;

        setPadding(new Insets(20));
        setSpacing(20);

        // Header
        Label header = new Label("📊 Analytics & Insights");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        // Refresh button
        Button refreshBtn = new Button("🔄 Refresh All Charts");
        refreshBtn.setStyle("-fx-font-size: 14px;");
        refreshBtn.setOnAction(e -> refreshCharts());

        // Charts in tabs
        TabPane chartTabs = new TabPane();
        chartTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab strengthComparisonTab = new Tab("💪 Strength Comparison");
        strengthComparisonTab.setContent(createStrengthComparisonChart());

        Tab resourceDistributionTab = new Tab("💰 Resource Distribution");
        resourceDistributionTab.setContent(createResourceDistributionChart());

        Tab factionTypesTab = new Tab("🏛️ Faction Types");
        factionTypesTab.setContent(createFactionTypesPieChart());

        Tab alignmentTab = new Tab("⚖️ Alignment Distribution");
        alignmentTab.setContent(createAlignmentPieChart());

        chartTabs.getTabs().addAll(strengthComparisonTab, resourceDistributionTab,
                factionTypesTab, alignmentTab);

        getChildren().addAll(header, refreshBtn, chartTabs);

        // Initial load
        refreshCharts();
    }

    private VBox createStrengthComparisonChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Factions");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Strength");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Faction Strength Comparison");
        barChart.setLegendVisible(false);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Strength");

        List<Faction> activeFactions = db.getActiveFactions();
        activeFactions.sort((f1, f2) -> Integer.compare(f2.getStrength(), f1.getStrength()));

        for (Faction faction : activeFactions) {
            series.getData().add(new XYChart.Data<>(faction.getName(), faction.getStrength()));
        }

        barChart.getData().add(series);

        container.getChildren().add(barChart);
        return container;
    }

    private VBox createResourceDistributionChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Factions");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Amount");

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Resource Distribution Across Factions");

        XYChart.Series<String, Number> goldSeries = new XYChart.Series<>();
        goldSeries.setName("Gold");

        XYChart.Series<String, Number> troopsSeries = new XYChart.Series<>();
        troopsSeries.setName("Troops");

        XYChart.Series<String, Number> magicSeries = new XYChart.Series<>();
        magicSeries.setName("Magic");

        XYChart.Series<String, Number> influenceSeries = new XYChart.Series<>();
        influenceSeries.setName("Influence");

        List<Faction> activeFactions = db.getActiveFactions();
        activeFactions.sort((f1, f2) -> Integer.compare(f2.getStrength(), f1.getStrength()));

        for (Faction faction : activeFactions.stream().limit(10).toList()) {
            goldSeries.getData().add(new XYChart.Data<>(faction.getName(), faction.getGold()));
            troopsSeries.getData().add(new XYChart.Data<>(faction.getName(), faction.getTroops()));
            magicSeries.getData().add(new XYChart.Data<>(faction.getName(), faction.getMagic()));
            influenceSeries.getData().add(new XYChart.Data<>(faction.getName(), faction.getInfluence()));
        }

        barChart.getData().addAll(goldSeries, troopsSeries, magicSeries, influenceSeries);

        container.getChildren().add(barChart);
        return container;
    }

    private VBox createFactionTypesPieChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Active Factions by Type");

        List<Faction> activeFactions = db.getActiveFactions();

        Map<FactionType, Long> typeCounts = activeFactions.stream()
                .collect(Collectors.groupingBy(Faction::getType, Collectors.counting()));

        for (Map.Entry<FactionType, Long> entry : typeCounts.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey().getDisplayName() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            pieChart.getData().add(slice);
        }

        if (pieChart.getData().isEmpty()) {
            Label noDataLabel = new Label("No active factions to display");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            container.getChildren().add(noDataLabel);
        } else {
            container.getChildren().add(pieChart);
        }

        return container;
    }

    private VBox createAlignmentPieChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Active Factions by Alignment");

        List<Faction> activeFactions = db.getActiveFactions();

        Map<Alignment, Long> alignmentCounts = activeFactions.stream()
                .collect(Collectors.groupingBy(Faction::getAlignment, Collectors.counting()));

        for (Map.Entry<Alignment, Long> entry : alignmentCounts.entrySet()) {
            PieChart.Data slice = new PieChart.Data(
                    entry.getKey().getDisplayName() + " (" + entry.getValue() + ")",
                    entry.getValue()
            );
            pieChart.getData().add(slice);
        }

        if (pieChart.getData().isEmpty()) {
            Label noDataLabel = new Label("No active factions to display");
            noDataLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
            container.getChildren().add(noDataLabel);
        } else {
            container.getChildren().add(pieChart);
        }

        return container;
    }

    private void refreshCharts() {
        // Rebuild the charts
        getChildren().clear();

        Label header = new Label("📊 Analytics & Insights");
        header.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Button refreshBtn = new Button("🔄 Refresh All Charts");
        refreshBtn.setStyle("-fx-font-size: 14px;");
        refreshBtn.setOnAction(e -> refreshCharts());

        TabPane chartTabs = new TabPane();
        chartTabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab strengthComparisonTab = new Tab("💪 Strength Comparison");
        strengthComparisonTab.setContent(createStrengthComparisonChart());

        Tab resourceDistributionTab = new Tab("💰 Resource Distribution");
        resourceDistributionTab.setContent(createResourceDistributionChart());

        Tab factionTypesTab = new Tab("🏛️ Faction Types");
        factionTypesTab.setContent(createFactionTypesPieChart());

        Tab alignmentTab = new Tab("⚖️ Alignment Distribution");
        alignmentTab.setContent(createAlignmentPieChart());

        chartTabs.getTabs().addAll(strengthComparisonTab, resourceDistributionTab,
                factionTypesTab, alignmentTab);

        getChildren().addAll(header, refreshBtn, chartTabs);
    }
}