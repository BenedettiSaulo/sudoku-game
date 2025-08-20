package com.sudoku.view;

import com.sudoku.model.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class StartMenuView {

	private VBox rootPane;
	private ComboBox<Difficulty> difficultyComboBox;
	private Button startButton;

	public StartMenuView() {
		rootPane = new VBox(20);
		rootPane.setAlignment(Pos.CENTER);
		rootPane.setPadding(new Insets(50));
		rootPane.setStyle("-fx-background-color: #f4f4f4;");

		Label title = new Label("Sudoku Game");
		title.setFont(new Font("Arial Black", 40));

		Label subtitle = new Label("Select Difficulty:");
		subtitle.setFont(new Font("Arial", 16));

		difficultyComboBox = new ComboBox<>();
		difficultyComboBox.getItems().setAll(Difficulty.values());
		difficultyComboBox.setValue(Difficulty.EASY);
		difficultyComboBox.setPrefWidth(200);

		startButton = new Button("Start Game");
		startButton.getStyleClass().add("control-button");

		rootPane.getChildren().addAll(title, subtitle, difficultyComboBox, startButton);
	}

	public VBox getRootPane() {
		return rootPane;
	}

	public ComboBox<Difficulty> getDifficultyComboBox() {
		return difficultyComboBox;
	}

	public Button getStartButton() {
		return startButton;
	}
}
