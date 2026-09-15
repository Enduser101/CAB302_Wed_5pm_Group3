package com.ecotwin.controller;

import com.ecotwin.AppContext;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

import java.util.List;

/** The post-login app shell: sidebar nav + a content area. Household and Account are real screens;
 *  the rest are placeholders reserved for the epics that own them. */
public class AppShellController {

    private final Navigator nav;
    private final AppContext ctx;

    @FXML private Button dashboardNav;
    @FXML private Button resourcesNav;
    @FXML private Button recommendationsNav;
    @FXML private Button scenariosNav;
    @FXML private Button householdNav;
    @FXML private Button accountNav;
    @FXML private StackPane contentArea;

    public AppShellController(Navigator nav, AppContext ctx) {
        this.nav = nav;
        this.ctx = ctx;
    }

    @FXML
    private void initialize() {
        showHousehold();
    }

    @FXML
    private void showDashboard() {
        showPlaceholder(dashboardNav, "Dashboard");
    }

    @FXML
    private void showResources() {
        showPlaceholder(resourcesNav, "Resources");
    }

    @FXML
    private void showRecommendations() {
        showPlaceholder(recommendationsNav, "Recommendations");
    }

    @FXML
    private void showScenarios() {
        showPlaceholder(scenariosNav, "Scenarios");
    }

    @FXML
    private void showHousehold() {
        setActive(householdNav);
        contentArea.getChildren().setAll(nav.load("/com/ecotwin/household-view.fxml"));
    }

    @FXML
    private void showAccount() {
        setActive(accountNav);
        contentArea.getChildren().setAll(nav.load("/com/ecotwin/account-view.fxml"));
    }

    private void showPlaceholder(Button navButton, String title) {
        setActive(navButton);
        Label label = new Label(title + " — coming soon");
        label.getStyleClass().add("muted-text");
        contentArea.getChildren().setAll(new StackPane(label));
    }

    private void setActive(Button active) {
        for (Button button : List.of(dashboardNav, resourcesNav, recommendationsNav, scenariosNav, householdNav, accountNav)) {
            button.getStyleClass().remove("nav-item-active");
        }
        active.getStyleClass().add("nav-item-active");
    }
}
