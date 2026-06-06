package com.cvinsight.ui.controllers;

import com.cvinsight.model.CV;
import com.cvinsight.model.ComparisonResult;
import com.cvinsight.model.SectionComparison;
import com.cvinsight.ui.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;

public class CompareController {

    @FXML private Label titleLabel;
    @FXML private VBox  contentBox;

    public void init(CV exampleCV, ComparisonResult result) {
        String ownerName = exampleCV.getOwnerName() != null ? exampleCV.getOwnerName() : "";
        int pipe = ownerName.indexOf(" | ");
        String personName = pipe >= 0 ? ownerName.substring(0, pipe) : ownerName;
        titleLabel.setText("Comparing with " + personName);

        contentBox.getChildren().clear();
        contentBox.getChildren().add(buildScoreCard(result));

        for (SectionComparison sec : result.sections()) {
            contentBox.getChildren().add(buildSectionHeader(sec.name()));
            contentBox.getChildren().add(buildSectionRow(sec));
            Region divider = new Region();
            divider.setPrefHeight(1);
            divider.setStyle("-fx-background-color: #e5e7eb;");
            contentBox.getChildren().add(divider);
        }
    }

    private VBox buildScoreCard(ComparisonResult result) {
        Label userTitle = new Label("Your CV");
        userTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label userScore = new Label(result.userScore() + " / 100");
        userScore.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #185FA5;");
        VBox userBox = new VBox(4, userTitle, userScore);
        userBox.setAlignment(Pos.CENTER);
        userBox.setStyle("-fx-background-color: #E6F1FB; -fx-background-radius: 10; "
            + "-fx-padding: 16 24 16 24;");
        HBox.setHgrow(userBox, Priority.ALWAYS);

        Label exTitle = new Label("Example CV");
        exTitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");
        Label exScore = new Label(result.exampleScore() + " / 100");
        exScore.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #1D9E75;");
        VBox exBox = new VBox(4, exTitle, exScore);
        exBox.setAlignment(Pos.CENTER);
        exBox.setStyle("-fx-background-color: #E1F5EE; -fx-background-radius: 10; "
            + "-fx-padding: 16 24 16 24;");
        HBox.setHgrow(exBox, Priority.ALWAYS);

        HBox scoreRow = new HBox(16, userBox, exBox);

        Label tipsLabel = new Label("KEY TAKEAWAYS");
        tipsLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");

        FlowPane chips = new FlowPane(8, 8);
        chips.setAlignment(Pos.CENTER_LEFT);
        for (String tip : result.overallTips()) {
            if (tip == null || tip.isBlank()) continue;
            Label chip = new Label(tip);
            chip.setStyle("-fx-background-color: #FAEEDA; -fx-text-fill: #854F0B; "
                + "-fx-font-size: 11px; -fx-background-radius: 99; -fx-padding: 5 12 5 12;");
            chip.setWrapText(true);
            chips.getChildren().add(chip);
        }

        VBox card = new VBox(16, scoreRow, tipsLabel, chips);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; "
            + "-fx-border-color: #e5e7eb; -fx-border-width: 1; -fx-border-radius: 12; "
            + "-fx-padding: 20;");
        return card;
    }

    private Label buildSectionHeader(String name) {
        Label lbl = new Label(name != null ? name.toUpperCase() : "");
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #94a3b8;");
        VBox.setMargin(lbl, new Insets(8, 0, 0, 0));
        return lbl;
    }

    private HBox buildSectionRow(SectionComparison sec) {
        VBox userCard    = buildSideCard("Your CV",    sec.userContent(),    sec.verdict(), sec.tip(), false);
        VBox exampleCard = buildSideCard("Example CV", sec.exampleContent(), sec.verdict(), sec.tip(), true);
        HBox.setHgrow(userCard,    Priority.ALWAYS);
        HBox.setHgrow(exampleCard, Priority.ALWAYS);
        return new HBox(12, userCard, exampleCard);
    }

    private VBox buildSideCard(String roleLabel, String content, String verdict, String tip, boolean isExample) {
        Label typeLabel = new Label(roleLabel);
        typeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        Label contentLabel = new Label(content != null ? content : "");
        contentLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #0f172a;");
        contentLabel.setWrapText(true);

        String leftBorderColor;
        if ("worse".equals(verdict)) {
            leftBorderColor = isExample ? "#B4B2A9" : "#E24B4A";
        } else if ("better".equals(verdict)) {
            leftBorderColor = "#1D9E75";
        } else {
            leftBorderColor = "#B4B2A9";
        }

        VBox card = new VBox(8, typeLabel, contentLabel);
        card.setStyle(
            "-fx-background-color: white; -fx-background-radius: 8; "
            + "-fx-border-color: transparent transparent transparent " + leftBorderColor + "; "
            + "-fx-border-width: 0 0 0 3; "
            + "-fx-border-radius: 8; "
            + "-fx-padding: 14;");

        if (!isExample) {
            String badgeText, badgeBg, badgeFg;
            if ("worse".equals(verdict)) {
                badgeText = "Needs improvement"; badgeBg = "#FEE2E2"; badgeFg = "#991B1B";
            } else if ("better".equals(verdict)) {
                badgeText = "Strong"; badgeBg = "#DCFCE7"; badgeFg = "#166534";
            } else {
                badgeText = "On par"; badgeBg = "#F1F5F9"; badgeFg = "#64748B";
            }
            Label badge = new Label(badgeText);
            badge.setStyle("-fx-background-color: " + badgeBg + "; -fx-text-fill: " + badgeFg + "; "
                + "-fx-font-size: 11px; -fx-font-weight: bold; "
                + "-fx-background-radius: 6; -fx-padding: 3 8 3 8;");
            card.getChildren().add(badge);
        }

        if (isExample && "worse".equals(verdict) && tip != null && !tip.isBlank()) {
            Label tipLabel = new Label("💡 " + tip);
            tipLabel.setStyle("-fx-font-size: 13px; -fx-font-style: italic; -fx-text-fill: #854F0B;");
            tipLabel.setWrapText(true);
            card.getChildren().add(tipLabel);
        }

        return card;
    }

    @FXML private void handleBack() {
        SceneManager.switchTo("comparison.fxml");
    }
}
