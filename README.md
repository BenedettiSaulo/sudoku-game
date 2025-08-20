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

Get the latest version for Windows right here. No installation needed—just unzip and run `sudoku.exe`.

<!-- [ [![Download Sudoku](https://img.shields.io/badge/Download-v1.0.0-blue?style=for-the-badge&logo=windows)](https://github.com/YourUsername/YourRepo/releases/download/v1.0.0/sudoku-v1.0-windows.zip) ] -->

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

**Build Steps:**

1.  **Clone the repository:**
    ```bash
    git clone [https://github.com/YourUsername/YourRepo.git](https://github.com/YourUsername/YourRepo.git)
    cd YourRepo
    ```

2.  **Run the build command:**
    (Make sure you run this from a **Developer Command Prompt for Visual Studio**)
    ```bash
    mvn clean gluonfx:build gluonfx:package
    ```

3.  **Find the output:**
    The final application folder will be in `target/gluonfx/x86_64-windows/`.

---
*This project was created with the help of Coding Partner.*
