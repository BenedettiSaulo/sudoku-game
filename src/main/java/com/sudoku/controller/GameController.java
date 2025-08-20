package com.sudoku.controller;

import com.sudoku.Main;
import com.sudoku.model.Cell;
import com.sudoku.model.Difficulty;
import com.sudoku.model.SudokuBoard;
import com.sudoku.view.SudokuView;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.TimeUnit;

public class GameController {
	private enum UpdateState { USER_ACTION, UNDO_ACTION, RESET_ACTION }
	private UpdateState currentState = UpdateState.USER_ACTION;

	private record Move(int row, int col, int oldValue, int newValue) {}
	private final Stack<Move> moveHistory = new Stack<>();
	private TextField selectedCellField = null;
	private Point selectedCellCoords = null;

	private static final PseudoClass HIGHLIGHTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("highlighted");
	private final SudokuBoard board;
	private final SudokuView view;
	private final Difficulty difficulty;
	private long startTime;
	private Timeline timeline;

	private final Main mainApp;

	public GameController(Main mainApp, SudokuBoard board, SudokuView view, Difficulty difficulty) {
		this.mainApp = mainApp;
		this.board = board;
		this.view = view;
		this.difficulty = difficulty;

		setupTimer();
		addEventHandlers();
		startNewGame(difficulty);
	}

	private void setupTimer() {
		timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
			long elapsedTime = System.currentTimeMillis() - startTime;

			String timeFormatted = String.format("%02d:%02d",
					TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
					TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
			);

			view.getTimeLabel().setText("Time: " + timeFormatted);
		}));

		timeline.setCycleCount(Timeline.INDEFINITE);
	}

	private void addEventHandlers() {
		view.getNewGameButton().setOnAction(e -> handleNewGameButton());
		view.getUndoButton().setOnAction(e -> handleUndoButton());
		view.getClearButton().setOnMousePressed(e -> handleClearButton());
		view.getRestartButton().setOnAction(e -> handleRestartButton());
		view.getBackToMenuButton().setOnAction(e -> handleBackToMenuButton());
		view.getThemeToggleButton().setOnAction(e -> handleThemeToggle());

		for (ToggleButton numButton : view.getNumberButtons()) {
			numButton.setOnAction(e -> handleNumpadButton(numButton.getText()));
		}

		Platform.runLater(() -> {
			view.getRootPane().getScene().focusOwnerProperty().addListener((observable, oldOwner, newOwner) -> {
				this.selectedCellField = null;
				this.selectedCellCoords = null;

				if (newOwner instanceof TextField) {
					findSelectedCellCoords((TextField) newOwner).ifPresent(point -> {
						this.selectedCellField = (TextField) newOwner;
						this.selectedCellCoords = point;
					});
				}

				updateCellHighlighting();
				updateNumpadState();
			});
		});

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				TextField cellField = view.getCellFields()[row][col];
				final int finalRow = row;
				final int finalCol = col;

				cellField.textProperty().addListener((observable, oldText, newText) -> handleTextChange(finalRow, finalCol, cellField, oldText, newText));
			}
		}
	}

	private boolean isCellOnBoard(TextField textField) {
		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				if (view.getCellFields()[row][col] == textField) {
					return true;
				}
			}
		}

		return false;
	}

	private void handleTextChange(int row, int col, TextField cellField, String oldText, String newText) {
		if (currentState != UpdateState.USER_ACTION) return;

		if (newText.length() > 1) {
			cellField.setText(newText.substring(0, 1));
			return;
		}

		int oldValueInt = oldText.isEmpty() ? 0 : Integer.parseInt(oldText);
		int newValueInt = newText.isEmpty() ? 0 : Integer.parseInt(newText);

		if (oldValueInt != newValueInt) {
			moveHistory.push(new Move(row, col, oldValueInt, newValueInt));
		}

		updateModelFromView();
		board.validateBoard();
		updateErrorHighlightingInView();
		updateCellHighlighting();
		updateSubgridHighlighting();
		checkWinCondition();
	}

	private void updateCellHighlighting() {
		int selectedRow = (selectedCellCoords != null) ? selectedCellCoords.row() : -1;
		int selectedCol = (selectedCellCoords != null) ? selectedCellCoords.col() : -1;
		int subgridStartRow = (selectedRow != -1) ? selectedRow - selectedRow % SudokuBoard.SUBGRID_SIZE : -1;
		int subgridStartCol = (selectedCol != -1) ? selectedCol - selectedCol % SudokuBoard.SUBGRID_SIZE : -1;

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				boolean shouldHighlight = selectedCellCoords != null && (row == selectedRow || col == selectedCol ||
						(row >= subgridStartRow && row < subgridStartRow + SudokuBoard.SUBGRID_SIZE &&
								col >= subgridStartCol && col < subgridStartCol + SudokuBoard.SUBGRID_SIZE));
				view.getCellFields()[row][col].pseudoClassStateChanged(HIGHLIGHTED_PSEUDO_CLASS, shouldHighlight);
			}
		}
	}

	private void updateSubgridHighlighting() {
		for (int subgridRow = 0; subgridRow < SudokuBoard.SUBGRID_SIZE; subgridRow++) {
			for (int subgridCol = 0; subgridCol < SudokuBoard.SUBGRID_SIZE; subgridCol++) {
				GridPane subGridPane = view.getSubGrids()[subgridRow][subgridCol];

				int startRow = subgridRow * SudokuBoard.SUBGRID_SIZE;
				int startCol = subgridCol * SudokuBoard.SUBGRID_SIZE;

				if (board.isSubgridComplete(startRow, startCol)) {
					subGridPane.getStyleClass().add("sub-grid-complete");
				} else {
					subGridPane.getStyleClass().remove("sub-grid-complete");
				}
			}
		}
	}

	private void handleNumpadButton(String number) {
		if (selectedCellField != null && selectedCellField.isEditable()) {
			selectedCellField.setText(number);
		}
	}

	private void updateNumpadState() {
		boolean isCellSelectedAndEditable = selectedCellField != null && selectedCellField.isEditable();

		for (ToggleButton numButton: view.getNumberButtons()) {
			numButton.setDisable(!isCellSelectedAndEditable);
		}
	}

	private void startNewGame(Difficulty difficulty) {
		currentState = UpdateState.RESET_ACTION;

		clearAllViewAndState();
		this.startTime = System.currentTimeMillis();

		if (timeline != null) {
			timeline.stop();
		}

		timeline.play();

		board.generateNewBoard(difficulty);
		updateViewFromModel();
		currentState = UpdateState.USER_ACTION;

		Platform.runLater(this::focusFirstEditableCell);
	}

	private void focusFirstEditableCell() {
		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				if (view.getCellFields()[row][col].isEditable()) {
					view.getCellFields()[row][col].requestFocus();
					return;
				}
			}
		}
	}

	private void handleNewGameButton() {
		startNewGame(this.difficulty);
	}

	private void handleRestartButton() {
		currentState = UpdateState.RESET_ACTION;
		board.clearUserNumbers();
		updateViewFromModel();
		this.startTime = System.currentTimeMillis();
		this.moveHistory.clear();
		updateNumpadState();
		board.validateBoard();
		updateErrorHighlightingInView();
		currentState = UpdateState.USER_ACTION;
	}

	private void handleUndoButton() {
		if (!moveHistory.isEmpty()) {
			Move lastMove = moveHistory.pop();
			currentState = UpdateState.UNDO_ACTION;
			TextField cellToUndo = view.getCellFields()[lastMove.row()][lastMove.col()];
			String previousValue = lastMove.oldValue() == 0 ? "" : String.valueOf(lastMove.oldValue());
			cellToUndo.setText(previousValue);
			currentState = UpdateState.USER_ACTION;
			updateModelFromView();
			board.validateBoard();
			updateErrorHighlightingInView();
		}
	}

	private void handleClearButton() {
		if (selectedCellField != null && selectedCellField.isEditable()) {
			selectedCellField.setText("");
			selectedCellField.requestFocus();
		}
	}

	private void handleBackToMenuButton() {
		timeline.stop();
		mainApp.showStartMenu();
	}

	private void handleThemeToggle() {
		ToggleButton toggleButton = view.getThemeToggleButton();

		Scene scene = view.getRootPane().getScene();
		scene.getStylesheets().clear();

		if (toggleButton.isSelected()) {
			scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
			toggleButton.setText("Light Theme");
		} else {
			scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
			toggleButton.setText("Dark Theme");
		}
	}

	private void checkWinCondition() {
		if (board.isBoardSolved()) {
			showVictoryAlert();
		}
	}

	private void showVictoryAlert() {
		timeline.stop();

		long endTime = System.currentTimeMillis();
		long durationMillis = endTime - startTime;

		String timeFormatted = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(durationMillis),
				TimeUnit.MILLISECONDS.toSeconds(durationMillis) -
						TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(durationMillis))
		);

		Platform.runLater(() -> {
			Dialog<ButtonType> dialog = new Dialog<>();
			dialog.initModality(Modality.APPLICATION_MODAL);
			dialog.setTitle("Congratulations!");

			VBox content = new VBox(15);
			content.setAlignment(Pos.CENTER);

			try {
				Image trophyImage = new Image(getClass().getResourceAsStream("/images/trophy.png"));
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

			DialogPane dialogPane = dialog.getDialogPane();
			dialogPane.setContent(content);
			dialogPane.getStyleClass().add("win-dialog");

			Scene scene = view.getRootPane().getScene();

			if (scene != null) {
				dialogPane.getStylesheets().addAll(scene.getStylesheets());
			}

			ButtonType newGameButtonType = new ButtonType("New Game (Same Difficulty)");
			ButtonType mainMenuButtonType = new ButtonType("Main Menu");

			dialog.getDialogPane().getButtonTypes().addAll(newGameButtonType, mainMenuButtonType);

			final Button newGameButton = (Button) dialog.getDialogPane().lookupButton(newGameButtonType);
			newGameButton.getStyleClass().add("control-button");

			final Button mainMenuButton = (Button) dialog.getDialogPane().lookupButton(mainMenuButtonType);
			mainMenuButton.getStyleClass().add("control-button");

			Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
			stage.setOnCloseRequest(event -> {
			});

			Optional<ButtonType> result = dialog.showAndWait();

			if (result.isPresent()) {
				if (result.get() == newGameButtonType) {
					handleNewGameButton();
				} else if (result.get() == mainMenuButtonType) {
					handleBackToMenuButton();
				}
			} else {
				handleBackToMenuButton();
			}
		});
	}

	private void updateViewFromModel() {
		TextField[][] cellFields = view.getCellFields();

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				Cell cell = board.getCell(row, col);
				TextField cellField = cellFields[row][col];

				cellField.getStyleClass().removeAll("sudoku-cell-fixed", "sudoku-cell-error");

				if (cell.getValue() != 0) {
					cellField.setText(String.valueOf(cell.getValue()));

					if (cell.isFixed()) {
						cellField.getStyleClass().add("sudoku-cell-fixed");
						cellField.setEditable(false);
					} else {
						cellField.setEditable(true);
					}
				} else {
					cellField.clear();
					cellField.setEditable(true);;
				}
			}
		}
		updateNumpadState();
	}

	private void updateErrorHighlightingInView() {
		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				view.getCellFields()[row][col].getStyleClass().remove("sudoku-cell-error");

				if (board.getCell(row, col).isHasError()) {
					view.getCellFields()[row][col].getStyleClass().add("sudoku-cell-error");
				}
			}
		}
	}

	private void clearAllViewAndState() {
		moveHistory.clear();
		selectedCellField = null;

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				TextField cellField = view.getCellFields()[row][col];
				cellField.clear();
				cellField.getStyleClass().removeAll("sudoku-cell-fixed", "sudoku-cell-error");
				cellField.setEditable(true);
			}
		}
	}

	private void updateModelFromView() {
		TextField[][] cellFields = view.getCellFields();

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				Cell cell = board.getCell(row, col);

				if (!cell.isFixed()) {
					String text = cellFields[row][col].getText();

					if (text.matches("[1-9]")) {
						cell.setValue(Integer.parseInt(text));
					} else {
						cell.setValue(0);
					}
				}
			}
		}
	}

	private record Point(int row, int col) {}

	private Optional<Point> findSelectedCellCoords(TextField textField) {
		if (textField == null) return Optional.empty();

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				if (view.getCellFields()[row][col] == textField) {
					return Optional.of(new Point(row, col));
				}
			}
		}

		return Optional.empty();
	}
}
