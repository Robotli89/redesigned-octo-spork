/*
 * Author: Kevin Li and Ethan Chuang
 * Date:   June 4, 2026
 * Course: ICS4U1
 * Project: Boggle Game
 *
 * Description:
 * This class is the main entry point for the Boggle game. It lets the user
 * choose between the text and graphical interfaces, configures each game
 * mode, and runs the text-based game loop.
 *
 * Copyright (c) Kevin Li and Ethan Chuang
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class BoggleGame {

    // File names used for saving a game and for the game log.
    public static final String SAVE_FILE_NAME = "boggleSave.txt";
    public static final String LOG_FILE_NAME = "boggleLog.txt";

    // Program start. Shows the top menu, where the user picks the text version,
    // the GUI version, or changes the word list.
    public static void main(String[] args) {
        File dict = findDictionaryFile();
        if (dict == null) {
            System.out.println("Dictionary file not found (wordlist.txt).");
            System.out.println(
                "Result: all words will be INVALID and AI will always pass."
            );
            dict = new File("wordlist.txt");
        }

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println();
            System.out.println("Welcome to the Boggle Program!");
            System.out.println(
                "You compete with others or AI to find who wins!"
            );
            System.out.println(
                "You will be given a board and you will be trying to find a word from it."
            );
            System.out.println("See who gets more points!");
            System.out.println(
                "------------------------------------------------------------------------"
            );
            System.out.println("Boggle — choose interface");
            System.out.println("Current word list: " + dict.getPath());
            System.out.println("1) Text version");
            System.out.println("2) GUI version");
            System.out.println("3) Choose word list");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String mode = sc.nextLine();
            if (mode == null) {
                mode = "";
            }
            mode = mode.trim();

            if (mode.equals("0")) {
                break;
            }
            if (mode.equals("3")) {
                dict = chooseWordList(sc, dict);
                continue;
            }
            if (mode.equals("2")) {
                // Launch the graphical version. Swing wants its windows created
                // on its own special thread, so invokeLater hands the job off to
                // that thread instead of building the window here.
                final File dictionaryForGui = dict;
                SwingUtilities.invokeLater(
                    new Runnable() {
                        public void run() {
                            new BoggleGUI(dictionaryForGui);
                        }
                    }
                );
                return;
            }
            if (!mode.equals("1")) {
                System.out.println("Invalid choice.");
                continue;
            }

            runTextMode(sc, dict);
        }

        sc.close();
    }

    // Lets the user pick which word list to play with: the standard list, the
    // small test list, or a custom file path they type in.
    public static File chooseWordList(Scanner sc, File currentDictionaryFile) {
        while (true) {
            File standard = findDictionaryFile();
            File test = findTestDictionaryFile();

            System.out.println();
            System.out.println("Choose word list");
            System.out.println(
                "1) Standard wordlist.txt" + describeFile(standard)
            );
            System.out.println("2) Test wordlistTest.txt" + describeFile(test));
            System.out.println("3) Custom path");
            System.out.println("0) Back");
            System.out.print("Choose: ");

            String ch = sc.nextLine();
            if (ch == null) ch = "";
            ch = ch.trim();

            if (ch.equals("0")) {
                return currentDictionaryFile;
            }
            if (ch.equals("1")) {
                if (standard != null && standard.exists()) return standard;
                System.out.println("Standard word list was not found.");
                continue;
            }
            if (ch.equals("2")) {
                if (test != null && test.exists()) return test;
                System.out.println("Test word list was not found.");
                continue;
            }
            if (ch.equals("3")) {
                System.out.print("Enter word list file path: ");
                String path = sc.nextLine();
                if (path == null) path = "";
                path = path.trim();
                File custom = new File(path);
                if (custom.exists()) return custom;
                System.out.println("Word list file was not found.");
                continue;
            }

            System.out.println("Invalid choice.");
        }
    }

    // Returns a short note about a file for the menu, e.g. " (src/wordlist.txt)"
    // or " (not found)".
    public static String describeFile(File file) {
        if (file == null || !file.exists()) return " (not found)";
        return " (" + file.getPath() + ")";
    }

    // The text-mode menu. Lets the user pick a game type, runs it, then asks if
    // they want to play another round.
    private static void runTextMode(Scanner sc, File dict) {
        while (true) {
            System.out.println();
            System.out.println("Boggle Menu");
            System.out.println("1) Player vs Player");
            System.out.println("2) Player vs AI");
            System.out.println("3) Multiplayer");
            System.out.println("4) Multiplayer + AI");
            System.out.println("5) AI vs AI");
            System.out.println("6) Play saved game");
            System.out.println("0) Back");
            System.out.print("Choose: ");
            String ch = sc.nextLine();
            if (ch == null) {
                ch = "";
            }
            ch = ch.trim();

            if (ch.equals("0")) {
                break;
            }
            if (ch.equals("1")) {
                runPlayerVsPlayer(sc, dict);
            } else if (ch.equals("2")) {
                runPlayerVsAI(sc, dict);
            } else if (ch.equals("3")) {
                runMultiplayer(sc, dict);
            } else if (ch.equals("4")) {
                runMultiplayerAI(sc, dict);
            } else if (ch.equals("5")) {
                runAIvsAI(sc, dict);
            } else if (ch.equals("6")) {
                if (!runSavedGame(sc, dict)) {
                    continue;
                }
            } else {
                System.out.println("Invalid choice.");
                continue;
            }

            System.out.print("Another round? (Y/N): ");
            String again = sc.nextLine();
            if (again == null) {
                again = "";
            }
            again = again.trim();
            if (!again.equalsIgnoreCase("Y")) {
                break;
            }
        }
    }

    // Loads the saved game from disk (if there is one) and plays it.
    public static boolean runSavedGame(Scanner sc, File dictionaryFile) {
        File saveFile = new File(SAVE_FILE_NAME);
        if (!saveFile.exists()) {
            System.out.println("No saved game found.");
            return false;
        }

        try {
            GameSession session = GameSession.loadGame(
                saveFile,
                dictionaryFile,
                3,
                0
            );
            System.out.println("Loaded last saved game.");
            gameLoop(sc, session, 0);
            return true;
        } catch (Exception e) {
            System.out.println("Could not load saved game.");
            return false;
        }
    }

    // Sets up and runs a two-human game: asks for names and settings, builds
    // the players, then hands off to the shared gameLoop.
    public static void runPlayerVsPlayer(Scanner sc, File dictionaryFile) {
        showRules();

        System.out.print("Player 1 name: ");
        String p1 = sc.nextLine();
        System.out.print("Player 2 name: ");
        String p2 = sc.nextLine();

        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = readTimerChoice(sc);
        boolean randomFirst = readRandomFirstChoice(sc);

        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(p1));
        players.add(new Player(p2));
        applyRandomFirstChoice(players, randomFirst);

        GameSession session = new GameSession(
            players,
            minLen,
            target,
            dictionaryFile
        );
        gameLoop(sc, session, timerSeconds);
    }

    // Asks the user how long each turn should last. Returns the number of
    // seconds, or 0 for no timer.
    public static int readTimerChoice(Scanner sc) {
        while (true) {
            System.out.println();
            System.out.println("Timer choices:");
            System.out.println("1) None");
            System.out.println("2) 15 seconds");
            System.out.println("3) 30 seconds");
            System.out.println("4) 60 seconds");
            System.out.println("5) Custom");
            System.out.print("Choose timer (1-5): ");
            String ch = sc.nextLine();

            if (ch == null) ch = "";
            ch = ch.trim();

            if (ch.equals("1")) return 0;
            if (ch.equals("2")) return 15;
            if (ch.equals("3")) return 30;
            if (ch.equals("4")) return 60;
            if (ch.equals("5")) {
                return readInt(sc, "Enter custom timer in seconds: ", 1);
            }
            System.out.println("Invalid choice.");
        }
    }

    // Asks yes/no whether the starting player should be chosen randomly.
    public static boolean readRandomFirstChoice(Scanner sc) {
        while (true) {
            System.out.print("Randomize who goes first? (Y/N): ");
            String ch = sc.nextLine();
            if (ch == null) ch = "";
            ch = ch.trim();

            if (ch.equalsIgnoreCase("Y")) return true;
            if (ch.equalsIgnoreCase("N")) return false;
            System.out.println("Enter Y or N.");
        }
    }

    // If the user asked to randomize the first player, do it and announce who
    // goes first.
    public static void applyRandomFirstChoice(
        ArrayList<Player> players,
        boolean randomFirst
    ) {
        if (!randomFirst) return;
        Player first = randomizeFirstPlayer(players);
        if (first != null) {
            System.out.println("Random first player: " + first.name);
        }
    }

    // Picks a random player to go first by rotating the list so that player is
    // at the front. Returns the new first player.
    public static Player randomizeFirstPlayer(ArrayList<Player> players) {
        if (players == null || players.isEmpty()) return null;
        int firstIndex = (int) (Math.random() * players.size());
        rotatePlayersToFirst(players, firstIndex);
        return players.get(0);
    }

    // Rotates the player list so the player at firstIndex becomes index 0, while
    // keeping everyone else in the same turn order after them.
    public static void rotatePlayersToFirst(
        ArrayList<Player> players,
        int firstIndex
    ) {
        if (players == null || players.isEmpty()) return;
        if (firstIndex < 0 || firstIndex >= players.size()) return;

        // Build the rotated order using % to wrap around the end of the list.
        ArrayList<Player> rotated = new ArrayList<Player>();
        for (int i = 0; i < players.size(); i++) {
            rotated.add(players.get((firstIndex + i) % players.size()));
        }

        // Copy the rotated order back into the original list.
        players.clear();
        for (int i = 0; i < rotated.size(); i++) {
            players.add(rotated.get(i));
        }
    }

    // Same rules text that showRules() prints, so the GUI can show it too.
    public static String getRulesText() {
        return (
            "Rules:\n" +
            "1. Connect adjacent letters (horizontal, vertical, diagonal).\n" +
            "2. A cube can only be used once per word.\n" +
            "3. Wrong word gives no points (2 wrong guesses = auto pass).\n" +
            "4. If all players pass, you may Shake the Board once.\n"
        );
    }

    // Prints the rules to the text console.
    public static void showRules() {
        System.out.print(getRulesText());
        System.out.println();
    }

    // The main text game loop shared by the human-only modes. Each pass shows
    // the board, reads the current player's input (word / PASS / QUIT / HINT),
    // applies it, then advances the turn and checks if the game should end.
    public static void gameLoop(
        Scanner sc,
        GameSession session,
        int timerSeconds
    ) {
        while (true) {
            System.out.println();
            GameSession.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println(
                "Turn: " + cur.name + "  Score=" + cur.totalScore
            );
            System.out.println(
                "Hint: " + (session.isHintUsed() ? "USED" : "available")
            );
            if (timerSeconds > 0) {
                System.out.println(
                    "(Timer limit: " + timerSeconds + " seconds)"
                );
            }
            System.out.print("Enter word (or PASS / QUIT / HINT): ");
            String input = getInputWithTimer(sc, timerSeconds, session);

            // Decide what the player typed and act on it.
            if (input.equals("TIMEOUT_PASS")) {
                // Timeout pass already handled inside getInputWithTimer.
            } else if (input.equalsIgnoreCase("QUIT")) {
                session.quit();
            } else if (input.equalsIgnoreCase("PASS")) {
                session.pass();
            } else if (input.equalsIgnoreCase("HINT")) {
                // Ask the AI for a strong word and show it, but only once a game.
                if (session.isHintUsed()) {
                    System.out.println("Hint can only be used once per game.");
                } else {
                    ArrayList<String> words =
                        session.boggleAI.findAllValidWords(
                            session.getBoard(),
                            session.dictionary,
                            session.minimumWordLength,
                            session.usedWords
                        );
                    if (words.isEmpty()) {
                        System.out.println("No hint available.");
                    } else {
                        String hint = session.boggleAI.chooseWord(
                            words,
                            "HARD"
                        );
                        System.out.println("Hint: " + hint);
                        session.markHintUsed();
                    }
                }
                continue;  // a hint does not use up the player's turn
            } else {
                // Otherwise treat the input as a word and report the result.
                int r = session.submitWord(input);
                if (r == 1) {
                    System.out.println(
                        "Valid! +" + input.trim().length() + " points"
                    );
                } else if (r == 2) {
                    System.out.println("Used before. 0 points.");
                } else {
                    System.out.println(
                        "Invalid word. Check length, dictionary, and board path. 0 points."
                    );
                }
            }

            // Advance to the next turn and react to the result code:
            // 2 = game over, 1 = everyone passed (maybe shake), 0 = keep going.
            int ar = session.nextTurn();
            if (ar == 2) {
                announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    announceWinner(session);
                    break;
                }
                System.out.print(
                    "All players passed. Shake the board? (Y/N): "
                );
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    announceWinner(session);
                    break;
                }
            }
        }
    }

    // Reads one line of input and, if a timer is on, measures how long it took.
    // If the player took too long it records a timeout and returns the special
    // marker "TIMEOUT_PASS". (Note: this checks the time AFTER the line is
    // entered, so it cannot interrupt someone mid-typing.)
    private static String getInputWithTimer(
        Scanner sc,
        int timerSeconds,
        GameSession session
    ) {
        long start = System.currentTimeMillis();
        String input = sc.nextLine();
        if (input == null) input = "";
        input = input.trim();
        if (timerSeconds > 0) {
            long elapsed = System.currentTimeMillis() - start;
            if (elapsed > timerSeconds * 1000L) {
                System.out.println(
                    "Time is up! (Took " +
                        (elapsed / 1000.0) +
                        " seconds, limit was " +
                        timerSeconds +
                        " seconds)"
                );
                session.timeout();
                return "TIMEOUT_PASS";
            } else {
                System.out.println(
                    "(Time taken: " + (elapsed / 1000.0) + " seconds)"
                );
            }
        }
        return input;
    }

    // Prints the final scores and the winner (or TIED), and writes the game log.
    // It finds the highest score and counts how many players share it.
    public static void announceWinner(GameSession session) {
        Player w = null;
        int bestScore = -1;
        int bestCount = 0;
        for (int i = 0; i < session.players.size(); i++) {
            Player p = session.players.get(i);
            if (p.quit) continue;
            if (p.totalScore > bestScore) {
                bestScore = p.totalScore;
                w = p;
                bestCount = 1;
            } else if (p.totalScore == bestScore) {
                bestCount++;
            }
        }
        System.out.println();
        if (bestCount > 1) {
            System.out.println("Game ended. Result: TIED");
        } else {
            System.out.println(
                "Game ended. Winner: " + (w == null ? "NONE" : w.name)
            );
        }
        for (int i = 0; i < session.players.size(); i++) {
            Player p = session.players.get(i);
            System.out.println(p.name + " score=" + p.totalScore);
        }
        session.writeLog(new File(LOG_FILE_NAME));
    }

    // Keeps asking until the user types a whole number that is at least
    // minValue, then returns it. Re-prompts on bad input.
    public static int readInt(Scanner sc, String prompt, int minValue) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < minValue) {
                    System.out.println("Must be >= " + minValue);
                } else {
                    return v;
                }
            } catch (Exception e) {
                System.out.println("Enter a number.");
            }
        }
    }

    // Like readInt but the smallest allowed value is 0 (used for "target score"
    // where 0 means "no target").
    public static int readIntAllowZero(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine();
            try {
                int v = Integer.parseInt(s.trim());
                if (v < 0) {
                    System.out.println("Must be >= 0");
                } else {
                    return v;
                }
            } catch (Exception e) {
                System.out.println("Enter a number.");
            }
        }
    }

    // Asks for the maximum word length. Accepts a number, or 0 / "NO LIMIT" /
    // "NONE" to mean no maximum.
    public static int readMaxWordLength(Scanner sc, int minimumWordLength) {
        while (true) {
            System.out.print(
                "Maximum word length (0 or NO LIMIT = no limit): "
            );
            String s = sc.nextLine();
            if (s == null) s = "";
            s = s.trim();
            if (
                s.equals("0") ||
                s.equalsIgnoreCase("NO LIMIT") ||
                s.equalsIgnoreCase("NONE")
            ) {
                return 0;
            }
            try {
                int v = Integer.parseInt(s);
                if (v < minimumWordLength) {
                    System.out.println(
                        "Must be >= " +
                            minimumWordLength +
                            ", or 0 for no limit."
                    );
                } else {
                    return v;
                }
            } catch (Exception e) {
                System.out.println("Enter a number, 0, or NO LIMIT.");
            }
        }
    }

    // Sets up and runs a human-vs-AI game. It has its own loop (instead of the
    // shared gameLoop) because on the AI's turn it calls the AI and can offer a
    // board shake when the AI pulls ahead of a player who has passed.
    public static void runPlayerVsAI(Scanner sc, File dictionaryFile) {
        showRules();

        System.out.print("Your name: ");
        String human = sc.nextLine();

        System.out.print("AI difficulty (Easy/Medium/Hard): ");
        String diff = sc.nextLine();

        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = readTimerChoice(sc);
        boolean randomFirst = readRandomFirstChoice(sc);

        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(human));
        players.add(BoggleAI.createAIPlayer("AI", diff));
        applyRandomFirstChoice(players, randomFirst);

        GameSession session = new GameSession(
            players,
            minLen,
            target,
            dictionaryFile
        );

        while (true) {
            Player cur = session.getCurrentPlayer();
            System.out.println();
            GameSession.printBoard(session.getBoard());
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println(
                "Turn: " + cur.name + "  Score=" + cur.totalScore
            );
            System.out.println(
                "Hint: " + (session.isHintUsed() ? "USED" : "available")
            );

            if (cur.isAI) {
                if (session.shouldOfferShakeAfterAILead(cur)) {
                    if (!offerShakeAfterAILead(sc, session, cur)) break;
                    continue;
                }

                AIResult r = session.runAITurnIfNeeded();
                if (r.passed) {
                    System.out.println("AI PASSED");
                } else {
                    System.out.println(
                        "AI played: " + r.word + " (+" + r.points + ")"
                    );
                }

                if (!r.passed && session.shouldOfferShakeAfterAILead(cur)) {
                    if (!offerShakeAfterAILead(sc, session, cur)) break;
                    continue;
                }
            } else {
                if (timerSeconds > 0) {
                    System.out.println(
                        "(Timer limit: " + timerSeconds + " seconds)"
                    );
                }
                System.out.print("Enter word (or PASS / QUIT / HINT): ");
                String input = getInputWithTimer(sc, timerSeconds, session);
                if (input.equals("TIMEOUT_PASS")) {
                    // Timeout pass already handled
                } else if (input.equalsIgnoreCase("QUIT")) {
                    session.quit();
                    session.saveIfPvAIQuit(new File(SAVE_FILE_NAME));
                } else if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                } else if (input.equalsIgnoreCase("HINT")) {
                    if (session.isHintUsed()) {
                        System.out.println(
                            "Hint can only be used once per game."
                        );
                    } else {
                        ArrayList<String> words =
                            session.boggleAI.findAllValidWords(
                                session.getBoard(),
                                session.dictionary,
                                session.minimumWordLength,
                                session.usedWords
                            );
                        if (words.isEmpty()) {
                            System.out.println("No hint available.");
                        } else {
                            String hint = session.boggleAI.chooseWord(
                                words,
                                "HARD"
                            );
                            System.out.println("Hint: " + hint);
                            session.markHintUsed();
                        }
                    }
                    continue;
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println(
                            "Valid! +" + input.trim().length() + " points"
                        );
                    } else if (sr == 2) {
                        System.out.println("Used before. 0 points.");
                    } else {
                        System.out.println(
                            "Invalid word. Check length, dictionary, and board path. 0 points."
                        );
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    announceWinner(session);
                    break;
                }
                System.out.print(
                    "All players passed. Shake the board? (Y/N): "
                );
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    announceWinner(session);
                    break;
                }
            }
        }
    }

    // When the AI is winning and the human has already passed, offer them one
    // board shake to keep playing. Returns true if the game should continue
    // (board was shaken) or false if it is over. Returns the human to their
    // turn after a shake so they get a fair chance.
    public static boolean offerShakeAfterAILead(
        Scanner sc,
        GameSession session,
        Player aiPlayer
    ) {
        int restartIndex = session.getPassedHumanIndexBehindAI(aiPlayer);

        if (session.isShakeUpUsed()) {
            System.out.println(aiPlayer.name + " is ahead. Game over.");
            announceWinner(session);
            return false;
        }

        System.out.print(
            aiPlayer.name + " is now ahead. Shake the board? (Y/N): "
        );
        String choice = sc.nextLine();
        if (choice != null && choice.trim().equalsIgnoreCase("Y")) {
            session.performShake();
            if (restartIndex >= 0) {
                session.currentTurnIndex = restartIndex;
            }
            System.out.println("Board was shaken.");
            return true;
        }

        System.out.println(aiPlayer.name + " is ahead. Game over.");
        announceWinner(session);
        return false;
    }

    // Sets up and runs a game with 3 or more human players. The loop is the same
    // idea as gameLoop, just with more players taking turns.
    public static void runMultiplayer(Scanner sc, File dictionaryFile) {
        showRules();

        int count = readInt(sc, "How many players (>=3): ", 3);
        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = readTimerChoice(sc);

        ArrayList<Player> players = new ArrayList<Player>();
        for (int i = 0; i < count; i++) {
            System.out.print("Player " + (i + 1) + " name: ");
            String name = sc.nextLine();
            players.add(new Player(name));
        }
        boolean randomFirst = readRandomFirstChoice(sc);
        applyRandomFirstChoice(players, randomFirst);

        GameSession session = new GameSession(
            players,
            minLen,
            target,
            dictionaryFile
        );

        while (true) {
            System.out.println();
            GameSession.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println(
                "Turn: " + cur.name + "  Score=" + cur.totalScore
            );
            System.out.println(
                "Hint: " + (session.isHintUsed() ? "USED" : "available")
            );
            if (timerSeconds > 0) {
                System.out.println(
                    "(Timer limit: " + timerSeconds + " seconds)"
                );
            }
            System.out.print("Enter word (or PASS / QUIT / HINT): ");
            String input = getInputWithTimer(sc, timerSeconds, session);

            if (input.equals("TIMEOUT_PASS")) {
                // Timeout pass already handled
            } else if (input.equalsIgnoreCase("QUIT")) {
                session.quit();
            } else if (input.equalsIgnoreCase("PASS")) {
                session.pass();
            } else if (input.equalsIgnoreCase("HINT")) {
                if (session.isHintUsed()) {
                    System.out.println("Hint can only be used once per game.");
                } else {
                    ArrayList<String> words =
                        session.boggleAI.findAllValidWords(
                            session.getBoard(),
                            session.dictionary,
                            session.minimumWordLength,
                            session.usedWords
                        );
                    if (words.isEmpty()) {
                        System.out.println("No hint available.");
                    } else {
                        String hint = session.boggleAI.chooseWord(
                            words,
                            "HARD"
                        );
                        System.out.println("Hint: " + hint);
                        session.markHintUsed();
                    }
                }
                continue;
            } else {
                int sr = session.submitWord(input);
                if (sr == 1) {
                    System.out.println(
                        "Valid! +" + input.trim().length() + " points"
                    );
                } else if (sr == 2) {
                    System.out.println("Used before. 0 points.");
                } else {
                    System.out.println(
                        "Invalid word. Check length, dictionary, and board path. 0 points."
                    );
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    announceWinner(session);
                    break;
                }
                System.out.print(
                    "All players passed. Shake the board? (Y/N): "
                );
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    announceWinner(session);
                    break;
                }
            }
        }
    }

    // Sets up and runs a game that mixes human players and AI players. On an
    // AI's turn the AI plays automatically; on a human's turn it reads input.
    public static void runMultiplayerAI(Scanner sc, File dictionaryFile) {
        showRules();

        int humanCount = readInt(sc, "How many human players (>=2): ", 2);
        int aiCount = readInt(sc, "How many AI players (>=1): ", 1);
        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = readTimerChoice(sc);

        ArrayList<Player> players = new ArrayList<Player>();
        for (int i = 0; i < humanCount; i++) {
            System.out.print("Human Player " + (i + 1) + " name: ");
            String name = sc.nextLine();
            players.add(new Player(name));
        }
        for (int i = 0; i < aiCount; i++) {
            System.out.print(
                "AI #" + (i + 1) + " difficulty (Easy/Medium/Hard): "
            );
            String diff = sc.nextLine();
            players.add(BoggleAI.createAIPlayer("AI" + (i + 1), diff));
        }
        boolean randomFirst = readRandomFirstChoice(sc);
        applyRandomFirstChoice(players, randomFirst);

        GameSession session = new GameSession(
            players,
            minLen,
            target,
            dictionaryFile
        );

        while (true) {
            System.out.println();
            GameSession.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println(
                "Turn: " + cur.name + "  Score=" + cur.totalScore
            );
            System.out.println(
                "Hint: " + (session.isHintUsed() ? "USED" : "available")
            );

            if (cur.isAI) {
                AIResult r = session.runAITurnIfNeeded();
                if (r.passed) System.out.println(cur.name + " PASSED");
                else System.out.println(
                    cur.name + " played: " + r.word + " (+" + r.points + ")"
                );
            } else {
                if (timerSeconds > 0) {
                    System.out.println(
                        "(Timer limit: " + timerSeconds + " seconds)"
                    );
                }
                System.out.print("Enter word (or PASS / QUIT / HINT): ");
                String input = getInputWithTimer(sc, timerSeconds, session);

                if (input.equals("TIMEOUT_PASS")) {
                    // Timeout pass already handled
                } else if (input.equalsIgnoreCase("QUIT")) {
                    session.quit();
                } else if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                } else if (input.equalsIgnoreCase("HINT")) {
                    if (session.isHintUsed()) {
                        System.out.println(
                            "Hint can only be used once per game."
                        );
                    } else {
                        ArrayList<String> words =
                            session.boggleAI.findAllValidWords(
                                session.getBoard(),
                                session.dictionary,
                                session.minimumWordLength,
                                session.usedWords
                            );
                        if (words.isEmpty()) {
                            System.out.println("No hint available.");
                        } else {
                            String hint = session.boggleAI.chooseWord(
                                words,
                                "HARD"
                            );
                            System.out.println("Hint: " + hint);
                            session.markHintUsed();
                        }
                    }
                    continue;
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println(
                            "Valid! +" + input.trim().length() + " points"
                        );
                    } else if (sr == 2) {
                        System.out.println("Used before. 0 points.");
                    } else {
                        System.out.println(
                            "Invalid word. Check length, dictionary, and board path. 0 points."
                        );
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    announceWinner(session);
                    break;
                }
                System.out.print(
                    "All players passed. Shake the board? (Y/N): "
                );
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    announceWinner(session);
                    break;
                }
            }
        }
    }

    // Runs a special mode where our AI plays against an outside AI. The board is
    // read from setBoard.txt so both programs use the same letters, and the
    // opponent's moves are typed in by hand to keep the two games in sync.
    public static void runAIvsAI(Scanner sc, File dictionaryFile) {
        System.out.println("AI vs AI Rules:");
        System.out.println(
            "- Put the 5x5 board letters in setBoard.txt before starting."
        );
        System.out.println("- My AI automatically uses the best AI setting.");
        System.out.println(
            "- Type the opponent AI's word manually to stay synced."
        );
        System.out.println();

        File boardFile = findSetBoardFile();
        char[][] fixedBoard = readBoardFile(boardFile);
        if (fixedBoard == null) {
            System.out.println(
                "Could not read a valid 5x5 board from setBoard.txt."
            );
            System.out.println(
                "Use 25 letters, with or without spaces/line breaks."
            );
            return;
        }

        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int maxLen = readMaxWordLength(sc, minLen);

        System.out.println("Who goes first?");
        System.out.println("1) My AI");
        System.out.println("2) Opponent AI");
        System.out.println("3) Random");
        System.out.print("Choose: ");
        String first = sc.nextLine();
        if (first == null) first = "";
        first = first.trim();

        Player myAI = BoggleAI.createAIPlayer("My AI", "HARD");
        Player opponentAI = new Player("Opponent AI");

        ArrayList<Player> players = new ArrayList<Player>();
        if (first.equals("2")) {
            players.add(opponentAI);
            players.add(myAI);
        } else {
            players.add(myAI);
            players.add(opponentAI);
            if (first.equals("3")) {
                applyRandomFirstChoice(players, true);
            }
        }

        GameSession session = new GameSession(
            players,
            minLen,
            0,
            dictionaryFile,
            maxLen
        );
        session.board = fixedBoard;

        while (true) {
            System.out.println();
            GameSession.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println(
                "Turn: " + cur.name + "  Score=" + cur.totalScore
            );
            if (maxLen > 0) {
                System.out.println("Word length: " + minLen + " to " + maxLen);
            } else {
                System.out.println("Word length: " + minLen + "+");
            }

            if (cur.isAI) {
                AIResult r = session.runAITurnIfNeeded();
                if (r.passed) System.out.println("My AI PASSED");
                else System.out.println(
                    "My AI played: " + r.word + " (+" + r.points + ")"
                );
            } else {
                System.out.print("Opponent AI move (word or PASS / QUIT): ");
                String input = sc.nextLine();
                if (input == null) input = "";
                input = input.trim();
                if (input.equalsIgnoreCase("QUIT")) {
                    session.quit();
                } else if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println(
                            "Accepted. +" + input.trim().length()
                        );
                    } else if (sr == 2) {
                        System.out.println("Rejected: used before.");
                    } else {
                        System.out.println(
                            "Rejected: invalid word. Check length, dictionary, and board path."
                        );
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                announceWinner(session);
                break;
            }
            if (ar == 1) {
                announceWinner(session);
                break;
            }
        }
    }

    // Reads a fixed 5x5 board from a file for AI-vs-AI mode. It gathers all the
    // letters (ignoring spaces and line breaks), checks there are at least 25
    // valid A-Z letters, then fills the grid. Returns null if anything is wrong.
    public static char[][] readBoardFile(File file) {
        try {
            if (file == null || !file.exists()) return null;

            // Read every token and glue them together into one string of letters.
            Scanner scanner = new Scanner(file);
            String letters = "";
            while (scanner.hasNext()) {
                letters = letters + scanner.next();
            }
            scanner.close();

            letters = letters.toUpperCase();
            int need = GameSession.BOARD_SIZE * GameSession.BOARD_SIZE;
            if (letters.length() < need) return null;  // not enough letters

            char[][] board =
                new char[GameSession.BOARD_SIZE][GameSession.BOARD_SIZE];
            int index = 0;
            for (int r = 0; r < GameSession.BOARD_SIZE; r++) {
                for (int c = 0; c < GameSession.BOARD_SIZE; c++) {
                    char ch = letters.charAt(index);
                    if (ch < 'A' || ch > 'Z') return null;  // not a real letter
                    board[r][c] = ch;
                    index++;
                }
            }
            return board;
        } catch (Exception e) {
            return null;
        }
    }

    // Looks for setBoard.txt in a few likely folders and returns the first one
    // found (or a default name if none exist).
    public static File findSetBoardFile() {
        String[] candidates = new String[] {
            "setBoard.txt",
            "../setBoard.txt",
            "src/setBoard.txt",
            "NO_OOP/setBoard.txt",
        };
        for (int i = 0; i < candidates.length; i++) {
            File f = new File(candidates[i]);
            if (f.exists()) return f;
        }
        return new File("setBoard.txt");
    }

    // Finds the main dictionary file (wordlist.txt).
    public static File findDictionaryFile() {
        return findWordListFile("wordlist.txt");
    }

    // Finds the small test dictionary file (wordlistTest.txt).
    public static File findTestDictionaryFile() {
        return findWordListFile("wordlistTest.txt");
    }

    // Searches several likely folders for a word list file and returns the first
    // one that exists, or null if none are found. This makes the program work no
    // matter which folder it is run from.
    public static File findWordListFile(String fileName) {
        String[] candidates = new String[] {
            "src/" + fileName,
            fileName,
            "BoggleAssignment/redesigned-octo-spork/src/" + fileName,
            "BoggleAssignment/" + fileName,
            "redesigned-octo-spork/src/" + fileName,
        };
        for (int i = 0; i < candidates.length; i++) {
            File f = new File(candidates[i]);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }
}
