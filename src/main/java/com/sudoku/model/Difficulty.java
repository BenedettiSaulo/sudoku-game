package com.sudoku.model;

public enum Difficulty {
	EASY("Easy"),
	MEDIUM("Medium"),
	HARD("Hard");

	private final String displayName;

	Difficulty(String displayName) {
		this.displayName = displayName;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
