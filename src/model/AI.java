package model;

public class AI {
    public static Player createAIPlayer(String name, String difficulty) {
        Player p = new Player(name);
        p.isAI = true;
        if (difficulty == null || difficulty.trim().length() == 0) {
            p.difficulty = "EASY";
        } else {
            p.difficulty = difficulty.trim().toUpperCase();
        }
        return p;
    }
}
