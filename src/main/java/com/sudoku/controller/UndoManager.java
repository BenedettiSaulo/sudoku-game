package com.sudoku.controller;

import javafx.scene.Scene;

import java.util.Optional;
import java.util.Stack;

public class UndoManager {
	public record Move(int row, int col, int oldValue, int newValue) {}
	private final Stack<Move> moveHistory = new Stack<>();

	public void addMove(int row, int col, int oldValue, int newValue) {
		moveHistory.push(new Move(row, col, oldValue, newValue));
	}

	public Optional<Move> undoLastMove() {
		if (moveHistory.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(moveHistory.pop());
	}

	public void clearHistory() {
		moveHistory.clear();
	}
}
