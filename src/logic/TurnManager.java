package logic;

import model.Player;

import java.util.List;

public class TurnManager {
    public int targetScore;

    public boolean shakeUpUsed;
    public String endReason;

    public TurnManager(int ts) {
        targetScore = ts;
    }

    public boolean isShakeUpUsed() {
        return shakeUpUsed;
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
        if (players != null) {
            for (int i = 0; i < players.size(); i++) {
                Player p = players.get(i);
                if (!p.quit) {
                    p.resetRoundState();
                }
            }
        }
    }
}
