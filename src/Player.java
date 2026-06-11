/*
 * Author: Kevin Li and Ethan Chuang
 * Date:   June 4, 2026
 * Course: ICS4U1
 * Project: Boggle Game
 *
 * Description:
 * This class stores a Boggle player's name, score, found words, turn state,
 * penalties, and AI settings.
 *
 * Copyright (c) Kevin Li and Ethan Chuang
 */

import java.util.ArrayList;

class Player {
    public String name;            // the player's display name
    public int totalScore;         // points collected so far
    public boolean passed;         // true once this player has passed
    public boolean quit;           // true once this player has quit

    public int wrongGuessCount;    // wrong guesses in a row (resets to 0)
    public int timeoutCount;       // timeouts in a row (resets to 0)

    public int autoPassWrongCount; // how many times forced to pass for wrong guesses
    public int autoPassTimerCount; // how many times forced to pass for running out of time

    public ArrayList<String> wordsFound; // every word this player has played

    public boolean isAI;           // true if the computer controls this player
    public String difficulty;      // AI skill level: EASY, MEDIUM, or HARD

    // Constructor: set up a fresh player with the given name. If the name is
    // blank, use "Player" so we never have an empty name.
    public Player(String n) {
        if (n == null || n.trim().isEmpty()) n = "Player";
        passed = false;
        quit = false;
        wordsFound = new ArrayList<String>();
        difficulty = "EASY";
        isAI = false;
        name = n.trim();
    }

    // Add points to the score (ignores zero or negative amounts).
    public void addScore(int points) {
        if (points > 0) {
            totalScore += points;
        }
    }

    // Count one more wrong guess.
    public void incrementWrongGuessCount() {
        wrongGuessCount++;
    }

    // Clear the wrong-guess streak (used after a correct word).
    public void resetWrongGuessCount() {
        wrongGuessCount = 0;
    }

    // Return how many times in a row this player has timed out.
    public int getTimeoutCount() {
        return timeoutCount;
    }

    // Count one more timeout.
    public void incrementTimeoutCount() {
        timeoutCount++;
    }

    // Clear the timeout streak.
    public void resetTimeoutCount() {
        timeoutCount = 0;
    }

    // Record that the player was auto-passed for too many wrong guesses.
    public void incrementAutoPassWrongCount() {
        autoPassWrongCount++;
    }

    // Record that the player was auto-passed for running out of time.
    public void incrementAutoPassTimerCount() {
        autoPassTimerCount++;
    }

    // Save a word the player successfully played (ignores blank words).
    public void addWordFound(String word) {
        if (word != null && word.trim().length() > 0) {
            wordsFound.add(word);
        }
    }

    // Reset the per-round flags, e.g. after the board is shaken up.
    public void resetRoundState() {
        passed = false;
        wrongGuessCount = 0;
        timeoutCount = 0;
    }
}
