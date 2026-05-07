package logic;

import java.util.List;

public final class DictionarySearch {
    public DictionarySearch() {}

    public static boolean checkDictionary(String word, List<String> wordList) {
        if (word == null || wordList == null || wordList.isEmpty()) return false;
        String w = word.toUpperCase();
        return checkDictionary(w, wordList, 0, wordList.size() - 1);
    }

    public static boolean checkDictionary(String word, List<String> wordList, int low, int high) {
        if (word == null || wordList == null) return false;
        if (low > high) return false;
        int mid = low + (high - low) / 2;
        int cmp = word.compareTo(wordList.get(mid));
        if (cmp == 0) return true;
        if (cmp < 0) return checkDictionary(word, wordList, low, mid - 1);
        return checkDictionary(word, wordList, mid + 1, high);
    }

    /**
     * Prefix check used for AI DFS pruning.
     * Returns true if there exists any dictionary word that starts with prefix.
     */
    public static boolean prefixExists(String prefix, List<String> wordList) {
        if (prefix == null || prefix.isEmpty() || wordList == null || wordList.isEmpty()) return false;
        String p = prefix.toUpperCase();

        int lo = 0;
        int hi = wordList.size() - 1;
        int firstCandidate = -1;

        // lower bound: first index with word >= prefix
        while (lo <= hi) {
            int mid = lo + (hi - lo) / 2;
            String midWord = wordList.get(mid);
            if (midWord.compareTo(p) >= 0) {
                firstCandidate = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }

        if (firstCandidate == -1) return false;
        return wordList.get(firstCandidate).startsWith(p);
    }
}
