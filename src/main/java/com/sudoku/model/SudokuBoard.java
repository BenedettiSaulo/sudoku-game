package com.sudoku.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SudokuBoard {

	private final Cell[][] board;
	private final int[][] solution;
	public static final int SIZE = 9;
	public static final int SUBGRID_SIZE = 3;

	public SudokuBoard() {
		board = new Cell[SIZE][SIZE];
		solution = new int[SIZE][SIZE];

		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				board[row][col] = new Cell(0, false);
			}
		}
	}

	public Cell getCell(int row, int col) {
		return board[row][col];
	}

	public void generateNewBoard(Difficulty difficulty) {
		resetBoard();
		fillBoard();
		pokeHoles(difficulty);
	}

	public void clearUserNumbers() {
		System.out.println("Clearing user numbers... ");
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				Cell cell = board[row][col];
				if (!cell.isFixed()) {
					cell.setValue(0);
				}
			}
		}
	}

	public boolean isBoardSolved() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col].getValue() != solution[row][col]) {
					return false;
				}
			}
		}
		return isBoardFull();
	}

	private boolean isBoardFull() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col].getValue() == 0) {
					return false;
				}
			}
		}
		return true;
	}

	public void validateBoard() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				board[row][col].setHasError(false);
			}
		}

		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				int value = board[row][col].getValue();
				if (value != 0 && !isMoveValid(row, col, value, true)) {
					board[row][col].setHasError(true);
				}
			}
		}
	}

	private void resetBoard() {
		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				board[row][col].setValue(0);
				board[row][col].setFixed(false);
				board[row][col].setHasError(false);
			}
		}
	}

	private boolean fillBoard() {
		List<Integer> numbers = new ArrayList<>();
		for (int i = 1; i <= SIZE; i++) {
			numbers.add(i);
		}

		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col].getValue() == 0) {
					Collections.shuffle(numbers);
					for (int number : numbers) {
						if (isMoveValid(row, col, number, false)) {
							board[row][col].setValue(number);
							if (fillBoard()) {
								return true;
							}
							board[row][col].setValue(0);
						}
					}
					return false;
				}
			}
		}

		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				solution[row][col] = board[row][col].getValue();
			}
		}
		return true;
	}

	private void pokeHoles(Difficulty difficulty) {
		List<Integer> indices = new ArrayList<>();
		for (int i = 0; i < SIZE * SIZE; i++) {
			indices.add(i);
		}
		Collections.shuffle(indices);

		for (int i = 0; i < difficulty.getHolesToPoke(); i++) {
			int index = indices.get(i);
			board[index / SIZE][index % SIZE].setValue(0);
		}

		for (int row = 0; row < SIZE; row++) {
			for (int col = 0; col < SIZE; col++) {
				if (board[row][col].getValue() != 0) {
					board[row][col].setFixed(true);
				}
			}
		}
	}

	private boolean isMoveValid(int row, int col, int number, boolean checkForSelf) {
		for (int i = 0; i < SIZE; i++) {
			if (checkForSelf && i == col) continue;
			if (board[row][i].getValue() == number) return false;
		}

		for (int i = 0; i < SIZE; i++) {
			if (checkForSelf && i == row) continue;
			if (board[i][col].getValue() == number) return false;
		}

		int startRow = row - row % SUBGRID_SIZE;
		int startCol = col - col % SUBGRID_SIZE;

		for (int i = 0; i < SUBGRID_SIZE; i++) {
			for (int j = 0; j < SUBGRID_SIZE; j++) {
				if (checkForSelf && (startRow + i) == row && (startCol + j) == col) continue;
				if (board[startRow + i][startCol + j].getValue() == number) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean isSubgridComplete(int startRow, int startCol) {
		for (int row = startRow; row < startRow + SUBGRID_SIZE; row++) {
			for (int col = startCol; col < startCol + SUBGRID_SIZE; col++) {
				if (board[row][col].getValue() == 0 || board[row][col].getValue() != solution[row][col]) {
					return false;
				}
			}
		}
		return true;
	}
}
