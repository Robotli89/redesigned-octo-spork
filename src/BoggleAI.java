import java.util.ArrayList;
import java.util.Random;

public class BoggleAI {
    public static Random random = new Random();

    public static BoggleGame.Player makeAIPlayer(String name, String difficulty) {
        BoggleGame.Player player = new BoggleGame.Player(name);

        player.isAI = true;

        if (difficulty == null || difficulty.trim().length() == 0) {
            player.difficulty = "Easy";
        }
        else {
            player.difficulty = difficulty.trim();
        }

        return player;
    }

    public static ArrayList<String> findAllValidWords(char[][] board, ArrayList<String> dictionary, int minimumLength, ArrayList<String> usedWords) {
        ArrayList<String> answer = new ArrayList<String>();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                boolean[][] usedSquares = new boolean[5][5];

                searchFromSquare(board, dictionary, usedWords, answer, "", row, col, usedSquares, minimumLength);
            }
        }

        return answer;
    }

    public static void searchFromSquare(
            char[][] board,
            ArrayList<String> dictionary,
            ArrayList<String> usedWords,
            ArrayList<String> answer,
            String wordSoFar,
            int row,
            int col,
            boolean[][] usedSquares,
            int minimumLength
    ) {
        if (row < 0 || row >= 5 || col < 0 || col >= 5) {
            return;
        }

        if (usedSquares[row][col] == true) {
            return;
        }

        String newWord = wordSoFar + board[row][col];

        if (BoggleGame.dictionaryHasPrefix(dictionary, newWord) == false) {
            return;
        }

        usedSquares[row][col] = true;

        if (newWord.length() >= minimumLength) {
            boolean inDictionary = BoggleGame.dictionaryHasWord(dictionary, newWord);
            boolean alreadyUsed = BoggleGame.listHasWord(usedWords, newWord);
            boolean alreadyFound = BoggleGame.listHasWord(answer, newWord);

            if (inDictionary == true && alreadyUsed == false && alreadyFound == false) {
                answer.add(newWord);
            }
        }

        for (int rowChange = -1; rowChange <= 1; rowChange++) {
            for (int colChange = -1; colChange <= 1; colChange++) {
                if (rowChange != 0 || colChange != 0) {
                    searchFromSquare(
                            board,
                            dictionary,
                            usedWords,
                            answer,
                            newWord,
                            row + rowChange,
                            col + colChange,
                            usedSquares,
                            minimumLength
                    );
                }
            }
        }

        usedSquares[row][col] = false;
    }

    public static String chooseWord(ArrayList<String> words, String difficulty) {
        if (words == null || words.size() == 0) {
            return null;
        }

        if (difficulty == null) {
            difficulty = "Easy";
        }

        String newDifficulty = difficulty.trim().toUpperCase();

        if (newDifficulty.equals("EASY")) {
            int number = random.nextInt(words.size());

            return words.get(number);
        }
        else if (newDifficulty.equals("MEDIUM")) {
            insertionSortByLength(words);

            int topHalf = words.size() / 2;

            if (topHalf < 1) {
                topHalf = 1;
            }

            int number = random.nextInt(topHalf);

            return words.get(number);
        }
        else {
            insertionSortByLength(words);

            return words.get(0);
        }
    }

    public static void insertionSortByLength(ArrayList<String> words) {
        for (int i = 1; i < words.size(); i++) {
            String currentWord = words.get(i);
            int j = i - 1;

            while (j >= 0 && words.get(j).length() < currentWord.length()) {
                words.set(j + 1, words.get(j));
                j = j - 1;
            }

            words.set(j + 1, currentWord);
        }
    }
}
