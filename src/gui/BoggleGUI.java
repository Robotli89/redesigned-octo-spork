package gui;

import game.GameSession;
import logic.ScoringManager;
import model.AI;
import model.Player;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
import java.util.List;
import java.util.Scanner;

public class BoggleGUI {
    public JFrame window;
    public CardLayout cardLayout;
    public JPanel allPanels;
    public JPanel mainMenuPanel;
    public JPanel setupPanel;
    public JPanel gamePanel;

    public int currentPhase;
    public int numberOfRounds;
    public int timeLeft;
    public int lastScoreBeforeWord;

    public GameSession gameSession;
    public Timer timer;

    public JTextField roundsField;
    public JTextField targetField;
    public JTextField minimumField;
    public JTextField dictionaryField;

    public JTextField player1Field;
    public JTextField player2Field;
    public JTextField humanField;
    public JComboBox<String> aiDifficultyBox;
    public JComboBox<String> firstPlayerBox;
    public JComboBox<String> playerCountBox;
    public JTextField[] playerNameFields;
    public JComboBox<String> humanCountBox;
    public JTextField[] humanNameFields;
    public JComboBox<String> aiTurnPositionBox;
    public JComboBox<String> phase4DifficultyBox;
    public JComboBox<String> ai1DifficultyBox;
    public JComboBox<String> ai2DifficultyBox;
    public JComboBox<String> phase5FirstBox;
    public JTextField boardFileField;

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

    public BoggleGUI() {
        window = new JFrame("Boggle");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(850, 650);

        cardLayout = new CardLayout();
        allPanels = new JPanel(cardLayout);

        mainMenuPanel = new JPanel();
        setupPanel = new JPanel();
        gamePanel = new JPanel();

        allPanels.add(mainMenuPanel, "menu");
        allPanels.add(setupPanel, "setup");
        allPanels.add(gamePanel, "game");

        window.add(allPanels);

        createMainMenu();

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        cardLayout.show(allPanels, "menu");
    }

    public void createMainMenu() {
        mainMenuPanel.removeAll();
        mainMenuPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("BOGGLE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 44));
        title.setBorder(BorderFactory.createEmptyBorder(40, 10, 30, 10));
        mainMenuPanel.add(title, BorderLayout.NORTH);

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(7, 1, 10, 10));
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 230, 40, 230));

        JButton phase1Button = new JButton("Phase 1: Player vs Player");
        JButton phase2Button = new JButton("Phase 2: Player vs AI");
        JButton phase3Button = new JButton("Phase 3: Multiplayer");
        JButton phase4Button = new JButton("Phase 4: Multiplayer + AI");
        JButton phase5Button = new JButton("Phase 5: AI vs AI Contest");
        JButton quitButton = new JButton("Quit");

        phase1Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(1);
            }
        });

        phase2Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(2);
            }
        });

        phase3Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(3);
            }
        });

        phase4Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(4);
            }
        });

        phase5Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showSetup(5);
            }
        });

        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        buttonsPanel.add(phase1Button);
        buttonsPanel.add(phase2Button);
        buttonsPanel.add(phase3Button);
        buttonsPanel.add(phase4Button);
        buttonsPanel.add(phase5Button);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(quitButton);

        mainMenuPanel.add(buttonsPanel, BorderLayout.CENTER);
    }

    public void showSetup(int phase) {
        currentPhase = phase;

        setupPanel.removeAll();
        setupPanel.setLayout(new BorderLayout());

        JLabel setupTitle = new JLabel(getPhaseName(phase) + " Setup", SwingConstants.CENTER);
        setupTitle.setFont(new Font("Arial", Font.BOLD, 28));
        setupTitle.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        setupPanel.add(setupTitle, BorderLayout.NORTH);

        JPanel fieldsPanel = new JPanel();
        fieldsPanel.setLayout(new BoxLayout(fieldsPanel, BoxLayout.Y_AXIS));
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        roundsField = new JTextField("5");
        targetField = new JTextField("0");
        minimumField = new JTextField("3");
        dictionaryField = new JTextField("src/wordlist.txt");

        fieldsPanel.add(makeRow("Number of rounds:", roundsField));
        fieldsPanel.add(makeRow("Point target (0 = no target):", targetField));
        fieldsPanel.add(makeRow("Minimum word length:", minimumField));
        fieldsPanel.add(makeRow("Dictionary file:", dictionaryField));

        if (phase == 1) {
            player1Field = new JTextField("Player 1");
            player2Field = new JTextField("Player 2");

            fieldsPanel.add(makeRow("Player 1 name:", player1Field));
            fieldsPanel.add(makeRow("Player 2 name:", player2Field));
        } else if (phase == 2) {
            humanField = new JTextField("Human");
            aiDifficultyBox = makeDifficultyBox();
            firstPlayerBox = new JComboBox<String>(new String[] {"Human first", "AI first"});

            fieldsPanel.add(makeRow("Human player name:", humanField));
            fieldsPanel.add(makeRow("AI difficulty:", aiDifficultyBox));
            fieldsPanel.add(makeRow("Who goes first:", firstPlayerBox));
        } else if (phase == 3) {
            playerCountBox = new JComboBox<String>(new String[] {"2", "3", "4", "5", "6"});
            playerNameFields = new JTextField[6];

            fieldsPanel.add(makeRow("Number of players:", playerCountBox));

            for (int i = 0; i < playerNameFields.length; i++) {
                playerNameFields[i] = new JTextField("Player " + (i + 1));
                fieldsPanel.add(makeRow("Player " + (i + 1) + " name:", playerNameFields[i]));
            }

            playerCountBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePhase3NameFields();
                }
            });

            updatePhase3NameFields();
        } else if (phase == 4) {
            humanCountBox = new JComboBox<String>(new String[] {"1", "2", "3", "4", "5", "6"});
            phase4DifficultyBox = makeDifficultyBox();
            humanNameFields = new JTextField[6];
            aiTurnPositionBox = new JComboBox<String>();

            fieldsPanel.add(makeRow("Number of human players:", humanCountBox));

            for (int i = 0; i < humanNameFields.length; i++) {
                humanNameFields[i] = new JTextField("Human " + (i + 1));
                fieldsPanel.add(makeRow("Human " + (i + 1) + " name:", humanNameFields[i]));
            }

            fieldsPanel.add(makeRow("AI difficulty:", phase4DifficultyBox));
            fieldsPanel.add(makeRow("AI turn position:", aiTurnPositionBox));

            humanCountBox.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    updatePhase4NameFields();
                    updateAIPositionChoices();
                }
            });

            updatePhase4NameFields();
            updateAIPositionChoices();
        } else if (phase == 5) {
            ai1DifficultyBox = makeDifficultyBox();
            ai2DifficultyBox = makeDifficultyBox();
            phase5FirstBox = new JComboBox<String>(new String[] {"AI 1 first", "AI 2 first"});
            boardFileField = new JTextField("");

            fieldsPanel.add(makeRow("AI 1 difficulty:", ai1DifficultyBox));
            fieldsPanel.add(makeRow("AI 2 difficulty:", ai2DifficultyBox));
            fieldsPanel.add(makeRow("Who goes first:", phase5FirstBox));
            fieldsPanel.add(makeRow("Optional board file:", boardFileField));
        }

        JScrollPane scrollPane = new JScrollPane(fieldsPanel);
        setupPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton backButton = new JButton("Back");
        JButton startButton = new JButton("Start Game");

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cardLayout.show(allPanels, "menu");
            }
        });

        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startGame();
            }
        });

        bottomPanel.add(backButton);
        bottomPanel.add(startButton);
        setupPanel.add(bottomPanel, BorderLayout.SOUTH);

        setupPanel.revalidate();
        setupPanel.repaint();

        cardLayout.show(allPanels, "setup");
    }

    public JPanel makeRow(String labelText, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(700, 45));

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(230, 30));

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    public JPanel makeRow(String labelText, JComboBox<String> box) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(700, 45));

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(230, 30));

        row.add(label, BorderLayout.WEST);
        row.add(box, BorderLayout.CENTER);

        return row;
    }

    public JComboBox<String> makeDifficultyBox() {
        JComboBox<String> box = new JComboBox<String>(new String[] {"Easy", "Medium", "Hard"});
        return box;
    }

    public void updatePhase3NameFields() {
        int count = getComboInt(playerCountBox);

        for (int i = 0; i < playerNameFields.length; i++) {
            if (i < count) {
                playerNameFields[i].setEnabled(true);
                playerNameFields[i].setVisible(true);
            } else {
                playerNameFields[i].setEnabled(false);
                playerNameFields[i].setVisible(false);
            }
        }

        setupPanel.revalidate();
        setupPanel.repaint();
    }

    public void updatePhase4NameFields() {
        int count = getComboInt(humanCountBox);

        for (int i = 0; i < humanNameFields.length; i++) {
            if (i < count) {
                humanNameFields[i].setEnabled(true);
                humanNameFields[i].setVisible(true);
            } else {
                humanNameFields[i].setEnabled(false);
                humanNameFields[i].setVisible(false);
            }
        }
    }

    public void updateAIPositionChoices() {
        int humanCount = getComboInt(humanCountBox);

        aiTurnPositionBox.removeAllItems();

        for (int i = 1; i <= humanCount + 1; i++) {
            aiTurnPositionBox.addItem("" + i);
        }
    }

    public void startGame() {
        try {
            numberOfRounds = Integer.parseInt(roundsField.getText().trim());

            if (numberOfRounds < 1) {
                JOptionPane.showMessageDialog(window, "Rounds must be at least 1.");
                return;
            }

            int targetScore = Integer.parseInt(targetField.getText().trim());

            if (targetScore < 0) {
                JOptionPane.showMessageDialog(window, "Point target must be 0 or more.");
                return;
            }

            int minimumWordLength = Integer.parseInt(minimumField.getText().trim());

            if (minimumWordLength < 3) {
                JOptionPane.showMessageDialog(window, "Minimum word length must be at least 3.");
                return;
            }

            File dictionaryFile = new File(dictionaryField.getText().trim());

            if (!dictionaryFile.exists()) {
                JOptionPane.showMessageDialog(window, "Dictionary file was not found.");
                return;
            }

            List<Player> players = new ArrayList<Player>();

            if (currentPhase == 1) {
                players.add(new Player(player1Field.getText()));
                players.add(new Player(player2Field.getText()));
            } else if (currentPhase == 2) {
                Player human = new Player(humanField.getText());
                Player ai = AI.createAIPlayer("AI", getComboText(aiDifficultyBox));

                if (firstPlayerBox.getSelectedIndex() == 0) {
                    players.add(human);
                    players.add(ai);
                } else {
                    players.add(ai);
                    players.add(human);
                }
            } else if (currentPhase == 3) {
                int count = getComboInt(playerCountBox);

                for (int i = 0; i < count; i++) {
                    players.add(new Player(playerNameFields[i].getText()));
                }
            } else if (currentPhase == 4) {
                int humanCount = getComboInt(humanCountBox);
                int aiPosition = getComboInt(aiTurnPositionBox);

                for (int i = 0; i < humanCount + 1; i++) {
                    if (i == aiPosition - 1) {
                        players.add(AI.createAIPlayer("AI", getComboText(phase4DifficultyBox)));
                    } else {
                        int humanIndex = i;

                        if (i > aiPosition - 1) {
                            humanIndex = i - 1;
                        }

                        players.add(new Player(humanNameFields[humanIndex].getText()));
                    }
                }
            } else if (currentPhase == 5) {
                Player ai1 = AI.createAIPlayer("AI 1", getComboText(ai1DifficultyBox));
                Player ai2 = AI.createAIPlayer("AI 2", getComboText(ai2DifficultyBox));

                if (phase5FirstBox.getSelectedIndex() == 0) {
                    players.add(ai1);
                    players.add(ai2);
                } else {
                    players.add(ai2);
                    players.add(ai1);
                }
            }

            gameSession = new GameSession(players, minimumWordLength, targetScore, dictionaryFile);

            if (currentPhase == 5 && boardFileField.getText().trim().length() > 0) {
                char[][] boardFromFile = readBoardFile(new File(boardFileField.getText().trim()));

                if (boardFromFile == null) {
                    JOptionPane.showMessageDialog(window, "Board file was not valid.");
                    return;
                }

                gameSession.board = boardFromFile;
            }

            createGamePanel();
            cardLayout.show(allPanels, "game");
            nextTurn();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Please check the setup fields.");
        }
    }

    public void createGamePanel() {
        gamePanel.removeAll();
        gamePanel.setLayout(new BorderLayout(10, 10));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        phaseLabel = new JLabel(getPhaseName(currentPhase), SwingConstants.CENTER);
        roundLabel = new JLabel("", SwingConstants.CENTER);
        phaseLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(phaseLabel);
        topPanel.add(roundLabel);

        gamePanel.add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        JPanel boardPanel = new JPanel(new GridLayout(5, 5, 5, 5));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        boardLabels = new JLabel[5][5];

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                JLabel letterLabel = new JLabel("", SwingConstants.CENTER);
                letterLabel.setFont(new Font("Arial", Font.BOLD, 28));
                letterLabel.setOpaque(true);
                letterLabel.setBackground(Color.WHITE);
                letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                boardLabels[r][c] = letterLabel;
                boardPanel.add(letterLabel);
            }
        }

        centerPanel.add(boardPanel, BorderLayout.CENTER);

        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(270, 400));

        currentPlayerLabel = new JLabel("Current Player:");
        timerLabel = new JLabel("Timer:");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        scorePanel = new JPanel();
        scorePanel.setLayout(new GridLayout(1, 1));
        scorePanel.setBorder(BorderFactory.createTitledBorder("Scores"));

        wordHistoryArea = new JTextArea();
        wordHistoryArea.setEditable(false);
        JScrollPane historyScrollPane = new JScrollPane(wordHistoryArea);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder("Word History"));

        sidePanel.add(currentPlayerLabel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(timerLabel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(scorePanel);
        sidePanel.add(Box.createVerticalStrut(10));
        sidePanel.add(historyScrollPane);

        centerPanel.add(sidePanel, BorderLayout.EAST);

        gamePanel.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        wordField = new JTextField();
        submitButton = new JButton("Submit");
        inputPanel.add(wordField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel();
        passButton = new JButton("Pass");
        shakeButton = new JButton("Shake Board");
        quitButton = new JButton("Quit");
        hintButton = new JButton("Hint");
        buttonPanel.add(passButton);
        buttonPanel.add(shakeButton);
        buttonPanel.add(quitButton);
        buttonPanel.add(hintButton);

        statusLabel = new JLabel("Ready.");

        bottomPanel.add(inputPanel, BorderLayout.NORTH);
        bottomPanel.add(buttonPanel, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);

        gamePanel.add(bottomPanel, BorderLayout.SOUTH);

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

        updateBoard();
        rebuildScoreTable();
        updateRoundLabel();
    }

    public void nextTurn() {
        if (gameSession.getCurrentRound() > numberOfRounds) {
            endGame();
            return;
        }

        int state = gameSession.turnManager.verifyGameState(gameSession.players);

        if (state == 2) {
            endGame();
            return;
        }

        if (state == 1) {
            askShakeOrEnd();
            return;
        }

        Player player = gameSession.getCurrentPlayer();
        currentPlayerLabel.setText("Current Player: " + player.name);
        updateRoundLabel();
        rebuildScoreTable();
        updateBoard();

        if (player.isAI) {
            setInputEnabled(false);
            statusLabel.setText(player.name + " is thinking...");

            Timer aiTimer = new Timer(900, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    runAIMove();
                }
            });

            aiTimer.setRepeats(false);
            aiTimer.start();
        } else {
            setInputEnabled(true);
            wordField.setText("");
            wordField.requestFocusInWindow();
            statusLabel.setText(player.name + "'s turn.");
            startTurnTimer();
        }
    }

    public void startTurnTimer() {
        stopTimer();

        timeLeft = 15;
        timerLabel.setForeground(Color.BLACK);
        timerLabel.setText("Timer: " + timeLeft);

        timer = new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                timeLeft = timeLeft - 1;
                timerLabel.setText("Timer: " + timeLeft);

                if (timeLeft <= 5) {
                    timerLabel.setForeground(Color.RED);
                }

                if (timeLeft <= 0) {
                    stopTimer();
                    gameSession.timeout();
                    statusLabel.setText("Time is up. Turn passed.");
                    afterTurn();
                }
            }
        });

        timer.start();
    }

    public void submitWord() {
        Player player = gameSession.getCurrentPlayer();
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

        lastScoreBeforeWord = player.totalScore;

        int result = gameSession.submitWord(word);

        if (result == 1) {
            int points = player.totalScore - lastScoreBeforeWord;
            statusLabel.setText("Word accepted: " + word);
            appendWordHistory(player.name, word, points);
            rebuildScoreTable();
            afterTurn();
        } else if (result == 2) {
            statusLabel.setText("Already used word.");
            startTurnTimer();
        } else if (result == -1) {
            statusLabel.setText("This player is not active.");
            afterTurn();
        } else {
            if (player.passed) {
                statusLabel.setText("Invalid word. Too many wrong guesses. Turn passed.");
                afterTurn();
            } else {
                statusLabel.setText("Invalid word. Try again.");
                startTurnTimer();
            }
        }
    }

    public void passTurn() {
        stopTimer();
        gameSession.pass();
        statusLabel.setText(gameSession.getCurrentPlayer().name + " passed.");
        afterTurn();
    }

    public void shakeBoard() {
        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to shake the board?");

        if (answer == JOptionPane.YES_OPTION) {
            stopTimer();
            gameSession.performShake();
            wordHistoryArea.setText("");
            statusLabel.setText("Board was shaken.");
            nextTurn();
        } else {
            if (gameSession.getCurrentPlayer().isAI == false) {
                startTurnTimer();
            }
        }
    }

    public void quitPlayer() {
        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to quit?");

        if (answer == JOptionPane.YES_OPTION) {
            stopTimer();
            gameSession.quit();
            statusLabel.setText(gameSession.getCurrentPlayer().name + " quit.");
            afterTurn();
        } else {
            if (gameSession.getCurrentPlayer().isAI == false) {
                startTurnTimer();
            }
        }
    }

    public void runAIMove() {
        GameSession.AIResult result = gameSession.runAITurnIfNeeded();
        Player player = gameSession.getCurrentPlayer();

        if (result.passed) {
            statusLabel.setText(player.name + " passed.");
        } else {
            statusLabel.setText(player.name + " played " + result.word + ".");
            appendWordHistory(player.name, result.word, result.points);
        }

        rebuildScoreTable();
        afterTurn();
    }

    public void afterTurn() {
        updateRoundLabel();
        clearBoardHighlights();
        rebuildScoreTable();

        int state = gameSession.turnManager.verifyGameState(gameSession.players);

        if (state == 2) {
            endGame();
            return;
        }

        if (state == 1) {
            askShakeOrEnd();
            return;
        }

        int nextState = gameSession.nextTurn();

        if (gameSession.getCurrentRound() > numberOfRounds) {
            endGame();
            return;
        }

        if (nextState == 2) {
            endGame();
            return;
        }

        if (nextState == 1) {
            askShakeOrEnd();
            return;
        }

        nextTurn();
    }

    public void askShakeOrEnd() {
        stopTimer();
        setInputEnabled(false);

        if (currentPhase == 5) {
            endGame();
            return;
        }

        if (gameSession.isShakeUpUsed()) {
            endGame();
            return;
        }

        int answer = JOptionPane.showConfirmDialog(window, "All players passed. Shake the board?");

        if (answer == JOptionPane.YES_OPTION) {
            gameSession.performShake();
            wordHistoryArea.setText("");
            statusLabel.setText("Board was shaken.");
            nextTurn();
        } else {
            endGame();
        }
    }

    public void endGame() {
        stopTimer();
        setInputEnabled(false);
        rebuildScoreTable();

        Player winner = gameSession.determineWinner();
        String message = "Game ended.\n";

        if (winner == null) {
            message = message + "Winner: NONE\n";
        } else {
            message = message + "Winner: " + winner.name + "\n";
        }

        message = message + "\nScores:\n";

        for (int i = 0; i < gameSession.players.size(); i++) {
            Player player = gameSession.players.get(i);
            message = message + player.name + ": " + player.totalScore + "\n";
        }

        JOptionPane.showMessageDialog(window, message);
        cardLayout.show(allPanels, "menu");
    }

    public void updateRoundLabel() {
        if (gameSession == null) {
            return;
        }

        int round = gameSession.getCurrentRound();

        if (round > numberOfRounds) {
            round = numberOfRounds;
        }

        roundLabel.setText("Round " + round + " of " + numberOfRounds);
    }

    public void rebuildScoreTable() {
        scorePanel.removeAll();
        scorePanel.setLayout(new GridLayout(gameSession.players.size() + 1, 3, 5, 5));

        scorePanel.add(new JLabel("Player"));
        scorePanel.add(new JLabel("Score"));
        scorePanel.add(new JLabel("Status"));

        for (int i = 0; i < gameSession.players.size(); i++) {
            Player player = gameSession.players.get(i);
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

    public void appendWordHistory(String playerName, String word, int points) {
        wordHistoryArea.append(playerName + " - " + word + " (+" + points + ")\n");
        wordHistoryArea.setCaretPosition(wordHistoryArea.getDocument().getLength());
    }

    public void setInputEnabled(boolean enabled) {
        wordField.setEnabled(enabled);
        submitButton.setEnabled(enabled);
        passButton.setEnabled(enabled);
        quitButton.setEnabled(enabled);
        hintButton.setEnabled(enabled);
    }

    public void updateBoard() {
        char[][] board = gameSession.getBoard();

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                boardLabels[r][c].setText("" + board[r][c]);
            }
        }
    }

    public void clearBoardHighlights() {
        if (boardLabels == null) {
            return;
        }

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 5; c++) {
                boardLabels[r][c].setBackground(Color.WHITE);
            }
        }
    }

    public void showHint() {
        List<String> words = gameSession.boggleAI.findAllValidWords(
                gameSession.board,
                gameSession.dictionary,
                gameSession.minimumWordLength,
                gameSession.usedWords
        );

        if (words == null || words.size() == 0) {
            statusLabel.setText("No hint found.");
            return;
        }

        String bestWord = words.get(0);
        int bestScore = ScoringManager.calculateScore(bestWord, gameSession.usedWords);

        for (int i = 1; i < words.size(); i++) {
            String nextWord = words.get(i);
            int nextScore = ScoringManager.calculateScore(nextWord, gameSession.usedWords);

            if (nextScore > bestScore) {
                bestWord = nextWord;
                bestScore = nextScore;
            }
        }

        statusLabel.setText("Hint: " + bestWord);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public int getComboInt(JComboBox<String> box) {
        String text = (String) box.getSelectedItem();
        return Integer.parseInt(text);
    }

    public String getComboText(JComboBox<String> box) {
        String text = (String) box.getSelectedItem();

        if (text == null) {
            text = "";
        }

        return text;
    }

    public String getPhaseName(int phase) {
        if (phase == 1) {
            return "Phase 1: Player vs Player";
        } else if (phase == 2) {
            return "Phase 2: Player vs AI";
        } else if (phase == 3) {
            return "Phase 3: Multiplayer";
        } else if (phase == 4) {
            return "Phase 4: Multiplayer + AI";
        } else if (phase == 5) {
            return "Phase 5: AI vs AI Contest";
        }

        return "Boggle";
    }

    public char[][] readBoardFile(File file) {
        try {
            if (!file.exists()) {
                return null;
            }

            Scanner scanner = new Scanner(new FileReader(file));
            String allLetters = "";

            while (scanner.hasNext()) {
                allLetters = allLetters + scanner.next();
            }

            scanner.close();
            allLetters = allLetters.toUpperCase();

            if (allLetters.length() < 25) {
                return null;
            }

            char[][] board = new char[5][5];
            int index = 0;

            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 5; c++) {
                    board[r][c] = allLetters.charAt(index);
                    index = index + 1;
                }
            }

            return board;
        } catch (Exception e) {
            return null;
        }
    }
}
