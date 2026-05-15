import java.util.ArrayList;
import java.util.Random;

public class BoggleAI {
    static final Random random = new Random();

    public ArrayList<String> findAllValidWords(
            char[][] board,
            ArrayList<String> dictionaryList,
            int minimumWordLength,
            ArrayList<String> usedWords
    ) {
        int minLen = minimumWordLength;
        if (minLen < 3) minLen = 3;
        ArrayList<String> found = new ArrayList<String>();
        int n = board.length;
        int m = board[0].length;

        boolean[][] visited = new boolean[n][m];
        StringBuilder sb = new StringBuilder();

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                dfs(board, r, c, sb, visited, dictionaryList, minLen, usedWords, found);
            }
        }

        return found;
    }

    void dfs(char[][] board, int r,  int c, StringBuilder current, boolean[][] visited, ArrayList<String> dictionary, int minLen, ArrayList<String> usedWords, ArrayList<String> out) {
        if (r < 0 || c < 0 || r >= board.length || c >= board[0].length) return;
        if (visited[r][c]) return;

        int lenBefore = current.length();
        current.append(Character.toUpperCase(board[r][c]));
        String currentWord = current.toString();

        if (!GameSession.prefixExists(currentWord, dictionary)) {
            current.setLength(lenBefore);
            return;
        }

        visited[r][c] = true;

        if (currentWord.length() >= minLen
                && GameSession.checkDictionary(currentWord, dictionary)
                && !GameSession.contains(usedWords, currentWord)) {
            if (!GameSession.contains(out, currentWord)) {
                out.add(currentWord);
            }
        }

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                dfs(board, r + dr, c + dc, current, visited, dictionary, minLen, usedWords, out);
            }
        }

        visited[r][c] = false;
        current.setLength(lenBefore);
    }

    public String chooseWord(ArrayList<String> aiWordList, String difficulty) {
        if (aiWordList == null || aiWordList.isEmpty()) return null;
        String diff = difficulty;
        if (diff == null || diff.trim().length() == 0) diff = "EASY";
        diff = diff.trim().toUpperCase();

        if (diff.equals("EASY")) {
            return aiWordList.get(random.nextInt(aiWordList.size()));
        }

        ArrayList<String> sorted = new ArrayList<String>();
        for (int i = 0; i < aiWordList.size(); i++) sorted.add(aiWordList.get(i));
        insertionSortByLength(sorted);

        if (diff.equals("HARD")) {
            return sorted.get(0);
        }

        // MEDIUM: random from top 50%
        int top = Math.max(1, sorted.size() / 2);
        return sorted.get(random.nextInt(top));
    }

    public static void insertionSortByLength(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String cur = words.get(i);
            int j = i - 1;
            for (; j >= 0 && words.get(j).length() < cur.length(); ) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, cur);
        }
    }

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
