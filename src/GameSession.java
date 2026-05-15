import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

class GameSession {
    public static final Random random = new Random();
    public static final int BOARD_SIZE = 5;

    public static final String[] dice = {
            "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY",
            "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW",
            "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY",
            "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW",
            "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
    };

    public int minimumWordLength;
    public int targetScore;

    public ArrayList<Player> players;
    public ArrayList<String> usedWords;
    public ArrayList<String> dictionary;

    public BoggleAI boggleAI;
    public boolean shakeUpUsed;
    public boolean hintUsed;
    public String endReason;
    public Player forcedWinner;

    public int currentTurnIndex;
    public int currentRound;
    public char[][] board;

    public GameSession(
            ArrayList<Player> playerList,
            int minWordLen,
            int target,
            File dictionaryFile
    ) {
        minimumWordLength = minWordLen;
        if (minWordLen < 3) minimumWordLength = 3;
        targetScore = target;
        if (target < 0) targetScore = 0;
        if (playerList == null || playerList.isEmpty()) playerList = new ArrayList<Player>();
        players = new ArrayList<Player>();
        for (int i = 0; i < playerList.size(); i++) players.add(playerList.get(i));
        usedWords = new ArrayList<String>();
        dictionary = loadDictionary(dictionaryFile);
        boggleAI = new BoggleAI();
        shakeUpUsed = false;
        hintUsed = false;
        currentTurnIndex = 0;
        currentRound = 1;
        generateNewBoard();
    }

    public char[][] getBoard() {
        return board;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

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

    public void markHintUsed() {
        hintUsed = true;
    }

    public void generateNewBoard() {
        board = generateBoard();
    }

    // return: 1=valid, 2=already used, 0=invalid, -1=not active
    public int submitWord(String word) {
        Player p = getCurrentPlayer();
        if (p.quit || p.passed) return -1;

        String w = word == null ? "" : word.trim().toUpperCase();
        boolean ok = isValidWord(w, board, dictionary, minimumWordLength, usedWords);
        if (!ok) {
            processWrongGuess(p);
            return 0;
        }

        int points = calculateScore(w, usedWords);
        if (points <= 0) return 2;

        usedWords.add(w);
        p.addScore(points);
        p.addWordFound(w);
        p.resetWrongGuessCount();
        p.resetTimeoutCount();

        return 1;
    }

    public void pass() {
        processPass(getCurrentPlayer());
    }

    public void timeout() {
        processTimeout(getCurrentPlayer());
    }

    public void quit() {
        processQuit(getCurrentPlayer());
    }

    // return: 0=continue, 1=offer shake, 2=ended
    public int nextTurn() {
        int vr = verifyGameState(players);
        if (vr == 2) return 2;
        if (vr == 1) return 1;

        int start = currentTurnIndex;
        do {
            currentTurnIndex = (currentTurnIndex + 1) % players.size();
            if (currentTurnIndex == 0) currentRound++;
        } while ((players.get(currentTurnIndex).quit || players.get(currentTurnIndex).passed) && currentTurnIndex != start);

        return 0;
    }

    public void performShake() {
        usedWords.clear();
        generateNewBoard();
        shakeUpUsed = true;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.resetRoundState();
        }
    }

    public AIResult runAITurnIfNeeded() {
        Player p = getCurrentPlayer();
        if (p.isAI == false) return AIResult.notAiTurn();

        ArrayList<String> found = boggleAI.findAllValidWords(board, dictionary, minimumWordLength, usedWords);
        String choice = boggleAI.chooseWord(found, p.difficulty);
        if (choice == null) {
            pass();
            return AIResult.aiPassed();
        }

        int points = calculateScore(choice, usedWords);
        if (points <= 0) {
            pass();
            return AIResult.aiPassed();
        }

        usedWords.add(choice);
        p.addScore(points);
        p.addWordFound(choice);
        return AIResult.aiPlayed(choice, points);
    }

    public Player determineWinner() {
        if (forcedWinner != null) return forcedWinner;

        Player best = null;
        for (Player p : players) {
            if (p.quit) continue;
            if (best == null || p.totalScore > best.totalScore) best = p;
        }
        return best;
    }

    public String getEndReason() {
        return endReason;
    }

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

    public void saveIfPvAIQuit(File saveFile) {
        try {
            saveGame(saveFile, currentRound, board, players, usedWords, shakeUpUsed);
        } catch (Exception e) {
            // ignore
        }
    }

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

    public Player findAIWinnerForWrongGuesses(Player currentPlayer) {
        if (currentPlayer == null || currentPlayer.isAI) return null;
        Player aiPlayer = null;
        int activeHumanCount = 0;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.quit) continue;
            if (p.isAI) {
                if (aiPlayer != null) return null;
                aiPlayer = p;
            } else {
                activeHumanCount++;
            }
        }

        if (activeHumanCount == 1) return aiPlayer;
        return null;
    }

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

    public void processPass(Player currentPlayer) {
        currentPlayer.passed = true;
    }

    public void processQuit(Player currentPlayer) {
        currentPlayer.quit = true;
        endReason = "OPPONENT_QUIT";
    }

    // return: 0=continue, 1=offer shake, 2=end game
    public int verifyGameState(ArrayList<Player> players) {
        if (forcedWinner != null) {
            if (endReason == null) endReason = "FORCED_WINNER";
            return 2;
        }

        if (players == null || players.isEmpty()) {
            endReason = "ALL_PLAYERS_PASSED";
            return 2;
        }

        if (targetScore > 0) {
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (p.totalScore >= targetScore) {
                    endReason = "TARGET_SCORE_REACHED";
                    return 2;
                }
            }
        }

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.quit) {
                endReason = "OPPONENT_QUIT";
                return 2;
            }
        }

        boolean anyPassed = false;
        boolean anyActive = false;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (!p.quit && !p.passed) anyActive = true;
            if (!p.quit && p.passed) anyPassed = true;
        }

        if (!anyActive && anyPassed) {
            if (!shakeUpUsed) return 1;
            endReason = "NO_MOVES_AFTER_SHAKE";
            return 2;
        }

        return 0;
    }

    public static char[][] generateBoard() {
        char[] letters = new char[dice.length];
        for (int i = 0;i < dice.length;i++) {
            int face = random.nextInt(dice[i].length());
            letters[i] = dice[i].charAt(face);
        }
        shuffleCharArray(letters);
        char[][] board = new char[BOARD_SIZE][BOARD_SIZE];
        int k = 0;
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                board[r][c] = letters[k++];
            }
        }
        return board;
    }

    public static void shuffleCharArray(char[] arr) {
        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = arr[i];
            arr[i] = arr[j];
            arr[j] = tmp;
        }
    }

    public static void printBoard(char[][] board) {
        System.out.println("\nBoggle Board:");
        for (char[] row : board) {
            for (char ch : row) {
                System.out.print(ch + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    public static ArrayList<String> loadDictionary(File wordListFile) {
        ArrayList<String> words = new ArrayList<String>();
        if (wordListFile == null || !wordListFile.exists()) return words;
        Scanner sc;
        try {
            sc = new Scanner(wordListFile);
        } catch (Exception e) {
            return words;
        }
        for (; sc.hasNextLine(); ) {
            String w = sc.nextLine();
            if (w != null) {
                w = w.trim();
                if (w.length() > 0) {
                    words.add(w.toUpperCase());
                }
            }
        }
        sc.close();

        insertionSort(words);
        return words;
    }

    public static void insertionSort(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String cur = words.get(i);
            int j = i - 1;
            for (; j >= 0 && words.get(j).compareTo(cur) > 0; ) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, cur);
        }
    }

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
            fw = new FileWriter(file, true);
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
            fw = new FileWriter(file, false);
        } catch (Exception e) {
            return false;
        }
        try {
            fw.write("Status: Paused\n");
            fw.write("CurrentRound: " + currentRound + "\n");
            fw.write("ShakeUpUsed: " + (shakeUpUsed ? "YES" : "NO") + "\n");

            String usedLine = "";
            if (usedWords != null) {
                for (int i = 0; i < usedWords.size(); i++) {
                    if (i > 0) usedLine += ",";
                    usedLine += usedWords.get(i);
                }
            }
            fw.write("UsedWords: " + usedLine + "\n");

            fw.write("Board:\n");
            for (int r = 0; r < currentBoard.length; r++) {
                for (int c = 0; c < currentBoard[0].length; c++) {
                    fw.write(String.valueOf(currentBoard[r][c]));
                }
                fw.write("\n");
            }

            fw.write("Players:\n");
            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    fw.write(p.name + "|" + p.totalScore + "\n");
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

    public static boolean isValidWord(
            String word,
            char[][] board,
            ArrayList<String> dictionary,
            int minWordLength,
            ArrayList<String> usedWords
    ) {
        if (word == null) return false;
        String w = word.trim().toUpperCase();
        int minLen = minWordLength;
        if (minLen < 3) minLen = 3;
        if (w.length() < minLen) return false;
        if (contains(usedWords, w)) return false;
        if (!checkDictionary(w, dictionary)) return false;
        return findLetter(board, w);
    }

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
                    boolean[][] visited = new boolean[n][m];
                    if (checkBoard(board, r, c, w, 0, visited)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean checkBoard(char[][] board, int r, int c, String word, int index, boolean[][] visited) {
        if (index == word.length()) return true;
        if (r < 0 || c < 0 || r >= board.length || c >= board[0].length) return false;
        if (visited[r][c]) return false;
        if (Character.toUpperCase(board[r][c]) != word.charAt(index)) return false;

        visited[r][c] = true;

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                if (checkBoard(board, r + dr, c + dc, word, index + 1, visited)) {
                    visited[r][c] = false;
                    return true;
                }
            }
        }

        visited[r][c] = false;
        return false;
    }

    public static boolean checkDictionary(String word, ArrayList<String> wordList) {
        if (word == null || wordList == null || wordList.isEmpty()) return false;
        String w = word.toUpperCase();
        return checkDictionary(w, wordList, 0, wordList.size() - 1);
    }

    public static boolean checkDictionary(String word, ArrayList<String> wordList, int low, int high) {
        if (word == null || wordList == null) return false;
        if (low > high) return false;
        int mid = low + (high - low) / 2;
        int cmp = word.compareTo(wordList.get(mid));
        if (cmp == 0) return true;
        if (cmp < 0) return checkDictionary(word, wordList, low, mid - 1);
        return checkDictionary(word, wordList, mid + 1, high);
    }

    public static boolean prefixExists(String prefix, ArrayList<String> wordList) {
        if (prefix == null || prefix.isEmpty() || wordList == null || wordList.isEmpty()) return false;
        String p = prefix.toUpperCase();

        int lo = 0;
        int hi = wordList.size() - 1;
        int firstCandidate = -1;

        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            String midWord = wordList.get(mid);
            if (midWord.compareTo(p) >= 0) {
                firstCandidate = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }

        if (firstCandidate == -1) return false;
        return wordList.get(firstCandidate).startsWith(p);
    }

    public static int calculateScore(String word, ArrayList<String> wordHistory) {
        if (word == null) return 0;
        String w = word.toUpperCase();
        if (contains(wordHistory, w)) {
            return 0;
        }
        return w.length();
    }

    public static boolean contains(ArrayList<String> list, String value) {
        if (list == null || value == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (value.equals(list.get(i))) return true;
        }
        return false;
    }
}
