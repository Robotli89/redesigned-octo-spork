/*
 * Author: Kevin Li and Ethan Chuang
 * Date:   June 4, 2026
 * Course: ICS4U1
 * Project: Boggle Game
 *
 * Description:
 * This class stores the result of an AI player's turn, including whether the
 * turn belongs to an AI, whether the AI passed, the chosen word, and the
 * points earned.
 *
 * Copyright (c) Kevin Li and Ethan Chuang
 */

// A small "data holder" class. After the game asks the AI to take its turn, it
// hands back one of these objects so the screen knows what happened.
class AIResult {
    public boolean isAiTurn;  // true if it really was an AI's turn
    public boolean passed;    // true if the AI passed (played no word)
    public String word;       // the word the AI played (null if it passed)
    public int points;        // points the AI earned this turn

    // Constructor: fill in all four fields when the object is created.
    public AIResult(boolean isAiTurn, boolean passed, String word, int points) {
        this.isAiTurn = isAiTurn;
        this.passed = passed;
        this.word = word;
        this.points = points;
    }
}
