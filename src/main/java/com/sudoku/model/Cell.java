package com.sudoku.model;

public class Cell {

	private int value;
	private boolean isFixed;
	private boolean hasError;

	public Cell(int value, boolean isFixed) {
		this.value = value;
		this.isFixed = isFixed;
		this.hasError = false;
	}

	public int getValue() {
		return value;
	}

	public boolean isFixed() {
		return isFixed;
	}

	public boolean isHasError() {
		return hasError;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public void setFixed(boolean fixed) {
		isFixed = fixed;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}
}
