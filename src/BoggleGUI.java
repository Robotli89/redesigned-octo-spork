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
import java.util.Scanner;

public class BoggleGUI {
    public JFrame window;
    public CardLayout cards;
    public JPanel mainPanel;
    public JPanel menuPanel;
    public JPanel setupPanel;
    public JPanel gamePanel;

    public int currentPhase;
    public int numberOfRounds;
    public int timeLeft;
    public int oldScore;

    public BoggleGame.GameSession game;
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
    }

    public void makeMenu() {
        menuPanel.removeAll();
        menuPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel("BOGGLE", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 44));
        title.setBorder(BorderFactory.createEmptyBorder(40, 10, 30, 10));

        menuPanel.add(title, BorderLayout.NORTH);

        JPanel buttons = new JPanel();
        buttons.setLayout(new GridLayout(7, 1, 10, 10));
        buttons.setBorder(BorderFactory.createEmptyBorder(10, 230, 40, 230));

        JButton phase1 = new JButton("Phase 1: Player vs Player");
        JButton phase2 = new JButton("Phase 2: Player vs AI");
        JButton phase3 = new JButton("Phase 3: Multiplayer");
        JButton phase4 = new JButton("Phase 4: Multiplayer + AI");
        JButton phase5 = new JButton("Phase 5: AI vs AI Contest");
        JButton quit = new JButton("Quit");

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

    public void showSetup(int phase) {
        currentPhase = phase;

        setupPanel.removeAll();
        setupPanel.setLayout(new BorderLayout());

        JLabel title = new JLabel(getPhaseName(phase) + " Setup", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 28));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        setupPanel.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel();
        fields.setLayout(new BoxLayout(fields, BoxLayout.Y_AXIS));
        fields.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));

        roundsField = new JTextField("5");
        targetField = new JTextField("0");
        minimumField = new JTextField("3");
        dictionaryField = new JTextField("src/wordlist.txt");

        fields.add(makeTextRow("Number of rounds:", roundsField));
        fields.add(makeTextRow("Point target:", targetField));
        fields.add(makeTextRow("Minimum word length:", minimumField));
        fields.add(makeTextRow("Dictionary file:", dictionaryField));

        if (phase == 1) {
            player1Field = new JTextField("Player 1");
            player2Field = new JTextField("Player 2");

            fields.add(makeTextRow("Player 1 name:", player1Field));
            fields.add(makeTextRow("Player 2 name:", player2Field));
        }
        else if (phase == 2) {
            humanField = new JTextField("Human");
            aiDifficultyBox = makeDifficultyBox();
            firstPlayerBox = new JComboBox<String>(new String[] {"Human first", "AI first"});

            fields.add(makeTextRow("Human player name:", humanField));
            fields.add(makeComboRow("AI difficulty:", aiDifficultyBox));
            fields.add(makeComboRow("Who goes first:", firstPlayerBox));
        }
        else if (phase == 3) {
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
        }
        else if (phase == 4) {
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
        }
        else if (phase == 5) {
            ai1DifficultyBox = makeDifficultyBox();
            ai2DifficultyBox = makeDifficultyBox();
            phase5FirstBox = new JComboBox<String>(new String[] {"AI 1 first", "AI 2 first"});
            boardFileField = new JTextField("");

            fields.add(makeComboRow("AI 1 difficulty:", ai1DifficultyBox));
            fields.add(makeComboRow("AI 2 difficulty:", ai2DifficultyBox));
            fields.add(makeComboRow("Who goes first:", phase5FirstBox));
            fields.add(makeTextRow("Optional board file:", boardFileField));
        }

        JScrollPane scroll = new JScrollPane(fields);
        setupPanel.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel();

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

    public JPanel makeTextRow(String text, JTextField field) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(700, 45));

        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(230, 30));

        row.add(label, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);

        return row;
    }

    public JPanel makeComboRow(String text, JComboBox<String> box) {
        JPanel row = new JPanel(new BorderLayout(10, 5));
        row.setMaximumSize(new Dimension(700, 45));

        JLabel label = new JLabel(text);
        label.setPreferredSize(new Dimension(230, 30));

        row.add(label, BorderLayout.WEST);
        row.add(box, BorderLayout.CENTER);

        return row;
    }

    public JComboBox<String> makeDifficultyBox() {
        JComboBox<String> box = new JComboBox<String>(new String[] {"Easy", "Medium", "Hard"});

        return box;
    }

    public void updatePlayerRows() {
        int count = getComboNumber(playerCountBox);

        for (int i = 0; i < 6; i++) {
            if (i < count) {
                playerRows[i].setVisible(true);
            }
            else {
                playerRows[i].setVisible(false);
            }
        }

        setupPanel.revalidate();
        setupPanel.repaint();
    }

    public void updateHumanRows() {
        int count = getComboNumber(humanCountBox);

        for (int i = 0; i < 6; i++) {
            if (i < count) {
                humanRows[i].setVisible(true);
            }
            else {
                humanRows[i].setVisible(false);
            }
        }

        setupPanel.revalidate();
        setupPanel.repaint();
    }

    public void updateAIPositions() {
        int count = getComboNumber(humanCountBox);

        aiPositionBox.removeAllItems();

        for (int i = 1; i <= count + 1; i++) {
            aiPositionBox.addItem("" + i);
        }
    }

    public void startGame() {
        try {
            numberOfRounds = Integer.parseInt(roundsField.getText().trim());
            int targetScore = Integer.parseInt(targetField.getText().trim());
            int minimumLength = Integer.parseInt(minimumField.getText().trim());
            File dictionaryFile = new File(dictionaryField.getText().trim());

            if (numberOfRounds < 1) {
                JOptionPane.showMessageDialog(window, "Rounds must be at least 1.");
                return;
            }

            if (targetScore < 0) {
                JOptionPane.showMessageDialog(window, "Target cannot be negative.");
                return;
            }

            if (minimumLength < 3) {
                JOptionPane.showMessageDialog(window, "Minimum word length must be at least 3.");
                return;
            }

            if (dictionaryFile.exists() == false) {
                JOptionPane.showMessageDialog(window, "Dictionary file was not found.");
                return;
            }

            ArrayList<BoggleGame.Player> players = new ArrayList<BoggleGame.Player>();

            if (currentPhase == 1) {
                players.add(new BoggleGame.Player(player1Field.getText()));
                players.add(new BoggleGame.Player(player2Field.getText()));
            }
            else if (currentPhase == 2) {
                BoggleGame.Player human = new BoggleGame.Player(humanField.getText());
                BoggleGame.Player ai = BoggleAI.makeAIPlayer("AI", getComboText(aiDifficultyBox));

                if (firstPlayerBox.getSelectedIndex() == 0) {
                    players.add(human);
                    players.add(ai);
                }
                else {
                    players.add(ai);
                    players.add(human);
                }
            }
            else if (currentPhase == 3) {
                int count = getComboNumber(playerCountBox);

                for (int i = 0; i < count; i++) {
                    players.add(new BoggleGame.Player(playerFields[i].getText()));
                }
            }
            else if (currentPhase == 4) {
                int humanCount = getComboNumber(humanCountBox);
                int aiPosition = getComboNumber(aiPositionBox);

                for (int i = 0; i < humanCount + 1; i++) {
                    if (i == aiPosition - 1) {
                        players.add(BoggleAI.makeAIPlayer("AI", getComboText(phase4DifficultyBox)));
                    }
                    else {
                        int humanNumber = i;

                        if (i > aiPosition - 1) {
                            humanNumber = i - 1;
                        }

                        players.add(new BoggleGame.Player(humanFields[humanNumber].getText()));
                    }
                }
            }
            else {
                BoggleGame.Player ai1 = BoggleAI.makeAIPlayer("AI 1", getComboText(ai1DifficultyBox));
                BoggleGame.Player ai2 = BoggleAI.makeAIPlayer("AI 2", getComboText(ai2DifficultyBox));

                if (phase5FirstBox.getSelectedIndex() == 0) {
                    players.add(ai1);
                    players.add(ai2);
                }
                else {
                    players.add(ai2);
                    players.add(ai1);
                }
            }

            game = new BoggleGame.GameSession(players, minimumLength, targetScore, dictionaryFile);

            if (currentPhase == 5 && boardFileField.getText().trim().length() > 0) {
                char[][] fileBoard = readBoardFile(new File(boardFileField.getText().trim()));

                if (fileBoard == null) {
                    JOptionPane.showMessageDialog(window, "Board file was not valid.");
                    return;
                }

                game.board = fileBoard;
            }

            makeGamePanel();
            cards.show(mainPanel, "game");
            nextTurn();
        }
        catch (Exception e) {
            JOptionPane.showMessageDialog(window, "Please check the setup fields.");
        }
    }

    public void makeGamePanel() {
        gamePanel.removeAll();
        gamePanel.setLayout(new BorderLayout(10, 10));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new GridLayout(2, 1));

        phaseLabel = new JLabel(getPhaseName(currentPhase), SwingConstants.CENTER);
        phaseLabel.setFont(new Font("Arial", Font.BOLD, 24));

        roundLabel = new JLabel("", SwingConstants.CENTER);

        top.add(phaseLabel);
        top.add(roundLabel);

        gamePanel.add(top, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(10, 10));

        JPanel boardPanel = new JPanel(new GridLayout(5, 5, 5, 5));
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        boardLabels = new JLabel[5][5];

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
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

        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(270, 400));

        currentPlayerLabel = new JLabel("Current Player:");
        timerLabel = new JLabel("Timer:");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));

        scorePanel = new JPanel();
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

        JPanel bottom = new JPanel(new BorderLayout(5, 5));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));

        wordField = new JTextField();
        submitButton = new JButton("Submit");

        inputPanel.add(wordField, BorderLayout.CENTER);
        inputPanel.add(submitButton, BorderLayout.EAST);

        JPanel buttons = new JPanel();

        passButton = new JButton("Pass");
        shakeButton = new JButton("Shake Board");
        quitButton = new JButton("Quit");
        hintButton = new JButton("Hint");

        buttons.add(passButton);
        buttons.add(shakeButton);
        buttons.add(quitButton);
        buttons.add(hintButton);

        statusLabel = new JLabel("Ready.");

        bottom.add(inputPanel, BorderLayout.NORTH);
        bottom.add(buttons, BorderLayout.CENTER);
        bottom.add(statusLabel, BorderLayout.SOUTH);

        gamePanel.add(bottom, BorderLayout.SOUTH);

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
        updateScores();
        updateRoundLabel();
    }

    public void nextTurn() {
        if (game.currentRound > numberOfRounds) {
            endGame();
            return;
        }

        BoggleGame.Player player = game.getCurrentPlayer();

        currentPlayerLabel.setText("Current Player: " + player.name);
        updateRoundLabel();
        updateScores();
        updateBoard();

        if (player.isAI == true) {
            setInput(false);
            statusLabel.setText(player.name + " is thinking...");

            Timer aiTimer = new Timer(900, new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    aiMove();
                }
            });

            aiTimer.setRepeats(false);
            aiTimer.start();
        }
        else {
            setInput(true);
            wordField.setText("");
            statusLabel.setText(player.name + "'s turn.");
            startTimer();
        }
    }

    public void startTimer() {
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
                    game.timeoutCurrentPlayer();
                    statusLabel.setText("Time is up. Turn passed.");
                    afterTurn();
                }
            }
        });

        timer.start();
    }

    public void submitWord() {
        BoggleGame.Player player = game.getCurrentPlayer();
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

        oldScore = player.score;

        int result = game.submitWord(word);

        if (result == 1) {
            int points = player.score - oldScore;

            statusLabel.setText("Word accepted: " + word);
            addWordHistory(player.name, word, points);
            updateScores();
            afterTurn();
        }
        else if (result == 2) {
            statusLabel.setText("Already used word.");
            startTimer();
        }
        else {
            if (player.passed == true) {
                statusLabel.setText("Too many wrong guesses. Turn passed.");
                afterTurn();
            }
            else {
                statusLabel.setText("Invalid word. Try again.");
                startTimer();
            }
        }
    }

    public void passTurn() {
        stopTimer();
        game.passCurrentPlayer();
        statusLabel.setText(game.getCurrentPlayer().name + " passed.");
        afterTurn();
    }

    public void shakeBoard() {
        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to shake the board?");

        if (answer == JOptionPane.YES_OPTION) {
            stopTimer();
            game.shakeBoard();
            wordHistoryArea.setText("");
            statusLabel.setText("Board was shaken.");
            nextTurn();
        }
        else {
            if (game.getCurrentPlayer().isAI == false) {
                startTimer();
            }
        }
    }

    public void quitPlayer() {
        int answer = JOptionPane.showConfirmDialog(window, "Are you sure you want to quit?");

        if (answer == JOptionPane.YES_OPTION) {
            stopTimer();
            game.quitCurrentPlayer();
            statusLabel.setText(game.getCurrentPlayer().name + " quit.");
            afterTurn();
        }
        else {
            if (game.getCurrentPlayer().isAI == false) {
                startTimer();
            }
        }
    }

    public void aiMove() {
        BoggleGame.Player player = game.getCurrentPlayer();
        BoggleGame.GameSession.AIResult result = game.doAITurn();

        if (result.passed == true) {
            statusLabel.setText(player.name + " passed.");
        }
        else {
            statusLabel.setText(player.name + " played " + result.word + ".");
            addWordHistory(player.name, result.word, result.points);
        }

        updateScores();
        afterTurn();
    }

    public void afterTurn() {
        updateRoundLabel();
        updateScores();

        int status = game.moveToNextPlayer();

        if (game.currentRound > numberOfRounds) {
            endGame();
            return;
        }

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

    public void askShakeOrEnd() {
        stopTimer();
        setInput(false);

        if (currentPhase == 5 || game.shakeUsed == true) {
            endGame();
            return;
        }

        int answer = JOptionPane.showConfirmDialog(window, "All players passed. Shake the board?");

        if (answer == JOptionPane.YES_OPTION) {
            game.shakeBoard();
            wordHistoryArea.setText("");
            statusLabel.setText("Board was shaken.");
            nextTurn();
        }
        else {
            endGame();
        }
    }

    public void endGame() {
        stopTimer();
        setInput(false);
        updateScores();

        BoggleGame.Player winner = game.getWinner();
        String message = "Game ended.\n";

        if (winner == null) {
            message = message + "Winner: NONE\n";
        }
        else {
            message = message + "Winner: " + winner.name + "\n";
        }

        message = message + "\nScores:\n";

        for (int i = 0; i < game.players.size(); i++) {
            BoggleGame.Player player = game.players.get(i);

            message = message + player.name + ": " + player.score + "\n";
        }

        JOptionPane.showMessageDialog(window, message);
        cards.show(mainPanel, "menu");
    }

    public void updateRoundLabel() {
        int round = game.currentRound;

        if (round > numberOfRounds) {
            round = numberOfRounds;
        }

        roundLabel.setText("Round " + round + " of " + numberOfRounds);
    }

    public void updateScores() {
        scorePanel.removeAll();
        scorePanel.setLayout(new GridLayout(game.players.size() + 1, 3, 5, 5));

        scorePanel.add(new JLabel("Player"));
        scorePanel.add(new JLabel("Score"));
        scorePanel.add(new JLabel("Status"));

        for (int i = 0; i < game.players.size(); i++) {
            BoggleGame.Player player = game.players.get(i);
            String status = "Active";

            if (player.quit == true) {
                status = "Quit";
            }
            else if (player.passed == true) {
                status = "Passed";
            }

            scorePanel.add(new JLabel(player.name));
            scorePanel.add(new JLabel("" + player.score));
            scorePanel.add(new JLabel(status));
        }

        scorePanel.revalidate();
        scorePanel.repaint();
    }

    public void addWordHistory(String playerName, String word, int points) {
        wordHistoryArea.append(playerName + " - " + word + " (+" + points + ")\n");
        wordHistoryArea.setCaretPosition(wordHistoryArea.getDocument().getLength());
    }

    public void setInput(boolean on) {
        wordField.setEnabled(on);
        submitButton.setEnabled(on);
        passButton.setEnabled(on);
        quitButton.setEnabled(on);
        hintButton.setEnabled(on);
    }

    public void updateBoard() {
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                boardLabels[row][col].setText("" + game.board[row][col]);
            }
        }
    }

    public void showHint() {
        ArrayList<String> words = BoggleAI.findAllValidWords(game.board, game.dictionary, game.minimumWordLength, game.usedWords);

        if (words.size() == 0) {
            statusLabel.setText("No hint found.");
            return;
        }

        String hint = BoggleAI.chooseWord(words, "Hard");

        statusLabel.setText("Hint: " + hint);
    }

    public void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    public int getComboNumber(JComboBox<String> box) {
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
        }
        else if (phase == 2) {
            return "Phase 2: Player vs AI";
        }
        else if (phase == 3) {
            return "Phase 3: Multiplayer";
        }
        else if (phase == 4) {
            return "Phase 4: Multiplayer + AI";
        }
        else {
            return "Phase 5: AI vs AI Contest";
        }
    }

    public char[][] readBoardFile(File file) {
        try {
            if (file.exists() == false) {
                return null;
            }

            Scanner scanner = new Scanner(new FileReader(file));
            String letters = "";

            while (scanner.hasNext()) {
                letters = letters + scanner.next();
            }

            scanner.close();
            letters = letters.toUpperCase();

            if (letters.length() < 25) {
                return null;
            }

            char[][] board = new char[5][5];
            int number = 0;

            for (int row = 0; row < 5; row++) {
                for (int col = 0; col < 5; col++) {
                    board[row][col] = letters.charAt(number);
                    number = number + 1;
                }
            }

            return board;
        }
        catch (Exception e) {
            return null;
        }
    }
}
