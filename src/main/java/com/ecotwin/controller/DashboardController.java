package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.ScoreBreakdown;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.Node;
import javafx.scene.control.Button;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * US-20/21: the household's overall score with its four domain scores beside it.
 * Each domain card shows its number and a grade, and the weakest card says "Weakest",
 * so the meaning never relies on colour alone.
 */
public class DashboardController {

    private final AppContext ctx;

    @FXML private Label noDataLabel;
    @FXML private VBox scoresSection;
    @FXML private Label totalScoreLabel;
    @FXML private Label totalGradeLabel;
    @FXML private Label scoreExplanationLabel; // left empty for US-22 (score meaning)
    @FXML private HBox domainCards;

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

        long total = whole(scores.total());
        totalScoreLabel.setText(total + " / 100");
        totalGradeLabel.setText(letter(total) + " - " + word(total));

        Map<String, Double> domains = new LinkedHashMap<>();
        domains.put("Energy", scores.energy());
        domains.put("Water", scores.water());
        domains.put("Waste", scores.waste());
        domains.put("Transport", scores.transport());

        long weakest = domains.values().stream().mapToLong(DashboardController::whole).min().orElseThrow();

        domainCards.getChildren().clear();
        domains.forEach((name, score) ->
                domainCards.getChildren().add(domainCard(name, whole(score), whole(score) == weakest)));
    }

    private VBox domainCard(String name, long score, boolean weakest) {
        String grade = letter(score) + " - " + (weakest ? "Weakest" : word(score));

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("domain-name");
        Label scoreLabel = new Label(String.valueOf(score));
        scoreLabel.getStyleClass().add("domain-score");
        Label gradeLabel = new Label(grade);
        gradeLabel.getStyleClass().add("domain-grade");

        VBox card = new VBox(6, nameLabel, scoreLabel, gradeLabel);
        card.getStyleClass().add("domain-card");
        if (weakest) {
            card.getStyleClass().add("domain-card-weakest");
        }
        card.setMaxWidth(Double.MAX_VALUE);
        card.setAccessibleText(name + " score " + score + " out of 100, " + grade);
        HBox.setHgrow(card, Priority.ALWAYS);
        return card;
    }
    @FXML
    private void openRecommendations() {
        clickNav("#recommendationsNav");
    }

    @FXML
    private void openScenarios() {
        clickNav("#scenariosNav");
    }

    @FXML
    private void openResources() {
        clickNav("#resourcesNav");
    }

    /** Presses the matching sidebar button, so the app shell stays in charge of switching pages. */
    private void clickNav(String navId) {
        Node node = domainCards.getScene().lookup(navId);
        if (node instanceof Button navButton) {
            navButton.fire();
        }
    }
    /** Grade bands from the high-fidelity design: A 80+, B 70-79, C 60-69, D below 60. */
    private static String letter(long score) {
        if (score >= 80) {
            return "A";
        }
        if (score >= 70) {
            return "B";
        }
        if (score >= 60) {
            return "C";
        }
        return "D";
    }

    private static String word(long score) {
        if (score >= 80) {
            return "Strong";
        }
        if (score >= 70) {
            return "Good";
        }
        if (score >= 60) {
            return "Fair";
        }
        return "Needs work";
    }

    private static long whole(double score) {
        return Math.round(score);
    }
}