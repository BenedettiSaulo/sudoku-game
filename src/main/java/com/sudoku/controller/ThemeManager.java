package com.sudoku.controller;

import javafx.scene.Scene;

public class ThemeManager {
	private final Scene scene;

	public ThemeManager(Scene scene) {
		this.scene = scene;
	}

	public void applyLightTheme() {
		scene.getStylesheets().clear();
		scene.getStylesheets().add(getClass().getResource("/css/light-theme.css").toExternalForm());
	}

	public void applyDarkTheme() {
		scene.getStylesheets().clear();
		scene.getStylesheets().add(getClass().getResource("/css/dark-theme.css").toExternalForm());
	}
}
