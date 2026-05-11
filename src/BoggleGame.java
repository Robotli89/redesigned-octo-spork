import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class BoggleGame {
    public static Random random = new Random();

    public static String[] dice = {
            "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY",
            "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW",
            "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY",
            "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW",
            "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
    };

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.println("Choose GUI mode or text mode");
        System.out.println("1) GUI mode");
        System.out.println("2) Text mode");
        System.out.print("Choose: ");

        String choice = input.nextLine();

        if (choice.equals("1")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new BoggleGUI();
                }
            });
        }
        else if (choice.equals("2")) {
            runTextMode(input);
        }
        else {
            System.out.println("Invalid choice.");
        }
    }

    public static void runTextMode(Scanner input) {
        File dictionaryFile = findDictionaryFile();

        if (dictionaryFile == null) {
            System.out.println("Dictionary file not found.");
            return;
        }

        while (true) {
            System.out.println();
            System.out.println("Boggle Menu");
            System.out.println("1) Player vs Player");
            System.out.println("2) Player vs AI");
            System.out.println("3) Multiplayer");
            System.out.println("4) Multiplayer + AI");
            System.out.println("5) AI vs AI");
            System.out.println("0) Exit");
            System.out.print("Choose: ");

            String choice = input.nextLine();

            if (choice.equals("0")) {
                break;
            }
            else if (choice.equals("1")) {
                setupTextGame(input, dictionaryFile, 1);
            }
            else if (choice.equals("2")) {
                setupTextGame(input, dictionaryFile, 2);
            }
            else if (choice.equals("3")) {
                setupTextGame(input, dictionaryFile, 3);
            }
            else if (choice.equals("4")) {
                setupTextGame(input, dictionaryFile, 4);
            }
            else if (choice.equals("5")) {
                setupTextGame(input, dictionaryFile, 5);
            }
            else {
                System.out.println("Invalid choice.");
            }
        }
    }

    public static void setupTextGame(Scanner input, File dictionaryFile, int phase) {
        ArrayList<Player> players = new ArrayList<Player>();

        System.out.print("Minimum word length: ");
        int minimumLength = readNumber(input, 3);

        System.out.print("Target score (0 = none): ");
        int targetScore = readNumber(input, 0);

        if (phase == 1) {
            System.out.print("Player 1 name: ");
            String player1 = input.nextLine();

            System.out.print("Player 2 name: ");
            String player2 = input.nextLine();

            players.add(new Player(player1));
            players.add(new Player(player2));
        }
        else if (phase == 2) {
            System.out.print("Human name: ");
            String humanName = input.nextLine();

            System.out.print("AI difficulty: ");
            String difficulty = input.nextLine();

            players.add(new Player(humanName));
            players.add(BoggleAI.makeAIPlayer("AI", difficulty));
        }
        else if (phase == 3) {
            System.out.print("How many players: ");
            int count = readNumber(input, 2);

            for (int i = 0; i < count; i++) {
                System.out.print("Player " + (i + 1) + " name: ");
                String name = input.nextLine();

                players.add(new Player(name));
            }
        }
        else if (phase == 4) {
            System.out.print("How many human players: ");
            int count = readNumber(input, 1);

            for (int i = 0; i < count; i++) {
                System.out.print("Human " + (i + 1) + " name: ");
                String name = input.nextLine();

                players.add(new Player(name));
            }

            System.out.print("AI difficulty: ");
            String difficulty = input.nextLine();

            players.add(BoggleAI.makeAIPlayer("AI", difficulty));
        }
        else {
            System.out.print("AI 1 difficulty: ");
            String ai1 = input.nextLine();

            System.out.print("AI 2 difficulty: ");
            String ai2 = input.nextLine();

            players.add(BoggleAI.makeAIPlayer("AI 1", ai1));
            players.add(BoggleAI.makeAIPlayer("AI 2", ai2));
        }

        GameSession session = new GameSession(players, minimumLength, targetScore, dictionaryFile);

        playTextGame(input, session, phase);
    }

    public static void playTextGame(Scanner input, GameSession session, int phase) {
        while (true) {
            Player currentPlayer = session.getCurrentPlayer();

            System.out.println();
            printBoard(session.board);
            System.out.println("Turn: " + currentPlayer.name);
            System.out.println("Score: " + currentPlayer.score);

            if (currentPlayer.isAI == true) {
                GameSession.AIResult result = session.doAITurn();

                if (result.passed == true) {
                    System.out.println(currentPlayer.name + " passed.");
                }
                else {
                    System.out.println(currentPlayer.name + " played " + result.word + " for " + result.points + " points.");
                }
            }
            else {
                System.out.print("Enter word, PASS, or QUIT: ");
                String word = input.nextLine();

                if (word.equalsIgnoreCase("PASS")) {
                    session.passCurrentPlayer();
                }
                else if (word.equalsIgnoreCase("QUIT")) {
                    session.quitCurrentPlayer();
                }
                else {
                    int result = session.submitWord(word);

                    if (result == 1) {
                        System.out.println("Word accepted.");
                    }
                    else {
                        System.out.println("Word not accepted.");
                    }
                }
            }

            int status = session.moveToNextPlayer();

            if (status == 1) {
                if (phase == 5) {
                    announceWinner(session);
                    break;
                }

                System.out.print("All players passed. Shake board? Y/N: ");
                String answer = input.nextLine();

                if (answer.equalsIgnoreCase("Y")) {
                    session.shakeBoard();
                }
                else {
                    announceWinner(session);
                    break;
                }
            }
            else if (status == 2) {
                announceWinner(session);
                break;
            }
        }
    }

    public static int readNumber(Scanner input, int minimum) {
        while (true) {
            try {
                String text = input.nextLine();
                int number = Integer.parseInt(text);

                if (number >= minimum) {
                    return number;
                }

                System.out.print("Enter a number at least " + minimum + ": ");
            }
            catch (Exception e) {
                System.out.print("Enter a number: ");
            }
        }
    }

    public static File findDictionaryFile() {
        String[] places = {
                "src/wordlist.txt",
                "wordlist.txt"
        };

        for (int i = 0; i < places.length; i++) {
            File file = new File(places[i]);

            if (file.exists()) {
                return file;
            }
        }

        return null;
    }

    public static ArrayList<String> loadDictionary(File file) {
        ArrayList<String> words = new ArrayList<String>();

        try {
            Scanner scanner = new Scanner(new FileReader(file));

            while (scanner.hasNext()) {
                String word = scanner.next().trim().toUpperCase();

                if (word.length() > 0) {
                    words.add(word);
                }
            }

            scanner.close();
            Collections.sort(words);
        }
        catch (Exception e) {
            System.out.println("Could not load dictionary.");
        }

        return words;
    }

    public static char[][] makeBoard() {
        ArrayList<String> diceList = new ArrayList<String>();

        for (int i = 0; i < dice.length; i++) {
            diceList.add(dice[i]);
        }

        Collections.shuffle(diceList);

        char[][] board = new char[5][5];
        int diceNumber = 0;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                String die = diceList.get(diceNumber);
                int side = random.nextInt(die.length());
                char letter = die.charAt(side);

                board[row][col] = letter;
                diceNumber = diceNumber + 1;
            }
        }

        return board;
    }

    public static void printBoard(char[][] board) {
        System.out.println("Board:");

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                System.out.print(board[row][col] + " ");
            }

            System.out.println();
        }
    }

    public static int calculateScore(String word) {
        if (word == null) {
            return 0;
        }

        return word.length();
    }

    public static boolean listHasWord(List<String> list, String word) {
        if (list == null || word == null) {
            return false;
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).equalsIgnoreCase(word)) {
                return true;
            }
        }

        return false;
    }

    public static boolean dictionaryHasWord(List<String> dictionary, String word) {
        if (word == null) {
            return false;
        }

        String newWord = word.toUpperCase();

        for (int i = 0; i < dictionary.size(); i++) {
            if (dictionary.get(i).equals(newWord)) {
                return true;
            }
        }

        return false;
    }

    public static boolean dictionaryHasPrefix(List<String> dictionary, String word) {
        if (word == null) {
            return false;
        }

        String newWord = word.toUpperCase();

        for (int i = 0; i < dictionary.size(); i++) {
            if (dictionary.get(i).startsWith(newWord)) {
                return true;
            }
        }

        return false;
    }

    public static boolean wordIsOnBoard(char[][] board, String word) {
        if (word == null) {
            return false;
        }

        String newWord = word.toUpperCase();

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 5; col++) {
                boolean[][] used = new boolean[5][5];

                if (checkBoardSpot(board, newWord, row, col, 0, used) == true) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean checkBoardSpot(char[][] board, String word, int row, int col, int letterNumber, boolean[][] used) {
        if (letterNumber == word.length()) {
            return true;
        }

        if (row < 0 || row >= 5 || col < 0 || col >= 5) {
            return false;
        }

        if (used[row][col] == true) {
            return false;
        }

        if (board[row][col] != word.charAt(letterNumber)) {
            return false;
        }

        used[row][col] = true;

        for (int rowChange = -1; rowChange <= 1; rowChange++) {
            for (int colChange = -1; colChange <= 1; colChange++) {
                if (rowChange != 0 || colChange != 0) {
                    boolean found = checkBoardSpot(board, word, row + rowChange, col + colChange, letterNumber + 1, used);

                    if (found == true) {
                        used[row][col] = false;
                        return true;
                    }
                }
            }
        }

        used[row][col] = false;
        return false;
    }

    public static void announceWinner(GameSession session) {
        Player winner = session.getWinner();

        System.out.println();
        System.out.println("Game over.");

        if (winner == null) {
            System.out.println("Winner: none");
        }
        else {
            System.out.println("Winner: " + winner.name);
        }

        for (int i = 0; i < session.players.size(); i++) {
            Player player = session.players.get(i);

            System.out.println(player.name + ": " + player.score);
        }
    }

    public static class Player {
        public String name;
        public int score;
        public int wrongGuesses;
        public int timeouts;
        public boolean passed;
        public boolean quit;
        public boolean isAI;
        public String difficulty;
        public ArrayList<String> words;

        public Player(String playerName) {
            if (playerName == null || playerName.trim().length() == 0) {
                name = "Player";
            }
            else {
                name = playerName.trim();
            }

            score = 0;
            wrongGuesses = 0;
            timeouts = 0;
            passed = false;
            quit = false;
            isAI = false;
            difficulty = "Easy";
            words = new ArrayList<String>();
        }
    }

    public static class GameSession {
        public ArrayList<Player> players;
        public ArrayList<String> dictionary;
        public ArrayList<String> usedWords;
        public char[][] board;
        public int currentPlayerNumber;
        public int currentRound;
        public int minimumWordLength;
        public int targetScore;
        public boolean shakeUsed;

        public GameSession(ArrayList<Player> newPlayers, int newMinimumWordLength, int newTargetScore, File dictionaryFile) {
            players = newPlayers;
            dictionary = loadDictionary(dictionaryFile);
            usedWords = new ArrayList<String>();
            board = makeBoard();
            currentPlayerNumber = 0;
            currentRound = 1;
            minimumWordLength = newMinimumWordLength;
            targetScore = newTargetScore;
            shakeUsed = false;

            if (minimumWordLength < 3) {
                minimumWordLength = 3;
            }
        }

        public Player getCurrentPlayer() {
            return players.get(currentPlayerNumber);
        }

        public int submitWord(String word) {
            Player player = getCurrentPlayer();

            if (player.quit == true || player.passed == true) {
                return -1;
            }

            if (word == null) {
                word = "";
            }

            String newWord = word.trim().toUpperCase();

            if (newWord.length() < minimumWordLength) {
                player.wrongGuesses = player.wrongGuesses + 1;
                checkWrongGuesses(player);
                return 0;
            }

            if (listHasWord(usedWords, newWord) == true) {
                return 2;
            }

            if (dictionaryHasWord(dictionary, newWord) == false) {
                player.wrongGuesses = player.wrongGuesses + 1;
                checkWrongGuesses(player);
                return 0;
            }

            if (wordIsOnBoard(board, newWord) == false) {
                player.wrongGuesses = player.wrongGuesses + 1;
                checkWrongGuesses(player);
                return 0;
            }

            int points = calculateScore(newWord);

            player.score = player.score + points;
            player.words.add(newWord);
            player.wrongGuesses = 0;
            player.timeouts = 0;
            usedWords.add(newWord);

            return 1;
        }

        public void checkWrongGuesses(Player player) {
            if (player.wrongGuesses >= 2) {
                player.wrongGuesses = 0;
                player.passed = true;
            }
        }

        public void passCurrentPlayer() {
            Player player = getCurrentPlayer();

            player.passed = true;
        }

        public void timeoutCurrentPlayer() {
            Player player = getCurrentPlayer();

            player.timeouts = player.timeouts + 1;
            player.passed = true;
        }

        public void quitCurrentPlayer() {
            Player player = getCurrentPlayer();

            player.quit = true;
        }

        public AIResult doAITurn() {
            Player player = getCurrentPlayer();

            ArrayList<String> words = BoggleAI.findAllValidWords(board, dictionary, minimumWordLength, usedWords);
            String word = BoggleAI.chooseWord(words, player.difficulty);

            if (word == null) {
                passCurrentPlayer();
                return new AIResult(true, null, 0);
            }

            int points = calculateScore(word);

            player.score = player.score + points;
            player.words.add(word);
            usedWords.add(word);

            return new AIResult(false, word, points);
        }

        public int moveToNextPlayer() {
            if (someoneReachedTarget() == true) {
                return 2;
            }

            if (someoneQuit() == true) {
                return 2;
            }

            if (allPlayersPassed() == true) {
                if (shakeUsed == true) {
                    return 2;
                }

                return 1;
            }

            int oldPlayerNumber = currentPlayerNumber;

            while (true) {
                currentPlayerNumber = currentPlayerNumber + 1;

                if (currentPlayerNumber >= players.size()) {
                    currentPlayerNumber = 0;
                    currentRound = currentRound + 1;
                }

                Player player = players.get(currentPlayerNumber);

                if (player.quit == false && player.passed == false) {
                    break;
                }

                if (currentPlayerNumber == oldPlayerNumber) {
                    break;
                }
            }

            return 0;
        }

        public boolean someoneReachedTarget() {
            if (targetScore <= 0) {
                return false;
            }

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                if (player.score >= targetScore) {
                    return true;
                }
            }

            return false;
        }

        public boolean someoneQuit() {
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                if (player.quit == true) {
                    return true;
                }
            }

            return false;
        }

        public boolean allPlayersPassed() {
            boolean foundOnePlayer = false;

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                if (player.quit == false) {
                    foundOnePlayer = true;

                    if (player.passed == false) {
                        return false;
                    }
                }
            }

            return foundOnePlayer;
        }

        public void shakeBoard() {
            board = makeBoard();
            usedWords.clear();
            shakeUsed = true;

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                player.passed = false;
                player.wrongGuesses = 0;
                player.timeouts = 0;
            }
        }

        public Player getWinner() {
            Player winner = null;

            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);

                if (player.quit == false) {
                    if (winner == null || player.score > winner.score) {
                        winner = player;
                    }
                }
            }

            return winner;
        }

        public static class AIResult {
            public boolean passed;
            public String word;
            public int points;

            public AIResult(boolean newPassed, String newWord, int newPoints) {
                passed = newPassed;
                word = newWord;
                points = newPoints;
            }
        }
    }
}
