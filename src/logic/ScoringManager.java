package logic;

import java.util.List;

public class ScoringManager {
    public ScoringManager() {}

    public static int calculateScore(String word, List<String> wordHistory) {
        if (word == null) return 0;
        String w = word.toUpperCase();
        if (contains(wordHistory, w)) {
            return 0;
        }
        return w.length();
    }

    public static boolean contains(List<String> list, String value) {
        if (list == null || value == null) return false;
        for (int i = 0; i < list.size(); i++) {
            if (value.equals(list.get(i))) return true;
        }
        return false;
    }
}
