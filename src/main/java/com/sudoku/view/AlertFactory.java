package com.sudoku.view;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class AlertFactory {

	public static Optional<ButtonType> showVictoryAlert(Scene ownerScene, long durationMillis) {
		String timeFormatted = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(durationMillis),
				TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60
		);

		Dialog<ButtonType> dialog = new Dialog<>();
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Congratulations!");

		VBox content = new VBox(15);
		content.setAlignment(Pos.CENTER);

		try {
			Image trophyImage = new Image(Objects.requireNonNull(AlertFactory.class.getResourceAsStream("/images/trophy.png")));
			ImageView trophyImageView = new ImageView(trophyImage);
			trophyImageView.setFitHeight(100);
			trophyImageView.setFitWidth(100);
			content.getChildren().add(trophyImageView);
		} catch (Exception e) {
			System.err.println("Trophy image not found. Please check the path.");
		}

		Label titleLabel = new Label("You Win!");
		titleLabel.getStyleClass().add("win-title");

		Label messageLabel = new Label("You solved the puzzle in " + timeFormatted + "!");
		messageLabel.getStyleClass().add("win-message");

		content.getChildren().addAll(titleLabel, messageLabel);

		dialog.getDialogPane().setContent(content);
		dialog.getDialogPane().getStyleClass().add("win-dialog");

		if (ownerScene != null) {
			dialog.getDialogPane().getStylesheets().addAll(ownerScene.getStylesheets());
		}

		ButtonType newGameButtonType = new ButtonType("New Game (Same Difficulty)");
		ButtonType mainMenuButtonType = new ButtonType("Main Menu");
		dialog.getDialogPane().getButtonTypes().addAll(newGameButtonType, mainMenuButtonType);

		// Style the buttons
		final Button newGameBtn = (Button) dialog.getDialogPane().lookupButton(newGameButtonType);
		newGameBtn.getStyleClass().add("control-button");
		final Button mainMenuBtn = (Button) dialog.getDialogPane().lookupButton(mainMenuButtonType);
		mainMenuBtn.getStyleClass().add("control-button");

		Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
		try {
			Image icon = new Image(Objects.requireNonNull(AlertFactory.class.getResourceAsStream("/images/sudoku-icon.png")));
			stage.getIcons().add(icon);
		} catch (Exception e) {
			System.err.println("Error: Icon image not found for the dialog.");
		}

		return dialog.showAndWait();
	}
}
