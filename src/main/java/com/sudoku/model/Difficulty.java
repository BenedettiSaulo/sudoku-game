package com.sudoku.model;

public enum Difficulty {
	EASY("Easy", 35),
	MEDIUM("Medium", 45),
	HARD("Hard", 55);

	private final String displayName;
	private final int holesToPoke;

	Difficulty(String displayName, int holesToPoke) {
		this.displayName = displayName;
		this.holesToPoke = holesToPoke;
	}

	public int getHolesToPoke() {
		return holesToPoke;
	}

	@Override
	public String toString() {
		return displayName;
	}
}
