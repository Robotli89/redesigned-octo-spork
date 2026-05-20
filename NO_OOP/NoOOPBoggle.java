/*
 * Author: Kevin Li and Ethan Chuang
 * Date:   May 19, 2026
 * Course: ICS4U
 * Project: Boggle Game
 *
 * Description:
 * This program is a text-only version of the Boggle game. Players take turns
 * entering words found on a randomly generated Boggle board. The program checks
 * each word against the board and dictionary, keeps score, supports pass/quit
 * commands, hints, board shake-ups, timers, multiplayer games, and AI players.
 * This version removes the custom object-oriented structure and GUI from the
 * original project, using static methods and arrays instead.
 *
 * Copyright (c) Kevin Li and Ethan Chuang
 */

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class NoOOPBoggle {
    // Shared constants used by the whole program.
    static final Random RANDOM = new Random();
    static final int BOARD_SIZE = 5;
    static final int MAX_PLAYERS = 20;

    // Boggle dice. Each string represents one die and the letters on its sides.
    static final String[] DICE = {
            "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY",
            "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW",
            "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY",
            "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW",
            "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
    };

    // General game settings and turn tracking.
    static int minimumWordLength;
    static int targetScore;
    static int playerCount;
    static int currentTurnIndex;
    static int currentRound;
    static int forcedWinnerIndex;

    static boolean shakeUpUsed;
    static boolean hintUsed;
    static String endReason;

    // Board, dictionary, and word history for the current game.
    static char[][] board;
    static ArrayList<String> dictionary;
    static ArrayList<String> usedWords;

    // Parallel arrays store player data without making a Player class.
    // The same index is used for one player's name, score, pass status, etc.
    static String[] playerNames = new String[MAX_PLAYERS];
    static int[] playerScores = new int[MAX_PLAYERS];
    static boolean[] playerPassed = new boolean[MAX_PLAYERS];
    static boolean[] playerQuit = new boolean[MAX_PLAYERS];
    static int[] wrongGuessCount = new int[MAX_PLAYERS];
    static int[] timeoutCount = new int[MAX_PLAYERS];
    static int[] autoPassWrongCount = new int[MAX_PLAYERS];
    static int[] autoPassTimerCount = new int[MAX_PLAYERS];
    static String[] playerWords = new String[MAX_PLAYERS];
    static boolean[] playerIsAI = new boolean[MAX_PLAYERS];
    static String[] playerDifficulty = new String[MAX_PLAYERS];

    // Stores the latest AI result so the text interface can print it.
    static String lastAIWord;
    static int lastAIPoints;

    // Program entry point. Shows the main menu and starts the chosen game mode.
    public static void main(String[] args) {
        File dict = findDictionaryFile();
        if (dict == null) {
            System.out.println("Dictionary file not found (wordlist.txt).");
            System.out.println("Result: all words will be INVALID and AI will always pass.");
            dict = new File("wordlist.txt");
        }

        Scanner sc = new Scanner(System.in);
        for (;;) {
            System.out.println();
            System.out.println("Boggle - NO OOP text version");
            System.out.println("1) Player vs Player");
            System.out.println("2) Player vs AI");
            System.out.println("3) Multiplayer");
            System.out.println("4) Multiplayer + AI");
            System.out.println("5) AI vs AI");
            System.out.println("0) Exit");
            System.out.print("Choose: ");

            String choice = readLine(sc).trim();
            if (choice.equals("0")) break;
            if (choice.equals("1")) runPlayerVsPlayer(sc, dict);
            else if (choice.equals("2")) runPlayerVsAI(sc, dict);
            else if (choice.equals("3")) runMultiplayer(sc, dict);
            else if (choice.equals("4")) runMultiplayerAI(sc, dict);
            else if (choice.equals("5")) runAIvsAI(sc, dict);
            else System.out.println("Invalid choice.");

            if (!choice.equals("0")) {
                System.out.print("Another round? (Y/N): ");
                String again = readLine(sc).trim();
                if (!again.equalsIgnoreCase("Y")) break;
            }
        }
        sc.close();
    }

    // Sets up a two-player human game.
    static void runPlayerVsPlayer(Scanner sc, File dictionaryFile) {
        showRules();
        resetPlayers();

        System.out.print("Player 1 name: ");
        addHumanPlayer(readLine(sc));
        System.out.print("Player 2 name: ");
        addHumanPlayer(readLine(sc));

        setupSession(sc, dictionaryFile);
        runNormalLoop(sc, false, null);
    }

    // Sets up one human player against one automatic AI player.
    static void runPlayerVsAI(Scanner sc, File dictionaryFile) {
        showRules();
        resetPlayers();

        System.out.print("Your name: ");
        String human = readLine(sc);
        addHumanPlayer(human);
        System.out.print("AI difficulty (Easy/Medium/Hard): ");
        addAIPlayer("AI", readLine(sc));

        setupSession(sc, dictionaryFile);
        runNormalLoop(sc, true, human);
    }

    // Sets up a game with three or more human players.
    static void runMultiplayer(Scanner sc, File dictionaryFile) {
        showRules();
        resetPlayers();

        int count = readInt(sc, "How many players (>=3): ", 3);
        for (int i = 0; i < count; i++) {
            System.out.print("Player " + (i + 1) + " name: ");
            addHumanPlayer(readLine(sc));
        }

        setupSession(sc, dictionaryFile);
        runNormalLoop(sc, false, null);
    }

    // Sets up a game with multiple human players and at least one AI player.
    static void runMultiplayerAI(Scanner sc, File dictionaryFile) {
        showRules();
        resetPlayers();

        int humanCount = readInt(sc, "How many human players (>=2): ", 2);
        int aiCount = readInt(sc, "How many AI players (>=1): ", 1);

        for (int i = 0; i < humanCount; i++) {
            System.out.print("Human Player " + (i + 1) + " name: ");
            addHumanPlayer(readLine(sc));
        }
        for (int i = 0; i < aiCount; i++) {
            System.out.print("AI #" + (i + 1) + " difficulty (Easy/Medium/Hard): ");
            addAIPlayer("AI" + (i + 1), readLine(sc));
        }

        setupSession(sc, dictionaryFile);
        runNormalLoop(sc, false, null);
    }

    // Runs the AI contest mode where one AI is automatic and the opponent is typed manually.
    static void runAIvsAI(Scanner sc, File dictionaryFile) {
        System.out.println("AI vs AI Rules:");
        System.out.println("- Your AI is automatic.");
        System.out.println("- Opponent AI move is typed manually.");
        System.out.println();

        resetPlayers();
        System.out.print("Your AI difficulty (Easy/Medium/Hard): ");
        addAIPlayer("YourAI", readLine(sc));
        System.out.print("Opponent AI name: ");
        addHumanPlayer(readLine(sc));

        setupSession(sc, dictionaryFile);

        for (;;) {
            printTurnHeader();
            int current = currentTurnIndex;
            if (playerIsAI[current]) {
                int aiResult = runAITurnIfNeeded();
                if (aiResult == 1) System.out.println(playerNames[current] + " PASSED");
                else System.out.println(playerNames[current] + " played: " + lastAIWord + " (+" + lastAIPoints + ")");
            } else {
                if (readTimerSettingForCurrentGame > 0) {
                    System.out.println("(Timer limit: " + readTimerSettingForCurrentGame + " seconds)");
                }
                System.out.print("Opponent move (word or PASS): ");
                String input = getInputWithTimer(sc, readTimerSettingForCurrentGame, true);
                if (input.equals("TIMEOUT_PASS")) {
                    // Timeout pass already handled.
                } else if (input.equalsIgnoreCase("PASS")) {
                    processPass(currentTurnIndex);
                } else {
                    int result = submitWord(input);
                    if (result == 1) System.out.println("Accepted. +" + input.trim().length());
                    else if (result == 2) System.out.println("Rejected: already used.");
                    else System.out.println("Rejected: invalid.");
                }
            }

            int after = nextTurn();
            if (after == 2 || after == 1) {
                announceWinner();
                break;
            }
        }
    }

    static int readTimerSettingForCurrentGame;

    // Reads the shared game settings and creates a new board/session.
    static void setupSession(Scanner sc, File dictionaryFile) {
        minimumWordLength = readInt(sc, "Minimum word length (>=3): ", 3);
        targetScore = readIntAllowZero(sc, "Target score (0 = no target): ");
        readTimerSettingForCurrentGame = readTimerChoice(sc);
        startSession(dictionaryFile);
    }

    // Resets round state, loads the dictionary, and generates the first board.
    static void startSession(File dictionaryFile) {
        if (minimumWordLength < 3) minimumWordLength = 3;
        if (targetScore < 0) targetScore = 0;
        currentTurnIndex = 0;
        currentRound = 1;
        forcedWinnerIndex = -1;
        shakeUpUsed = false;
        hintUsed = false;
        endReason = null;
        dictionary = loadDictionary(dictionaryFile);
        usedWords = new ArrayList<String>();
        board = generateBoard();
    }

    // Main turn loop for regular modes. It handles human turns, AI turns, and game ending.
    static void runNormalLoop(Scanner sc, boolean savePvAIQuit, String saveName) {
        for (;;) {
            printTurnHeader();
            int current = currentTurnIndex;

            if (playerIsAI[current]) {
                if (shouldOfferShakeAfterAILead(current)) {
                    if (!offerShakeAfterAILead(sc, current)) break;
                    continue;
                }

                int aiResult = runAITurnIfNeeded();
                if (aiResult == 1) System.out.println(playerNames[current] + " PASSED");
                else System.out.println(playerNames[current] + " played: " + lastAIWord + " (+" + lastAIPoints + ")");

                if (aiResult == 2 && shouldOfferShakeAfterAILead(current)) {
                    if (!offerShakeAfterAILead(sc, current)) break;
                    continue;
                }
            } else {
                if (readTimerSettingForCurrentGame > 0) {
                    System.out.println("(Timer limit: " + readTimerSettingForCurrentGame + " seconds)");
                }
                System.out.print("Enter word (or PASS / QUIT / HINT): ");
                int moveMade = processHumanInput(sc, savePvAIQuit, saveName);
                if (moveMade == 0) continue;
            }

            int after = nextTurn();
            if (after == 2) {
                announceWinner();
                break;
            }
            if (after == 1) {
                if (shakeUpUsed) {
                    announceWinner();
                    break;
                }
                System.out.print("All active players passed. Shake Up? (Y/N): ");
                String answer = readLine(sc).trim();
                if (answer.equalsIgnoreCase("Y")) performShake();
                else {
                    announceWinner();
                    break;
                }
            }
        }
    }

    // In Player vs AI, the human gets one shake chance once the AI passes their score.
    static boolean offerShakeAfterAILead(Scanner sc, int aiIndex) {
        int restartIndex = getPassedHumanIndexBehindAI(aiIndex);

        if (shakeUpUsed) {
            System.out.println(playerNames[aiIndex] + " is ahead. Game over.");
            announceWinner();
            return false;
        }

        System.out.print(playerNames[aiIndex] + " is now ahead. Shake the board? (Y/N): ");
        String answer = readLine(sc).trim();
        if (answer.equalsIgnoreCase("Y")) {
            performShake();
            if (restartIndex >= 0) currentTurnIndex = restartIndex;
            System.out.println("Board was shaken.");
            return true;
        }

        System.out.println(playerNames[aiIndex] + " is ahead. Game over.");
        announceWinner();
        return false;
    }

    // Handles one human command: word submission, pass, quit, hint, or timeout.
    static int processHumanInput(Scanner sc, boolean savePvAIQuit, String saveName) {
        String input = getInputWithTimer(sc, readTimerSettingForCurrentGame, true);
        if (input.equals("TIMEOUT_PASS")) {
            return 1;
        }
        if (input.equalsIgnoreCase("QUIT")) {
            processQuit(currentTurnIndex);
            if (savePvAIQuit) saveGame(new File(safeSaveName(saveName) + "Save.txt"));
            return 1;
        }
        if (input.equalsIgnoreCase("PASS")) {
            processPass(currentTurnIndex);
            return 1;
        }
        if (input.equalsIgnoreCase("HINT")) {
            showHint();
            return 0;
        }

        int result = submitWord(input);
        if (result == 1) System.out.println("Valid! +" + input.trim().length() + " points");
        else if (result == 2) System.out.println("Already used. 0 points.");
        else System.out.println("Invalid. 0 points.");
        return 1;
    }

    // Prints the board and the current turn information.
    static void printTurnHeader() {
        System.out.println();
        printBoard(board);
        System.out.println("Round: " + currentRound);
        System.out.println("Turn: " + playerNames[currentTurnIndex] + "  Score=" + playerScores[currentTurnIndex]);
        System.out.println("Hint: " + (hintUsed ? "USED" : "available"));
    }

    // Prints the rules before a new game starts.
    static void showRules() {
        System.out.println("Rules:");
        System.out.println("1. Connect adjacent letters (horizontal, vertical, diagonal).");
        System.out.println("2. A cube can only be used once per word.");
        System.out.println("3. Wrong word gives no points (2 wrong guesses = auto pass).");
        System.out.println("4. If all players pass, you may Shake the Board once.");
        System.out.println();
    }

    // Finds and prints one strong valid word, if the hint has not already been used.
    static void showHint() {
        if (hintUsed) {
            System.out.println("Hint already used.");
            return;
        }
        ArrayList<String> words = findAllValidWords(board, dictionary, minimumWordLength, usedWords);
        if (words.isEmpty()) {
            System.out.println("No hint available.");
            return;
        }
        String hint = chooseWord(words, "HARD");
        System.out.println("Hint: " + hint);
        hintUsed = true;
    }

    // Clears every player slot before setting up a new game.
    static void resetPlayers() {
        playerCount = 0;
        for (int i = 0; i < MAX_PLAYERS; i++) {
            playerNames[i] = "";
            playerScores[i] = 0;
            playerPassed[i] = false;
            playerQuit[i] = false;
            wrongGuessCount[i] = 0;
            timeoutCount[i] = 0;
            autoPassWrongCount[i] = 0;
            autoPassTimerCount[i] = 0;
            playerWords[i] = "";
            playerIsAI[i] = false;
            playerDifficulty[i] = "EASY";
        }
    }

    // Adds a human player into the next open parallel-array slot.
    static void addHumanPlayer(String name) {
        addPlayer(name, false, "EASY");
    }

    // Adds an AI player into the next open parallel-array slot.
    static void addAIPlayer(String name, String difficulty) {
        addPlayer(name, true, difficulty);
    }

    // Common helper for adding either a human or AI player.
    static void addPlayer(String name, boolean isAI, String difficulty) {
        if (playerCount >= MAX_PLAYERS) return;
        String finalName = name == null ? "" : name.trim();
        if (finalName.length() == 0) finalName = "Player";
        playerNames[playerCount] = finalName;
        playerIsAI[playerCount] = isAI;
        playerDifficulty[playerCount] = cleanDifficulty(difficulty);
        playerWords[playerCount] = "";
        playerCount++;
    }

    // Normalizes AI difficulty input so spelling/capitalization mistakes do not crash the game.
    static String cleanDifficulty(String difficulty) {
        if (difficulty == null || difficulty.trim().length() == 0) return "EASY";
        String d = difficulty.trim().toUpperCase();
        if (d.equals("MEDIUM") || d.equals("HARD")) return d;
        return "EASY";
    }

    // Checks a submitted word, awards points, and updates the current player's state.
    static int submitWord(String word) {
        int current = currentTurnIndex;
        if (playerQuit[current] || playerPassed[current]) return -1;

        String w = word == null ? "" : word.trim().toUpperCase();
        boolean ok = isValidWord(w, board, dictionary, minimumWordLength, usedWords);
        if (!ok) {
            processWrongGuess(current);
            return 0;
        }

        int points = calculateScore(w, usedWords);
        if (points <= 0) return 2;

        usedWords.add(w);
        playerScores[current] += points;
        addWordFound(current, w);
        wrongGuessCount[current] = 0;
        timeoutCount[current] = 0;
        return 1;
    }

    // Adds a word to the player's text history.
    static void addWordFound(int playerIndex, String word) {
        if (word == null || word.trim().length() == 0) return;
        if (playerWords[playerIndex] == null || playerWords[playerIndex].length() == 0) {
            playerWords[playerIndex] = word;
        } else {
            playerWords[playerIndex] += ", " + word;
        }
    }

    // Tracks wrong guesses. Two wrong guesses cause an automatic pass.
    static void processWrongGuess(int playerIndex) {
        wrongGuessCount[playerIndex]++;
        if (wrongGuessCount[playerIndex] >= 2) {
            wrongGuessCount[playerIndex] = 0;
            autoPassWrongCount[playerIndex]++;

            int aiWinner = findAIWinnerForWrongGuesses(playerIndex);
            if (aiWinner != -1) {
                forcedWinnerIndex = aiWinner;
                endReason = "AI_WON_WRONG_GUESSES";
                return;
            }
            processPass(playerIndex);
        }
    }

    // In one-human-vs-one-AI mode, two wrong guesses can immediately give AI the win.
    static int findAIWinnerForWrongGuesses(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= playerCount || playerIsAI[playerIndex]) return -1;

        int aiIndex = -1;
        int activeHumanCount = 0;
        for (int i = 0; i < playerCount; i++) {
            if (playerQuit[i]) continue;
            if (playerIsAI[i]) {
                if (aiIndex != -1) return -1;
                aiIndex = i;
            } else {
                activeHumanCount++;
            }
        }

        if (activeHumanCount == 1) return aiIndex;
        return -1;
    }

    // Marks a player as passed for the current board.
    static void processPass(int playerIndex) {
        playerPassed[playerIndex] = true;
    }

    // Applies timeout behavior. A timeout counts as a pass.
    static void processTimeout(int playerIndex) {
        timeoutCount[playerIndex]++;
        if (timeoutCount[playerIndex] >= 2) {
            timeoutCount[playerIndex] = 0;
            autoPassTimerCount[playerIndex]++;
        }
        processPass(playerIndex);
    }

    // Marks a player as quit and records why the game ended.
    static void processQuit(int playerIndex) {
        playerQuit[playerIndex] = true;
        endReason = "OPPONENT_QUIT";
    }

    // Moves to the next active player. Returns 0=continue, 1=offer shake, 2=end.
    static int nextTurn() {
        int verified = verifyGameState();
        if (verified == 2) return 2;
        if (verified == 1) return 1;

        int start = currentTurnIndex;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % playerCount;
            if (currentTurnIndex == 0) currentRound++;
        } while ((playerQuit[currentTurnIndex] || playerPassed[currentTurnIndex]) && currentTurnIndex != start);

        return 0;
    }

    // Checks target score, quits, forced winners, and all-player-pass situations.
    static int verifyGameState() {
        if (forcedWinnerIndex != -1) {
            if (endReason == null) endReason = "FORCED_WINNER";
            return 2;
        }

        if (playerCount == 0) {
            endReason = "ALL_PLAYERS_PASSED";
            return 2;
        }

        if (targetScore > 0) {
            for (int i = 0; i < playerCount; i++) {
                if (playerScores[i] >= targetScore) {
                    endReason = "TARGET_SCORE_REACHED";
                    return 2;
                }
            }
        }

        for (int i = 0; i < playerCount; i++) {
            if (playerQuit[i]) {
                endReason = "OPPONENT_QUIT";
                return 2;
            }
        }

        boolean anyPassed = false;
        boolean anyActive = false;
        for (int i = 0; i < playerCount; i++) {
            if (!playerQuit[i] && !playerPassed[i]) anyActive = true;
            if (!playerQuit[i] && playerPassed[i]) anyPassed = true;
        }

        if (!anyActive && anyPassed) {
            if (!shakeUpUsed) return 1;
            endReason = "NO_MOVES_AFTER_SHAKE";
            return 2;
        }

        return 0;
    }

    // Checks whether the AI has overtaken the only human player after that human passed.
    static boolean shouldOfferShakeAfterAILead(int aiIndex) {
        return getPassedHumanIndexBehindAI(aiIndex) >= 0;
    }

    // Returns the passed human's index if a one-human-vs-one-AI shake offer is needed.
    static int getPassedHumanIndexBehindAI(int aiIndex) {
        if (aiIndex < 0 || aiIndex >= playerCount || !playerIsAI[aiIndex]) return -1;

        int aiCount = 0;
        int humanCount = 0;
        int passedHumanIndex = -1;

        for (int i = 0; i < playerCount; i++) {
            if (playerQuit[i]) continue;

            if (playerIsAI[i]) {
                aiCount++;
            } else {
                humanCount++;
                if (playerPassed[i] && playerScores[aiIndex] > playerScores[i]) {
                    passedHumanIndex = i;
                }
            }
        }

        if (aiCount == 1 && humanCount == 1) return passedHumanIndex;
        return -1;
    }

    // Generates a new board and clears round-only pass/wrong/timer state.
    static void performShake() {
        usedWords.clear();
        board = generateBoard();
        shakeUpUsed = true;
        for (int i = 0; i < playerCount; i++) {
            playerPassed[i] = false;
            wrongGuessCount[i] = 0;
            timeoutCount[i] = 0;
        }
    }

    // Runs one AI turn. Return values: 0=not AI, 1=AI passed, 2=AI played a word.
    static int runAITurnIfNeeded() {
        int current = currentTurnIndex;
        lastAIWord = null;
        lastAIPoints = 0;
        if (!playerIsAI[current]) return 0;

        ArrayList<String> found = findAllValidWords(board, dictionary, minimumWordLength, usedWords);
        String choice = chooseWord(found, playerDifficulty[current]);
        if (choice == null) {
            processPass(current);
            return 1;
        }

        int points = calculateScore(choice, usedWords);
        if (points <= 0) {
            processPass(current);
            return 1;
        }

        usedWords.add(choice);
        playerScores[current] += points;
        addWordFound(current, choice);
        lastAIWord = choice;
        lastAIPoints = points;
        return 2;
    }

    // Searches the board for every valid unused word the AI could play.
    static ArrayList<String> findAllValidWords(
            char[][] boardToSearch,
            ArrayList<String> dictionaryList,
            int minWordLength,
            ArrayList<String> wordsAlreadyUsed
    ) {
        int minLen = minWordLength;
        if (minLen < 3) minLen = 3;
        ArrayList<String> found = new ArrayList<String>();
        if (boardToSearch == null || dictionaryList == null) return found;

        boolean[][] visited = new boolean[boardToSearch.length][boardToSearch[0].length];
        StringBuilder current = new StringBuilder();

        for (int r = 0; r < boardToSearch.length; r++) {
            for (int c = 0; c < boardToSearch[0].length; c++) {
                dfsFindWords(boardToSearch, r, c, current, visited, dictionaryList, minLen, wordsAlreadyUsed, found);
            }
        }

        return found;
    }

    // Recursive board search used by the AI. It builds possible words one letter at a time.
    static void dfsFindWords(
            char[][] boardToSearch,
            int r,
            int c,
            StringBuilder current,
            boolean[][] visited,
            ArrayList<String> dictionaryList,
            int minLen,
            ArrayList<String> wordsAlreadyUsed,
            ArrayList<String> output
    ) {
        if (r < 0 || c < 0 || r >= boardToSearch.length || c >= boardToSearch[0].length) return;
        if (visited[r][c]) return;

        int lengthBefore = current.length();
        current.append(Character.toUpperCase(boardToSearch[r][c]));
        String currentWord = current.toString();

        if (!prefixExists(currentWord, dictionaryList)) {
            current.setLength(lengthBefore);
            return;
        }

        visited[r][c] = true;

        if (currentWord.length() >= minLen
                && checkDictionary(currentWord, dictionaryList)
                && !contains(wordsAlreadyUsed, currentWord)
                && !contains(output, currentWord)) {
            output.add(currentWord);
        }

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                dfsFindWords(boardToSearch, r + dr, c + dc, current, visited, dictionaryList, minLen, wordsAlreadyUsed, output);
            }
        }

        visited[r][c] = false;
        current.setLength(lengthBefore);
    }

    // Chooses an AI word based on difficulty.
    static String chooseWord(ArrayList<String> aiWordList, String difficulty) {
        if (aiWordList == null || aiWordList.isEmpty()) return null;
        String diff = cleanDifficulty(difficulty);

        if (diff.equals("EASY")) {
            ArrayList<String> easyWords = new ArrayList<String>();
            for (int i = 0; i < aiWordList.size(); i++) {
                String word = aiWordList.get(i);
                if (word != null && word.length() >= 3 && word.length() <= 4) {
                    easyWords.add(word);
                }
            }
            if (easyWords.isEmpty()) return null;
            return easyWords.get(RANDOM.nextInt(easyWords.size()));
        }

        ArrayList<String> sorted = new ArrayList<String>();
        for (int i = 0; i < aiWordList.size(); i++) sorted.add(aiWordList.get(i));
        insertionSortByLength(sorted);

        if (diff.equals("HARD")) return sorted.get(0);

        int top = Math.max(1, sorted.size() / 2);
        return sorted.get(RANDOM.nextInt(top));
    }

    // Rolls all dice and places them into a shuffled 5x5 board.
    static char[][] generateBoard() {
        char[] letters = new char[DICE.length];
        for (int i = 0; i < DICE.length; i++) {
            int face = RANDOM.nextInt(DICE[i].length());
            letters[i] = DICE[i].charAt(face);
        }
        shuffleCharArray(letters);

        char[][] newBoard = new char[BOARD_SIZE][BOARD_SIZE];
        int k = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                newBoard[r][c] = letters[k++];
            }
        }
        return newBoard;
    }

    // Fisher-Yates shuffle for the rolled dice letters.
    static void shuffleCharArray(char[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    // Prints the current 5x5 board to the terminal.
    static void printBoard(char[][] boardToPrint) {
        System.out.println();
        System.out.println("Boggle Board:");
        for (int r = 0; r < boardToPrint.length; r++) {
            for (int c = 0; c < boardToPrint[0].length; c++) {
                System.out.print(boardToPrint[r][c] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // Loads the word list and sorts it so binary search can be used.
    static ArrayList<String> loadDictionary(File wordListFile) {
        ArrayList<String> words = new ArrayList<String>();
        if (wordListFile == null || !wordListFile.exists()) return words;

        Scanner fileScanner;
        try {
            fileScanner = new Scanner(wordListFile);
        } catch (Exception e) {
            return words;
        }

        while (fileScanner.hasNextLine()) {
            String word = fileScanner.nextLine();
            if (word != null) {
                word = word.trim();
                if (word.length() > 0) words.add(word.toUpperCase());
            }
        }
        fileScanner.close();

        Collections.sort(words);
        return words;
    }

    // A valid word must be long enough, unused, in the dictionary, and possible on the board.
    static boolean isValidWord(
            String word,
            char[][] boardToCheck,
            ArrayList<String> dictionaryList,
            int minWordLength,
            ArrayList<String> wordsAlreadyUsed
    ) {
        if (word == null) return false;
        String w = word.trim().toUpperCase();
        int minLen = minWordLength;
        if (minLen < 3) minLen = 3;
        if (w.length() < minLen) return false;
        if (contains(wordsAlreadyUsed, w)) return false;
        if (!checkDictionary(w, dictionaryList)) return false;
        return findLetter(boardToCheck, w);
    }

    // Starts board validation by trying every square that matches the first letter.
    static boolean findLetter(char[][] boardToCheck, String word) {
        if (boardToCheck == null || word == null) return false;
        String w = word.toUpperCase();
        if (w.length() == 0) return false;

        int rows = boardToCheck.length;
        int cols = boardToCheck[0].length;
        char first = w.charAt(0);

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (Character.toUpperCase(boardToCheck[r][c]) == first) {
                    boolean[][] visited = new boolean[rows][cols];
                    if (checkBoard(boardToCheck, r, c, w, 0, visited)) return true;
                }
            }
        }
        return false;
    }

    // Recursive word path checker. A square cannot be reused in the same word.
    static boolean checkBoard(char[][] boardToCheck, int r, int c, String word, int index, boolean[][] visited) {
        if (index == word.length()) return true;
        if (r < 0 || c < 0 || r >= boardToCheck.length || c >= boardToCheck[0].length) return false;
        if (visited[r][c]) return false;
        if (Character.toUpperCase(boardToCheck[r][c]) != word.charAt(index)) return false;

        visited[r][c] = true;
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                if (checkBoard(boardToCheck, r + dr, c + dc, word, index + 1, visited)) {
                    visited[r][c] = false;
                    return true;
                }
            }
        }

        visited[r][c] = false;
        return false;
    }

    // Looks up an exact word in the sorted dictionary.
    static boolean checkDictionary(String word, ArrayList<String> wordList) {
        if (word == null || wordList == null || wordList.isEmpty()) return false;
        String w = word.toUpperCase();
        return checkDictionary(w, wordList, 0, wordList.size() - 1);
    }

    // Binary search helper for dictionary lookup.
    static boolean checkDictionary(String word, ArrayList<String> wordList, int low, int high) {
        if (word == null || wordList == null) return false;
        if (low > high) return false;

        int mid = low + (high - low) / 2;
        int comparison = word.compareTo(wordList.get(mid));
        if (comparison == 0) return true;
        if (comparison < 0) return checkDictionary(word, wordList, low, mid - 1);
        return checkDictionary(word, wordList, mid + 1, high);
    }

    // Checks whether any dictionary word starts with the current AI search prefix.
    static boolean prefixExists(String prefix, ArrayList<String> wordList) {
        if (prefix == null || prefix.length() == 0 || wordList == null || wordList.isEmpty()) return false;
        String p = prefix.toUpperCase();

        int low = 0;
        int high = wordList.size() - 1;
        int firstCandidate = -1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            String midWord = wordList.get(mid);
            if (midWord.compareTo(p) >= 0) {
                firstCandidate = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }

        if (firstCandidate == -1) return false;
        return wordList.get(firstCandidate).startsWith(p);
    }

    // Scores words by length. Reused words are worth zero.
    static int calculateScore(String word, ArrayList<String> wordHistory) {
        if (word == null) return 0;
        String w = word.toUpperCase();
        if (contains(wordHistory, w)) return 0;
        return w.length();
    }

    // Small manual contains check used throughout the program.
    static boolean contains(ArrayList<String> list, String value) {
        if (list == null || value == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (value.equals(list.get(i))) return true;
        }
        return false;
    }

    // Sorts strings alphabetically. Kept as a simple classroom sorting example.
    static void insertionSort(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String current = words.get(i);
            int j = i - 1;
            while (j >= 0 && words.get(j).compareTo(current) > 0) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, current);
        }
    }

    // Sorts AI candidate words from longest to shortest.
    static void insertionSortByLength(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String current = words.get(i);
            int j = i - 1;
            while (j >= 0 && words.get(j).length() < current.length()) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, current);
        }
    }

    // Prints final scores and writes a game summary log.
    static void announceWinner() {
        int winnerIndex = determineWinnerIndex();
        int bestScore = -1;
        int bestCount = 0;

        for (int i = 0; i < playerCount; i++) {
            if (playerQuit[i]) continue;
            if (playerScores[i] > bestScore) {
                bestScore = playerScores[i];
                bestCount = 1;
            } else if (playerScores[i] == bestScore) {
                bestCount++;
            }
        }

        System.out.println();
        if (bestCount > 1) {
            System.out.println("Game ended. Result: TIED");
        } else {
            System.out.println("Game ended. Winner: " + (winnerIndex == -1 ? "NONE" : playerNames[winnerIndex]));
        }
        for (int i = 0; i < playerCount; i++) {
            System.out.println(playerNames[i] + " score=" + playerScores[i]);
        }
        writeGameLog(new File("boggleSave.txt"));
    }

    // Finds the non-quit player with the highest score.
    static int determineWinnerIndex() {
        if (forcedWinnerIndex != -1) return forcedWinnerIndex;

        int best = -1;
        for (int i = 0; i < playerCount; i++) {
            if (playerQuit[i]) continue;
            if (best == -1 || playerScores[i] > playerScores[best]) best = i;
        }
        return best;
    }

    // Appends a readable summary of the completed game to a file.
    static void writeGameLog(File file) {
        if (file == null) return;

        FileWriter writer;
        try {
            writer = new FileWriter(file, true);
        } catch (Exception e) {
            return;
        }

        try {
            int winner = determineWinnerIndex();
            writer.write("--- GAME SUMMARY ---\n");
            writer.write("Time: " + LocalDateTime.now() + "\n");
            writer.write("Winner: " + (winner == -1 ? "NONE" : playerNames[winner]) + "\n");
            writer.write("Reason: " + (endReason == null ? "UNKNOWN" : endReason) + "\n");
            writer.write("Total Rounds: " + currentRound + "\n");
            writer.write("Shake-ups used: " + (shakeUpUsed ? "YES" : "NO") + "\n");

            for (int i = 0; i < playerCount; i++) {
                writer.write("Name: " + playerNames[i] + " | Score: " + playerScores[i] + "\n");
                writer.write("Words Found: " + (playerWords[i] == null ? "" : playerWords[i]) + "\n");
                writer.write("Auto-passes (Wrong Words): " + autoPassWrongCount[i] + "\n");
                writer.write("Auto-passes (Timer): " + autoPassTimerCount[i] + "\n");
            }
            writer.write("\n");
        } catch (Exception e) {
            // Keep the game playable if logging fails.
        }

        try {
            writer.close();
        } catch (Exception e) {
            // Nothing else to do.
        }
    }

    // Saves the current game state when the Player vs AI human quits.
    static boolean saveGame(File file) {
        if (file == null) return false;

        FileWriter writer;
        try {
            writer = new FileWriter(file, false);
        } catch (Exception e) {
            return false;
        }

        try {
            writer.write("Status: Paused\n");
            writer.write("CurrentRound: " + currentRound + "\n");
            writer.write("ShakeUpUsed: " + (shakeUpUsed ? "YES" : "NO") + "\n");
            writer.write("UsedWords: ");
            for (int i = 0; i < usedWords.size(); i++) {
                if (i > 0) writer.write(",");
                writer.write(usedWords.get(i));
            }
            writer.write("\n");

            writer.write("Board:\n");
            for (int r = 0; r < board.length; r++) {
                for (int c = 0; c < board[0].length; c++) {
                    writer.write(String.valueOf(board[r][c]));
                }
                writer.write("\n");
            }

            writer.write("Players:\n");
            for (int i = 0; i < playerCount; i++) {
                writer.write(playerNames[i] + "|" + playerScores[i] + "\n");
            }
        } catch (Exception e) {
            try {
                writer.close();
            } catch (Exception closeException) {
                // Nothing else to do.
            }
            return false;
        }

        try {
            writer.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Reads typed input and checks whether it took longer than the selected timer.
    static String getInputWithTimer(Scanner sc, int timerSeconds, boolean allowTimeout) {
        long start = System.currentTimeMillis();
        String input = readLine(sc).trim();

        if (allowTimeout && timerSeconds > 0) {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > timerSeconds * 1000L) {
                System.out.println("Time is up! (Took " + (elapsed / 1000.0) + " seconds, limit was " + timerSeconds + " seconds)");
                processTimeout(currentTurnIndex);
                return "TIMEOUT_PASS";
            }
            System.out.println("(Time taken: " + (elapsed / 1000.0) + " seconds)");
        }

        return input;
    }

    // Lets the user choose a timer length for human turns.
    static int readTimerChoice(Scanner sc) {
        for (;;) {
            System.out.println();
            System.out.println("Timer choices:");
            System.out.println("1) None");
            System.out.println("2) 15 seconds");
            System.out.println("3) 30 seconds");
            System.out.println("4) 60 seconds");
            System.out.println("5) Custom");
            System.out.print("Choose timer (1-5): ");

            String choice = readLine(sc).trim();
            if (choice.equals("1")) return 0;
            if (choice.equals("2")) return 15;
            if (choice.equals("3")) return 30;
            if (choice.equals("4")) return 60;
            if (choice.equals("5")) return readInt(sc, "Enter custom timer in seconds: ", 1);
            System.out.println("Invalid choice.");
        }
    }

    // Reads an integer that must be at least minValue.
    static int readInt(Scanner sc, String prompt, int minValue) {
        for (;;) {
            System.out.print(prompt);
            String value = readLine(sc);
            try {
                int number = Integer.parseInt(value.trim());
                if (number < minValue) System.out.println("Must be >= " + minValue);
                else return number;
            } catch (Exception e) {
                System.out.println("Enter a number.");
            }
        }
    }

    // Reads an integer that can be zero but cannot be negative.
    static int readIntAllowZero(Scanner sc, String prompt) {
        for (;;) {
            System.out.print(prompt);
            String value = readLine(sc);
            try {
                int number = Integer.parseInt(value.trim());
                if (number < 0) System.out.println("Must be >= 0");
                else return number;
            } catch (Exception e) {
                System.out.println("Enter a number.");
            }
        }
    }

    // Safe wrapper for nextLine so the program does not crash if input ends.
    static String readLine(Scanner sc) {
        if (sc == null || !sc.hasNextLine()) return "";
        return sc.nextLine();
    }

    // Removes characters that could make an unsafe save-file name.
    static String safeSaveName(String name) {
        if (name == null || name.trim().length() == 0) return "Player";
        return name.trim().replaceAll("[^A-Za-z0-9_-]", "_");
    }

    // Finds the dictionary file from common locations.
    static File findDictionaryFile() {
        String[] candidates = {
                "wordlist.txt",
                "NO_OOP/wordlist.txt",
                "../src/wordlist.txt",
                "src/wordlist.txt",
                "../wordlist.txt"
        };

        for (int i = 0; i < candidates.length; i++) {
            File file = new File(candidates[i]);
            if (file.exists()) return file;
        }
        return null;
    }
}
