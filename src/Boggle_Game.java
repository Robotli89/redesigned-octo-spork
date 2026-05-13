import javax.swing.SwingUtilities;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Boggle_Game {
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
            System.out.println("Boggle — choose interface");
            System.out.println("1) Text version");
            System.out.println("2) GUI version");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String mode = sc.nextLine();
            if (mode == null) {
                mode = "";
            }
            mode = mode.trim();

            if (mode.equals("0")) {
                break;
            }
            if (mode.equals("2")) {
                final File dictionaryForGui = dict;
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        new Boggle_GUI(dictionaryForGui);
                    }
                });
                return;
            }
            if (!mode.equals("1")) {
                System.out.println("Invalid choice.");
                continue;
            }

            runTextMode(sc, dict);
        }

        sc.close();
    }

    private static void runTextMode(Scanner sc, File dict) {
        for (;;) {
            System.out.println();
            System.out.println("Boggle Menu");
            System.out.println("1) Player vs Player");
            System.out.println("2) Player vs AI");
            System.out.println("3) Multiplayer");
            System.out.println("4) Multiplayer + AI");
            System.out.println("5) AI vs AI");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String ch = sc.nextLine();
            if (ch == null) {
                ch = "";
            }
            ch = ch.trim();

            if (ch.equals("0")) {
                break;
            }
            if (ch.equals("1")) {
                Phase1PlayerVsPlayer.run(sc, dict);
            } else if (ch.equals("2")) {
                Phase2PlayerVsAI.run(sc, dict);
            } else if (ch.equals("3")) {
                Phase3Multiplayer.run(sc, dict);
            } else if (ch.equals("4")) {
                Phase4MultiplayerAI.run(sc, dict);
            } else if (ch.equals("5")) {
                Phase5AIvsAI.run(sc, dict);
            } else {
                System.out.println("Invalid choice.");
                continue;
            }

            System.out.print("Another round? (Y/N): ");
            String again = sc.nextLine();
            if (again == null) {
                again = "";
            }
            again = again.trim();
            if (!again.equalsIgnoreCase("Y")) {
                break;
            }
        }
    }

    public static File findDictionaryFile() {
        String[] candidates = new String[] {
                "src/wordlist.txt",
                "wordlist.txt",
                "BoggleAssignment/redesigned-octo-spork/src/wordlist.txt",
                "BoggleAssignment/wordlist.txt",
                "redesigned-octo-spork/src/wordlist.txt"
        };
        for (int i = 0; i < candidates.length; i++) {
            File f = new File(candidates[i]);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }
}

class DictionaryManager {
    public DictionaryManager() {}

    public static List<String> loadDictionary(File wordListFile) {
        List<String> words = new ArrayList<String>();
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

    public static void insertionSort(List<String> words) {
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
}

class GameLogManager {
    public GameLogManager() {}

    public static void writeGameLog(
            File file,
            String winnerName,
            String endReason,
            int roundCount,
            List<Player> players,
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
}

class SaveFileManager {
    public SaveFileManager() {}

    public static void saveGame(
            File file,
            int currentRound,
            char[][] currentBoard,
            List<Player> players,
            List<String> usedWords,
            boolean shakeUpUsed
    ) {
        if (file == null) return;
        FileWriter fw;
        try {
            fw = new FileWriter(file, false);
        } catch (Exception e) {
            return;
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
            // ignore
        }
        try { fw.close(); } catch (Exception e) {}
    }

    public static LoadedGame loadGame(File file) {
        if (file == null || !file.exists()) return null;
        int round = 1;
        boolean shake = false;
        List<String> used = new ArrayList<String>();
        List<PlayerScore> playerScores = new ArrayList<PlayerScore>();
        List<String> boardLines = new ArrayList<String>();

        boolean inBoard = false;
        boolean inPlayers = false;

        Scanner sc;
        try {
            sc = new Scanner(file);
        } catch (Exception e) {
            return null;
        }
        for (; sc.hasNextLine(); ) {
            String line = sc.nextLine();
            if (line == null) line = "";
            line = line.trim();

            if (line.startsWith("CurrentRound:")) {
                round = Integer.parseInt(line.substring("CurrentRound:".length()).trim());
            } else if (line.startsWith("ShakeUpUsed:")) {
                shake = line.substring("ShakeUpUsed:".length()).trim().equalsIgnoreCase("YES");
            } else if (line.startsWith("UsedWords:")) {
                String rest = line.substring("UsedWords:".length()).trim();
                if (rest.length() > 0) {
                    String[] parts = rest.split(",");
                    for (int i = 0; i < parts.length; i++) {
                        String w = parts[i].trim();
                        if (w.length() > 0) used.add(w.toUpperCase());
                    }
                }
            } else if (line.equals("Board:")) {
                inBoard = true;
                inPlayers = false;
            } else if (line.equals("Players:")) {
                inPlayers = true;
                inBoard = false;
            } else if (inBoard && line.length() > 0) {
                boardLines.add(line);
            } else if (inPlayers && line.length() > 0 && line.indexOf("|") >= 0) {
                String[] parts = line.split("\\|", 2);
                playerScores.add(new PlayerScore(parts[0], Integer.parseInt(parts[1])));
            }
        }
        sc.close();

        char[][] board = null;
        if (boardLines.size() > 0) {
            int n = boardLines.size();
            int m = boardLines.get(0).length();
            board = new char[n][m];
            for (int r = 0; r < n; r++) {
                String row = boardLines.get(r);
                for (int c = 0; c < m; c++) {
                    board[r][c] = row.charAt(c);
                }
            }
        }

        LoadedGame g = new LoadedGame();
        g.currentRound = round;
        g.board = board;
        g.usedWords = used;
        g.playerScores = playerScores;
        g.shakeUpUsed = shake;
        return g;
    }

    public static class PlayerScore {
        public String name;
        public int score;

        public PlayerScore(String n, int s) {
            name = n;
            score = s;
        }
    }

    public static class LoadedGame {
        public int currentRound;
        public char[][] board;
        public List<String> usedWords;
        public List<PlayerScore> playerScores;
        public boolean shakeUpUsed;
    }
}

class GameSession {
    public int minimumWordLength;
    public int targetScore;

    public List<Player> players;
    public List<String> usedWords;
    public List<String> dictionary;

    public TurnManager turnManager;
    public Boggle_AI boggleAI;

    public int currentTurnIndex;
    public int currentRound;
    public char[][] board;

    public GameSession(
            List<Player> playerList,
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
        dictionary = DictionaryManager.loadDictionary(dictionaryFile);
        turnManager = new TurnManager(targetScore);
        boggleAI = new Boggle_AI();
        currentTurnIndex = 0;
        currentRound = 1;
        generateNewBoard();
    }

    public char[][] getBoard() {
        return board;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCurrentPlayer() {
        return players.get(currentTurnIndex);
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public boolean isShakeUpUsed() {
        return turnManager.isShakeUpUsed();
    }

    public boolean isHintUsed() {
        return turnManager.isHintUsed();
    }

    public void markHintUsed() {
        turnManager.hintUsed = true;
    }

    public void generateNewBoard() {
        board = BoggleBoard.generateBoard();
    }

    // return: 1=valid, 2=already used, 0=invalid, -1=not active
    public int submitWord(String word) {
        Player p = getCurrentPlayer();
        if (p.quit || p.passed) return -1;

        String w = word == null ? "" : word.trim().toUpperCase();
        boolean ok = WordValidator.isValidWord(w, board, dictionary, minimumWordLength, usedWords);
        if (!ok) {
            turnManager.processWrongGuess(p);
            return 0;
        }

        int points = ScoringManager.calculateScore(w, usedWords);
        if (points <= 0) return 2;

        usedWords.add(w);
        p.addScore(points);
        p.addWordFound(w);
        p.resetWrongGuessCount();
        p.resetTimeoutCount();

        return 1;
    }

    public void pass() {
        turnManager.processPass(getCurrentPlayer());
    }

    public void timeout() {
        turnManager.processTimeout(getCurrentPlayer());
    }

    public void quit() {
        turnManager.processQuit(getCurrentPlayer());
    }

    // return: 0=continue, 1=offer shake, 2=ended
    public int nextTurn() {
        int vr = turnManager.verifyGameState(players);
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
        turnManager.performShake(players);
    }

    public AIResult runAITurnIfNeeded() {
        Player p = getCurrentPlayer();
        if (p.isAI == false) return AIResult.notAiTurn();

        List<String> found = boggleAI.findAllValidWords(board, dictionary, minimumWordLength, usedWords);
        String choice = boggleAI.chooseWord(found, p.difficulty);
        if (choice == null) {
            pass();
            return AIResult.aiPassed();
        }

        int points = ScoringManager.calculateScore(choice, usedWords);
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
        Player best = null;
        for (Player p : players) {
            if (p.quit) continue;
            if (best == null || p.totalScore > best.totalScore) best = p;
        }
        return best;
    }

    public String getEndReason() {
        return turnManager.getEndReason();
    }

    public void writeLog(File logFile) {
        Player winner = determineWinner();
        try {
            GameLogManager.writeGameLog(
                    logFile,
                    winner == null ? "NONE" : winner.name,
                    getEndReason(),
                    currentRound,
                    players,
                    turnManager.isShakeUpUsed()
            );
        } catch (Exception e) {
            // ignore
        }
    }

    public void saveIfPvAIQuit(File saveFile) {
        try {
            SaveFileManager.saveGame(saveFile, currentRound, board, players, usedWords, turnManager.isShakeUpUsed());
        } catch (Exception e) {
            // ignore
        }
    }

    public static class AIResult {
        public boolean isAiTurn;
        public boolean passed;
        public String word;
        public int points;

        public static AIResult notAiTurn() {
            AIResult r = new AIResult();
            r.isAiTurn = false;
            r.passed = false;
            r.word = null;
            r.points = 0;
            return r;
        }

        public static AIResult aiPassed() {
            AIResult r = new AIResult();
            r.isAiTurn = true;
            r.passed = true;
            r.word = null;
            r.points = 0;
            return r;
        }

        public static AIResult aiPlayed(String word, int points) {
            AIResult r = new AIResult();
            r.isAiTurn = true;
            r.passed = false;
            r.word = word;
            r.points = points;
            return r;
        }
    }
}

class Phase1PlayerVsPlayer {
    public static void run(Scanner sc, File dictionaryFile) {
        showRules();

        System.out.print("Player 1 name: ");
        String p1 = sc.nextLine();
        System.out.print("Player 2 name: ");
        String p2 = sc.nextLine();

        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = readTimerChoice(sc);

        List<Player> players = new ArrayList<Player>();
        players.add(new Player(p1));
        players.add(new Player(p2));

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);
        gameLoop(sc, session, timerSeconds);
    }

    public static int readTimerChoice(Scanner sc) {
        for (;;) {
            System.out.println();
            System.out.println("Timer choices:");
            System.out.println("1) None");
            System.out.println("2) 15 seconds");
            System.out.println("3) 30 seconds");
            System.out.println("4) 60 seconds");
            System.out.println("5) Custom");
            System.out.print("Choose timer (1-5): ");
            String ch = sc.nextLine();
            
            if (ch == null) ch = "";
            ch = ch.trim();
            
            if (ch.equals("1")) return 0;
            if (ch.equals("2")) return 15;
            if (ch.equals("3")) return 30;
            if (ch.equals("4")) return 60;
            if (ch.equals("5")) {
                return readInt(sc, "Enter custom timer in seconds: ", 1);
            }
            System.out.println("Invalid choice.");
        }
    }

    /** Same text as printed by {@link #showRules()}, for GUI dialogs. */
    public static String getRulesText() {
        return "Rules:\n"
                + "1. Connect adjacent letters (horizontal, vertical, diagonal).\n"
                + "2. A cube can only be used once per word.\n"
                + "3. Wrong word gives no points (2 wrong guesses = auto pass).\n"
                + "4. If all players pass, you may Shake the Board once.\n";
    }

    public static void showRules() {
        System.out.print(getRulesText());
        System.out.println();
    }

    public static void gameLoop(Scanner sc, GameSession session, int timerSeconds) {
        for (;;) {
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);
            System.out.println("Shake Up: " + (session.isShakeUpUsed() ? "USED" : "available"));
            System.out.println("Hint: " + (session.isHintUsed() ? "USED" : "available"));
            System.out.print("Enter word (or PASS / QUIT / HINT): ");
            String input = sc.nextLine();
            if (input == null) input = "";
            input = input.trim();

            if (input.equalsIgnoreCase("QUIT")) {
                session.quit();
            } else if (input.equalsIgnoreCase("PASS")) {
                session.pass();
            } else if (input.equalsIgnoreCase("HINT")) {
                if (session.isHintUsed()) {
                    System.out.println("Hint already used.");
                } else {
                    List<String> words = session.boggleAI.findAllValidWords(
                            session.getBoard(), session.dictionary, session.minimumWordLength, session.usedWords);
                    if (words.isEmpty()) {
                        System.out.println("No hint available.");
                    } else {
                        String hint = session.boggleAI.chooseWord(words, "HARD");
                        System.out.println("Hint: " + hint);
                        session.markHintUsed();
                    }
                }
                continue;
            } else {
                int r = session.submitWord(input);
                if (r == 1) {
                    System.out.println("Valid! +" + input.trim().length() + " points");
                } else if (r == 2) {
                    System.out.println("Already used. 0 points.");
                } else {
                    System.out.println("Invalid. 0 points.");
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    announceWinner(session);
                    break;
                }
                System.out.print("All passed. Shake Up? (Y/N): ");
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    announceWinner(session);
                    break;
                }
            }
        }
    }

    public static void announceWinner(GameSession session) {
        Player w = null;
        int bestScore = -1;
        int bestCount = 0;
        for (int i = 0; i < session.players.size(); i++) {
            Player p = session.players.get(i);
            if (p.quit) continue;
            if (p.totalScore > bestScore) {
                bestScore = p.totalScore;
                w = p;
                bestCount = 1;
            } else if (p.totalScore == bestScore) {
                bestCount++;
            }
        }
        System.out.println();
        if (bestCount > 1) {
            System.out.println("Game ended. Result: TIED");
        } else {
            System.out.println("Game ended. Winner: " + (w == null ? "NONE" : w.name));
        }
        for (int i = 0; i < session.players.size(); i++) {
            Player p = session.players.get(i);
            System.out.println(p.name + " score=" + p.totalScore);
        }
    }

    public static int readInt(Scanner sc, String prompt, int minValue) {
        for (;;) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < minValue) {
                    System.out.println("Must be >= " + minValue);
                } else {
                    return v;
                }
            } catch (Exception e) {
                System.out.println("Enter a number.");
            }
        }
    }

    public static int readIntAllowZero(Scanner sc, String prompt) {
        for (;;) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < 0) {
                    System.out.println("Must be >= 0");
                } else {
                    return v;
                }
            } catch (Exception e) {
                System.out.println("Enter a number.");
            }
        }
    }
}

class Phase2PlayerVsAI {
    // Called when human becomes "passed" (manual PASS or auto-pass after 2 wrong).
    // return true if game should end immediately.
    public static boolean handleHumanPassed(Scanner sc, GameSession session) {
        // Offer shake right away (only if shake not used yet)
        if (!session.isShakeUpUsed()) {
            System.out.print("Shake Up? (Y/N): ");
            String ch = sc.nextLine();
            if (ch == null) ch = "";
            ch = ch.trim();
            if (ch.equalsIgnoreCase("Y")) {
                session.performShake();
                return false;
            }
        }

        // target score = 0: if someone passes and shake already used (or user said no shake),
        // the remaining player continues until score > passer. If no words and same score => tie.
        if (session.targetScore == 0) {
            Player passer = session.players.get(0); // human is always index 0 here
            Player other = session.players.get(1);  // AI is always index 1 here
            int passScore = passer.totalScore;

            int toOther = session.nextTurn();
            if (toOther == 2) return true;

            for (;;) {
                if (other.totalScore > passScore) break;

                if (session.getCurrentPlayer().isAI) {
                    GameSession.AIResult rr = session.runAITurnIfNeeded();
                    if (rr.passed) break;
                } else {
                    break;
                }

                int adv = session.nextTurn();
                if (adv == 2) break;

                // skip the passer (already passed) so other can keep trying
                if (!session.getCurrentPlayer().isAI) {
                    int skip = session.nextTurn();
                    if (skip == 2) break;
                }
            }

            return true;
        }

        return true;
    }

    public static void run(Scanner sc, File dictionaryFile) {
        Phase1PlayerVsPlayer.showRules();

        System.out.print("Your name: ");
        String human = sc.nextLine();

        System.out.print("AI difficulty (Easy/Medium/Hard): ");
        String diff = sc.nextLine();

        int minLen = Phase1PlayerVsPlayer.readInt(sc, "Minimum word length (>=3): ", 3);
        int target = Phase1PlayerVsPlayer.readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = Phase1PlayerVsPlayer.readTimerChoice(sc);

        List<Player> players = new ArrayList<Player>();
        players.add(new Player(human));
        players.add(AI.createAIPlayer("AI", diff));

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);

        for (;;) {
            Player cur = session.getCurrentPlayer();
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);
            System.out.println("Shake Up: " + (session.isShakeUpUsed() ? "USED" : "available"));
            System.out.println("Hint: " + (session.isHintUsed() ? "USED" : "available"));

            if (cur.isAI) {
                GameSession.AIResult r = session.runAITurnIfNeeded();
                if (r.passed) {
                    System.out.println("AI PASSED");
                } else {
                    System.out.println("AI played: " + r.word + " (+" + r.points + ")");
                }
            } else {
                System.out.print("Enter word (or PASS / QUIT / HINT): ");
                String input = sc.nextLine();
                if (input == null) input = "";
                input = input.trim();
                if (input.equalsIgnoreCase("QUIT")) {
                    session.quit();
                    session.saveIfPvAIQuit(new File(human + "_save.txt"));
                } else if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                    boolean endNow = handleHumanPassed(sc, session);
                    if (endNow) {
                        Phase1PlayerVsPlayer.announceWinner(session);
                        break;
                    }
                    continue;
                } else if (input.equalsIgnoreCase("HINT")) {
                    if (session.isHintUsed()) {
                        System.out.println("Hint already used.");
                    } else {
                        List<String> words = session.boggleAI.findAllValidWords(
                                session.getBoard(), session.dictionary, session.minimumWordLength, session.usedWords);
                        if (words.isEmpty()) {
                            System.out.println("No hint available.");
                        } else {
                            String hint = session.boggleAI.chooseWord(words, "HARD");
                            System.out.println("Hint: " + hint);
                            session.markHintUsed();
                        }
                    }
                    continue;
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println("Valid! +" + input.trim().length() + " points");
                    } else if (sr == 2) {
                        System.out.println("Already used. 0 points.");
                    } else {
                        System.out.println("Invalid. 0 points.");
                    }

                    // If this invalid guess triggered auto-pass (2 wrong), offer shake right away.
                    if (session.players.get(0).passed) {
                        boolean endNow = handleHumanPassed(sc, session);
                        if (endNow) {
                            Phase1PlayerVsPlayer.announceWinner(session);
                            break;
                        }
                        continue;
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
                System.out.print("All passed. Shake Up? (Y/N): ");
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
            }
        }
    }
}

class Phase3Multiplayer {
    public static void run(Scanner sc, File dictionaryFile) {
        Phase1PlayerVsPlayer.showRules();

        int count = Phase1PlayerVsPlayer.readInt(sc, "How many players (>=3): ", 3);
        int minLen = Phase1PlayerVsPlayer.readInt(sc, "Minimum word length (>=3): ", 3);
        int target = Phase1PlayerVsPlayer.readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = Phase1PlayerVsPlayer.readTimerChoice(sc);

        List<Player> players = new ArrayList<Player>();
        for (int i = 0; i < count; i++) {
            System.out.print("Player " + (i + 1) + " name: ");
            String name = sc.nextLine();
            players.add(new Player(name));
        }

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);

        for (;;) {
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);
            System.out.println("Shake Up: " + (session.isShakeUpUsed() ? "USED" : "available"));
            System.out.println("Hint: " + (session.isHintUsed() ? "USED" : "available"));
            System.out.print("Enter word (or PASS / QUIT / HINT): ");
            String input = sc.nextLine();
            if (input == null) input = "";
            input = input.trim();

            if (input.equalsIgnoreCase("QUIT")) {
                session.quit();
            } else if (input.equalsIgnoreCase("PASS")) {
                session.pass();
            } else if (input.equalsIgnoreCase("HINT")) {
                if (session.isHintUsed()) {
                    System.out.println("Hint already used.");
                } else {
                    List<String> words = session.boggleAI.findAllValidWords(
                            session.getBoard(), session.dictionary, session.minimumWordLength, session.usedWords);
                    if (words.isEmpty()) {
                        System.out.println("No hint available.");
                    } else {
                        String hint = session.boggleAI.chooseWord(words, "HARD");
                        System.out.println("Hint: " + hint);
                        session.markHintUsed();
                    }
                }
                continue;
            } else {
                int sr = session.submitWord(input);
                if (sr == 1) {
                    System.out.println("Valid! +" + input.trim().length() + " points");
                } else if (sr == 2) {
                    System.out.println("Already used. 0 points.");
                } else {
                    System.out.println("Invalid. 0 points.");
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
                System.out.print("All active players passed. Shake Up? (Y/N): ");
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
            }
        }
    }
}

class Phase4MultiplayerAI {
    public static void run(Scanner sc, File dictionaryFile) {
        Phase1PlayerVsPlayer.showRules();

        int humanCount = Phase1PlayerVsPlayer.readInt(sc, "How many human players (>=2): ", 2);
        int aiCount = Phase1PlayerVsPlayer.readInt(sc, "How many AI players (>=1): ", 1);
        int minLen = Phase1PlayerVsPlayer.readInt(sc, "Minimum word length (>=3): ", 3);
        int target = Phase1PlayerVsPlayer.readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = Phase1PlayerVsPlayer.readTimerChoice(sc);

        List<Player> players = new ArrayList<Player>();
        for (int i = 0; i < humanCount; i++) {
            System.out.print("Human Player " + (i + 1) + " name: ");
            String name = sc.nextLine();
            players.add(new Player(name));
        }
        for (int i = 0; i < aiCount; i++) {
            System.out.print("AI #" + (i + 1) + " difficulty (Easy/Medium/Hard): ");
            String diff = sc.nextLine();
            players.add(AI.createAIPlayer("AI" + (i + 1), diff));
        }

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);

        for (;;) {
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);
            System.out.println("Shake Up: " + (session.isShakeUpUsed() ? "USED" : "available"));
            System.out.println("Hint: " + (session.isHintUsed() ? "USED" : "available"));

            if (cur.isAI) {
                GameSession.AIResult r = session.runAITurnIfNeeded();
                if (r.passed) System.out.println(cur.name + " PASSED");
                else System.out.println(cur.name + " played: " + r.word + " (+" + r.points + ")");
            } else {
                System.out.print("Enter word (or PASS / QUIT / HINT): ");
                String input = sc.nextLine();
                if (input == null) input = "";
                input = input.trim();

                if (input.equalsIgnoreCase("QUIT")) {
                    session.quit();
                } else if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                } else if (input.equalsIgnoreCase("HINT")) {
                    if (session.isHintUsed()) {
                        System.out.println("Hint already used.");
                    } else {
                        List<String> words = session.boggleAI.findAllValidWords(
                                session.getBoard(), session.dictionary, session.minimumWordLength, session.usedWords);
                        if (words.isEmpty()) {
                            System.out.println("No hint available.");
                        } else {
                            String hint = session.boggleAI.chooseWord(words, "HARD");
                            System.out.println("Hint: " + hint);
                            session.markHintUsed();
                        }
                    }
                    continue;
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println("Valid! +" + input.trim().length() + " points");
                    } else if (sr == 2) {
                        System.out.println("Already used. 0 points.");
                    } else {
                        System.out.println("Invalid. 0 points.");
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
                System.out.print("All active players passed. Shake Up? (Y/N): ");
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
            }
        }
    }
}

class Phase5AIvsAI {
    public static void run(Scanner sc, File dictionaryFile) {
        System.out.println("AI vs AI Rules:");
        System.out.println("- Both AIs use the same dictionary and same board.");
        System.out.println("- Your AI is automatic. Opponent AI move is typed manually.");
        System.out.println();

        System.out.print("Your AI difficulty (Easy/Medium/Hard): ");
        String yourDiff = sc.nextLine();
        System.out.print("Opponent AI name: ");
        String oppName = sc.nextLine();

        int minLen = Phase1PlayerVsPlayer.readInt(sc, "Minimum word length (>=3): ", 3);
        int target = Phase1PlayerVsPlayer.readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = Phase1PlayerVsPlayer.readTimerChoice(sc);

        List<Player> players = new ArrayList<Player>();
        players.add(AI.createAIPlayer("YourAI", yourDiff));
        players.add(new Player(oppName));

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);

        for (;;) {
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);
            System.out.println("Shake Up: " + (session.isShakeUpUsed() ? "USED" : "available"));
            System.out.println("Hint: " + (session.isHintUsed() ? "USED" : "available"));

            if (cur.isAI) {
                GameSession.AIResult r = session.runAITurnIfNeeded();
                if (r.passed) System.out.println("YourAI PASSED");
                else System.out.println("YourAI played: " + r.word + " (+" + r.points + ")");
            } else {
                System.out.print("Opponent move (word or PASS): ");
                String input = sc.nextLine();
                if (input == null) input = "";
                input = input.trim();
                if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println("Accepted. +" + input.trim().length());
                    } else if (sr == 2) {
                        System.out.println("Rejected: already used.");
                    } else {
                        System.out.println("Rejected: invalid.");
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
            if (ar == 1) {
                // AI vs AI: end on first all-pass
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
        }
    }
}

class BoardSearch {
    public BoardSearch() {}

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
}

class DictionarySearch {
    public DictionarySearch() {}

    public static boolean checkDictionary(String word, List<String> wordList) {
        if (word == null || wordList == null || wordList.isEmpty()) return false;
        String w = word.toUpperCase();
        return checkDictionary(w, wordList, 0, wordList.size() - 1);
    }

    public static boolean checkDictionary(String word, List<String> wordList, int low, int high) {
        if (word == null || wordList == null) return false;
        if (low > high) return false;
        int mid = low + (high - low) / 2;
        int cmp = word.compareTo(wordList.get(mid));
        if (cmp == 0) return true;
        if (cmp < 0) return checkDictionary(word, wordList, low, mid - 1);
        return checkDictionary(word, wordList, mid + 1, high);
    }

    /**
     * Prefix check used for AI DFS pruning.
     * Returns true if there exists any dictionary word that starts with prefix.
     */
    public static boolean prefixExists(String prefix, List<String> wordList) {
        if (prefix == null || prefix.isEmpty() || wordList == null || wordList.isEmpty()) return false;
        String p = prefix.toUpperCase();

        int lo = 0;
        int hi = wordList.size() - 1;
        int firstCandidate = -1;

        // lower bound: first index with word >= prefix
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
}

class ScoringManager {
    public ScoringManager() {}

    public static int calculateScore(String word, List<String> wordHistory) {
        if (word == null) return 0;
        String w = word.toUpperCase();
        if (contains(wordHistory, w)) {
            return 0;
        }
        return w.length();
    }

    public static boolean contains(List<String> list, String value) {
        if (list == null || value == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (value.equals(list.get(i))) return true;
        }
        return false;
    }
}

class TurnManager {
    public int targetScore;

    public boolean shakeUpUsed;
    public boolean hintUsed;
    public String endReason;

    public TurnManager(int ts) {
        targetScore = ts;
        shakeUpUsed = false;
        hintUsed = false;
    }

    public boolean isShakeUpUsed() {
        return shakeUpUsed;
    }

    public boolean isHintUsed() {
        return hintUsed;
    }

    public String getEndReason() {
        return endReason;
    }

    public void processWrongGuess(Player currentPlayer) {
        currentPlayer.incrementWrongGuessCount();
        if (currentPlayer.wrongGuessCount >= 2) {
            currentPlayer.resetWrongGuessCount();
            currentPlayer.incrementAutoPassWrongCount();
            processPass(currentPlayer);
        }
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
    public int verifyGameState(List<Player> players) {
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

    public void performShake(List<Player> players) {
        shakeUpUsed = true;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.resetRoundState();
        }
    }
}

class WordValidator {
    public WordValidator() {}

    public static boolean isValidWord(
            String word,
            char[][] board,
            List<String> dictionary,
            int minWordLength,
            List<String> usedWords
    ) {
        if (word == null) return false;
        String w = word.trim().toUpperCase();
        int minLen = minWordLength;
        if (minLen < 3) minLen = 3;
        if (w.length() < minLen) return false;
        if (ScoringManager.contains(usedWords, w)) return false;
        if (!DictionarySearch.checkDictionary(w, dictionary)) return false;
        return BoardSearch.findLetter(board, w);
    }
}

class BoggleBoard {
  public static final Random random = new Random();
  
  public static final String[] dice = {
    "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY",
    "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW",
    "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY",
    "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW",
    "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
  };
  public static final int size = 5;

  public static void main(String[] args) {
    char[][] board = generateBoard();
    printBoard(board);
  }

  public static char[][] generateBoard() {
    char[] letters = new char[dice.length];
    for (int i = 0;i < dice.length;i++) {
      int face = random.nextInt(dice[i].length());
      letters[i] = dice[i].charAt(face);
    }
    shuffleCharArray(letters);
    char[][] board = new char[size][size];
    int k = 0;
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
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
}

class Player {
    public String name;
    public int totalScore;
    public boolean passed;
    public boolean quit;

    public int wrongGuessCount;
    public int timeoutCount;

    public int autoPassWrongCount;
    public int autoPassTimerCount;

    public List<String> wordsFound;

    public boolean isAI;
    public String difficulty;

    public Player(String n) {
        if (n == null || n.trim().isEmpty()) n = "Player";
        passed = false;
        quit = false;
        wordsFound = new ArrayList<String>();
        difficulty = "EASY";
        isAI = false;
        name = n.trim();
    }

    public void addScore(int points) {
        if (points > 0) {
            totalScore += points;
        }
    }

    public void incrementWrongGuessCount() {
        wrongGuessCount++;
    }

    public void resetWrongGuessCount() {
        wrongGuessCount = 0;
    }

    public int getTimeoutCount() {
        return timeoutCount;
    }

    public void incrementTimeoutCount() {
        timeoutCount++;
    }

    public void resetTimeoutCount() {
        timeoutCount = 0;
    }

    public void incrementAutoPassWrongCount() {
        autoPassWrongCount++;
    }

    public void incrementAutoPassTimerCount() {
        autoPassTimerCount++;
    }

    public void addWordFound(String word) {
        if (word != null && word.trim().length() > 0) {
            wordsFound.add(word);
        }
    }

    public void resetRoundState() {
        passed = false;
        wrongGuessCount = 0;
        timeoutCount = 0;
    }
}