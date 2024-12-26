package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class Main extends Application {
    // Bank of words and hints
    private final String[][] words = {
            {"PROGRAMMING", "Something you do to write software"},
            {"JAVA", "A popular programming language"},
            {"COMPUTER", "A device used to perform calculations"},
            {"SOFTWARE", "Non-physical part of a computer"},
            {"ENGINEER", "A person who builds or designs things"},
            {"DEVELOPER", "Someone who creates software applications"},
            {"PYTHON", "A programming language named after a snake"},
            {"DATABASE", "Used to store and retrieve data"},
            {"ALGORITHM", "A step-by-step procedure to solve problems"},
            {"DEBUGGING", "The process of finding and fixing errors in code"},
            {"NETWORK", "A group of interconnected computers"},
            {"SECURITY", "Protection of data and systems"},
            {"ENCRYPTION", "Converting data into unreadable form"},
            {"HARDWARE", "The physical parts of a computer"},
            {"CLOUD", "Internet-based computing services"},
            {"ARTIFICIAL", "Something not natural"},
            {"INTELLIGENCE", "The ability to learn and think"},
            {"LANGUAGE", "Used to communicate instructions to computers"},
            {"OPERATOR", "A symbol that performs operations"},
            {"VIRTUAL", "Not physically existing but simulated"},
            {"FUNCTION", "A block of code that performs a specific task"},
            {"VARIABLE", "A storage location for values in programming"},
            {"OBJECT", "An instance of a class in OOP"},
            {"CLASS", "A blueprint for creating objects"},
            {"LOOP", "Repeats a block of code multiple times"},
            {"CONDITIONAL", "Used to perform decisions in programming"},
            {"COMPILER", "Translates code into machine language"},
            {"INTERPRETER", "Executes code line by line"},
            {"ARRAY", "A collection of elements stored in a sequence"},
            {"STRING", "A sequence of characters in programming"},
            {"STACK", "A data structure that follows LIFO order"},
            {"QUEUE", "A data structure that follows FIFO order"},
            {"RECURSION", "A function that calls itself"},
            {"INTERFACE", "A contract in programming for implementing methods"},
            {"FRAMEWORK", "A collection of tools and libraries for development"},
            {"LIBRARY", "A collection of pre-written code for reuse"},
            {"SYNTAX", "The set of rules for writing code"},
            {"BOOLEAN", "A data type with true or false values"},
            {"INTEGER", "A data type representing whole numbers"},
            {"FLOAT", "A data type representing decimal numbers"}
    };

    private String wordToGuess; // Current word to guess
    private String hint; // Hint for the current word
    private StringBuilder currentGuess; // Current progress of guessed word
    private int heartsLeft = 6; // Number of hearts remaining
    private Set<String> usedWords = new HashSet<>(); // Track words already used
    private List<String> guessedWords = new ArrayList<>(); // Track all guessed words
    private Map<String, String> gameResults = new HashMap<>(); // Track guessed words and their status

    private Label wordLabel = new Label(); // Displays the word
    private Label heartsLabel = new Label(); // Displays hearts
    private Label messageLabel = new Label(); // Displays win/lose messages
    private TextField inputField = new TextField(); // Input field for guesses
    private Label hintLabel = new Label(); // Displays the hint

    private String playerName;
    private int playerAge;
    private Map<String, Integer> levelStats = new HashMap<>(); // Track stats for each level
    private int wins = 0;
    private int losses = 0;

    @Override
    public void start(Stage primaryStage) {
        getPlayerDetails(primaryStage);
    }

    private void getPlayerDetails(Stage stage) {
        while (true) {
            TextInputDialog nameDialog = new TextInputDialog();
            nameDialog.setTitle("Player Details");
            nameDialog.setHeaderText("Enter your details");
            nameDialog.setContentText("Name:");

            Optional<String> nameInput = nameDialog.showAndWait();
            if (nameInput.isEmpty()) {
                exitGame(stage);
                return;
            }

            if (!nameInput.get().matches("[A-Za-z]+")) {
                showError("Enter a valid name (letters only).", stage);
                continue;
            }
            playerName = nameInput.get();
            break;
        }

        while (true) {
            TextInputDialog ageDialog = new TextInputDialog();
            ageDialog.setTitle("Player Details");
            ageDialog.setHeaderText("Enter your details");
            ageDialog.setContentText("Age:");

            Optional<String> ageInput = ageDialog.showAndWait();
            if (ageInput.isEmpty()) {
                exitGame(stage);
                return;
            }

            if (!ageInput.get().matches("\\d+") || Integer.parseInt(ageInput.get()) <= 0) {
                showError("Enter a valid age (positive numbers only).", stage);
                continue;
            }
            playerAge = Integer.parseInt(ageInput.get());
            break;
        }

        playGame(stage);
    }

    private void showError(String message, Stage stage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    private void exitGame(Stage stage) {
        Alert summaryAlert = new Alert(Alert.AlertType.INFORMATION);
        summaryAlert.setTitle("Game Summary");
        summaryAlert.setHeaderText("Thanks for playing, " + playerName + "!");

        StringBuilder summary = new StringBuilder("Game Summary:\n\n");
        for (String guessedWord : guessedWords) {
            summary.append(guessedWord).append("\n");
        }
        summary.append("\nWins: ").append(wins).append("\nLosses: ").append(losses);

        summaryAlert.setContentText(summary.toString());
        summaryAlert.showAndWait();

        stage.close();
    }

    private void playGame(Stage stage) {
        // Only select difficulty for the first game
        if (levelStats.isEmpty()) {
            String difficulty = selectDifficulty();
            levelStats.put(difficulty, levelStats.getOrDefault(difficulty, 0) + 1);
        } else {
            // Increment the level count each time the game is played
            String difficulty = levelStats.keySet().iterator().next(); // Get the current difficulty
            levelStats.put(difficulty, levelStats.getOrDefault(difficulty, 0) + 1);
        }

        // Set up the game with the selected difficulty level
        setupGame();

        // Style labels
        wordLabel.setText("Word: " + formatWord(currentGuess.toString()));
        wordLabel.setStyle("-fx-font-size: 20; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        heartsLabel.setText("Hearts: ♥♥♥♥♥♥");
        heartsLabel.setStyle("-fx-font-size: 18; -fx-text-fill: #e74c3c;");

        hintLabel.setText("Hint: " + hint);
        hintLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #16a085; -fx-font-style: italic;");

        messageLabel.setText("");

        // Style guess button
        Button guessButton = new Button("Guess");
        guessButton.setStyle(
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;");
        guessButton.setOnMouseEntered(e -> guessButton.setStyle(
            "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;"));
        guessButton.setOnMouseExited(e -> guessButton.setStyle(
            "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;"));
        guessButton.setOnAction(e -> handleGuess(stage));

        // Exit button
        Button exitButton = new Button("Exit");
        exitButton.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;");
        exitButton.setOnMouseEntered(e -> exitButton.setStyle(
            "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;"));
        exitButton.setOnMouseExited(e -> exitButton.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;"));
        exitButton.setOnAction(e -> showSummary(stage));

        // Add styles to layout
        HBox buttonsBox = new HBox(15, guessButton, exitButton);
        buttonsBox.setStyle("-fx-alignment: center;");

        VBox layout = new VBox(15, wordLabel, hintLabel, heartsLabel, inputField, buttonsBox, messageLabel);
        layout.setStyle(
            "-fx-padding: 30; -fx-alignment: center; " +
            "-fx-background-color: linear-gradient(to bottom, #f3e5f5, #e1bee7); " +
            "-fx-spacing: 20; -fx-border-color: #8e44ad; -fx-border-width: 3; -fx-border-radius: 10;");
        layout.setPrefWidth(400);

        Scene scene = new Scene(layout, 450, 400);
        stage.setTitle("Word Guessing Game");
        stage.setScene(scene);
        stage.show();
    }

    private void setupGame() {
        Random random = new Random();

        // Select a random word that hasn't been used yet
        String[] selectedWord;
        do {
            selectedWord = words[random.nextInt(words.length)];
        } while (usedWords.contains(selectedWord[0]));

        wordToGuess = selectedWord[0];
        hint = selectedWord[1];
        usedWords.add(wordToGuess); // Mark the word as used

        // Determine pre-filled letters based on difficulty
        String difficulty = levelStats.keySet().iterator().next(); // Get the current difficulty
        int preFilledCount = switch (difficulty) {
            case "Easy" -> wordToGuess.length() / 2;
            case "Hard" -> 1;
            default -> wordToGuess.length() / 3;
        };

        // Initialize the current guess with blanks and pre-filled letters
        currentGuess = new StringBuilder("_".repeat(wordToGuess.length()));
        for (int i = 0; i < preFilledCount; i++) {
            int randomIndex;
            do {
                randomIndex = random.nextInt(wordToGuess.length());
            } while (currentGuess.charAt(randomIndex) != '_');
            currentGuess.setCharAt(randomIndex, wordToGuess.charAt(randomIndex));
        }

        heartsLeft = 6; // Reset hearts for a new game
    }


    private String selectDifficulty() {
        ChoiceDialog<String> dialog = new ChoiceDialog<>("Normal", "Easy", "Normal", "Hard");
        dialog.setTitle("Select Difficulty");
        dialog.setHeaderText("Choose a difficulty level");
        dialog.setContentText("Difficulty:");
        return dialog.showAndWait().orElse("Normal");
    }

    private void handleGuess(Stage stage) {
        String input = inputField.getText().toUpperCase();
        inputField.clear();

        boolean correctGuess = false;

        if (input.length() == 1) {
            for (int i = 0; i < wordToGuess.length(); i++) {
                if (wordToGuess.charAt(i) == input.charAt(0) && currentGuess.charAt(i) == '_') {
                    currentGuess.setCharAt(i, input.charAt(0));
                    correctGuess = true;
                }
            }
            if (!correctGuess) {
                heartsLeft--; // Decrease hearts for incorrect letter guesses
            }
        } else if (input.equals(wordToGuess)) {
            currentGuess = new StringBuilder(wordToGuess);
            correctGuess = true;
        } else {
            heartsLeft--; // Decrease hearts for incorrect word guesses
        }

        updateHeartsLabel();
        wordLabel.setText("Word: " + formatWord(currentGuess.toString()));

        if (currentGuess.toString().equals(wordToGuess)) {
            messageLabel.setText("Correct! Moving to the next word...");
            messageLabel.setStyle("-fx-text-fill: green; -fx-font-size: 16;");
            wins++;
            guessedWords.add(wordToGuess + " (Correct)");

            if (usedWords.size() == words.length) {
                showSummary(stage); // All words guessed, show summary
            } else {
                playGame(stage); // Start the next word
            }
        } else if (heartsLeft == 0) {
            messageLabel.setText("You Lose! The word was: " + wordToGuess);
            messageLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16;");
            losses++;
            guessedWords.add(wordToGuess + " (Incorrect)");
            showSummary(stage); // Show game summary and exit
        }
    }

    private void updateHeartsLabel() {
        // Directly set the text for hearts without extra spacing
        String hearts = "♥".repeat(heartsLeft);
        heartsLabel.setText("Hearts: " + hearts);

        // Optional: Apply styling for compact display
        heartsLabel.setStyle("-fx-font-size: 16; -fx-text-fill: red; -fx-padding: 0; -fx-spacing: 0;");
    }


    private String formatWord(String word) {
        return word.replace("", " ").trim(); // Add spaces between letters
    }

    private void showSummary(Stage stage) {
        VBox summaryLayout = new VBox(15);
        summaryLayout.setStyle(
            "-fx-padding: 30; -fx-alignment: center; -fx-background-color: linear-gradient(to bottom, #e8f6f3, #d1f2eb); " +
            "-fx-spacing: 20; -fx-border-color: #16a085; -fx-border-width: 3; -fx-border-radius: 10;");

        Label headerLabel = new Label("Game Summary");
        headerLabel.setStyle("-fx-font-size: 24; -fx-font-weight: bold; -fx-text-fill: #34495e;");

        StringBuilder summary = new StringBuilder();
        summary.append("Player: ").append(playerName).append("\n");
        summary.append("Age: ").append(playerAge).append("\n\n");
        summary.append("Wins: ").append(wins).append("\n");
        summary.append("Losses: ").append(losses).append("\n\n");
        summary.append("Levels Played:\n");

        for (Map.Entry<String, Integer> entry : levelStats.entrySet()) {
            summary.append("- ").append(entry.getKey()).append("\n");
        }

        summary.append("\nGuessed Words:\n");
        for (String guessedWord : guessedWords) {
            summary.append("- ").append(guessedWord).append("\n");
        }

        Label summaryLabel = new Label(summary.toString());
        summaryLabel.setStyle("-fx-font-size: 16; -fx-text-fill: #2c3e50; -fx-wrap-text: true;");

        Button playAgainButton = new Button("Play Again");
        playAgainButton.setStyle(
            "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;");
        playAgainButton.setOnAction(e -> {
            stage.close();
            levelStats.clear();
            playGame(stage);
        });

        Button exitButton = new Button("Exit");
        exitButton.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 16; " +
            "-fx-padding: 10 20; -fx-border-radius: 10; -fx-background-radius: 10;");
        exitButton.setOnAction(e -> stage.close());

        HBox buttonsBox = new HBox(15, playAgainButton, exitButton);
        buttonsBox.setStyle("-fx-alignment: center;");

        summaryLayout.getChildren().addAll(headerLabel, summaryLabel, buttonsBox);

        Scene summaryScene = new Scene(summaryLayout, 500, javafx.scene.layout.Region.USE_COMPUTED_SIZE);
        stage.setTitle("Game Summary");
        stage.setScene(summaryScene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}