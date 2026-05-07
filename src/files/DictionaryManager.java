package files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DictionaryManager {
    public DictionaryManager() {}

    public static List<String> loadDictionary(File wordListFile) {
        List<String> words = new ArrayList<String>();
        if (wordListFile == null || !wordListFile.exists()) return words;
        Scanner sc;
        try {
            sc = new Scanner(wordListFile);
        } catch (Exception e) {
            return words;
        }
        for (; sc.hasNextLine(); ) {
            String w = sc.nextLine();
            if (w != null) {
                w = w.trim();
                if (w.length() > 0) {
                    words.add(w.toUpperCase());
                }
            }
        }
        sc.close();

        insertionSort(words);
        return words;
    }

    public static void insertionSort(List<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String cur = words.get(i);
            int j = i - 1;
            for (; j >= 0 && words.get(j).compareTo(cur) > 0; ) {
                words.set(j + 1, words.get(j));
                j--;
            }
            words.set(j + 1, cur);
        }
    }
}
