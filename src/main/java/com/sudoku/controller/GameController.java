package com.sudoku.controller;

import com.sudoku.Main;
import com.sudoku.model.Cell;
import com.sudoku.model.Difficulty;
import com.sudoku.model.SudokuBoard;
import com.sudoku.view.AlertFactory;
import com.sudoku.view.SudokuView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.util.Optional;

public class GameController {

	private final UndoManager undoManager;
	private final GameTimer gameTimer;

	private enum UpdateState { USER_ACTION, UNDO_ACTION, RESET_ACTION }
	private UpdateState currentState = UpdateState.USER_ACTION;

	private TextField selectedCellField = null;
	private Point selectedCellCoords = null;

	private static final PseudoClass HIGHLIGHTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("highlighted");
	private final boolean[][] subgridCompletionState = new boolean[SudokuBoard.SUBGRID_SIZE][SudokuBoard.SUBGRID_SIZE];
	private final SudokuBoard board;
	private final SudokuView view;
	private final Difficulty difficulty;

	private final Main mainApp;

	public GameController(Main mainApp, SudokuBoard board, SudokuView view, Difficulty difficulty) {
		this.mainApp = mainApp;
		this.board = board;
		this.view = view;
		this.difficulty = difficulty;

		this.undoManager = new UndoManager();
		this.gameTimer = new GameTimer(view.getTimeLabel());

		addEventHandlers();
		startNewGame(difficulty);
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

		Platform.runLater(() ->
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
			})
		);

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				TextField cellField = view.getCellFields()[row][col];
				final int finalRow = row;
				final int finalCol = col;

				cellField.textProperty().addListener((observable, oldText, newText) -> handleTextChange(finalRow, finalCol, cellField, oldText, newText));
			}
		}
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
			undoManager.addMove(row, col, oldValueInt, newValueInt);
		}

		updateModelFromView();
		board.validateBoard();
		updateErrorHighlightingInView();
		updateCellHighlighting();
		updateSubgridHighlighting();
		checkWinCondition();
	}

	private void updateCellHighlighting() {
		if (selectedCellField == null) {
			for (int row = 0; row < SudokuBoard.SIZE; row++) {
				for (int col = 0; col < SudokuBoard.SIZE; col++) {
					view.getCellFields()[row][col].pseudoClassStateChanged(HIGHLIGHTED_PSEUDO_CLASS, false);
				}
			}
			return;
		}

		int selectedRow = selectedCellCoords.row();
		int selectedCol = selectedCellCoords.col();

		int subgridStartRow = selectedRow - selectedRow % SudokuBoard.SUBGRID_SIZE;
		int subgridStartCol = selectedCol - selectedCol % SudokuBoard.SUBGRID_SIZE;

		if (board.isSubgridComplete(subgridStartRow, subgridStartCol)) {
			for (int r = 0; r < SudokuBoard.SIZE; r++) {
				for (int c = 0; c < SudokuBoard.SIZE; c++) {
					view.getCellFields()[r][c].pseudoClassStateChanged(HIGHLIGHTED_PSEUDO_CLASS, false);
				}
			}
			return;
		}

		for (int row = 0; row < SudokuBoard.SIZE; row++) {
			for (int col = 0; col < SudokuBoard.SIZE; col++) {
				boolean highlightRow = (row == selectedRow);
				boolean highlightCol = (col == selectedCol);

				boolean highlightSubgrid = (row >= subgridStartRow && row < subgridStartRow + SudokuBoard.SUBGRID_SIZE &&
						col >= subgridStartCol && col < subgridStartCol + SudokuBoard.SUBGRID_SIZE);

				boolean shouldHighlight = highlightRow || highlightCol || highlightSubgrid;
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

				boolean isNowComplete = board.isSubgridComplete(startRow, startCol);
				boolean wasAlreadyComplete = subgridCompletionState[subgridRow][subgridCol];

				if (isNowComplete && !wasAlreadyComplete) {
					Glow glow = new Glow();
					subGridPane.setEffect(glow);

					Timeline timeline = new Timeline(
							new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.0)),
							new KeyFrame(Duration.millis(500), new KeyValue(glow.levelProperty(), 0.7)),
							new KeyFrame(Duration.millis(1500), new KeyValue(glow.levelProperty(), 0.0))
					);

					timeline.setOnFinished(e -> subGridPane.setEffect(null));
					timeline.play();
				}

				if (isNowComplete) {
					if (!subGridPane.getStyleClass().contains("sub-grid-complete")) {
						subGridPane.getStyleClass().add("sub-grid-complete");
					}
				} else {
					subGridPane.getStyleClass().remove("sub-grid-complete");
				}

				subgridCompletionState[subgridRow][subgridCol] = isNowComplete;
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
		resetSubgridCompletionState();

		gameTimer.reset();
		gameTimer.start();

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
		resetSubgridCompletionState();
		gameTimer.reset();
		gameTimer.start();
		undoManager.clearHistory();
		updateNumpadState();
		board.validateBoard();
		updateErrorHighlightingInView();
		currentState = UpdateState.USER_ACTION;
	}

	private void handleUndoButton() {
		undoManager.undoLastMove().ifPresent(lastMove -> {
			currentState = UpdateState.UNDO_ACTION;
			TextField cellToUndo = view.getCellFields()[lastMove.row()][lastMove.col()];
			String previousValue = lastMove.oldValue() == 0 ? "" : String.valueOf(lastMove.oldValue());
			cellToUndo.setText(previousValue);
			currentState = UpdateState.USER_ACTION;
			updateModelFromView();
			board.validateBoard();
			updateErrorHighlightingInView();
		});
	}

	private void handleClearButton() {
		if (selectedCellField != null && selectedCellField.isEditable()) {
			selectedCellField.setText("");
			selectedCellField.requestFocus();
		}
	}

	private void handleBackToMenuButton() {
		gameTimer.stop();
		mainApp.showStartMenu();
	}

	private void handleThemeToggle() {
		ToggleButton toggleButton = view.getThemeToggleButton();

		ThemeManager themeManager = new ThemeManager(view.getRootPane().getScene());

		if (toggleButton.isSelected()) {
			themeManager.applyDarkTheme();
			toggleButton.setText("Light Theme");
		} else {
			themeManager.applyLightTheme();
			toggleButton.setText("Dark Theme");
		}
	}

	private void checkWinCondition() {
		if (board.isBoardSolved()) {
			gameTimer.stop();
			long durationMillis = gameTimer.getElapsedTimeMillis();

			Optional<ButtonType> result = AlertFactory.showVictoryAlert(view.getRootPane().getScene(), durationMillis);

			result.ifPresent(buttonType -> {
				if (buttonType.getText().equals("New Game (Same Difficulty)")) {
					handleNewGameButton();
				} else if (buttonType.getText().equals("Main Menu")) {
					handleBackToMenuButton();
				}
			});
		}
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
					cellField.setEditable(true);
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
		undoManager.clearHistory();
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

	private void resetSubgridCompletionState() {
		for (int subgridRow = 0; subgridRow < SudokuBoard.SUBGRID_SIZE; subgridRow++) {
			for (int subgridCol = 0; subgridCol < SudokuBoard.SUBGRID_SIZE; subgridCol++) {
				subgridCompletionState[subgridRow][subgridCol] = false;
			}
		}
	}
}
