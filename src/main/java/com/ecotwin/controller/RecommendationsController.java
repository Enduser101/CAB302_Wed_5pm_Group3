package com.ecotwin.controller;

import com.ecotwin.AppContext;
import com.ecotwin.model.Recommendation;
import com.ecotwin.model.Recommendation.Domain;
import com.ecotwin.model.SustainabilityScenario;
import com.ecotwin.service.IndicativeScoreCalculator;
import com.ecotwin.service.RecommendationService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;
import com.ecotwin.service.RecommendationExplanationService;


/**
 * US-24: the household's recommendations, weakest domain first, filterable by domain.
 * Each card shows the points the total score would gain.
 */
public class RecommendationsController {

    private final AppContext ctx;
    private final RecommendationService recommendationService =
            new RecommendationService(new IndicativeScoreCalculator());

    private final RecommendationExplanationService explanationService =
            new RecommendationExplanationService();

    private List<Recommendation> recommendations = List.of();
    private Domain weakest;

    @FXML private Label householdLabel;
    @FXML private Label introLabel;
    @FXML private HBox filterTabs;
    @FXML private VBox recommendationList;

    public RecommendationsController(Navigator nav, AppContext ctx) {
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        if (!ctx.session.hasActiveHousehold()) {
            introLabel.setText("Join or create a household to get recommendations.");
            return;
        }

        householdLabel.setText(ctx.session.getCurrentHousehold().getName());

        Optional<SustainabilityScenario> values =
                ctx.householdScoreService.currentValues(ctx.session.getCurrentHousehold());

        if (values.isEmpty()) {
            introLabel.setText("Record energy, water, waste and transport information to get recommendations.");
            return;
        }

        recommendations = recommendationService.forHousehold(values.get(),
                ctx.session.getCurrentHousehold().getOccupants());

        if (recommendations.isEmpty()) {
            introLabel.setText("Nothing to recommend: every domain is at or better than its benchmark.");
            return;
        }

        weakest = recommendations.get(0).domain();
        introLabel.setText("Based on your latest readings, starting with your weakest area.");

        buildTabs();
        show(null);
    }

    private void buildTabs() {
        filterTabs.getChildren().add(tab("All", null));
        for (Domain domain : Domain.values()) {
            filterTabs.getChildren().add(tab(name(domain), domain));
        }
    }

    private Button tab(String text, Domain domain) {
        Button button = new Button(text);
        button.getStyleClass().add("filter-tab");
        button.setOnAction(e -> show(domain));
        return button;
    }

    /** @param domain the domain to show, or null for all */
    private void show(Domain domain) {
        for (int i = 0; i < filterTabs.getChildren().size(); i++) {
            Domain tabDomain = i == 0 ? null : Domain.values()[i - 1];
            filterTabs.getChildren().get(i).getStyleClass().remove("filter-tab-active");
            if (tabDomain == domain) {
                filterTabs.getChildren().get(i).getStyleClass().add("filter-tab-active");
            }
        }

        recommendationList.getChildren().clear();
        recommendations.stream()
                .filter(r -> domain == null || r.domain() == domain)
                .forEach(r -> recommendationList.getChildren().add(card(r)));
    }

    private HBox card(Recommendation recommendation) {
        Label domain = new Label(name(recommendation.domain()));
        domain.getStyleClass().add("rec-domain");

        Label title = new Label(recommendation.title());
        title.getStyleClass().add("rec-title");

        Label whyLabel = new Label("Why this recommendation?");
        whyLabel.getStyleClass().add("rec-detail");

        Label explanation = new Label(explanationService.explain(recommendation));
        explanation.getStyleClass().add("rec-detail");
        explanation.setWrapText(true);

        VBox text = new VBox(2, domain, title, whyLabel, explanation);

        HBox.setHgrow(text, Priority.ALWAYS);

        Label points = new Label("+" + recommendation.points());
        points.getStyleClass().add("rec-points");

        HBox card = new HBox(16, text, points);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("domain-card");
        if (recommendation.domain() == weakest) {
            card.getStyleClass().add("rec-card-weakest");
        }
        return card;
    }

    private static String name(Domain domain) {
        String lower = domain.name().toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}