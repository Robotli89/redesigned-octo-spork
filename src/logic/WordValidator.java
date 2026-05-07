package logic;

import java.util.List;

public class WordValidator {
    public WordValidator() {}

    public static boolean isValidWord(
            String word,
            char[][] board,
            List<String> dictionary,
            int minWordLength,
            List<String> usedWords
    ) {
        if (word == null) return false;
        String w = word.trim().toUpperCase();
        int minLen = minWordLength;
        if (minLen < 3) minLen = 3;
        if (w.length() < minLen) return false;
        if (ScoringManager.contains(usedWords, w)) return false;
        if (!DictionarySearch.checkDictionary(w, dictionary)) return false;
        return BoardSearch.findLetter(board, w);
    }
}
