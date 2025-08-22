package com.sudoku.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.util.concurrent.TimeUnit;

public class GameTimer {
	private final Label timeLabel;
	private Timeline timeline;
	private long startTime;

	public GameTimer(Label timeLabel) {
		this.timeLabel = timeLabel;
		setupTimeline();
	}

	private void setupTimeline() {
		timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateLabel()));
		timeline.setCycleCount(Timeline.INDEFINITE);
	}

	public void start() {
		startTime = System.currentTimeMillis();
		timeline.play();
	}

	public void stop() {
		timeline.stop();
	}

	public void reset() {
		timeline.stop();
		timeLabel.setText("Time: 00:00");
	}

	private void updateLabel() {
		long elapsedTime = System.currentTimeMillis() - startTime;

		String timeFormatted = String.format("%02d:%02d",
				TimeUnit.MILLISECONDS.toMinutes(elapsedTime),
				TimeUnit.MILLISECONDS.toSeconds(elapsedTime) % 60
		);

		timeLabel.setText("Time: " + timeFormatted);
	}

	public long getStartTime() {
		return startTime;
	}

	public long getElapsedTimeMillis() {
		return (startTime == 0) ? 0 : System.currentTimeMillis() - startTime;
	}
}
