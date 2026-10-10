package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.ScoreBreakdown;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * US-20/21: the household's overall score with its four domain scores beside it.
 * Each domain shows a bar, its number and a word
 */
public class DashboardController {

    private final AppContext ctx;

    @FXML private Label noDataLabel;
    @FXML private VBox scoresSection;
    @FXML private Label totalScoreLabel;
    @FXML private Label scoreExplanationLabel; // left empty for US-22 (score meaning)
    @FXML private VBox domainRows;

    public DashboardController(Navigator nav, AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        if (!ctx.session.hasActiveHousehold()) {
            showNoData("Join or create a household to see its sustainability scores.");
            return;
        }

        Optional<ScoreBreakdown> scores =
                ctx.householdScoreService.currentScores(ctx.session.getCurrentHousehold());

        if (scores.isEmpty()) {
            showNoData("Record energy, water, waste and transport information to see your scores.");
            return;
        }

        showScores(scores.get());
    }

    private void showNoData(String message) {
        noDataLabel.setText(message);
        scoresSection.setVisible(false);
        scoresSection.setManaged(false);
    }

    private void showScores(ScoreBreakdown scores) {
        noDataLabel.setVisible(false);
        noDataLabel.setManaged(false);
        totalScoreLabel.setText(whole(scores.total()) + " / 100 - " + rating(scores.total()));

        Map<String, Double> domains = new LinkedHashMap<>();
        domains.put("Energy", scores.energy());
        domains.put("Water", scores.water());
        domains.put("Waste", scores.waste());
        domains.put("Transport", scores.transport());

        double lowest = domains.values().stream().min(Double::compare).orElseThrow();

        domainRows.getChildren().clear();
        domains.forEach((name, score) ->
                domainRows.getChildren().add(domainRow(name, score, score == lowest)));
    }

    private HBox domainRow(String name, double score, boolean lowest) {
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("field-label");
        nameLabel.setMinWidth(80);

        ProgressBar bar = new ProgressBar(score / 100);
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setAccessibleText(name + " score " + whole(score) + " out of 100");
        HBox.setHgrow(bar, Priority.ALWAYS);

        Label valueLabel = new Label(whole(score) + " / 100 - " + rating(score));
        valueLabel.getStyleClass().add("value-text");
        valueLabel.setMinWidth(150);

        HBox row = new HBox(12, nameLabel, bar, valueLabel);
        row.setAlignment(Pos.CENTER_LEFT);

        Label badge = new Label("Lowest");
        badge.getStyleClass().add("badge");
        badge.setVisible(lowest);
        row.getChildren().add(badge);
        return row;
    }

    private static String rating(double score) {
        if (score >= 70) {
            return "Good";
        }
        if (score >= 40) {
            return "Fair";
        }
        return "Needs work";
    }

    private static long whole(double score) {
        return Math.round(score);
    }
}