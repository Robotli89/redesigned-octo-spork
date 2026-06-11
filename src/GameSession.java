/*
 * Author: Kevin Li and Ethan Chuang
 * Date:   June 4, 2026
 * Course: ICS4U1
 * Project: Boggle Game
 *
 * Description:
 * This class manages a Boggle game session. It stores the board, dictionary,
 * players, scores, rounds, and game state, and applies the rules for words,
 * turns, passing, hints, and winning.
 *
 * Copyright (c) Kevin Li and Ethan Chuang
 */

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class GameSession {
    // One shared random generator for the whole game (board, shuffle, etc.).
    public static final Random random = new Random();
    public static final int BOARD_SIZE = 5;  // the board is 5 x 5

    // The 25 Boggle "dice". Each string is one die, and each letter is one of
    // its faces. When we build a board we roll each die (pick one face).
    public static final String[] dice = {
            "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY",
            "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW",
            "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY",
            "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW",
            "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
    };

    public int minimumWordLength;  // shortest allowed word
    public int maximumWordLength;  // longest allowed word (0 means no limit)
    public int targetScore;        // score that ends the game (0 means no target)

    public ArrayList<Player> players;     // everyone playing this game
    public ArrayList<String> usedWords;   // words already played (no repeats)
    public ArrayList<String> dictionary;  // the sorted list of legal words

    public BoggleAI boggleAI;       // helper that searches the board for words
    public boolean shakeUpUsed;     // the board can only be shaken once
    public boolean hintUsed;        // the hint can only be used once
    public String endReason;        // why the game ended (used in the log)
    public Player forcedWinner;     // set when a player auto-loses (e.g. wrong guesses)

    public int currentTurnIndex;    // index into players of whose turn it is
    public int currentRound;        // which round we are on
    public char[][] board;          // the 5 x 5 grid of letters

    // Shorter constructor (overload) for callers with no maximum word length.
    // It just calls the full constructor with maxWordLen = 0.
    public GameSession(
            ArrayList<Player> playerList,
            int minWordLen,
            int target,
            File dictionaryFile
    ) {
        this(playerList, minWordLen, target, dictionaryFile, 0);
    }

    // Full constructor: sets up everything a game needs and deals a board.
    public GameSession(
            ArrayList<Player> playerList,
            int minWordLen,
            int target,
            File dictionaryFile,
            int maxWordLen
    ) {
        // Force the minimum length to be at least 3 (Boggle rule).
        minimumWordLength = minWordLen;
        if (minWordLen < 3) minimumWordLength = 3;
        maximumWordLength = maxWordLen;
        // A maximum below the minimum makes no sense, so bump it up.
        if (maximumWordLength > 0 && maximumWordLength < minimumWordLength) maximumWordLength = minimumWordLength;
        targetScore = target;
        if (target < 0) targetScore = 0;
        // Guard against a missing player list.
        if (playerList == null || playerList.isEmpty()) playerList = new ArrayList<Player>();
        // Copy the players into our own list so the caller's list is untouched.
        players = new ArrayList<Player>();
        for (int i = 0; i < playerList.size(); i++) players.add(playerList.get(i));
        usedWords = new ArrayList<String>();
        dictionary = loadDictionary(dictionaryFile);  // load + sort once here
        boggleAI = new BoggleAI();
        shakeUpUsed = false;
        hintUsed = false;
        currentTurnIndex = 0;
        currentRound = 1;
        generateNewBoard();
    }

    // --- Simple "getter" methods that just hand back a piece of game state. ---
    public char[][] getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    // The player whose turn it currently is.
    public Player getCurrentPlayer() {
        return players.get(currentTurnIndex);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isShakeUpUsed() {
        return shakeUpUsed;
    }

    public boolean isHintUsed() {
        return hintUsed;
    }

    // Record that the one-time hint has now been used.
    public void markHintUsed() {
        hintUsed = true;
    }

    // Deal a brand-new random board into the board field.
    public void generateNewBoard() {
        board = generateBoard();
    }

    // Tries to play a word for the current player and reports what happened.
    // return: 1=valid, 2=already used, 0=invalid, -1=not active
    public int submitWord(String word) {
        Player p = getCurrentPlayer();
        // A player who quit or passed cannot play.
        if (p.quit || p.passed) return -1;

        // Clean up the input (trim spaces, make uppercase).
        String w = word == null ? "" : word.trim().toUpperCase();
        if (contains(usedWords, w)) {
            return 2;  // someone already played this word
        }

        // Check length, dictionary, and whether the word can be traced on the board.
        boolean ok = isValidWord(w, board, dictionary, minimumWordLength, maximumWordLength, usedWords);
        if (!ok) {
            processWrongGuess(p);  // wrong guess may trigger an auto-pass
            return 0;
        }

        int points = calculateScore(w, usedWords);
        if (points <= 0) return 2;

        // Word is good: record it, add the score, and clear the streak counters.
        usedWords.add(w);
        p.addScore(points);
        p.addWordFound(w);
        p.resetWrongGuessCount();
        p.resetTimeoutCount();

        return 1;
    }

    // The current player chooses to pass.
    public void pass() {
        processPass(getCurrentPlayer());
    }

    // The current player ran out of time.
    public void timeout() {
        processTimeout(getCurrentPlayer());
    }

    // The current player quits the game.
    public void quit() {
        processQuit(getCurrentPlayer());
    }

    // Moves play to the next player who can still take a turn.
    // return: 0=continue, 1=offer shake, 2=ended
    public int nextTurn() {
        // First check whether the game should pause or end.
        int vr = verifyGameState(players);
        if (vr == 2) return 2;
        if (vr == 1) return 1;

        // Step forward (wrapping around with %), skipping players who quit or
        // passed. We remember where we started so we never loop forever.
        int start = currentTurnIndex;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % players.size();
            if (currentTurnIndex == 0) currentRound++;  // wrapped past the last player
        } while ((players.get(currentTurnIndex).quit || players.get(currentTurnIndex).passed) && currentTurnIndex != start);

        return 0;
    }

    // "Shake" the board: wipe used words, deal a new board, and let everyone
    // play again. Can only happen once per game.
    public void performShake() {
        usedWords.clear();
        generateNewBoard();
        shakeUpUsed = true;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.resetRoundState();
        }
    }

    // If it is an AI player's turn, let the AI find and play a word. Returns an
    // AIResult describing what the AI did (or that it was not an AI's turn).
    public AIResult runAITurnIfNeeded() {
        Player p = getCurrentPlayer();
        if (p.isAI == false) return new AIResult(false, false, null, 0);

        // Ask the AI helper for every valid word, then pick one by difficulty.
        ArrayList<String> found = boggleAI.findAllValidWords(board, dictionary, minimumWordLength, usedWords, maximumWordLength);
        String choice = boggleAI.chooseWord(found, p.difficulty);
        if (choice == null) {
            pass();  // nothing to play
            return new AIResult(true, true, null, 0);
        }

        int points = calculateScore(choice, usedWords);
        if (points <= 0) {
            pass();
            return new AIResult(true, true, null, 0);
        }

        // Record the AI's word and score it.
        usedWords.add(choice);
        p.addScore(points);
        p.addWordFound(choice);
        return new AIResult(true, false, choice, points);
    }

    // True if we should offer a board shake because the AI just pulled ahead of
    // a human who had already passed (only in a 1-human vs 1-AI game).
    public boolean shouldOfferShakeAfterAILead(Player aiPlayer) {
        return getPassedHumanIndexBehindAI(aiPlayer) >= 0;
    }

    // In a 1-human-vs-1-AI game, returns the index of the human if they have
    // passed and are now losing to the AI; otherwise returns -1.
    public int getPassedHumanIndexBehindAI(Player aiPlayer) {
        if (aiPlayer == null || !aiPlayer.isAI) return -1;

        int aiCount = 0;
        int humanCount = 0;
        int passedHumanIndex = -1;

        // Count AIs and humans, and note a passed-and-losing human.
        for (int i = 0; i < players.size(); i++) {
            Player player = players.get(i);
            if (player.quit) continue;

            if (player.isAI) {
                aiCount++;
            } else {
                humanCount++;
                if (player.passed && aiPlayer.totalScore > player.totalScore) {
                    passedHumanIndex = i;
                }
            }
        }

        // Only meaningful when it is exactly one human against one AI.
        if (aiCount == 1 && humanCount == 1) return passedHumanIndex;
        return -1;
    }

    // Figures out who won: the forced winner if there is one, otherwise the
    // active player with the highest score.
    public Player determineWinner() {
        if (forcedWinner != null) return forcedWinner;

        Player best = null;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.quit) continue;
            if (best == null || p.totalScore > best.totalScore) best = p;
        }
        return best;
    }

    public String getEndReason() {
        return endReason;
    }

    // Append a summary of this finished game to the log file.
    public void writeLog(File logFile) {
        Player winner = determineWinner();
        try {
            writeGameLog(
                    logFile,
                    winner == null ? "NONE" : winner.name,
                    getEndReason(),
                    currentRound,
                    players,
                    shakeUpUsed
            );
        } catch (Exception e) {
            // ignore
        }
    }

    // Save the current game to a file (used when a player quits a vs-AI game).
    public void saveIfPvAIQuit(File saveFile) {
        try {
            saveGame(saveFile, currentRound, board, players, usedWords, shakeUpUsed);
        } catch (Exception e) {
            // ignore
        }
    }

    // Handle a wrong guess. Two wrong guesses in a row force the player to pass.
    // In a solo human-vs-AI game, that hands the win to the AI.
    public void processWrongGuess(Player currentPlayer) {
        currentPlayer.incrementWrongGuessCount();
        if (currentPlayer.wrongGuessCount >= 2) {
            currentPlayer.resetWrongGuessCount();
            currentPlayer.incrementAutoPassWrongCount();
            Player aiWinner = findAIWinnerForWrongGuesses(currentPlayer);
            if (aiWinner != null) {
                forcedWinner = aiWinner;
                endReason = "AI_WON_WRONG_GUESSES";
                return;
            }
            processPass(currentPlayer);
        }
    }

    // If the game is one human against exactly one AI, return that AI (so it can
    // be declared the winner). Otherwise return null.
    public Player findAIWinnerForWrongGuesses(Player currentPlayer) {
        if (currentPlayer == null || currentPlayer.isAI) return null;
        Player aiPlayer = null;
        int activeHumanCount = 0;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.quit) continue;
            if (p.isAI) {
                if (aiPlayer != null) return null;  // more than one AI: not this rule
                aiPlayer = p;
            } else {
                activeHumanCount++;
            }
        }

        if (activeHumanCount == 1) return aiPlayer;
        return null;
    }

    // Handle a timeout. Either way the player passes; two in a row also counts
    // as a timer auto-pass for the end-of-game stats.
    public void processTimeout(Player currentPlayer) {
        currentPlayer.incrementTimeoutCount();
        if (currentPlayer.timeoutCount >= 2) {
            currentPlayer.resetTimeoutCount();
            currentPlayer.incrementAutoPassTimerCount();
            processPass(currentPlayer);
        } else {
            processPass(currentPlayer);
        }
    }

    // Mark a player as having passed.
    public void processPass(Player currentPlayer) {
        currentPlayer.passed = true;
    }

    // Mark a player as having quit, and remember why the game is ending.
    public void processQuit(Player currentPlayer) {
        currentPlayer.quit = true;
        endReason = "OPPONENT_QUIT";
    }

    // Decides whether the game should keep going, offer a shake, or end. It also
    // records endReason so the log can explain how the game finished.
    // return: 0=continue, 1=offer shake, 2=end game
    public int verifyGameState(ArrayList<Player> players) {
        // A forced winner (e.g. opponent lost on wrong guesses) ends the game.
        if (forcedWinner != null) {
            if (endReason == null) endReason = "FORCED_WINNER";
            return 2;
        }

        if (players == null || players.isEmpty()) {
            endReason = "ALL_PLAYERS_PASSED";
            return 2;
        }

        // If a target score is set and someone reached it, the game ends.
        if (targetScore > 0) {
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (p.totalScore >= targetScore) {
                    endReason = "TARGET_SCORE_REACHED";
                    return 2;
                }
            }
        }

        // If anyone quit, the game ends.
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.quit) {
                endReason = "OPPONENT_QUIT";
                return 2;
            }
        }

        // Look at whether anyone can still move and whether anyone has passed.
        boolean anyPassed = false;
        boolean anyActive = false;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (!p.quit && !p.passed) anyActive = true;
            if (!p.quit && p.passed) anyPassed = true;
        }

        // Everyone who is left has passed: offer a shake (if not used yet),
        // otherwise the game is truly over.
        if (!anyActive && anyPassed) {
            if (!shakeUpUsed) return 1;
            endReason = "NO_MOVES_AFTER_SHAKE";
            return 2;
        }

        return 0;  // game continues normally
    }

    // Builds a random 5 x 5 board by rolling each die for one letter, shuffling
    // all 25 letters, then filling the grid row by row.
    public static char[][] generateBoard() {
        char[] letters = new char[dice.length];
        for (int i = 0;i < dice.length;i++) {
            int face = random.nextInt(dice[i].length());  // roll: pick a face
            letters[i] = dice[i].charAt(face);
        }
        shuffleCharArray(letters);  // mix up which square each letter lands on
        char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
        int k = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = letters[k++];
            }
        }
        return board;
    }

    // Shuffles an array fairly using the Fisher-Yates shuffle: walk from the
    // last spot to the first, and swap each item with a random earlier-or-equal
    // spot. This gives every ordering an equal chance.
    public static void shuffleCharArray(char[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);  // random index from 0..i
            char tmp = arr[i];              // swap arr[i] and arr[j]
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    // Prints the board to the text console, one row per line.
    public static void printBoard(char[][] board) {
        System.out.println("\nBoggle Board:");
        for (int row = 0; row < board.length; row++) {
            for (int col = 0; col < board[row].length; col++) {
                System.out.print(board[row][col] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    // ---------------------------------------------------------------------
    // HARD PART (setup for fast lookups): "sort once, search many".
    //
    // We read every word from the file into a list, then sort the list ONE
    // time here. Because the list stays sorted, later lookups can use binary
    // search (checkDictionary / prefixExists), which is far faster than scanning
    // the whole list every time. We pay the sorting cost once and reuse it.
    // ---------------------------------------------------------------------
    public static ArrayList<String> loadDictionary(File wordListFile) {
        ArrayList<String> words = new ArrayList<String>();
        if (wordListFile == null || !wordListFile.exists()) return words;
        Scanner sc;
        try {
            sc = new Scanner(wordListFile);
        } catch (Exception e) {
            return words;  // file could not be opened: return an empty list
        }
        // Read the file line by line, keeping each non-blank word in uppercase.
        while (sc.hasNextLine()) {
            String w = sc.nextLine();
            if (w != null) {
                w = w.trim();
                if (w.length() > 0) {
                    words.add(w.toUpperCase());
                }
            }
        }
        sc.close();

        insertionSort(words);  // sort once so binary search works later
        return words;
    }

    // Sorts the word list into alphabetical (dictionary) order using insertion
    // sort. Same card-sorting idea as before, but here we compare words with
    // compareTo so they end up A-to-Z.
    public static void insertionSort(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String cur = words.get(i);
            int j = i - 1;
            // Slide bigger (later-alphabet) words right to open a gap for cur.
            while (j >= 0 && words.get(j).compareTo(cur) > 0) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, cur);
        }
    }

    // Writes a human-readable summary of one finished game. Opened in "append"
    // mode (the true below) so each game is added under the previous ones.
    public static void writeGameLog(
            File file,
            String winnerName,
            String endReason,
            int roundCount,
            ArrayList<Player> players,
            boolean shakeUpUsed
    ) {
        if (file == null) return;
        FileWriter fw;
        try {
            fw = new FileWriter(file, true);  // true = append, do not overwrite
        } catch (Exception e) {
            return;
        }
        try {
            fw.write("--- GAME SUMMARY ---\n");
            fw.write("Time: " + LocalDateTime.now() + "\n");
            fw.write("Winner: " + winnerName + "\n");
            fw.write("Reason: " + (endReason == null ? "UNKNOWN" : endReason) + "\n");
            fw.write("Total Rounds: " + roundCount + "\n");
            fw.write("Shake-ups used: " + (shakeUpUsed ? "YES" : "NO") + "\n");

            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    fw.write("Name: " + p.name + " | Score: " + p.totalScore + "\n");
                    fw.write("Words Found: " + p.wordsFound + "\n");
                    fw.write("Auto-passes (Wrong Words): " + p.autoPassWrongCount + "\n");
                    fw.write("Auto-passes (Timer): " + p.autoPassTimerCount + "\n");
                }
            }
            fw.write("\n");
        } catch (Exception e) {
            // ignore
        }
        try { fw.close(); } catch (Exception e) {}
    }

    // Saves the whole game state to a file so it can be loaded again later.
    // Opened in "overwrite" mode (false) so there is only ever one saved game.
    // The format is simple labelled lines that loadGame() reads back.
    public static boolean saveGame(
            File file,
            int currentRound,
            char[][] currentBoard,
            ArrayList<Player> players,
            ArrayList<String> usedWords,
            boolean shakeUpUsed
    ) {
        if (file == null) return false;
        FileWriter fw;
        try {
            fw = new FileWriter(file, false);  // false = overwrite the old save
        } catch (Exception e) {
            return false;
        }
        try {
            fw.write("Status: Paused\n");
            fw.write("CurrentRound: " + currentRound + "\n");
            fw.write("ShakeUpUsed: " + (shakeUpUsed ? "YES" : "NO") + "\n");

            // Join the used words into one comma-separated line.
            String usedLine = "";
            if (usedWords != null) {
                for (int i = 0; i < usedWords.size(); i++) {
                    if (i > 0) usedLine += ",";
                    usedLine += usedWords.get(i);
                }
            }
            fw.write("UsedWords: " + usedLine + "\n");

            // Write the board, one row per line.
            fw.write("Board:\n");
            for (int r = 0; r < currentBoard.length; r++) {
                for (int c = 0; c < currentBoard[0].length; c++) {
                    fw.write(String.valueOf(currentBoard[r][c]));
                }
                fw.write("\n");
            }

            // Write each player as: name|score|word1,word2,...
            fw.write("Players:\n");
            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    String wordsLine = "";
                    if (p.wordsFound != null) {
                        for (int j = 0; j < p.wordsFound.size(); j++) {
                            if (j > 0) wordsLine += ",";
                            wordsLine += p.wordsFound.get(j);
                        }
                    }
                    fw.write(p.name + "|" + p.totalScore + "|" + wordsLine + "\n");
                }
            }
        } catch (Exception e) {
            try { fw.close(); } catch (Exception closeException) {}
            return false;
        }
        try {
            fw.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    // Shorter version (overload) with no maximum word length.
    public static boolean isValidWord(
            String word,
            char[][] board,
            ArrayList<String> dictionary,
            int minWordLength,
            ArrayList<String> usedWords
    ) {
        return isValidWord(word, board, dictionary, minWordLength, 0, usedWords);
    }

    // Full check that a word is allowed: right length, not already used, a real
    // dictionary word, and actually traceable on the board.
    public static boolean isValidWord(
            String word,
            char[][] board,
            ArrayList<String> dictionary,
            int minWordLength,
            int maxWordLength,
            ArrayList<String> usedWords
    ) {
        if (word == null) return false;
        String w = word.trim().toUpperCase();
        int minLen = minWordLength;
        if (minLen < 3) minLen = 3;
        if (w.length() < minLen) return false;                       // too short
        if (maxWordLength > 0 && w.length() > maxWordLength) return false;  // too long
        if (contains(usedWords, w)) return false;                    // already played
        if (!checkDictionary(w, dictionary)) return false;           // not a real word
        return findLetter(board, w);                                 // can it be traced?
    }

    // Checks whether the word can be spelled along connected squares on the
    // board. It tries starting from every square whose letter matches the
    // word's first letter.
    public static boolean findLetter(char[][] board, String word) {
        if (board == null || word == null) return false;
        String w = word.toUpperCase();
        if (w.isEmpty()) return false;

        int n = board.length;
        int m = board[0].length;
        char first = w.charAt(0);

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                if (Character.toUpperCase(board[r][c]) == first) {
                    // Each starting square gets its own fresh visited grid.
                    boolean[][] visited = new boolean[n][m];
                    if (checkBoard(board, r, c, w, 0, visited)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ---------------------------------------------------------------------
    // HARD PART: recursive DFS that traces ONE specific word on the board.
    //
    // "index" is how far into the word we have matched. At square (r, c) we
    // check that its letter equals word[index]; if so we mark it visited and
    // try to match the next letter from any of the 8 neighbours. If we match
    // the whole word (index reaches the end) we return true. The visited grid
    // is undone on the way out (backtracking) so other paths stay free.
    // ---------------------------------------------------------------------
    public static boolean checkBoard(char[][] board, int r, int c, String word, int index, boolean[][] visited) {
        if (index == word.length()) return true;  // matched the whole word
        if (r < 0 || c < 0 || r >= board.length || c >= board[0].length) return false;  // off-board
        if (visited[r][c]) return false;          // square already used in this path
        if (Character.toUpperCase(board[r][c]) != word.charAt(index)) return false;  // wrong letter

        visited[r][c] = true;  // use this square for the current letter

        // Try to match the next letter from any of the 8 neighbours.
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                if (checkBoard(board, r + dr, c + dc, word, index + 1, visited)) {
                    visited[r][c] = false;  // undo before returning success
                    return true;
                }
            }
        }

        visited[r][c] = false;  // backtrack: free this square for other paths
        return false;
    }

    // Public entry point for a dictionary lookup. Hands off to the recursive
    // binary search below, searching the whole list (index 0 to last).
    public static boolean checkDictionary(String word, ArrayList<String> wordList) {
        if (word == null || wordList == null || wordList.isEmpty()) return false;
        String w = word.toUpperCase();
        return checkDictionary(w, wordList, 0, wordList.size() - 1);
    }

    // ---------------------------------------------------------------------
    // HARD PART: recursive binary search (the list MUST be sorted).
    //
    // Instead of checking every word, we look at the middle word and compare:
    //   - equal      -> found it.
    //   - word smaller -> the answer can only be in the LEFT half.
    //   - word bigger  -> the answer can only be in the RIGHT half.
    // Each step throws away half of what is left, so even a huge dictionary is
    // searched in only a handful of comparisons.
    // ---------------------------------------------------------------------
    public static boolean checkDictionary(String word, ArrayList<String> wordList, int low, int high) {
        if (word == null || wordList == null) return false;
        if (low > high) return false;  // nothing left to search: not found
        int mid = low + (high - low) / 2;
        int cmp = word.compareTo(wordList.get(mid));
        if (cmp == 0) return true;                                       // exact match
        if (cmp < 0) return checkDictionary(word, wordList, low, mid - 1);  // search left
        return checkDictionary(word, wordList, mid + 1, high);              // search right
    }

    // ---------------------------------------------------------------------
    // HARD PART: binary search for a PREFIX (used to prune the AI's search).
    //
    // We want to know "does any dictionary word start with these letters?".
    // Using binary search we find the first word that is >= the prefix (the
    // earliest spot the prefix could appear). If that word actually starts with
    // the prefix, then yes; otherwise no word does. This is still O(log n).
    // ---------------------------------------------------------------------
    public static boolean prefixExists(String prefix, ArrayList<String> wordList) {
        if (prefix == null || prefix.isEmpty() || wordList == null || wordList.isEmpty()) return false;
        String p = prefix.toUpperCase();

        int lo = 0;
        int hi = wordList.size() - 1;
        int firstCandidate = -1;  // index of the first word >= the prefix

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            String midWord = wordList.get(mid);
            if (midWord.compareTo(p) >= 0) {
                // midWord could be the first match; remember it and look further left.
                firstCandidate = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;  // midWord is too small, look right
            }
        }

        if (firstCandidate == -1) return false;  // every word is smaller than the prefix
        // The only word that could start with the prefix is the first one >= it.
        return wordList.get(firstCandidate).startsWith(p);
    }

    // Scores a word: its length in points, or 0 if it was already played.
    public static int calculateScore(String word, ArrayList<String> wordHistory) {
        if (word == null) return 0;
        String w = word.toUpperCase();
        if (contains(wordHistory, w)) {
            return 0;
        }
        return w.length();
    }

    // Simple helper: returns true if the list already contains the given value.
    public static boolean contains(ArrayList<String> list, String value) {
        if (list == null || value == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (value.equals(list.get(i))) return true;
        }
        return false;
    }

    // Reads a saved game back from a file (the reverse of saveGame). It looks at
    // the label at the start of each line to know what that line contains, then
    // rebuilds the round number, board, used words, and players.
    public static GameSession loadGame(
            File file,
            File dictionaryFile,
            int minimumLength,
            int targetScore
    ) throws Exception {
        Scanner sc = new Scanner(file);
        int loadedRound = 1;
        boolean loadedShakeUp = false;
        ArrayList<String> loadedUsedWords = new ArrayList<String>();
        char[][] loadedBoard = new char[BOARD_SIZE][BOARD_SIZE];
        ArrayList<Player> loadedPlayers = new ArrayList<Player>();

        try {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                // Each branch handles one kind of labelled line from the save file.
                if (line.startsWith("CurrentRound:")) {
                    loadedRound = Integer.parseInt(line.substring("CurrentRound:".length()).trim());
                } else if (line.startsWith("ShakeUpUsed:")) {
                    String val = line.substring("ShakeUpUsed:".length()).trim();
                    loadedShakeUp = val.equalsIgnoreCase("YES");
                } else if (line.startsWith("UsedWords:")) {
                    String val = line.substring("UsedWords:".length()).trim();
                    if (!val.isEmpty()) {
                        String[] parts = val.split(",");
                        for (int i = 0; i < parts.length; i++) {
                            String part = parts[i];
                            if (!part.trim().isEmpty()) {
                                loadedUsedWords.add(part.trim().toUpperCase());
                            }
                        }
                    }
                } else if (line.startsWith("Board:")) {
                    // The next BOARD_SIZE lines each hold one row of letters.
                    for (int r = 0; r < BOARD_SIZE; r++) {
                        if (!sc.hasNextLine()) {
                            throw new Exception("Unexpected end of file while reading board.");
                        }
                        String boardLine = sc.nextLine().trim();
                        boardLine = boardLine.replace(" ", "");
                        if (boardLine.length() < BOARD_SIZE) {
                            throw new Exception("Invalid board line in save file: " + boardLine);
                        }
                        for (int c = 0; c < BOARD_SIZE; c++) {
                            loadedBoard[r][c] = Character.toUpperCase(boardLine.charAt(c));
                        }
                    }
                } else if (line.startsWith("Players:")) {
                    // Every remaining line is one player: name|score|words.
                    while (sc.hasNextLine()) {
                        String playerLine = sc.nextLine().trim();
                        if (playerLine.isEmpty()) continue;
                        // Stop at the appended game-summary block (older saves may
                        // contain one). Its lines aren't player records.
                        if (playerLine.startsWith("---")) break;
                        String[] parts = playerLine.split("\\|");
                        if (parts.length >= 2) {
                            String name = parts[0].trim();
                            int score = Integer.parseInt(parts[1].trim());
                            Player p = new Player(name);
                            p.totalScore = score;
                            // Older saves only stored name|score; newer saves append
                            // each player's found words as a comma-separated list.
                            if (parts.length >= 3) {
                                String wordsCsv = parts[2].trim();
                                if (!wordsCsv.isEmpty()) {
                                    String[] wordParts = wordsCsv.split(",");
                                    for (int w = 0; w < wordParts.length; w++) {
                                        String word = wordParts[w].trim().toUpperCase();
                                        if (!word.isEmpty()) {
                                            p.addWordFound(word);
                                        }
                                    }
                                }
                            }
                            loadedPlayers.add(p);
                        }
                    }
                }
            }
        } finally {
            sc.close();
        }

        if (loadedPlayers.isEmpty()) {
            throw new Exception("No players found in save file.");
        }

        // Build a normal session, then overwrite its fresh state with the
        // values we just read from the file (round, shake flag, words, board).
        GameSession session = new GameSession(loadedPlayers, minimumLength, targetScore, dictionaryFile);
        session.currentRound = loadedRound;
        session.shakeUpUsed = loadedShakeUp;
        session.usedWords = loadedUsedWords;
        session.board = loadedBoard;

        return session;
    }
}
