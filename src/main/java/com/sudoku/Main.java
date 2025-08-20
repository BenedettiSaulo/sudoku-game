package com.sudoku;

import com.sudoku.controller.GameController;
import com.sudoku.model.Difficulty;
import com.sudoku.model.SudokuBoard;
import com.sudoku.view.StartMenuView;
import com.sudoku.view.SudokuView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
	private Stage primaryStage;

	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
		primaryStage.setTitle("Sudoku");

		try {
			Image icon = new Image(getClass().getResourceAsStream("/images/sudoku-icon.png"));
			primaryStage.getIcons().add(icon);
		} catch (Exception e) {
			System.err.println("Error: Icon image not found.");
		}

		showStartMenu();
		primaryStage.show();
	}

	public void showStartMenu() {
		StartMenuView startMenuView = new StartMenuView();
		startMenuView.getStartButton().setOnAction(e -> {
			Difficulty selectedDifficulty = startMenuView.getDifficultyComboBox().getValue();
			startGame(selectedDifficulty);
		});

		Scene scene = new Scene(startMenuView.getRootPane(), 450, 300);
		scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
		primaryStage.setScene(scene);
		primaryStage.centerOnScreen();
	}

	public void startGame(Difficulty difficulty) {
		SudokuBoard board = new SudokuBoard();
		SudokuView view = new SudokuView(SudokuBoard.SIZE, SudokuBoard.SUBGRID_SIZE);
		new GameController(this, board, view, difficulty);

		Scene scene = new Scene(view.getRootPane(), 650, 800);
		scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());

		primaryStage.setScene(scene);
		primaryStage.setResizable(false);
		primaryStage.centerOnScreen();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
