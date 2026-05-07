package model;

import java.util.ArrayList;
import java.util.List;

public class Player {
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
