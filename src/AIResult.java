class AIResult {
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
