package com.sudoku.view;

import com.sudoku.model.SudokuBoard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;

public class SudokuView {

	private final BorderPane rootPane;
	private final TextField[][] cellFields;
	private Button undoButton;
	private Button clearButton;
	private Button newGameButton;
	private Button backToMenuButton;
	private Button restartButton;
	private ToggleButton themeToggleButton;
	private ToggleGroup numberToggleGroup;
	private final List<ToggleButton> numberButtons;
	private final GridPane[][] subGrids;
	private Label timeLabel;

	public SudokuView(int size, int subGridSize) {
		rootPane = new BorderPane();
		rootPane.getStyleClass().add("root");
		rootPane.setPadding(new Insets(10));

		cellFields = new TextField[size][size];
		subGrids = new GridPane[subGridSize][subGridSize];
		numberButtons = new ArrayList<>();

		HBox topToolbar = createTopToolbar();
		GridPane boardPanel = createBoardPanel(subGridSize);
		VBox bottomActionPanel = createBottomActionPanel(size);

		rootPane.setTop(topToolbar);
		rootPane.setCenter(boardPanel);
		rootPane.setBottom(bottomActionPanel);

		BorderPane.setAlignment(topToolbar, Pos.CENTER);
		BorderPane.setMargin(bottomActionPanel, new Insets(10, 0, 0, 0));
	}

	public BorderPane getRootPane() {
		return rootPane;
	}

	private HBox createTopToolbar() {
		HBox toolbar = new HBox(15);
		toolbar.setAlignment(Pos.CENTER);
		toolbar.setPadding(new Insets(0, 10, 10, 10));

		HBox actionButtons = new HBox(10);
		actionButtons.setAlignment(Pos.CENTER_LEFT);

		newGameButton = new Button("New Game");
		restartButton = new Button("Restart Game");
		backToMenuButton = new Button("Back to Menu");

		newGameButton.getStyleClass().add("control-button");
		restartButton.getStyleClass().add("control-button");
		backToMenuButton.getStyleClass().add("control-button");

		actionButtons.getChildren().addAll(newGameButton, restartButton, backToMenuButton);

		Pane spacer = new Pane();
		HBox.setHgrow(spacer, Priority.ALWAYS);

		HBox rightControls = new HBox(20);
		rightControls.setAlignment(Pos.CENTER);

		themeToggleButton = new ToggleButton("Dark Mode");
		themeToggleButton.getStyleClass().add("toggle-button");

		timeLabel = new Label("Time: 00:00");
		timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

		rightControls.getChildren().addAll(themeToggleButton, timeLabel);

		toolbar.getChildren().addAll(actionButtons, spacer, rightControls);

		return toolbar;
	}

	private GridPane createBoardPanel(int subGridSize) {
		GridPane masterBoardPanel = new GridPane();
		masterBoardPanel.setAlignment(Pos.CENTER);

		for (int subGridRow = 0; subGridRow < subGridSize; subGridRow++) {
			for (int subGridCol = 0; subGridCol < subGridSize; subGridCol++) {
				GridPane subGrid = new GridPane();
				subGrid.getStyleClass().add("sub-grid");

				subGrids[subGridRow][subGridCol] = subGrid;

				for (int rowInSubGrid = 0; rowInSubGrid < subGridSize; rowInSubGrid++) {
					for (int colInSubGrid = 0; colInSubGrid < subGridSize; colInSubGrid++) {
						int globalRow = subGridRow * subGridSize + rowInSubGrid;
						int globalCol = subGridCol * subGridSize + colInSubGrid;

						TextField cell = new TextField();
						cell.getStyleClass().add("sudoku-cell");
						cell.setPrefSize(50, 50);

						cellFields[globalRow][globalCol] = cell;

						subGrid.add(cell, colInSubGrid, rowInSubGrid);
					}
				}
				masterBoardPanel.add(subGrid, subGridCol, subGridRow);
			}
		}
		return masterBoardPanel;
	}

	private VBox createBottomActionPanel(int size) {
		VBox bottomPanel = new VBox(15);
		bottomPanel.setAlignment(Pos.CENTER);

		HBox actionButtonBox = new HBox(10);
		actionButtonBox.setAlignment(Pos.CENTER);

		undoButton = new Button("Undo");
		undoButton.getStyleClass().add("control-button");
		undoButton.setFocusTraversable(false);

		clearButton = new Button("Clear");
		clearButton.getStyleClass().add("control-button");
		clearButton.setFocusTraversable(false);

		actionButtonBox.getChildren().addAll(undoButton, clearButton);

		GridPane numberPad = new GridPane();
		numberPad.setAlignment(Pos.CENTER);
		numberPad.setHgap(10);
		numberPad.setVgap(10);

		numberToggleGroup = new ToggleGroup();

		for (int i = 1; i <= size; i++) {
			ToggleButton numberButton = new ToggleButton(String.valueOf(i));
			numberButton.getStyleClass().add("number-button");
			numberButton.setToggleGroup(numberToggleGroup);
			numberButton.setPrefSize(50, 50);
			numberButton.setFocusTraversable(false);

			numberButtons.add(numberButton);

			numberPad.add(numberButton, (i - 1) % 3, (i - 1) / 3);
		}

		bottomPanel.getChildren().addAll(actionButtonBox, numberPad);

		return bottomPanel;
	}

	public TextField[][] getCellFields() { return cellFields; }
	public Button getUndoButton() { return undoButton; }
	public Button getClearButton() { return clearButton; }
	public Button getNewGameButton() { return newGameButton; }
	public ToggleGroup getNumberToggleGroup() { return numberToggleGroup; }
	public Button getBackToMenuButton() { return backToMenuButton; }
	public Button getRestartButton() { return restartButton; }
	public List<ToggleButton> getNumberButtons() { return numberButtons; }
	public Label getTimeLabel() { return timeLabel; }
	public GridPane[][] getSubGrids() { return subGrids; }
	public ToggleButton getThemeToggleButton() { return themeToggleButton; }
}
