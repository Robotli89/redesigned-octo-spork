package logic;

public final class BoardSearch {
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
