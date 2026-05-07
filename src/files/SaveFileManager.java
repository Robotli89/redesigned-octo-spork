package files;

import model.Player;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class SaveFileManager {
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
