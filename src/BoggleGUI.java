import javax.swing.BorderFactory; // Imports tools for creating borders and spacing.
import javax.swing.Box; // Imports Box helpers for fixed empty space in layouts.
import javax.swing.BoxLayout; // Imports a layout that stacks components in one direction.
import javax.swing.JButton; // Imports clickable buttons.
import javax.swing.JComboBox; // Imports dropdown boxes.
import javax.swing.JFrame; // Imports the main application window class.
import javax.swing.JLabel; // Imports text display labels.
import javax.swing.JOptionPane; // Imports popup message and confirm dialogs.
import javax.swing.JPanel; // Imports containers that hold GUI components.
import javax.swing.JScrollPane; // Imports scrollable containers.
import javax.swing.JTextArea; // Imports multi-line text boxes.
import javax.swing.JTextField; // Imports single-line text boxes.
import javax.swing.SwingConstants; // Imports constants for label alignment.
import javax.swing.Timer; // Imports Swing timers for delayed or repeated actions.
import java.awt.BorderLayout; // Imports a layout with north, south, east, west, and center areas.
import java.awt.CardLayout; // Imports a layout that swaps between screens.
import java.awt.Color; // Imports color values for GUI styling.
import java.awt.Dimension; // Imports width and height size objects.
import java.awt.Font; // Imports font styling for labels.
import java.awt.GridLayout; // Imports a layout that arranges components in a grid.
import java.awt.event.ActionEvent; // Imports the event object passed to button listeners.
import java.awt.event.ActionListener; // Imports the interface used for button click code.
import java.io.File; // Imports file path objects.
import java.io.FileReader; // Imports a reader for loading text from files.
import java.util.ArrayList; // Imports a resizeable list.
import java.util.Scanner; // Imports a simple text/file reader.

/*
 * BoggleGUI is the visual version of the Boggle game.
 *
 * This class uses Java Swing. Swing programs are usually built from:
 * 1. A main window, called a JFrame.
 * 2. Smaller containers, called JPanels, that hold parts of the screen.
 * 3. Controls, such as JButtons, JTextFields, JComboBoxes, and JLabels.
 * 4. ActionListeners, which are small blocks of code that run after a user
 *    clicks a button, presses Enter, or changes a dropdown.
 *
 * The game logic still lives in BoggleGame and BoggleAI. This GUI class mostly
 * collects user choices, displays the board and scores, and calls the game
 * methods when the player takes an action.
 */
public class BoggleGUI { // Defines the GUI class for the Boggle program.
    /*
     * These fields are stored at the class level because many methods need to
     * use them later. For example, startGame() reads the setup text fields, and
     * updateBoard() changes labels that were created earlier in makeGamePanel().
     */

    // The main application window.
    public JFrame window; // Stores the main Boggle window.

    /*
     * CardLayout lets one JPanel act like a stack of screens.
     * This program has three cards: menu, setup, and game.
     */
    public CardLayout cards; // Stores the screen switcher for menu, setup, and game.
    public JPanel mainPanel; // Stores the panel that contains all screens.
    public JPanel menuPanel; // Stores the main menu screen.
    public JPanel setupPanel; // Stores the setup screen.
    public JPanel gamePanel; // Stores the active game screen.

    // Basic game state used by the GUI.
    public int currentPhase; // Stores which phase the user selected.
    public int timeLeft; // Stores seconds remaining in the current turn.
    public int oldScore; // Stores the score before a submitted word.

    // The actual game session and the countdown timer for human turns.
    public BoggleGame.GameSession game; // Stores the active game session.
    public Timer timer; // Stores the countdown timer for human turns.

    // Setup fields that appear in more than one phase.
    public JTextField targetField; // Stores the point-target input field.
    public JTextField minimumField; // Stores the minimum-word-length input field.
    public JTextField dictionaryField; // Stores the dictionary-file input field.

    // Setup controls for the different phase options.
    public JTextField player1Field; // Stores the Phase 1 first player name field.
    public JTextField player2Field; // Stores the Phase 1 second player name field.
    public JTextField humanField; // Stores the Phase 2 human player name field.
    public JComboBox<String> aiDifficultyBox; // Stores the Phase 2 AI difficulty dropdown.
    public JComboBox<String> firstPlayerBox; // Stores the Phase 2 first-turn dropdown.
    public JComboBox<String> playerCountBox; // Stores the Phase 3 player-count dropdown.
    public JTextField[] playerFields; // Stores all possible Phase 3 player-name fields.
    public JPanel[] playerRows; // Stores the rows that hold Phase 3 player fields.
    public JComboBox<String> humanCountBox; // Stores the Phase 4 human-count dropdown.
    public JTextField[] humanFields; // Stores all possible Phase 4 human-name fields.
    public JPanel[] humanRows; // Stores the rows that hold Phase 4 human fields.
    public JComboBox<String> aiPositionBox; // Stores where the AI should appear in Phase 4 turn order.
    public JComboBox<String> phase4DifficultyBox; // Stores the Phase 4 AI difficulty dropdown.
    public JComboBox<String> ai1DifficultyBox; // Stores AI 1 difficulty for Phase 5.
    public JComboBox<String> ai2DifficultyBox; // Stores AI 2 difficulty for Phase 5.
    public JComboBox<String> phase5FirstBox; // Stores which AI goes first in Phase 5.
    public JTextField boardFileField; // Stores the Phase 5 board-file input field.

    // Game-screen controls and display labels.
    public JLabel phaseLabel; // Stores the title label on the game screen.
    public JLabel roundLabel; // Stores the round number label on the game screen.
    public JLabel currentPlayerLabel; // Stores the current-player label.
    public JLabel timerLabel; // Stores the countdown display label.
    public JLabel statusLabel; // Stores short messages for the player.
    public JLabel[][] boardLabels; // Stores the 5 by 5 board display labels.
    public JPanel scorePanel; // Stores the score table panel.
    public JTextArea wordHistoryArea; // Stores the visible list of accepted words.
    public JTextField wordField; // Stores the input box for entering a word.
    public JButton submitButton; // Stores the Submit button.
    public JButton passButton; // Stores the Pass button.
    public JButton shakeButton; // Stores the Shake Board button.
    public JButton quitButton; // Stores the Quit button.
    public JButton hintButton; // Stores the Hint button.

    /*
     * The constructor runs when we write: new BoggleGUI();
     * It builds the window, creates the three screens, and shows the menu first.
     */
    public BoggleGUI() { // Starts the constructor that builds the first window.
        window = new JFrame("Boggle"); // Creates and stores a new value in window.
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Makes window close the program when the window closes.
        window.setSize(850, 650); // Sets the starting size of window.

        // Create the screen manager and the panel that holds all screens.
        cards = new CardLayout(); // Creates and stores a new value in cards.
        mainPanel = new JPanel(cards); // Creates and stores a new value in mainPanel.

        // Each panel represents a full screen in the app.
        menuPanel = new JPanel(); // Creates and stores a new value in menuPanel.
        setupPanel = new JPanel(); // Creates and stores a new value in setupPanel.
        gamePanel = new JPanel(); // Creates and stores a new value in gamePanel.

        // The names "menu", "setup", and "game" are used later by cards.show().
        mainPanel.add(menuPanel, "menu"); // Adds a component to mainPanel.
        mainPanel.add(setupPanel, "setup"); // Adds a component to mainPanel.
        mainPanel.add(gamePanel, "game"); // Adds a component to mainPanel.

        window.add(mainPanel); // Adds a component to window.

        // Fill the menu panel with its title and buttons.
        makeMenu(); // Builds the menu screen.

        window.setLocationRelativeTo(null); // Centers window on the screen.
        window.setVisible(true); // Shows or hides window.

        // Start the program on the menu screen.
        cards.show(mainPanel, "menu"); // Switches which screen cards displays.
    } // Ends this code block.

    /*
     * Builds the first screen: the menu where the user picks which phase to play.
     *
     * A common GUI pattern is:
     * 1. Clear the panel.
     * 2. Set its layout.
     * 3. Create labels/buttons/fields.
     * 4. Attach listeners to buttons.
     * 5. Add everything to the panel.
     */
    public void makeMenu() { // Builds the menu screen.
        menuPanel.removeAll(); // Removes all existing components from menuPanel.
        menuPanel.setLayout(new BorderLayout()); // Sets how menuPanel arranges its components.

        JLabel title = new JLabel("BOGGLE", SwingConstants.CENTER); // Creates the title label.
        title.setFont(new Font("Arial", Font.BOLD, 44)); // Changes the font used by title.
        title.setBorder(BorderFactory.createEmptyBorder(40, 10, 30, 10)); // Adds spacing or a border around title.

        menuPanel.add(title, BorderLayout.NORTH); // Adds a component to menuPanel.

        JPanel buttons = new JPanel(); // Creates the buttons panel.
        buttons.setLayout(new GridLayout(7, 1, 10, 10)); // Sets how buttons arranges its components.
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 230, 40, 230)); // Adds spacing or a border around buttons.

        JButton phase1 = new JButton("Phase 1: Player vs Player"); // Creates the phase1 button.
        JButton phase2 = new JButton("Phase 2: Player vs AI"); // Creates the phase2 button.
        JButton phase3 = new JButton("Phase 3: Multiplayer"); // Creates the phase3 button.
        JButton phase4 = new JButton("Phase 4: Multiplayer + AI"); // Creates the phase4 button.
        JButton phase5 = new JButton("Phase 5: AI vs AI Contest"); // Creates the phase5 button.
        JButton quit = new JButton("Quit"); // Creates the quit button.

        // Each button opens the setup screen for a different game phase.
        phase1.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                showSetup(1); // Opens the setup screen for Phase 1.
            } // Ends this code block.
        }); // Ends this code block.

        phase2.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                showSetup(2); // Opens the setup screen for Phase 2.
            } // Ends this code block.
        }); // Ends this code block.

        phase3.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                showSetup(3); // Opens the setup screen for Phase 3.
            } // Ends this code block.
        }); // Ends this code block.

        phase4.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                showSetup(4); // Opens the setup screen for Phase 4.
            } // Ends this code block.
        }); // Ends this code block.

        phase5.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                showSetup(5); // Opens the setup screen for Phase 5.
            } // Ends this code block.
        }); // Ends this code block.

        quit.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                // System.exit(0) closes the whole program.
                System.exit(0); // Calls exit on System.
            } // Ends this code block.
        }); // Ends this code block.

        buttons.add(phase1); // Adds a component to buttons.
        buttons.add(phase2); // Adds a component to buttons.
        buttons.add(phase3); // Adds a component to buttons.
        buttons.add(phase4); // Adds a component to buttons.
        buttons.add(phase5); // Adds a component to buttons.
        buttons.add(Box.createVerticalStrut(10)); // Adds a component to buttons.
        buttons.add(quit); // Adds a component to buttons.

        menuPanel.add(buttons, BorderLayout.CENTER); // Adds a component to menuPanel.
    } // Ends this code block.

    /*
     * Builds the setup screen for the selected phase.
     *
     * The setup screen changes depending on the phase. For example, Phase 1 asks
     * for two human player names, while Phase 5 asks for two AI difficulties and
     * a board file.
     */
    public void showSetup(int phase) { // Builds the setup screen for the chosen phase.
        currentPhase = phase; // Remembers which phase is being set up.

        // Clear old controls because this panel is reused for all phases.
        setupPanel.removeAll(); // Removes all existing components from setupPanel.
        setupPanel.setLayout(new BorderLayout()); // Sets how setupPanel arranges its components.

        JLabel title = new JLabel(getPhaseName(phase) + " Setup", SwingConstants.CENTER); // Creates the title label.
        title.setFont(new Font("Arial", Font.BOLD, 28)); // Changes the font used by title.
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10)); // Adds spacing or a border around title.

        setupPanel.add(title, BorderLayout.NORTH); // Adds a component to setupPanel.

        JPanel fields = new JPanel(); // Creates the fields panel.
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS)); // Sets how fields arranges its components.
        fields.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30)); // Adds spacing or a border around fields.

        targetField = new JTextField("50"); // Creates the point-target field with a default winning score.
        minimumField = new JTextField("3"); // Creates and stores a new value in minimumField.
        dictionaryField = new JTextField("src/wordlist.txt"); // Creates and stores a new value in dictionaryField.

        // These setup values are shared by every phase.
        fields.add(makeTextRow("Point target:", targetField)); // Adds a component to fields.
        fields.add(makeTextRow("Minimum word length:", minimumField)); // Adds a component to fields.

        if (phase == 1) { // Checks for Phase 1.
            // Phase 1 is simple: two human players.
            player1Field = new JTextField("Player 1"); // Creates and stores a new value in player1Field.
            player2Field = new JTextField("Player 2"); // Creates and stores a new value in player2Field.

            fields.add(makeTextRow("Player 1 name:", player1Field)); // Adds a component to fields.
            fields.add(makeTextRow("Player 2 name:", player2Field)); // Adds a component to fields.
        } // Ends this code block.
        else if (phase == 2) { // Checks this next condition if earlier conditions were false.
            // Phase 2 has one human, one AI, and a choice for who starts.
            humanField = new JTextField("Human"); // Creates and stores a new value in humanField.
            aiDifficultyBox = makeDifficultyBox(); // Assigns a new value to aiDifficultyBox.
            firstPlayerBox = new JComboBox<String>(new String[] {"Human first", "AI first"}); // Creates and stores a new value in firstPlayerBox.

            fields.add(makeTextRow("Human player name:", humanField)); // Adds a component to fields.
            fields.add(makeComboRow("AI difficulty:", aiDifficultyBox)); // Adds a component to fields.
            fields.add(makeComboRow("Who goes first:", firstPlayerBox)); // Adds a component to fields.
        } // Ends this code block.
        else if (phase == 3) { // Checks this next condition if earlier conditions were false.
            // Phase 3 lets the user choose 2 to 6 human players.
            playerCountBox = new JComboBox<String>(new String[] {"2", "3", "4", "5", "6"}); // Creates and stores a new value in playerCountBox.
            playerFields = new JTextField[6]; // Creates and stores a new value in playerFields.
            playerRows = new JPanel[6]; // Creates and stores a new value in playerRows.

            fields.add(makeComboRow("Number of players:", playerCountBox)); // Adds a component to fields.

            for (int i = 0; i < 6; i++) { // Loops through all six possible player rows.
                playerFields[i] = new JTextField("Player " + (i + 1)); // Creates and stores a new value in playerFields[i].
                playerRows[i] = makeTextRow("Player " + (i + 1) + " name:", playerFields[i]); // Assigns a new value to playerRows[i].
                fields.add(playerRows[i]); // Adds a component to fields.
            } // Ends this code block.

            // When the dropdown changes, hide or show the right number of names.
            playerCountBox.addActionListener(new ActionListener() { // Starts a new code block.
                public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                    updatePlayerRows(); // Refreshes visible Phase 3 player rows.
                } // Ends this code block.
            }); // Ends this code block.

            updatePlayerRows(); // Refreshes visible Phase 3 player rows.
        } // Ends this code block.
        else if (phase == 4) { // Checks this next condition if earlier conditions were false.
            // Phase 4 has several humans plus one AI inserted into the turn order.
            humanCountBox = new JComboBox<String>(new String[] {"1", "2", "3", "4", "5", "6"}); // Creates and stores a new value in humanCountBox.
            humanFields = new JTextField[6]; // Creates and stores a new value in humanFields.
            humanRows = new JPanel[6]; // Creates and stores a new value in humanRows.
            phase4DifficultyBox = makeDifficultyBox(); // Assigns a new value to phase4DifficultyBox.
            aiPositionBox = new JComboBox<String>(); // Creates and stores a new value in aiPositionBox.

            fields.add(makeComboRow("Number of human players:", humanCountBox)); // Adds a component to fields.

            for (int i = 0; i < 6; i++) { // Loops through all six possible player rows.
                humanFields[i] = new JTextField("Human " + (i + 1)); // Creates and stores a new value in humanFields[i].
                humanRows[i] = makeTextRow("Human " + (i + 1) + " name:", humanFields[i]); // Assigns a new value to humanRows[i].
                fields.add(humanRows[i]); // Adds a component to fields.
            } // Ends this code block.

            fields.add(makeComboRow("AI difficulty:", phase4DifficultyBox)); // Adds a component to fields.
            fields.add(makeComboRow("AI turn position:", aiPositionBox)); // Adds a component to fields.

            // Updating the human count also changes the possible AI positions.
            humanCountBox.addActionListener(new ActionListener() { // Starts a new code block.
                public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                    updateHumanRows(); // Refreshes visible Phase 4 human rows.
                    updateAIPositions(); // Refreshes possible AI positions.
                } // Ends this code block.
            }); // Ends this code block.

            updateHumanRows(); // Refreshes visible Phase 4 human rows.
            updateAIPositions(); // Refreshes possible AI positions.
        } // Ends this code block.
        else if (phase == 5) { // Checks this next condition if earlier conditions were false.
            // Phase 5 is AI vs AI, so no human name fields are needed.
            ai1DifficultyBox = makeDifficultyBox(); // Assigns a new value to ai1DifficultyBox.
            ai2DifficultyBox = makeDifficultyBox(); // Assigns a new value to ai2DifficultyBox.
            phase5FirstBox = new JComboBox<String>(new String[] {"AI 1 first", "AI 2 first"}); // Creates and stores a new value in phase5FirstBox.
            boardFileField = new JTextField(""); // Creates and stores a new value in boardFileField.

            fields.add(makeComboRow("AI 1 difficulty:", ai1DifficultyBox)); // Adds a component to fields.
            fields.add(makeComboRow("AI 2 difficulty:", ai2DifficultyBox)); // Adds a component to fields.
            fields.add(makeComboRow("Who goes first:", phase5FirstBox)); // Adds a component to fields.
            fields.add(makeTextRow("Optional board file:", boardFileField)); // Adds the optional board-file field to the setup screen.
        } // Ends this code block.

        JScrollPane scroll = new JScrollPane(fields); // Creates the scroll scroll pane.
        setupPanel.add(scroll, BorderLayout.CENTER); // Adds a component to setupPanel.

        // Bottom navigation buttons for this setup screen.
        JPanel bottom = new JPanel(); // Creates the bottom panel.

        JButton back = new JButton("Back"); // Creates the back button.
        JButton start = new JButton("Start Game"); // Creates the start button.

        back.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                // Return to the main menu without starting a game.
                cards.show(mainPanel, "menu"); // Switches which screen cards displays.
            } // Ends this code block.
        }); // Ends this code block.

        start.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                // Validate setup fields and create a GameSession.
                startGame(); // Starts the game using the setup fields.
            } // Ends this code block.
        }); // Ends this code block.

        bottom.add(back); // Adds a component to bottom.
        bottom.add(start); // Adds a component to bottom.

        setupPanel.add(bottom, BorderLayout.SOUTH); // Adds a component to setupPanel.
        setupPanel.revalidate(); // Recalculates the layout for setupPanel.
        setupPanel.repaint(); // Redraws setupPanel on screen.

        cards.show(mainPanel, "setup"); // Switches which screen cards displays.
    } // Ends this code block.

    /*
     * Creates one row with a label on the left and a text field on the right.
     * Reusing this method keeps the setup screen code shorter and more consistent.
     */
    public JPanel makeTextRow(String text, JTextField field) { // Builds a reusable label-plus-text-field row.
        JPanel row = new JPanel(new BorderLayout(10, 5)); // Creates the row panel.
        row.setMaximumSize(new Dimension(700, 45)); // Sets the largest size row should use.

        JLabel label = new JLabel(text); // Creates the label label.
        label.setPreferredSize(new Dimension(230, 30)); // Sets the preferred size of label.

        row.add(label, BorderLayout.WEST); // Adds a component to row.
        row.add(field, BorderLayout.CENTER); // Adds a component to row.

        return row; // Returns the completed row panel.
    } // Ends this code block.

    /*
     * Creates one row with a label on the left and a dropdown box on the right.
     * JComboBox is Swing's dropdown control.
     */
    public JPanel makeComboRow(String text, JComboBox<String> box) { // Builds a reusable label-plus-dropdown row.
        JPanel row = new JPanel(new BorderLayout(10, 5)); // Creates the row panel.
        row.setMaximumSize(new Dimension(700, 45)); // Sets the largest size row should use.

        JLabel label = new JLabel(text); // Creates the label label.
        label.setPreferredSize(new Dimension(230, 30)); // Sets the preferred size of label.

        row.add(label, BorderLayout.WEST); // Adds a component to row.
        row.add(box, BorderLayout.CENTER); // Adds a component to row.

        return row; // Returns the completed row panel.
    } // Ends this code block.

    /*
     * Creates the AI difficulty dropdown.
     * A method is useful here because several phases need the same choices.
     */
    public JComboBox<String> makeDifficultyBox() { // Builds an AI difficulty dropdown.
        JComboBox<String> box = new JComboBox<String>(new String[] {"Easy", "Medium", "Hard"}); // Creates the box dropdown.

        return box; // Returns the completed dropdown box.
    } // Ends this code block.

    /*
     * Shows only the player-name rows needed for Phase 3.
     * The fields all exist, but unused rows are hidden with setVisible(false).
     */
    public void updatePlayerRows() { // Shows or hides Phase 3 player rows.
        int count = getComboNumber(playerCountBox); // Reads the selected count from the dropdown.

        for (int i = 0; i < 6; i++) { // Loops through all six possible player rows.
            if (i < count) { // Checks whether i < count is true.
                playerRows[i].setVisible(true); // Runs this Java line.
            } // Ends this code block.
            else { // Handles the alternative case.
                playerRows[i].setVisible(false); // Runs this Java line.
            } // Ends this code block.
        } // Ends this code block.

        setupPanel.revalidate(); // Recalculates the layout for setupPanel.
        setupPanel.repaint(); // Redraws setupPanel on screen.
    } // Ends this code block.

    /*
     * Shows only the human-name rows needed for Phase 4.
     */
    public void updateHumanRows() { // Shows or hides Phase 4 human rows.
        int count = getComboNumber(humanCountBox); // Reads the selected count from the dropdown.

        for (int i = 0; i < 6; i++) { // Loops through all six possible player rows.
            if (i < count) { // Checks whether i < count is true.
                humanRows[i].setVisible(true); // Runs this Java line.
            } // Ends this code block.
            else { // Handles the alternative case.
                humanRows[i].setVisible(false); // Runs this Java line.
            } // Ends this code block.
        } // Ends this code block.

        setupPanel.revalidate(); // Recalculates the layout for setupPanel.
        setupPanel.repaint(); // Redraws setupPanel on screen.
    } // Ends this code block.

    /*
     * Rebuilds the Phase 4 AI-position dropdown.
     * If there are 3 humans, the AI can be position 1, 2, 3, or 4.
     */
    public void updateAIPositions() { // Refreshes the Phase 4 AI position choices.
        int count = getComboNumber(humanCountBox); // Reads the selected count from the dropdown.

        aiPositionBox.removeAllItems(); // Clears every option from aiPositionBox.

        for (int i = 1; i <= count + 1; i++) { // Adds each possible AI turn position to the dropdown.
            aiPositionBox.addItem("" + i); // Adds one option to aiPositionBox.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Reads all setup controls, checks for errors, creates players, creates the
     * GameSession, then switches from the setup screen to the game screen.
     *
     * This method is the bridge between the GUI and the actual game logic.
     */
    public void startGame() { // Validates setup choices and starts the game.
        try { // Starts code that might fail and need error handling.
            // Convert text fields into numbers. trim() removes extra spaces.
            int targetScore = Integer.parseInt(targetField.getText().trim()); // Reads and converts the target score from the setup field.
            int minimumLength = Integer.parseInt(minimumField.getText().trim()); // Reads and converts the minimum word length from the setup field.
            File dictionaryFile = new File(dictionaryField.getText().trim()); // Creates the dictionary file file object.

            // Validate setup values before starting the game.
            if (targetScore <= 0) { // Rejects games without a positive target score.
                JOptionPane.showMessageDialog(window, "Point target must be at least 1."); // Shows a popup message to the user.
                return; // Stops this method so the game does not start with bad setup data.
            } // Ends this code block.

            if (minimumLength < 3) { // Rejects words shorter than the game allows.
                JOptionPane.showMessageDialog(window, "Minimum word length must be at least 3."); // Shows a popup message to the user.
                return; // Stops this method so the game does not start with bad setup data.
            } // Ends this code block.

            if (dictionaryFile.exists() == false) { // Checks whether the dictionary file is missing.
                JOptionPane.showMessageDialog(window, "Dictionary file was not found."); // Shows a popup message to the user.
                return; // Stops this method so the game does not start with bad setup data.
            } // Ends this code block.

            char[][] phase5Board = null; // Starts with no Phase 5 board loaded yet.

            // Phase 5 needs a fixed board from a file so both AIs play the same board.
            if (currentPhase == 5) { // Checks whether the current game is Phase 5.
                if (boardFileField.getText().trim().length() > 0) { // Checks whether the user typed an optional board-file path.
                    phase5Board = readBoardFile(new File(boardFileField.getText().trim())); // Reads the custom board from the typed file path.

                    if (phase5Board == null) { // Checks whether the board file could not be loaded.
                        JOptionPane.showMessageDialog(window, "Board file was not valid."); // Shows a popup message to the user.
                        return; // Stops this method because the custom board file is invalid.
                    } // Ends this code block.
                } // Ends this code block.
            } // Ends this code block.

            ArrayList<BoggleGame.Player> players = new ArrayList<BoggleGame.Player>(); // Creates the players list.

            // Build the player list differently for each phase.
            if (currentPhase == 1) { // Checks whether currentPhase == 1 is true.
                players.add(new BoggleGame.Player(player1Field.getText())); // Adds a component to players.
                players.add(new BoggleGame.Player(player2Field.getText())); // Adds a component to players.
            } // Ends this code block.
            else if (currentPhase == 2) { // Checks this next condition if earlier conditions were false.
                // Add the human and AI in the selected turn order.
                BoggleGame.Player human = new BoggleGame.Player(humanField.getText()); // Creates the human object.
                BoggleGame.Player ai = BoggleAI.makeAIPlayer("AI", getComboText(aiDifficultyBox)); // Creates the AI player object.

                if (firstPlayerBox.getSelectedIndex() == 0) { // Checks whether the human should go first.
                    players.add(human); // Adds a component to players.
                    players.add(ai); // Adds a component to players.
                } // Ends this code block.
                else { // Handles the alternative case.
                    players.add(ai); // Adds a component to players.
                    players.add(human); // Adds a component to players.
                } // Ends this code block.
            } // Ends this code block.
            else if (currentPhase == 3) { // Checks this next condition if earlier conditions were false.
                int count = getComboNumber(playerCountBox); // Reads the selected count from the dropdown.

                // Only read the visible player fields.
                for (int i = 0; i < count; i++) { // Loops through the number of selected players.
                    players.add(new BoggleGame.Player(playerFields[i].getText())); // Adds a component to players.
                } // Ends this code block.
            } // Ends this code block.
            else if (currentPhase == 4) { // Checks this next condition if earlier conditions were false.
                int humanCount = getComboNumber(humanCountBox); // Reads how many human players were selected.
                int aiPosition = getComboNumber(aiPositionBox); // Reads where the AI should be placed in turn order.

                /*
                 * Add humans and the AI in one loop. The loop has humanCount + 1
                 * spots because the AI is an extra player.
                 */
                for (int i = 0; i < humanCount + 1; i++) { // Loops through every turn-order spot, including the AI.
                    if (i == aiPosition - 1) { // Checks whether this turn-order spot belongs to the AI.
                        players.add(BoggleAI.makeAIPlayer("AI", getComboText(phase4DifficultyBox))); // Adds a component to players.
                    } // Ends this code block.
                    else { // Handles the alternative case.
                        int humanNumber = i; // Starts with the matching human field index.

                        if (i > aiPosition - 1) { // Checks whether this human comes after the AI.
                            humanNumber = i - 1; // Adjusts the human field index after the AI position.
                        } // Ends this code block.

                        players.add(new BoggleGame.Player(humanFields[humanNumber].getText())); // Adds a component to players.
                    } // Ends this code block.
                } // Ends this code block.
            } // Ends this code block.
            else { // Handles the alternative case.
                // Phase 5 creates two AI players and orders them by the dropdown.
                BoggleGame.Player ai1 = BoggleAI.makeAIPlayer("AI 1", getComboText(ai1DifficultyBox)); // Creates the first AI player.
                BoggleGame.Player ai2 = BoggleAI.makeAIPlayer("AI 2", getComboText(ai2DifficultyBox)); // Creates the second AI player.

                if (phase5FirstBox.getSelectedIndex() == 0) { // Checks whether AI 1 should go first.
                    players.add(ai1); // Adds a component to players.
                    players.add(ai2); // Adds a component to players.
                } // Ends this code block.
                else { // Handles the alternative case.
                    players.add(ai2); // Adds a component to players.
                    players.add(ai1); // Adds a component to players.
                } // Ends this code block.
            } // Ends this code block.

            game = new BoggleGame.GameSession(players, minimumLength, targetScore, dictionaryFile); // Creates the game session that keeps making rounds until the target score is reached.

            // Replace the random board with the board loaded from file for Phase 5.
            if (phase5Board != null) { // Checks whether a custom board was loaded.
                game.board = phase5Board; // Replaces the game board with the loaded board.
            } // Ends this code block.

            // Build the game screen and start the first turn.
            makeGamePanel(); // Builds the game screen.
            cards.show(mainPanel, "game"); // Switches which screen cards displays.
            nextTurn(); // Starts the next turn.
        } // Ends this code block.
        catch (Exception e) { // Catches errors from the try block.
            // If a number field cannot be parsed, the user sees a friendly error.
            JOptionPane.showMessageDialog(window, "Please check the setup fields."); // Shows a popup message to the user.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Builds the main play screen.
     *
     * The play screen has:
     * - a title at the top,
     * - the 5x5 letter board in the middle,
     * - scores and word history on the right,
     * - word input and buttons at the bottom.
     */
    public void makeGamePanel() { // Builds the game-play screen.
        gamePanel.removeAll(); // Removes all existing components from gamePanel.
        gamePanel.setLayout(new BorderLayout(10, 10)); // Sets how gamePanel arranges its components.
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adds spacing or a border around gamePanel.

        JPanel top = new JPanel(new GridLayout(2, 1)); // Creates the top panel with one row for phase and one row for round.

        phaseLabel = new JLabel(getPhaseName(currentPhase), SwingConstants.CENTER); // Creates and stores a new value in phaseLabel.
        phaseLabel.setFont(new Font("Arial", Font.BOLD, 24)); // Changes the font used by phaseLabel.

        roundLabel = new JLabel("", SwingConstants.CENTER); // Creates the label that shows the current round number.
        roundLabel.setFont(new Font("Arial", Font.PLAIN, 16)); // Sets the round label to a smaller font than the title.

        top.add(phaseLabel); // Adds a component to top.
        top.add(roundLabel); // Adds the round label below the phase title.

        gamePanel.add(top, BorderLayout.NORTH); // Adds a component to gamePanel.

        JPanel center = new JPanel(new BorderLayout(10, 10)); // Creates the center panel.

        JPanel boardPanel = new JPanel(new GridLayout(5, 5, 5, 5)); // Creates the board panel panel.
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Adds spacing or a border around boardPanel.

        // boardLabels stores the 25 labels so updateBoard() can change them later.
        boardLabels = new JLabel[5][5]; // Creates and stores a new value in boardLabels.

        for (int row = 0; row < 5; row++) { // Loops through each board row.
            for (int col = 0; col < 5; col++) { // Loops through each board column.
                // Each label is one square on the Boggle board.
                JLabel label = new JLabel("", SwingConstants.CENTER); // Creates the label label.
                label.setFont(new Font("Arial", Font.BOLD, 28)); // Changes the font used by label.
                label.setOpaque(true); // Calls setOpaque on label.
                label.setBackground(Color.WHITE); // Calls setBackground on label.
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Adds spacing or a border around label.

                boardLabels[row][col] = label; // Runs this Java line.
                boardPanel.add(label); // Adds a component to boardPanel.
            } // Ends this code block.
        } // Ends this code block.

        center.add(boardPanel, BorderLayout.CENTER); // Adds a component to center.

        JPanel side = new JPanel(); // Creates the side panel.
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS)); // Sets how side arranges its components.
        side.setPreferredSize(new Dimension(270, 400)); // Sets the preferred size of side.

        currentPlayerLabel = new JLabel("Current Player:"); // Creates and stores a new value in currentPlayerLabel.
        timerLabel = new JLabel("Timer:"); // Creates and stores a new value in timerLabel.
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18)); // Changes the font used by timerLabel.

        scorePanel = new JPanel(); // Creates and stores a new value in scorePanel.
        scorePanel.setBorder(BorderFactory.createTitledBorder("Scores")); // Adds spacing or a border around scorePanel.

        wordHistoryArea = new JTextArea(); // Creates and stores a new value in wordHistoryArea.
        wordHistoryArea.setEditable(false); // Controls whether the user can edit wordHistoryArea.

        JScrollPane historyScroll = new JScrollPane(wordHistoryArea); // Creates the history scroll scroll pane.
        historyScroll.setBorder(BorderFactory.createTitledBorder("Word History")); // Adds spacing or a border around historyScroll.

        side.add(currentPlayerLabel); // Adds a component to side.
        side.add(Box.createVerticalStrut(10)); // Adds a component to side.
        side.add(timerLabel); // Adds a component to side.
        side.add(Box.createVerticalStrut(10)); // Adds a component to side.
        side.add(scorePanel); // Adds a component to side.
        side.add(Box.createVerticalStrut(10)); // Adds a component to side.
        side.add(historyScroll); // Adds a component to side.

        // BorderLayout.EAST puts the score/history panel on the right side.
        center.add(side, BorderLayout.EAST); // Adds a component to center.

        gamePanel.add(center, BorderLayout.CENTER); // Adds a component to gamePanel.

        JPanel bottom = new JPanel(new BorderLayout(5, 5)); // Creates the bottom panel.

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5)); // Creates the input panel panel.

        wordField = new JTextField(); // Creates and stores a new value in wordField.
        submitButton = new JButton("Submit"); // Creates and stores a new value in submitButton.

        inputPanel.add(wordField, BorderLayout.CENTER); // Adds a component to inputPanel.
        inputPanel.add(submitButton, BorderLayout.EAST); // Adds a component to inputPanel.

        JPanel buttons = new JPanel(); // Creates the buttons panel.

        passButton = new JButton("Pass"); // Creates and stores a new value in passButton.
        shakeButton = new JButton("Shake Board"); // Creates and stores a new value in shakeButton.
        quitButton = new JButton("Quit"); // Creates and stores a new value in quitButton.
        hintButton = new JButton("Hint"); // Creates and stores a new value in hintButton.

        buttons.add(passButton); // Adds a component to buttons.
        buttons.add(shakeButton); // Adds a component to buttons.
        buttons.add(quitButton); // Adds a component to buttons.
        buttons.add(hintButton); // Adds a component to buttons.

        statusLabel = new JLabel("Ready."); // Creates and stores a new value in statusLabel.

        bottom.add(inputPanel, BorderLayout.NORTH); // Adds a component to bottom.
        bottom.add(buttons, BorderLayout.CENTER); // Adds a component to bottom.
        bottom.add(statusLabel, BorderLayout.SOUTH); // Adds a component to bottom.

        gamePanel.add(bottom, BorderLayout.SOUTH); // Adds a component to gamePanel.

        // Button listeners connect user actions to methods in this class.
        submitButton.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                submitWord(); // Submits the typed word.
            } // Ends this code block.
        }); // Ends this code block.

        // Pressing Enter in the word field also submits the word.
        wordField.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                submitWord(); // Submits the typed word.
            } // Ends this code block.
        }); // Ends this code block.

        passButton.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                passTurn(); // Passes the current turn.
            } // Ends this code block.
        }); // Ends this code block.

        shakeButton.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                shakeBoard(); // Runs the shake-board action.
            } // Ends this code block.
        }); // Ends this code block.

        quitButton.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                quitPlayer(); // Runs the quit-player action.
            } // Ends this code block.
        }); // Ends this code block.

        hintButton.addActionListener(new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                showHint(); // Shows a possible word hint.
            } // Ends this code block.
        }); // Ends this code block.

        updateBoard(); // Refreshes the board display.
        updateRoundLabel(); // Refreshes the round label.
        updateScores(); // Refreshes the score display.
    } // Ends this code block.

    /*
     * Starts the current player's turn.
     *
     * Human players get the text field and buttons enabled. AI players do not
     * need input, so the GUI waits briefly and then calls aiMove().
     */
    public void nextTurn() { // Begins the current player turn.
        BoggleGame.Player player = game.getCurrentPlayer(); // Gets the player currently taking a turn.

        currentPlayerLabel.setText("Current Player: " + player.name); // Changes the text shown by currentPlayerLabel.
        updateRoundLabel(); // Updates the round label before the turn starts.
        updateScores(); // Refreshes the score display.
        updateBoard(); // Refreshes the board display.

        if (player.isAI == true) { // Checks whether the current player is an AI.
            setInput(false); // Enables or disables input controls.
            statusLabel.setText(player.name + " is thinking..."); // Changes the text shown by statusLabel.

            /*
             * This short timer makes the AI move feel visible instead of instant.
             * setRepeats(false) means it runs only one time.
             */
            Timer aiTimer = new Timer(getAIDelay(player.difficulty), new ActionListener() { // Waits longer for harder AI levels, like the algorithm says.
                public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                    aiMove(); // Runs the AI move.
                } // Ends this code block.
            }); // Ends this code block.

            aiTimer.setRepeats(false); // Controls whether aiTimer runs more than once.
            aiTimer.start(); // Starts aiTimer.
        } // Ends this code block.
        else { // Handles the alternative case.
            setInput(true); // Enables or disables input controls.
            wordField.setText(""); // Changes the text shown by wordField.
            hintButton.setEnabled(true); // Gives this player one hint chance for this turn.
            statusLabel.setText(player.name + "'s turn."); // Changes the text shown by statusLabel.
            // Human players have 15 seconds to submit a word or pass.
            startTimer(); // Restarts the human turn timer.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Starts the countdown for a human player's turn.
     *
     * Swing Timer runs code every 1000 milliseconds. Because this is a Swing
     * Timer, it is safe to update Swing labels inside the ActionListener.
     */
    public void startTimer() { // Starts the 15-second turn timer.
        stopTimer(); // Stops the current timer.

        timeLeft = 15; // Sets the timer back to 15 seconds.
        timerLabel.setForeground(Color.BLACK); // Changes the text color of timerLabel.
        timerLabel.setText("Timer: " + timeLeft); // Changes the text shown by timerLabel.

        timer = new Timer(1000, new ActionListener() { // Starts a new code block.
            public void actionPerformed(ActionEvent e) { // Starts the actionPerformed method.
                timeLeft = timeLeft - 1; // Sets the timer back to 15 seconds.
                timerLabel.setText("Timer: " + timeLeft); // Changes the text shown by timerLabel.

                // Turn the timer red when the player is almost out of time.
                if (timeLeft <= 5) { // Checks whether the timer should turn red.
                    timerLabel.setForeground(Color.RED); // Changes the text color of timerLabel.
                } // Ends this code block.

                if (timeLeft <= 0) { // Checks whether the turn has run out of time.
                    // When time runs out, the game treats this like a passed turn.
                    stopTimer(); // Stops the current timer.
                    game.timeoutCurrentPlayer(); // Marks the current player as timed out in game.
                    statusLabel.setText("Time is up. Turn passed."); // Changes the text shown by statusLabel.
                    afterTurn(); // Continues the game after this turn.
                } // Ends this code block.
            } // Ends this code block.
        }); // Ends this code block.

        timer.start(); // Starts timer.
    } // Ends this code block.

    /*
     * Handles the Submit button and Enter key.
     *
     * The GUI cleans the word, asks GameSession if it is valid, then updates
     * labels and moves to the next turn when needed.
     */
    public void submitWord() { // Submits the typed word to the game logic.
        BoggleGame.Player player = game.getCurrentPlayer(); // Gets the player currently taking a turn.
        String word = wordField.getText(); // Reads the word typed by the player.

        if (word == null) { // Protects against a missing word value.
            word = ""; // Cleans up the word text and makes it uppercase.
        } // Ends this code block.

        word = word.trim().toUpperCase(); // Cleans up the word text and makes it uppercase.

        if (word.length() == 0) { // Rejects an empty word submission.
            statusLabel.setText("Type a word first."); // Changes the text shown by statusLabel.
            return; // Runs this Java line.
        } // Ends this code block.

        stopTimer(); // Stops the current timer.

        // Save the old score so we can calculate how many points this word gave.
        oldScore = player.score; // Records the score before the word is checked.

        int result = game.submitWord(word); // Gets the result from the game logic.

        if (result == 1) { // Checks whether the submitted word was accepted.
            int points = player.score - oldScore; // Calculates how many points the word earned.

            statusLabel.setText("Word accepted: " + word); // Changes the text shown by statusLabel.
            addWordHistory(player.name, word, points); // Adds this word to the history display.
            updateScores(); // Refreshes the score display.
            afterTurn(); // Continues the game after this turn.
        } // Ends this code block.
        else if (result == 2) { // Checks this next condition if earlier conditions were false.
            // Repeated words do not end the turn, so restart the timer.
            statusLabel.setText("Already used word."); // Changes the text shown by statusLabel.
            startTimer(); // Restarts the human turn timer.
        } // Ends this code block.
        else { // Handles the alternative case.
            if (player.passed == true) { // Checks whether the game logic passed the player.
                // The game logic can pass the player after too many wrong guesses.
                statusLabel.setText("Too many wrong guesses. Turn passed."); // Changes the text shown by statusLabel.
                afterTurn(); // Continues the game after this turn.
            } // Ends this code block.
            else { // Handles the alternative case.
                // Invalid words let the player continue trying during this turn.
                statusLabel.setText("Invalid word. Try again."); // Changes the text shown by statusLabel.
                startTimer(); // Restarts the human turn timer.
            } // Ends this code block.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Handles the Pass button.
     * Passing ends the current player's turn without changing the score.
     */
    public void passTurn() { // Passes the current player turn.
        stopTimer(); // Stops the current timer.
        game.passCurrentPlayer(); // Marks the current player as passed in game.
        statusLabel.setText(game.getCurrentPlayer().name + " passed."); // Changes the text shown by statusLabel.
        afterTurn(); // Continues the game after this turn.
    } // Ends this code block.

    /*
     * Handles the Shake Board button.
     * JOptionPane.showConfirmDialog returns YES_OPTION only if the user confirms.
     */
    public void shakeBoard() { // Asks to shake the board and handles the answer.
        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to shake the board?"); // Stores the user answer from the popup.

        if (answer == JOptionPane.YES_OPTION) { // Checks whether the user clicked Yes.
            stopTimer(); // Stops the current timer.
            game.shakeBoard(); // Asks game to create a new board.
            // Old words do not apply to the new board, so clear the visible history.
            wordHistoryArea.setText(""); // Changes the text shown by wordHistoryArea.
            statusLabel.setText("Board was shaken."); // Changes the text shown by statusLabel.
            nextTurn(); // Starts the next turn.
        } // Ends this code block.
        else { // Handles the alternative case.
            if (game.getCurrentPlayer().isAI == false) { // Checks whether the current player is human.
                startTimer(); // Restarts the human turn timer.
            } // Ends this code block.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Handles the Quit button.
     * Quitting removes the current player from active play.
     */
    public void quitPlayer() { // Asks to quit the current player and handles the answer.
        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to quit?"); // Stores the user answer from the popup.

        if (answer == JOptionPane.YES_OPTION) { // Checks whether the user clicked Yes.
            stopTimer(); // Stops the current timer.
            game.quitCurrentPlayer(); // Marks the current player as quit in game.
            statusLabel.setText(game.getCurrentPlayer().name + " quit."); // Changes the text shown by statusLabel.
            afterTurn(); // Continues the game after this turn.
        } // Ends this code block.
        else { // Handles the alternative case.
            if (game.getCurrentPlayer().isAI == false) { // Checks whether the current player is human.
                startTimer(); // Restarts the human turn timer.
            } // Ends this code block.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Makes one AI play its turn.
     * GameSession chooses the word and returns the result in an AIResult object.
     */
    public void aiMove() { // Lets the current AI take its turn.
        BoggleGame.Player player = game.getCurrentPlayer(); // Gets the player currently taking a turn.
        BoggleGame.GameSession.AIResult result = game.doAITurn(); // Gets the result from the game logic.

        if (result.passed == true) { // Checks whether the AI passed instead of playing a word.
            statusLabel.setText(player.name + " passed."); // Changes the text shown by statusLabel.
        } // Ends this code block.
        else { // Handles the alternative case.
            statusLabel.setText(player.name + " played " + result.word + "."); // Changes the text shown by statusLabel.
            addWordHistory(player.name, result.word, result.points); // Adds this word to the history display.
        } // Ends this code block.

        updateScores(); // Refreshes the score display.
        afterTurn(); // Continues the game after this turn.
    } // Ends this code block.

    /*
     * Runs after any turn ends.
     *
     * moveToNextPlayer() returns a status code:
     * 0 = keep playing,
     * 1 = all active players passed,
     * 2 = the game is over.
     */
    public void afterTurn() { // Moves the game forward after a turn ends.
        updateScores(); // Refreshes the score display.

        int status = game.moveToNextPlayer(); // Stores the game status after moving to the next player.

        if (status == 2) { // Checks whether the game is over.
            if (game.someoneReachedTarget() == true || game.someoneQuit() == true) { // Checks for conditions that end the whole game immediately.
                endGame(); // Ends the game now.
            } // Ends this code block.
            else { // Handles a finished round when more rounds may still exist.
                endRound(); // Shows the round result and automatically starts the next round.
            } // Ends this code block.

            return; // Stops after handling the game or round end.
        } // Ends this code block.

        if (status == 1) { // Checks whether all active players passed.
            askShakeOrEnd(); // Asks whether to shake or end.
            return; // Stops so the popup choice can decide what happens next.
        } // Ends this code block.

        nextTurn(); // Starts the next turn.
    } // Ends this code block.

    /*
     * Called when every active player has passed.
     * The players can shake the board one time, or end the game.
     */
    public void askShakeOrEnd() { // Asks whether to shake the board or end the game.
        stopTimer(); // Stops the current timer.
        setInput(false); // Enables or disables input controls.

        // Phase 5 and already-shaken games end immediately here.
        if (currentPhase == 5 || game.shakeUsed == true) { // Ends instead of shaking when shaking is not allowed.
            endRound(); // Finishes this round and moves to the next round if allowed.
            return; // Stops so endRound can handle the next step.
        } // Ends this code block.

        int answer = JOptionPane.showConfirmDialog(window, "All players passed. Shake the board?"); // Stores the user answer from the popup.

        if (answer == JOptionPane.YES_OPTION) { // Checks whether the user clicked Yes.
            game.shakeBoard(); // Asks game to create a new board.
            wordHistoryArea.setText(""); // Changes the text shown by wordHistoryArea.
            statusLabel.setText("Board was shaken."); // Changes the text shown by statusLabel.
            nextTurn(); // Starts the next turn.
        } // Ends this code block.
        else { // Handles the alternative case.
            endRound(); // Ends this round instead of ending the whole game right away.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Ends the current round.
     * The game starts another round automatically until the point target is reached.
     */
    public void endRound() { // Shows round results and starts another round.
        stopTimer(); // Stops the current timer.
        setInput(false); // Turns off word input while the popup is open.

        BoggleGame.Player winner = game.getRoundWinner(); // Gets the player with the highest score this round.
        String message = "Round " + game.currentRound + " ended.\n"; // Starts the round-end message.

        if (winner == null) { // Checks whether there is no round winner.
            message = message + "Round winner: NONE\n"; // Adds a no-winner line to the message.
        } // Ends this code block.
        else { // Handles the case where a round winner exists.
            message = message + "Round winner: " + winner.name + "\n"; // Adds the round winner name to the message.
        } // Ends this code block.

        message = message + "\nRound scores:\n"; // Adds a heading for round scores.

        for (int i = 0; i < game.players.size(); i++) { // Loops through every player in the game.
            BoggleGame.Player player = game.players.get(i); // Gets this player from the player list.

            message = message + player.name + ": " + player.roundScore + "\n"; // Adds this player's round score.
        } // Ends this code block.

        JOptionPane.showMessageDialog(window, message + "\nNext round will start."); // Shows the round result before continuing.
        game.startNextRound(); // Resets round-only data and creates a new board.
        wordHistoryArea.setText(""); // Clears the visible word history for the new round.
        updateRoundLabel(); // Updates the round label on the game screen.
        statusLabel.setText("Round " + game.currentRound + " started."); // Shows a round-start message.
        nextTurn(); // Starts the first turn of the new round.
    } // Ends this code block.

    /*
     * Stops the game, shows the final scores, and returns to the main menu.
     */
    public void endGame() { // Shows final scores and returns to the menu.
        stopTimer(); // Stops the current timer.
        setInput(false); // Enables or disables input controls.
        updateScores(); // Refreshes the score display.

        BoggleGame.Player winner = game.getWinner(); // Gets the winner from the game session.
        String message = "Game ended.\n"; // Starts building the final message text.

        if (winner == null) { // Checks whether there is no winner.
            message = message + "Winner: NONE\n"; // Assigns a new value to message.
        } // Ends this code block.
        else { // Handles the alternative case.
            message = message + "Winner: " + winner.name + "\n"; // Assigns a new value to message.
        } // Ends this code block.

        message = message + "\nFinal scores:\n"; // Adds a heading for total scores.

        for (int i = 0; i < game.players.size(); i++) { // Loops through every player in the game.
            BoggleGame.Player player = game.players.get(i); // Gets this player from the player list.

            message = message + player.name + ": " + player.score + "\n"; // Assigns a new value to message.
        } // Ends this code block.

        JOptionPane.showMessageDialog(window, message); // Shows a popup message to the user.
        cards.show(mainPanel, "menu"); // Switches which screen cards displays.
    } // Ends this code block.

    /*
     * Rebuilds the score display from the current player list.
     * The panel is cleared and recreated because scores and statuses can change
     * after every turn.
     */
    public void updateScores() { // Refreshes the score display.
        scorePanel.removeAll(); // Removes all existing components from scorePanel.
        scorePanel.setLayout(new GridLayout(game.players.size() + 1, 4, 5, 5)); // Creates four columns: player, round, total, and status.

        scorePanel.add(new JLabel("Player")); // Adds a component to scorePanel.
        scorePanel.add(new JLabel("Round")); // Adds a round-score heading to scorePanel.
        scorePanel.add(new JLabel("Total")); // Adds a total-score heading to scorePanel.
        scorePanel.add(new JLabel("Status")); // Adds a component to scorePanel.

        for (int i = 0; i < game.players.size(); i++) { // Loops through every player in the game.
            BoggleGame.Player player = game.players.get(i); // Gets this player from the player list.
            String status = "Active"; // Starts this player's visible status as active.

            if (player.quit == true) { // Checks whether this player quit.
                status = "Quit"; // Updates the visible status for this player.
            } // Ends this code block.
            else if (player.passed == true) { // Checks this next condition if earlier conditions were false.
                status = "Passed"; // Updates the visible status for this player.
            } // Ends this code block.

            scorePanel.add(new JLabel(player.name)); // Adds a component to scorePanel.
            scorePanel.add(new JLabel("" + player.roundScore)); // Shows the player's score for this round.
            scorePanel.add(new JLabel("" + player.score)); // Shows the player's total game score.
            scorePanel.add(new JLabel(status)); // Adds a component to scorePanel.
        } // Ends this code block.

        scorePanel.revalidate(); // Recalculates the layout for scorePanel.
        scorePanel.repaint(); // Redraws scorePanel on screen.
    } // Ends this code block.

    /*
     * Adds one accepted word to the visible word-history box.
     * setCaretPosition scrolls the text area to the newest line.
     */
    public void addWordHistory(String playerName, String word, int points) { // Adds one accepted word to the history display.
        wordHistoryArea.append(playerName + " - " + word + " (+" + points + ")\n"); // Adds text to the end of wordHistoryArea.
        wordHistoryArea.setCaretPosition(wordHistoryArea.getDocument().getLength()); // Scrolls wordHistoryArea to a specific text position.
    } // Ends this code block.

    /*
     * Updates the round label at the top of the game panel.
     */
    public void updateRoundLabel() { // Shows the current round number and target score.
        roundLabel.setText("Round " + game.currentRound + " - first to " + game.targetScore + " points"); // Displays the round number and winning score.
    } // Ends this code block.

    /*
     * Enables or disables the human input controls.
     * This is used during AI turns and after the game ends.
     */
    public void setInput(boolean on) { // Enables or disables human input controls.
        wordField.setEnabled(on); // Enables or disables wordField.
        submitButton.setEnabled(on); // Enables or disables submitButton.
        passButton.setEnabled(on); // Enables or disables passButton.
        quitButton.setEnabled(on); // Enables or disables quitButton.
        hintButton.setEnabled(on); // Enables or disables hintButton.
    } // Ends this code block.

    /*
     * Copies the letters from game.board into the 25 labels on the screen.
     */
    public void updateBoard() { // Refreshes the board letters on screen.
        for (int row = 0; row < 5; row++) { // Loops through each board row.
            for (int col = 0; col < 5; col++) { // Loops through each board column.
                boardLabels[row][col].setText("" + game.board[row][col]); // Runs this Java line.
            } // Ends this code block.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Shows one hint using the first and last letter of a strong word.
     * This follows the PDF algorithm without giving away the entire answer.
     */
    public void showHint() { // Finds and displays a valid word hint.
        ArrayList<String> words = BoggleAI.findAllValidWords(game.board, game.dictionary, game.minimumWordLength, game.usedWords); // Finds all valid words that could be used as hints.

        if (words.size() == 0) { // Checks whether no hint words were found.
            statusLabel.setText("No hint found."); // Changes the text shown by statusLabel.
            return; // Stops because there is no hint to show.
        } // Ends this code block.

        String hint = BoggleAI.chooseWord(words, "Hard"); // Chooses one hint word from the valid words.
        String firstLetter = hint.substring(0, 1); // Gets the first letter of the hint word.
        String lastLetter = hint.substring(hint.length() - 1); // Gets the last letter of the hint word.

        statusLabel.setText("Hint: starts with " + firstLetter + " and ends with " + lastLetter); // Shows only first and last letters.
        hintButton.setEnabled(false); // Allows only one hint during this turn.
        startTimer(); // Resets the human timer after using a hint.
    } // Ends this code block.

    /*
     * Returns the AI thinking delay from the PDF algorithm.
     */
    public int getAIDelay(String difficulty) { // Converts AI difficulty into a delay in milliseconds.
        if (difficulty == null) { // Checks whether the AI difficulty text is missing.
            return 2000; // Uses the Easy delay when no difficulty is available.
        } // Ends this code block.

        String text = difficulty.trim().toUpperCase(); // Cleans the difficulty text for comparison.

        if (text.equals("EASY")) { // Checks for Easy AI.
            return 2000; // Easy AI waits about 2 seconds.
        } // Ends this code block.
        else if (text.equals("MEDIUM")) { // Checks for Medium AI.
            return 4000; // Medium AI waits about 4 seconds.
        } // Ends this code block.
        else { // Handles Hard AI or any unknown difficulty.
            return 6000; // Hard AI waits about 6 seconds.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Stops the current countdown timer if it exists.
     * Checking for null avoids an error before the first timer has been created.
     */
    public void stopTimer() { // Stops the active timer if one exists.
        if (timer != null) { // Checks that a timer exists before stopping it.
            timer.stop(); // Stops timer.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Reads a selected dropdown item and converts it to an int.
     * This is used for dropdowns that contain numbers like "2", "3", and "4".
     */
    public int getComboNumber(JComboBox<String> box) { // Reads a numeric dropdown value.
        String text = (String) box.getSelectedItem(); // Reads the selected dropdown text.

        return Integer.parseInt(text); // Returns the selected dropdown value as a number.
    } // Ends this code block.

    /*
     * Safely reads a selected dropdown item as text.
     */
    public String getComboText(JComboBox<String> box) { // Reads a text dropdown value safely.
        String text = (String) box.getSelectedItem(); // Reads the selected dropdown text.

        if (text == null) { // Protects against a missing dropdown value.
            text = ""; // Uses an empty string when no dropdown text exists.
        } // Ends this code block.

        return text; // Returns the dropdown text.
    } // Ends this code block.

    /*
     * Converts a phase number into the title shown in the GUI.
     */
    public String getPhaseName(int phase) { // Returns the display name for a phase number.
        if (phase == 1) { // Checks for Phase 1.
            return "Phase 1: Player vs Player"; // Returns the phase title text.
        } // Ends this code block.
        else if (phase == 2) { // Checks this next condition if earlier conditions were false.
            return "Phase 2: Player vs AI"; // Returns the phase title text.
        } // Ends this code block.
        else if (phase == 3) { // Checks this next condition if earlier conditions were false.
            return "Phase 3: Multiplayer"; // Returns the phase title text.
        } // Ends this code block.
        else if (phase == 4) { // Checks this next condition if earlier conditions were false.
            return "Phase 4: Multiplayer + AI"; // Returns the phase title text.
        } // Ends this code block.
        else { // Handles the alternative case.
            return "Phase 5: AI vs AI Contest"; // Returns the phase title text.
        } // Ends this code block.
    } // Ends this code block.

    /*
     * Reads a board file for Phase 5.
     *
     * The file can contain spaces or line breaks. The scanner reads every token,
     * joins them together, changes them to uppercase, and uses the first 25
     * letters to fill a 5 by 5 board.
     */
    public char[][] readBoardFile(File file) { // Loads a 5 by 5 board from a text file.
        try { // Starts code that might fail and need error handling.
            if (file.exists() == false) { // Checks whether the board file is missing.
                return null; // Returns null to mean the value is missing or invalid.
            } // Ends this code block.

            Scanner scanner = new Scanner(new FileReader(file)); // Creates the scanner scanner for reading text.
            String letters = ""; // Starts an empty string for all board letters.

            while (scanner.hasNext()) { // Repeats while this scanner still has text to read.
                letters = letters + scanner.next(); // Adds the next text token to the board-letter string.
            } // Ends this code block.

            scanner.close(); // Closes scanner.
            letters = letters.toUpperCase(); // Adds the next text token to the board-letter string.

            if (letters.length() < 25) { // Rejects board files with fewer than 25 letters.
                return null; // Returns null to mean the value is missing or invalid.
            } // Ends this code block.

            char[][] board = new char[5][5]; // Creates the board object.
            int number = 0; // Tracks which character should be copied next.

            // Fill the board from left to right, top to bottom.
            for (int row = 0; row < 5; row++) { // Loops through each board row.
                for (int col = 0; col < 5; col++) { // Loops through each board column.
                    board[row][col] = letters.charAt(number); // Runs this Java line.
                    number = number + 1; // Moves to the next board-file character.
                } // Ends this code block.
            } // Ends this code block.

            return board; // Returns the loaded board.
        } // Ends this code block.
        catch (Exception e) { // Catches errors from the try block.
            // Returning null tells startGame() that the board file was invalid.
            return null; // Returns null to mean the value is missing or invalid.
        } // Ends this code block.
    } // Ends this code block.
} // Ends this code block.
