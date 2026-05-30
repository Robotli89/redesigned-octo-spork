import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Swing front end for the Boggle game.
 *
 * This class only handles screens, buttons, labels, and user input. The actual
 * game rules live in GameSession, so most GUI methods either read fields from
 * the screen or call a GameSession method and then refresh the display.
 */
public class BoggleGUI {
    // CardLayout lets one JFrame swap between menu, setup, and game screens.
    public JFrame window;
    public CardLayout cards;
    public JPanel mainPanel;
    public JPanel menuPanel;
    public JPanel setupPanel;
    public JPanel gamePanel;

    // Values that describe the current GUI/game state.
    public int currentPhase;
    public int timeLeft;
    public int oldScore;

    // GameSession stores the board, players, scores, and rule decisions.
    public GameSession game;
    public Timer timer;

    /** When false, human turns have no countdown and do not auto-timeout. */
    public boolean useTurnTimer;
    public int timerSeconds;
    public boolean hintUsedThisRound;

    private final File defaultDictionaryFile;

    // Setup screen input controls.
    public JTextField targetField;
    public JTextField minimumField;
    public JTextField maximumField;
    public JTextField dictionaryField;
    public JTextField importSaveField;
    public JButton importSaveBrowseButton;


    public JTextField player1Field;
    public JTextField player2Field;
    public JTextField humanField;
    public JComboBox<String> aiDifficultyBox;
    public JComboBox<String> firstPlayerBox;
    public JComboBox<String> playerCountBox;
    public JTextField[] playerFields;
    public JPanel[] playerRows;
    public JComboBox<String> humanCountBox;
    public JTextField[] humanFields;
    public JPanel[] humanRows;
    public JComboBox<String> aiPositionBox;
    public JComboBox<String> phase4DifficultyBox;
    public JComboBox<String> ai1DifficultyBox;
    public JComboBox<String> ai2DifficultyBox;
    public JComboBox<String> phase5FirstBox;
    public JTextField boardFileField;
    public JComboBox<String> timerSelectionBox;
    public JTextField customTimerField;
    public JComboBox<String> boardColorBox;
    public JTextField saveFileField;

    // Game screen output controls.
    public JLabel phaseLabel;
    public JLabel roundLabel;
    public JLabel currentPlayerLabel;
    public JLabel timerLabel;
    public JLabel statusLabel;
    public JLabel[][] boardLabels;
    public JPanel scorePanel;
    public JTextArea wordHistoryArea;
    public JTextField wordField;
    public JButton submitButton;
    public JButton passButton;
    public JButton shakeButton;
    public JButton quitButton;
    public JButton hintButton;
    public JButton saveButton;
    public Color boardTileColor;
    public File saveFile;

    public BoggleGUI() {
        this(null);
    }

    public BoggleGUI(File defaultDictionaryFile) {
        this.defaultDictionaryFile = defaultDictionaryFile;
        window = new JFrame("Boggle");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(850, 650);

        // mainPanel holds every screen. The string names are used with cards.show().
        cards = new CardLayout();
        mainPanel = new JPanel(cards);

        menuPanel = new JPanel();
        setupPanel = new JPanel();
        gamePanel = new JPanel();

        mainPanel.add(menuPanel, "menu");
        mainPanel.add(setupPanel, "setup");
        mainPanel.add(gamePanel, "game");

        window.add(mainPanel);

        makeMenu();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        cards.show(mainPanel, "menu");

        applyColorTheme(Color.WHITE);
    }

    /**
     * Builds the first screen. Each phase button opens the setup screen with a
     * different set of fields.
     */
    public void makeMenu() {
        menuPanel.removeAll();
        menuPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("BOGGLE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 44));
        title.setBorder(BorderFactory.createEmptyBorder(40, 10, 30, 10));

        menuPanel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.setLayout(new GridLayout(7, 1, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 230, 40, 230));

        JButton phase1 = new JButton("Phase 1: Player vs Player");
        JButton phase2 = new JButton("Phase 2: Player vs AI");
        JButton phase3 = new JButton("Phase 3: Multiplayer");
        JButton phase4 = new JButton("Phase 4: Multiplayer + AI");
        JButton phase5 = new JButton("Phase 5: AI vs AI");
        JButton quit = new JButton("Quit");

        // ActionListeners are the code that runs when a Swing button is clicked.
        phase1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(1);
            }
        });

        phase2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(2);
            }
        });

        phase3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(3);
            }
        });

        phase4.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(4);
            }
        });

        phase5.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(5);
            }
        });

        quit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttons.add(phase1);
        buttons.add(phase2);
        buttons.add(phase3);
        buttons.add(phase4);
        buttons.add(phase5);
        buttons.add(Box.createVerticalStrut(10));
        buttons.add(quit);

        menuPanel.add(buttons, BorderLayout.CENTER);
    }

    /**
     * Builds the setup form for the chosen phase.
     *
     * The common settings are added first, then phase-specific player/AI fields
     * are added below them.
     */
    public void showSetup(int phase) {
        currentPhase = phase;

        // The same setupPanel is reused each time, so clear old controls first.
        setupPanel.removeAll();
        setupPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel(getPhaseName(phase) + " Setup", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        setupPanel.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setOpaque(false);
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        // Default values make the form usable without typing every setting.
        targetField = new JTextField("0");
        minimumField = new JTextField("3");
        maximumField = new JTextField("0");
        String dictDefault = "src/wordlist.txt";
        if (defaultDictionaryFile != null) {
            try {
                if (defaultDictionaryFile.exists()) {
                    dictDefault = defaultDictionaryFile.getAbsolutePath();
                }
            } catch (Exception ignored) {
            }
        }
        dictionaryField = new JTextField(dictDefault);
        importSaveField = new JTextField("");
        importSaveBrowseButton = new JButton("Browse...");
        importSaveBrowseButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(new File("."));
                int result = chooser.showOpenDialog(window);
                if (result == JFileChooser.APPROVE_OPTION) {
                    importSaveField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        timerSelectionBox = new JComboBox<String>(new String[] {"None", "15 sec", "30 sec", "60 sec", "Other"});
        customTimerField = new JTextField("30");
        customTimerField.setEnabled(false);
        boardColorBox = new JComboBox<String>(new String[] {"White", "Blue", "Green", "Yellow", "Pink"});
        saveFileField = new JTextField("boggleSave.txt");

        fields.add(makeTextRow("Point target:", targetField));
        fields.add(makeTextRow("Minimum word length:", minimumField));
        if (phase == 5) {
            fields.add(makeTextRow("Maximum word length (0 = no limit):", maximumField));
        }

        JPanel importSaveRow = makeFileBrowseRow("Import Save File (Optional):", importSaveField, importSaveBrowseButton);
        importSaveRow.setVisible(phase != 1 && phase != 3 && phase != 5);
        fields.add(importSaveRow);

        fields.add(makeComboRow("Turn timer:", timerSelectionBox));
        fields.add(makeTextRow("Custom timer (sec):", customTimerField));
        fields.add(makeComboRow("Board color:", boardColorBox));
        fields.add(makeTextRow("Save file:", saveFileField));


        boardColorBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                applyColorTheme(getSelectedBoardColor());
            }
        });

        applyColorTheme(getSelectedBoardColor());

        // Only enable the custom timer box when "Other" is selected.
        timerSelectionBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                customTimerField.setEnabled(timerSelectionBox.getSelectedIndex() == 4);
            }
        });

        // Each phase has different players, so each branch adds different fields.
        if (phase == 1) {
            player1Field = new JTextField("Player 1");
            player2Field = new JTextField("Player 2");

            fields.add(makeTextRow("Player 1 name:", player1Field));
            fields.add(makeTextRow("Player 2 name:", player2Field));
        } else if (phase == 2) {
            humanField = new JTextField("Human");
            aiDifficultyBox = makeDifficultyBox();
            firstPlayerBox = new JComboBox<String>(new String[] {"Human first", "AI first"});

            fields.add(makeTextRow("Human player name:", humanField));
            fields.add(makeComboRow("AI difficulty:", aiDifficultyBox));
            fields.add(makeComboRow("Who goes first:", firstPlayerBox));
        } else if (phase == 3) {
            playerCountBox = new JComboBox<String>(new String[] {"2", "3", "4", "5", "6"});
            playerFields = new JTextField[6];
            playerRows = new JPanel[6];

            fields.add(makeComboRow("Number of players:", playerCountBox));

            for (int i = 0; i < 6; i++) {
                playerFields[i] = new JTextField("Player " + (i + 1));
                playerRows[i] = makeTextRow("Player " + (i + 1) + " name:", playerFields[i]);
                fields.add(playerRows[i]);
            }

            playerCountBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePlayerRows();
                }
            });

            updatePlayerRows();
        } else if (phase == 4) {
            humanCountBox = new JComboBox<String>(new String[] {"1", "2", "3", "4", "5", "6"});
            humanFields = new JTextField[6];
            humanRows = new JPanel[6];
            phase4DifficultyBox = makeDifficultyBox();
            aiPositionBox = new JComboBox<String>();

            fields.add(makeComboRow("Number of human players:", humanCountBox));

            for (int i = 0; i < 6; i++) {
                humanFields[i] = new JTextField("Human " + (i + 1));
                humanRows[i] = makeTextRow("Human " + (i + 1) + " name:", humanFields[i]);
                fields.add(humanRows[i]);
            }

            fields.add(makeComboRow("AI difficulty:", phase4DifficultyBox));
            fields.add(makeComboRow("AI turn position:", aiPositionBox));

            humanCountBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updateHumanRows();
                    updateAIPositions();
                }
            });

            updateHumanRows();
            updateAIPositions();
        } else if (phase == 5) {
            phase5FirstBox = new JComboBox<String>(new String[] {"My AI first", "Opponent AI first"});
            boardFileField = new JTextField("setBoard.txt");

            fields.add(makeComboRow("Who goes first:", phase5FirstBox));
            fields.add(makeTextRow("Board file:", boardFileField));
        }

        // A scroll pane keeps the setup screen usable when many player fields exist.
        JScrollPane scroll = new JScrollPane(fields);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(null);
        setupPanel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);

        JButton back = new JButton("Back");
        JButton start = new JButton("Start Game");

        back.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cards.show(mainPanel, "menu");
            }
        });

        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        bottom.add(back);
        bottom.add(start);

        setupPanel.add(bottom, BorderLayout.SOUTH);
        setupPanel.revalidate();
        setupPanel.repaint();

        cards.show(mainPanel, "setup");
    }

    /** Creates one label + text box row for the setup form. */
    public JPanel makeTextRow(String text, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(700, 45));
        row.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(230, 30));

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    /** Creates one label + dropdown row for the setup form. */
    public JPanel makeComboRow(String text, JComboBox<String> box) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(700, 45));
        row.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(230, 30));

        row.add(label, BorderLayout.WEST);
        row.add(box, BorderLayout.CENTER);

        return row;
    }

    public JPanel makeFileBrowseRow(String text, JTextField field, JButton button) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(700, 45));
        row.setOpaque(false);

        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(230, 30));

        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        rightPanel.add(field, BorderLayout.CENTER);
        rightPanel.add(button, BorderLayout.EAST);
        rightPanel.setOpaque(false);

        row.add(label, BorderLayout.WEST);
        row.add(rightPanel, BorderLayout.CENTER);

        return row;
    }

    /** All AI difficulty dropdowns use the same choices. */
    public JComboBox<String> makeDifficultyBox() {
        return new JComboBox<String>(new String[] {"Easy", "Medium", "Hard"});
    }

    /** Shows only the player-name rows needed for the selected player count. */
    public void updatePlayerRows() {
        int count = getComboNumber(playerCountBox);

        for (int i = 0; i < 6; i++) {
            playerRows[i].setVisible(i < count);
        }

        setupPanel.revalidate();
        setupPanel.repaint();
    }

    /** Shows only the human-name rows needed for the selected human count. */
    public void updateHumanRows() {
        int count = getComboNumber(humanCountBox);

        for (int i = 0; i < 6; i++) {
            humanRows[i].setVisible(i < count);
        }

        setupPanel.revalidate();
        setupPanel.repaint();
    }

    /** Rebuilds the AI position dropdown after the human count changes. */
    public void updateAIPositions() {
        int count = getComboNumber(humanCountBox);

        aiPositionBox.removeAllItems();

        for (int i = 1; i <= count + 1; i++) {
            aiPositionBox.addItem("" + i);
        }
    }

    /**
     * Reads the setup form, validates values, creates the player list, and starts
     * a new GameSession.
     */
    public void startGame() {
        try {
            // Text fields store strings, so numbers must be parsed before use.
            int targetScore = Integer.parseInt(targetField.getText().trim());
            int minimumLength = Integer.parseInt(minimumField.getText().trim());
            int maximumLength = 0;
            if (currentPhase == 5) {
                maximumLength = Integer.parseInt(maximumField.getText().trim());
            }
            File dictionaryFile = new File(dictionaryField.getText().trim());
            boardTileColor = getSelectedBoardColor();
            saveFile = new File(saveFileField.getText().trim());
            
            // Convert the timer dropdown into useTurnTimer + timerSeconds.
            int timerSelection = timerSelectionBox.getSelectedIndex();
            if (timerSelection == 0) {
                useTurnTimer = false;
                timerSeconds = 0;
            } else if (timerSelection == 1) {
                useTurnTimer = true;
                timerSeconds = 15;
            } else if (timerSelection == 2) {
                useTurnTimer = true;
                timerSeconds = 30;
            } else if (timerSelection == 3) {
                useTurnTimer = true;
                timerSeconds = 60;
            } else { // Custom
                try {
                    timerSeconds = Integer.parseInt(customTimerField.getText().trim());
                    if (timerSeconds <= 0) {
                        JOptionPane.showMessageDialog(window, "Custom timer must be greater than 0.");
                        return;
                    }
                    useTurnTimer = true;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(window, "Please enter a valid custom timer value.");
                    return;
                }
            }
            
            hintUsedThisRound = false;

            if (targetScore < 0) {
                JOptionPane.showMessageDialog(window, "Target cannot be negative.");
                return;
            }

            if (minimumLength < 3) {
                JOptionPane.showMessageDialog(window, "Minimum word length must be at least 3.");
                return;
            }

            if (maximumLength < 0) {
                JOptionPane.showMessageDialog(window, "Maximum word length cannot be negative.");
                return;
            }

            if (maximumLength > 0 && maximumLength < minimumLength) {
                JOptionPane.showMessageDialog(window, "Maximum word length must be at least the minimum, or 0 for no limit.");
                return;
            }

            if (!dictionaryFile.exists()) {
                JOptionPane.showMessageDialog(window, "Dictionary file was not found.");
                return;
            }

            // Show the same rules text used by the command-line version.
            JTextArea rulesArea = new JTextArea(BoggleGame.getRulesText(), 7, 42);
            rulesArea.setEditable(false);
            rulesArea.setLineWrap(true);
            rulesArea.setWrapStyleWord(true);
            JOptionPane.showMessageDialog(window, new JScrollPane(rulesArea), "Boggle rules",
                    JOptionPane.INFORMATION_MESSAGE);

            String importPath = importSaveField == null ? "" : importSaveField.getText().trim();
            if (importPath.length() > 0 && currentPhase != 1 && currentPhase != 3 && currentPhase != 5) {
                File importFile = new File(importPath);
                if (!importFile.exists()) {
                    JOptionPane.showMessageDialog(window, "Import save file not found.");
                    return;
                }
                game = GameSession.loadGame(importFile, dictionaryFile, minimumLength, targetScore);
                for (int i = 0; i < game.players.size(); i++) {
                    Player p = game.players.get(i);
                    if (currentPhase == 2) {
                        if (p.name.equalsIgnoreCase("AI")) {
                            p.isAI = true;
                            p.difficulty = getComboText(aiDifficultyBox).toUpperCase();
                        } else {
                            p.isAI = false;
                        }
                    } else if (currentPhase == 4) {
                        if (p.name.equalsIgnoreCase("AI")) {
                            p.isAI = true;
                            p.difficulty = getComboText(phase4DifficultyBox).toUpperCase();
                        } else {
                            p.isAI = false;
                        }
                    }
                }
            } else {
                // Build players in the exact order they should take turns.
                ArrayList<Player> players = new ArrayList<Player>();

                if (currentPhase == 1) {
                    players.add(new Player(player1Field.getText()));
                    players.add(new Player(player2Field.getText()));
                } else if (currentPhase == 2) {
                    Player human = new Player(humanField.getText());
                    Player ai = BoggleAI.createAIPlayer("AI", getComboText(aiDifficultyBox));

                    if (firstPlayerBox.getSelectedIndex() == 0) {
                        players.add(human);
                        players.add(ai);
                    } else {
                        players.add(ai);
                        players.add(human);
                    }
                } else if (currentPhase == 3) {
                    int count = getComboNumber(playerCountBox);

                    for (int i = 0; i < count; i++) {
                        players.add(new Player(playerFields[i].getText()));
                    }
                } else if (currentPhase == 4) {
                    int humanCount = getComboNumber(humanCountBox);
                    int aiPosition = getComboNumber(aiPositionBox);

                    // Insert the AI at the selected turn position and humans around it.
                    for (int i = 0; i < humanCount + 1; i++) {
                        if (i == aiPosition - 1) {
                            players.add(BoggleAI.createAIPlayer("AI", getComboText(phase4DifficultyBox)));
                        } else {
                            int humanNumber = i;
                            if (i > aiPosition - 1) {
                                humanNumber = i - 1;
                            }

                            players.add(new Player(humanFields[humanNumber].getText()));
                        }
                    }
                } else {
                    Player myAI = BoggleAI.createAIPlayer("My AI", "HARD");
                    Player opponentAI = new Player("Opponent AI");

                    if (phase5FirstBox.getSelectedIndex() == 0) {
                        players.add(myAI);
                        players.add(opponentAI);
                    } else {
                        players.add(opponentAI);
                        players.add(myAI);
                    }
                }

                // From here onward, GameSession owns the rules and board state.
                if (currentPhase == 5) {
                    game = new GameSession(players, minimumLength, targetScore, dictionaryFile, maximumLength);
                } else {
                    game = new GameSession(players, minimumLength, targetScore, dictionaryFile);
                }
            }

            if (currentPhase == 5 && boardFileField.getText().trim().length() > 0) {
                // Phase 5 can use a fixed board so both AIs compete on the same letters.
                char[][] fileBoard = readBoardFile(new File(boardFileField.getText().trim()));

                if (fileBoard == null) {
                    JOptionPane.showMessageDialog(window, "Board file was not valid.");
                    return;
                }

                game.board = fileBoard;
            }

            // Build the game screen after the session exists, then begin turn 1.
            makeGamePanel();
            cards.show(mainPanel, "game");
            nextTurn();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Please check the setup fields.");
        }
    }

    /**
     * Builds the main game screen: top labels, board grid, score area, word
     * history, input field, and action buttons.
     */
    public void makeGamePanel() {
        gamePanel.removeAll();
        gamePanel.setLayout(new BorderLayout(10, 10));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        gamePanel.setBackground(boardTileColor);

        JPanel top = new JPanel(new GridLayout(2, 1));
        top.setOpaque(false);

        phaseLabel = new JLabel(getPhaseName(currentPhase), SwingConstants.CENTER);
        phaseLabel.setFont(new Font("Arial", Font.BOLD, 24));

        roundLabel = new JLabel("", SwingConstants.CENTER);

        top.add(phaseLabel);
        top.add(roundLabel);

        gamePanel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setOpaque(false);

        // The board grid uses one JLabel for each letter on the GameSession board.
        JPanel boardPanel = new JPanel(new GridLayout(GameSession.BOARD_SIZE, GameSession.BOARD_SIZE, 5, 5));
        boardPanel.setOpaque(false);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        boardLabels = new JLabel[GameSession.BOARD_SIZE][GameSession.BOARD_SIZE];

        for (int row = 0; row < GameSession.BOARD_SIZE; row++) {
            for (int col = 0; col < GameSession.BOARD_SIZE; col++) {
                JLabel label = new JLabel("", SwingConstants.CENTER);
                label.setFont(new Font("Arial", Font.BOLD, 28));
                label.setOpaque(true);
                label.setBackground(Color.WHITE);
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));

                boardLabels[row][col] = label;
                boardPanel.add(label);
            }
        }

        center.add(boardPanel, BorderLayout.CENTER);

        // The right side shows changing game information.
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(270, 400));
        side.setOpaque(false);

        currentPlayerLabel = new JLabel("Current Player:");
        timerLabel = new JLabel("Timer:");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        scorePanel = new JPanel();
        scorePanel.setOpaque(false);
        scorePanel.setBorder(BorderFactory.createTitledBorder("Scores"));

        wordHistoryArea = new JTextArea();
        wordHistoryArea.setEditable(false);

        JScrollPane historyScroll = new JScrollPane(wordHistoryArea);
        historyScroll.setBorder(BorderFactory.createTitledBorder("Word History"));

        side.add(currentPlayerLabel);
        side.add(Box.createVerticalStrut(10));
        side.add(timerLabel);
        side.add(Box.createVerticalStrut(10));
        side.add(scorePanel);
        side.add(Box.createVerticalStrut(10));
        side.add(historyScroll);

        center.add(side, BorderLayout.EAST);

        gamePanel.add(center, BorderLayout.CENTER);

        // The bottom area accepts words and exposes turn actions.
        JPanel bottom = new JPanel(new BorderLayout(5, 5));
        bottom.setOpaque(false);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setOpaque(false);

        wordField = new JTextField();
        submitButton = new JButton("Submit");

        inputPanel.add(wordField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        JPanel buttons = new JPanel();
        buttons.setOpaque(false);

        passButton = new JButton("Pass");
        shakeButton = new JButton("Shake Board");
        quitButton = new JButton("Quit");
        hintButton = new JButton("Hint");
        saveButton = new JButton("Save Game");
        saveButton.setVisible(currentPhase == 2 || currentPhase == 4);


        buttons.add(passButton);
        buttons.add(shakeButton);
        buttons.add(quitButton);
        buttons.add(hintButton);
        buttons.add(saveButton);

        statusLabel = new JLabel("Ready.");

        bottom.add(inputPanel, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);
        bottom.add(statusLabel, BorderLayout.SOUTH);

        gamePanel.add(bottom, BorderLayout.SOUTH);

        // Pressing the Submit button or Enter in the text field does the same thing.
        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submitWord();
            }
        });

        wordField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                submitWord();
            }
        });

        passButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                passTurn();
            }
        });

        shakeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                shakeBoard();
            }
        });

        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                quitPlayer();
            }
        });

        hintButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showHint();
            }
        });

        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveGameFromGui(true);
            }
        });

        // Fill the newly created labels with the current game state.
        updateBoard();
        updateScores();
        updateRoundLabel();
    }

    /** Sets up the screen for whoever is about to play next. */
    public void nextTurn() {
        Player player = game.getCurrentPlayer();

        currentPlayerLabel.setText("Current Player: " + player.name);
        updateRoundLabel();
        updateScores();
        updateBoard();

        if (player.isAI) {
            if (shouldStopForPassedHuman(player)) {
                offerShakeAfterAILead(player);
                return;
            }

            // AI turns do not need typing controls; wait briefly so the UI visibly updates.
            setInput(false);
            statusLabel.setText(player.name + " is thinking...");
            stopTimer();
            timerLabel.setForeground(Color.BLACK);
            timerLabel.setText("Timer: —");

            Timer aiTimer = new Timer(900, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    aiMove();
                }
            });

            aiTimer.setRepeats(false);
            aiTimer.start();
        } else {
            // Human turns clear the previous word and start the timer if enabled.
            setInput(true);
            wordField.setText("");
            statusLabel.setText(player.name + "'s turn.");
            if (useTurnTimer) {
                startTimer();
            } else {
                stopTimer();
                timerLabel.setForeground(Color.BLACK);
                timerLabel.setText("Timer: off");
            }
        }
    }

    /** Starts or restarts the per-turn countdown for human players. */
    public void startTimer() {
        stopTimer();

        if (!useTurnTimer) {
            timerLabel.setForeground(Color.BLACK);
            timerLabel.setText("Timer: off");
            return;
        }

        timeLeft = timerSeconds;
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setText("Timer: " + timeLeft);

        // Swing Timer fires on the GUI thread once per second.
        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLeft = timeLeft - 1;
                timerLabel.setText("Timer: " + timeLeft);

                if (timeLeft <= 5) {
                    timerLabel.setForeground(Color.RED);
                }

                if (timeLeft <= 0) {
                    stopTimer();
                    game.timeout();
                    statusLabel.setText("Time is up. Turn passed.");
                    afterTurn();
                }
            }
        });

        timer.start();
    }

    /** Handles a human word submission from the button or Enter key. */
    public void submitWord() {
        Player player = game.getCurrentPlayer();
        String word = wordField.getText();

        if (word == null) {
            word = "";
        }

        word = word.trim().toUpperCase();

        if (word.length() == 0) {
            statusLabel.setText("Type a word first.");
            return;
        }

        stopTimer();

        // Store the old score so the GUI can show how many points this word added.
        oldScore = player.totalScore;

        int result = game.submitWord(word);

        if (result == 1) {
            int points = player.totalScore - oldScore;

            statusLabel.setText("Word accepted: " + word);
            addWordHistory(player.name, word, points);
            updateScores();
            afterTurn();
        } else if (result == 2) {
            statusLabel.setText("That word was used before.");
            // Used words do not end the turn, so resume the timer.
            startTimer();
        } else {
            addWrongWordHistory(player.name, word);
            updateScores();

            if (game.forcedWinner != null) {
                statusLabel.setText("Two wrong guesses. " + game.forcedWinner.name + " wins.");
                afterTurn();
            } else if (currentPhase == 2 && !player.isAI) {
                statusLabel.setText("Invalid word: " + word + ". AI's turn.");
                afterTurn();
            } else if (player.passed) {
                statusLabel.setText("Too many wrong guesses. Turn passed.");
                afterTurn();
            } else {
                statusLabel.setText("Invalid word. Check length, dictionary, and board path.");
                startTimer();
            }
        }
    }

    /** Passes the current human player's turn. */
    public void passTurn() {
        stopTimer();
        game.pass();
        statusLabel.setText(game.getCurrentPlayer().name + " passed.");
        afterTurn();
    }

    /**
     * Lets the current player shake the board, but only after that player has
     * passed and only once per game.
     */
    public void shakeBoard() {
        // Shake is a one-time comeback option.
        if (game.isShakeUpUsed()) {
            statusLabel.setText("Board can only be shaken once per game.");
            return;
        }
        
        // A player must pass before using the shake option.
        Player player = game.getCurrentPlayer();
        if (!player.passed) {
            statusLabel.setText("Can only shake when player has passed.");
            return;
        }

        // Pause countdown while modal is open; otherwise timer can expire during the dialog.
        boolean humanTurn = !game.getCurrentPlayer().isAI;
        if (humanTurn) {
            stopTimer();
        }

        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to shake the board?");

        if (answer == JOptionPane.YES_OPTION) {
            stopTimer();
            game.performShake();
            wordHistoryArea.setText("");
            statusLabel.setText("Board was shaken.");
            nextTurn();
        } else {
            if (!game.getCurrentPlayer().isAI) {
                startTimer();
            }
        }
    }

    /** Confirms a quit action and optionally saves before removing the player. */
    public void quitPlayer() {
        boolean humanTurn = !game.getCurrentPlayer().isAI;
        if (humanTurn) {
            stopTimer();
        }

        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to quit?");

        if (answer == JOptionPane.YES_OPTION) {
            int saveAnswer = JOptionPane.showConfirmDialog(window, "Save game to file before quitting?");
            if (saveAnswer == JOptionPane.YES_OPTION && !saveGameFromGui(false)) {
                if (humanTurn) {
                    startTimer();
                }
                return;
            }
            stopTimer();
            game.quit();
            statusLabel.setText(game.getCurrentPlayer().name + " quit.");
            afterTurn();
        } else {
            if (!game.getCurrentPlayer().isAI) {
                startTimer();
            }
        }
    }

    /** Runs one AI turn and writes the result into the status/history areas. */
    public void aiMove() {
        Player player = game.getCurrentPlayer();
        AIResult result = game.runAITurnIfNeeded();

        if (result.passed) {
            statusLabel.setText(player.name + " passed.");
        } else {
            statusLabel.setText(player.name + " played " + result.word + ".");
            addWordHistory(player.name, result.word, result.points);
        }

        updateScores();
        if (!result.passed && shouldStopForPassedHuman(player)) {
            offerShakeAfterAILead(player);
            return;
        }
        afterTurn();
    }

    /** In Player vs AI, stop AI scoring once it has overtaken a passed human. */
    public boolean shouldStopForPassedHuman(Player aiPlayer) {
        return currentPhase == 2 && game.shouldOfferShakeAfterAILead(aiPlayer);
    }

    /** Returns the passed human who should get the next chance after a shake. */
    public int getPassedHumanIndexBehind(Player aiPlayer) {
        if (currentPhase != 2) return -1;
        return game.getPassedHumanIndexBehindAI(aiPlayer);
    }

    /** Offers the passed human a board shake after the AI takes the lead. */
    public void offerShakeAfterAILead(Player aiPlayer) {
        stopTimer();
        setInput(false);
        updateScores();
        int restartIndex = getPassedHumanIndexBehind(aiPlayer);

        if (game.isShakeUpUsed()) {
            statusLabel.setText(aiPlayer.name + " is ahead. Game over.");
            endGame();
            return;
        }

        int answer = JOptionPane.showConfirmDialog(
                window,
                aiPlayer.name + " is now ahead. Shake the board?");

        if (answer == JOptionPane.YES_OPTION) {
            game.performShake();
            if (restartIndex >= 0) {
                game.currentTurnIndex = restartIndex;
            }
            wordHistoryArea.setText("");
            statusLabel.setText("Board was shaken.");
            nextTurn();
        } else {
            statusLabel.setText(aiPlayer.name + " is ahead. Game over.");
            endGame();
        }
    }

    /**
     * Advances the model to the next turn, then decides which screen/action comes
     * next based on the status code returned by GameSession.nextTurn().
     */
    public void afterTurn() {
        updateRoundLabel();
        updateScores();

        int status = game.nextTurn();

        if (status == 2) {
            endGame();
            return;
        }

        if (status == 1) {
            askShakeOrEnd();
            return;
        }

        nextTurn();
    }

    /** Called when every active player has passed. */
    public void askShakeOrEnd() {
        stopTimer();
        setInput(false);

        if (currentPhase == 5 || game.isShakeUpUsed()) {
            endGame();
            return;
        }

        int answer = JOptionPane.showConfirmDialog(window, "All players passed. Shake the board?");

        if (answer == JOptionPane.YES_OPTION) {
            game.performShake();
            wordHistoryArea.setText("");
            statusLabel.setText("Board was shaken.");
            nextTurn();
        } else {
            endGame();
        }
    }

    /** Disables input, shows final scores, and returns to the menu. */
    public void endGame() {
        stopTimer();
        setInput(false);
        updateScores();

        if (saveFile != null) {
            game.writeLog(saveFile);
        }


        Player winner = game.determineWinner();
        String message = "Game ended.\n";

        if (winner == null) {
            message = message + "Winner: NONE\n";
        } else {
            message = message + "Winner: " + winner.name + "\n";
        }

        message = message + "\nScores:\n";

        for (int i = 0; i < game.players.size(); i++) {
            Player player = game.players.get(i);

            message = message + player.name + ": " + player.totalScore + "\n";
        }

        JOptionPane.showMessageDialog(window, message);
        cards.show(mainPanel, "menu");
    }

    /** Copies the current round number from GameSession into the label. */
    public void updateRoundLabel() {
        roundLabel.setText("Round: " + game.currentRound);
    }

    /** Rebuilds the score table from the current players list. */
    public void updateScores() {
        scorePanel.removeAll();
        scorePanel.setLayout(new GridLayout(game.players.size() + 1, 3, 5, 5));

        scorePanel.add(new JLabel("Player"));
        scorePanel.add(new JLabel("Score"));
        scorePanel.add(new JLabel("Status"));

        for (int i = 0; i < game.players.size(); i++) {
            Player player = game.players.get(i);
            String status = "Active";

            if (player.quit) {
                status = "Quit";
            } else if (player.passed) {
                status = "Passed";
            }

            scorePanel.add(new JLabel(player.name));
            scorePanel.add(new JLabel("" + player.totalScore));
            scorePanel.add(new JLabel(status));
        }

        scorePanel.revalidate();
        scorePanel.repaint();
    }

    /** Adds one accepted word to the scrolling word history. */
    public void addWordHistory(String playerName, String word, int points) {
        wordHistoryArea.append(playerName + " - " + word + " (+" + points + ")\n");
        wordHistoryArea.setCaretPosition(wordHistoryArea.getDocument().getLength());
    }

    /** Adds an invalid submitted word to the scrolling word history. */
    public void addWrongWordHistory(String playerName, String word) {
        wordHistoryArea.append(playerName + " - " + word + " (wrong)\n");
        wordHistoryArea.setCaretPosition(wordHistoryArea.getDocument().getLength());
    }

    /** Enables or disables controls that only make sense during human turns. */
    public void setInput(boolean on) {
        wordField.setEnabled(on);
        submitButton.setEnabled(on);
        passButton.setEnabled(on);
        quitButton.setEnabled(on);
        hintButton.setEnabled(on);
        saveButton.setEnabled(game != null && on && (currentPhase == 2 || currentPhase == 4));
    }

    /** Copies every board letter from GameSession into the board labels. */
    public void updateBoard() {
        for (int row = 0; row < GameSession.BOARD_SIZE; row++) {
            for (int col = 0; col < GameSession.BOARD_SIZE; col++) {
                boardLabels[row][col].setText("" + game.board[row][col]);
                boardLabels[row][col].setBackground(boardTileColor == null ? Color.WHITE : boardTileColor);
            }
        }
    }

    /**
     * Saves the current game. When allowChooseFile is true, the user can pick a
     * new location with JFileChooser.
     */
    public boolean saveGameFromGui(boolean allowChooseFile) {
        if (game == null) {
            return false;
        }

        File file = saveFile;
        if (file == null || file.getPath().trim().length() == 0 || allowChooseFile) {
            JFileChooser chooser = new JFileChooser(new File("."));
            if (file != null && file.getPath().trim().length() > 0) {
                chooser.setSelectedFile(file);
            } else {
                chooser.setSelectedFile(new File("boggleSave.txt"));
            }

            int result = chooser.showSaveDialog(window);
            if (result != JFileChooser.APPROVE_OPTION) {
                return false;
            }
            file = chooser.getSelectedFile();
            saveFile = file;
        }

        try {
            boolean saved = GameSession.saveGame(
                    file,
                    game.currentRound,
                    game.board,
                    game.players,
                    game.usedWords,
                    game.isShakeUpUsed());
            if (!saved) {
                JOptionPane.showMessageDialog(window, "Could not save game file.");
                return false;
            }
            statusLabel.setText("Saved game to " + file.getName() + ".");
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Could not save game file.");
            return false;
        }
    }

    /** Finds and displays one valid word without submitting it. */
    public void showHint() {
        // Hints are intentionally limited to once per game.
        if (game.isHintUsed()) {
            statusLabel.setText("Hint can only be used once per game.");
            return;
        }

        // Ask the AI helper for every valid unused word, then choose a strong hint.
        ArrayList<String> words = game.boggleAI.findAllValidWords(
                game.board, game.dictionary, game.minimumWordLength, game.usedWords);

        if (words.size() == 0) {
            statusLabel.setText("No hint available.");
            return;
        }

        String hint = game.boggleAI.chooseWord(words, "HARD");
        game.markHintUsed();
        hintUsedThisRound = true;

        statusLabel.setText("Hint: " + hint);
    }

    /** Stops the active Swing timer, if one exists. */
    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    /** Reads a numeric dropdown value such as "2" or "6". */
    public int getComboNumber(JComboBox<String> box) {
        String text = (String) box.getSelectedItem();

        return Integer.parseInt(text);
    }

    /** Safely reads the selected text from a dropdown. */
    public String getComboText(JComboBox<String> box) {
        String text = (String) box.getSelectedItem();

        if (text == null) {
            text = "";
        }

        return text;
    }

    /** Converts the board color dropdown into a Java Color object. */
    public Color getSelectedBoardColor() {
        String color = getComboText(boardColorBox);

        if (color.equals("Blue")) {
            return new Color(210, 230, 255);
        } else if (color.equals("Green")) {
            return new Color(215, 245, 220);
        } else if (color.equals("Yellow")) {
            return new Color(255, 245, 190);
        } else if (color.equals("Pink")) {
            return new Color(255, 220, 235);
        }

        return Color.WHITE;
    }

    /** Converts phase numbers into the titles shown on the GUI. */
    public String getPhaseName(int phase) {
        if (phase == 1) {
            return "Phase 1: Player vs Player";
        } else if (phase == 2) {
            return "Phase 2: Player vs AI";
        } else if (phase == 3) {
            return "Phase 3: Multiplayer";
        } else if (phase == 4) {
            return "Phase 4: Multiplayer + AI";
        } else {
            return "Phase 5: AI vs AI";
        }
    }

    public void applyColorTheme(Color c) {
        boardTileColor = c;
        if (window != null && window.getContentPane() != null) {
            window.getContentPane().setBackground(c);
        }
        if (mainPanel != null) {
            mainPanel.setBackground(c);
            mainPanel.setOpaque(true);
        }
        if (menuPanel != null) {
            menuPanel.setBackground(c);
            menuPanel.setOpaque(true);
        }
        if (setupPanel != null) {
            setupPanel.setBackground(c);
            setupPanel.setOpaque(true);
        }
        if (gamePanel != null) {
            gamePanel.setBackground(c);
            gamePanel.setOpaque(true);
        }
        if (game != null) {
            updateBoard();
        }
    }

    /**
     * Reads a board file for AI vs AI. The first 25 letters become the
     * 5-by-5 board, read left-to-right and top-to-bottom.
     */
    public char[][] readBoardFile(File file) {
        try {
            if (!file.exists()) {
                return null;
            }

            Scanner scanner = new Scanner(new FileReader(file));
            String letters = "";

            while (scanner.hasNext()) {
                letters = letters + scanner.next();
            }

            scanner.close();
            letters = letters.toUpperCase();

            int need = GameSession.BOARD_SIZE * GameSession.BOARD_SIZE;
            if (letters.length() < need) {
                return null;
            }

            char[][] board = new char[GameSession.BOARD_SIZE][GameSession.BOARD_SIZE];
            int number = 0;

            for (int row = 0; row < GameSession.BOARD_SIZE; row++) {
                for (int col = 0; col < GameSession.BOARD_SIZE; col++) {
                    board[row][col] = letters.charAt(number);
                    number = number + 1;
                }
            }

            return board;
        } catch (Exception e) {
            return null;
        }
    }

    /** Starts the GUI directly. */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BoggleGUI(null);
            }
        });
    }
}
