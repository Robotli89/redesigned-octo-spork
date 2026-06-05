/*
 * Author: Kevin Li and Ethan Chuang
 * Date:   June 4, 2026
 * Course: ICS4U1
 * Project: Boggle Game
 *
 * Description:
 * This class controls the Boggle AI. It searches the board for valid words
 * and selects words according to the AI player's chosen difficulty.
 *
 * Copyright (c) Kevin Li and Ethan Chuang
 */

import java.util.ArrayList;
import java.util.Random;

public class BoggleAI {
    // One shared random number generator, used when the AI picks a word.
    static final Random random = new Random();

    // Shorter version of findAllValidWords for callers that do not care about a
    // maximum word length. It just calls the full version with maxLen = 0
    // (0 means "no maximum"). This is method overloading: same name, fewer
    // parameters.
    public ArrayList<String> findAllValidWords(
            char[][] board,
            ArrayList<String> dictionaryList,
            int minimumWordLength,
            ArrayList<String> usedWords
    ) {
        return findAllValidWords(board, dictionaryList, minimumWordLength, usedWords, 0);
    }

    // Finds every valid word that can be made on the board. It does this by
    // starting a search (a depth-first search) from each square and letting
    // that search spell out words letter by letter.
    public ArrayList<String> findAllValidWords(
            char[][] board,
            ArrayList<String> dictionaryList,
            int minimumWordLength,
            ArrayList<String> usedWords,
            int maximumWordLength
    ) {
        // Words must be at least 3 letters in Boggle, so never go below 3.
        int minLen = minimumWordLength;
        if (minLen < 3) minLen = 3;
        ArrayList<String> found = new ArrayList<String>();
        int n = board.length;       // number of rows
        int m = board[0].length;    // number of columns

        // visited[r][c] remembers which squares are already part of the word we
        // are currently spelling, so the same square is not used twice.
        boolean[][] visited = new boolean[n][m];
        // sb holds the letters of the word as we build it up during the search.
        StringBuilder sb = new StringBuilder();

        // Try starting a word from every square on the board.
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < m; c++) {
                dfs(board, r, c, sb, visited, dictionaryList, minLen, usedWords, found, maximumWordLength);
            }
        }

        return found;
    }

    // ---------------------------------------------------------------------
    // HARD PART: recursive backtracking depth-first search (DFS).
    //
    // The idea: stand on square (r, c), add its letter to the word we are
    // building, then "walk" to each of the 8 neighbouring squares and repeat.
    // This explores every possible path of connected letters on the board.
    //
    // "Backtracking" means that after we finish exploring from a square we undo
    // our changes (mark it unvisited again and remove its letter) so the next
    // path starts from a clean state. Without undoing, later paths would be
    // blocked by squares left marked as visited.
    //
    // Two speed tricks keep this fast even though the dictionary is huge:
    //   1) prefixExists(): if no dictionary word even STARTS with the letters
    //      so far, there is no point continuing down this path, so we stop
    //      early (this is called "pruning").
    //   2) One shared StringBuilder is reused for the whole search. We append a
    //      letter on the way in and chop it back off (setLength) on the way
    //      out, instead of creating a brand-new string at every step.
    // ---------------------------------------------------------------------
    void dfs(char[][] board, int r,  int c, StringBuilder current, boolean[][] visited, ArrayList<String> dictionary, int minLen, ArrayList<String> usedWords, ArrayList<String> out, int maxLen) {
        // Stop if we walked off the edge of the board.
        if (r < 0 || c < 0 || r >= board.length || c >= board[0].length) return;
        // Stop if this square is already used in the current word.
        if (visited[r][c]) return;

        // Remember the word's length before adding this letter, so we can chop
        // back to exactly here when we backtrack later.
        int lenBefore = current.length();
        current.append(Character.toUpperCase(board[r][c]));
        String currentWord = current.toString();

        // If the word is now longer than allowed, abandon this path (undo first).
        if (maxLen > 0 && currentWord.length() > maxLen) {
            current.setLength(lenBefore);
            return;
        }

        // PRUNING: if no dictionary word starts with these letters, this path
        // can never become a real word, so undo and give up early.
        if (!GameSession.prefixExists(currentWord, dictionary)) {
            current.setLength(lenBefore);
            return;
        }

        // Mark this square as used while we explore from it.
        visited[r][c] = true;

        // If the word is long enough, is a real dictionary word, has not been
        // used in the game yet, and we have not already recorded it, keep it.
        if (currentWord.length() >= minLen
                && GameSession.checkDictionary(currentWord, dictionary)
                && !GameSession.contains(usedWords, currentWord)) {
            if (!GameSession.contains(out, currentWord)) {
                out.add(currentWord);
            }
        }

        // Walk to all 8 neighbours (the two loops cover -1, 0, +1 for both row
        // and column). Skip (0,0) because that is the current square itself.
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue;
                dfs(board, r + dr, c + dc, current, visited, dictionary, minLen, usedWords, out, maxLen);
            }
        }

        // BACKTRACK: undo this square so other paths can use it again, and chop
        // this letter off the word we are building.
        visited[r][c] = false;
        current.setLength(lenBefore);
    }

    // Picks which word the AI will play from the list of words it found.
    // The choice depends on difficulty: EASY plays short words, HARD plays the
    // longest (highest scoring) word, MEDIUM is somewhere in between.
    public String chooseWord(ArrayList<String> aiWordList, String difficulty) {
        if (aiWordList == null || aiWordList.isEmpty()) return null;
        // Clean up the difficulty text and default to EASY if it is missing.
        String diff = difficulty;
        if (diff == null || diff.trim().length() == 0) diff = "EASY";
        diff = diff.trim().toUpperCase();

        if (diff.equals("EASY")) {
            // EASY: only consider short 3-4 letter words, then pick one at random.
            ArrayList<String> easyWords = new ArrayList<String>();
            for (int i = 0; i < aiWordList.size(); i++) {
                String word = aiWordList.get(i);
                if (word != null && word.length() >= 3 && word.length() <= 4) {
                    easyWords.add(word);
                }
            }
            if (easyWords.isEmpty()) return null;
            return easyWords.get(random.nextInt(easyWords.size()));
        }

        // For MEDIUM and HARD, copy the list and sort it longest-word-first.
        ArrayList<String> sorted = new ArrayList<String>();
        for (int i = 0; i < aiWordList.size(); i++) sorted.add(aiWordList.get(i));
        insertionSortByLength(sorted);

        if (diff.equals("HARD")) {
            // HARD: always take the longest word (best score).
            return sorted.get(0);
        }

        // MEDIUM: pick randomly from the top (longest) half of the words.
        int top = Math.max(1, sorted.size() / 2);
        return sorted.get(random.nextInt(top));
    }

    // Sorts the list of words so the LONGEST words come first, using insertion
    // sort. Insertion sort works like sorting cards in your hand: take each word
    // and slide it left past every word that is shorter than it, until it lands
    // in the right spot.
    public static void insertionSortByLength(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String cur = words.get(i);       // the word we are placing
            int j = i - 1;
            // Slide shorter words one spot to the right to make room.
            while (j >= 0 && words.get(j).length() < cur.length()) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, cur);           // drop cur into its correct place
        }
    }

    // Builds a Player object that is controlled by the AI, with the given name
    // and difficulty. Defaults to EASY if no difficulty is given.
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
