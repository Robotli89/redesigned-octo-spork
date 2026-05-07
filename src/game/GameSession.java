package game;

import ai.BoggleAI;
import files.DictionaryManager;
import files.GameLogManager;
import files.SaveFileManager;
import logic.ScoringManager;
import logic.TurnManager;
import logic.WordValidator;
import model.BoggleBoard;
import model.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GameSession {
    public int minimumWordLength;
    public int targetScore;

    public List<Player> players;
    public List<String> usedWords;
    public List<String> dictionary;

    public TurnManager turnManager;
    public BoggleAI boggleAI;

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
        boggleAI = new BoggleAI();
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
