# Sudoku Game 🧩

A classic Sudoku game built with modern Java technologies. This project features a clean user interface, multiple difficulty levels, and is packaged as a standalone native application using GraalVM.

<!-- ![Sudoku Gameplay GIF](https-placeholder-for-your-gif.gif) ] -->

## ✨ Features

* **Multiple Difficulty Levels:** Choose between Easy, Medium, and Hard puzzles.
* **Light & Dark Themes:** Switch between themes for comfortable viewing.
* **Interactive Number Pad:** Easily input numbers into the grid.
* **Real-time Error Highlighting:** Instantly see conflicting numbers.
* **Undo Functionality:** Rewind your moves.
* **Game Timer:** Track how long it takes to solve the puzzle.
* **Standalone Application:** Packaged with GraalVM for a fast, native experience with no need to install Java.

## 🚀 Download and Play

Get the latest version for Windows right here. No installation needed—just run `sudoku.exe`.

[![Download Sudoku](https://img.shields.io/badge/Download-v1.0.0-blue?style=for-the-badge&logo=windows)](https://github.com/BenedettiSaulo/sudoku-game/releases/download/v1.1.0/sudoku.exe)

## 🕹️ How to Play

1.  **Select a Cell:** Click on any cell in the grid.
2.  **Enter a Number:** Use the number pad at the bottom or your keyboard to enter a digit (1-9).
3.  **Clear a Cell:** Select a cell and click the "Clear" button or press the Backspace/Delete key.
4.  **Undo a Move:** Click the "Undo" button to revert your last action.
5.  **Complete the Grid:** Fill all empty cells correctly to win the game!

## 🛠️ Built With

* **Java 21:** The core programming language.
* **JavaFX:** The framework for the graphical user interface.
* **Maven:** The build and dependency management tool.
* **GraalVM Native Image:** For compiling the Java application into a native executable.
* **GluonFX Plugin:** To simplify the GraalVM native build process for JavaFX.

## ⚙️ Building from Source

If you want to build the project yourself, you'll need the following prerequisites:

* **GraalVM for JDK 21:** [Download here](https://www.graalvm.org/downloads/)
* **Maven 3.8+**
* **Visual Studio Build Tools:** With the "Desktop development with C++" workload installed.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/BenedettiSaulo/sudoku-game.git
    cd sudoku-game
    ```

2.  **Run in Development Mode (Optional):**
    To run the application without creating a native executable:
    ```bash
    mvn gluonfx:run
    ```

3.  **Build the Native Executable:**
    (Make sure you run this from a **Developer Command Prompt for Visual Studio**)
    ```bash
    mvn clean gluonfx:build
    ```

4.  **Find the output:**
    The final application folder will be in `target/gluonfx/x86_64-windows/`.
