import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class BoggleGame {

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
            System.out.println("Boggle — choose interface");
            System.out.println("1) Text version");
            System.out.println("2) GUI version");
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
            if (mode.equals("2")) {
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

    private static void runTextMode(Scanner sc, File dict) {
        while (true) {
            System.out.println();
            System.out.println("Boggle Menu");
            System.out.println("1) Player vs Player");
            System.out.println("2) Player vs AI");
            System.out.println("3) Multiplayer");
            System.out.println("4) Multiplayer + AI");
            System.out.println("5) AI vs AI");
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

    public static void runPlayerVsPlayer(Scanner sc, File dictionaryFile) {
        showRules();

        System.out.print("Player 1 name: ");
        String p1 = sc.nextLine();
        System.out.print("Player 2 name: ");
        String p2 = sc.nextLine();

        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = readTimerChoice(sc);

        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(p1));
        players.add(new Player(p2));

        GameSession session = new GameSession(
            players,
            minLen,
            target,
            dictionaryFile
        );
        gameLoop(sc, session, timerSeconds);
    }

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

    /** Same text as printed by {@link #showRules()}, for GUI dialogs. */
    public static String getRulesText() {
        return (
            "Rules:\n" +
            "1. Connect adjacent letters (horizontal, vertical, diagonal).\n" +
            "2. A cube can only be used once per word.\n" +
            "3. Wrong word gives no points (2 wrong guesses = auto pass).\n" +
            "4. If all players pass, you may Shake the Board once.\n"
        );
    }

    public static void showRules() {
        System.out.print(getRulesText());
        System.out.println();
    }

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

            if (input.equals("TIMEOUT_PASS")) {
                // Timeout pass already handled
            } else if (input.equalsIgnoreCase("QUIT")) {
                session.quit();
            } else if (input.equalsIgnoreCase("PASS")) {
                session.pass();
            } else if (input.equalsIgnoreCase("HINT")) {
                if (session.isHintUsed()) {
                    System.out.println("Hint already used.");
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
                int r = session.submitWord(input);
                if (r == 1) {
                    System.out.println(
                        "Valid! +" + input.trim().length() + " points"
                    );
                } else if (r == 2) {
                    System.out.println("Already used. 0 points.");
                } else {
                    System.out.println("Invalid. 0 points.");
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
                System.out.print("All passed. Shake Up? (Y/N): ");
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
        session.writeLog(new File("boggleSave.txt"));
    }

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

    public static void runPlayerVsAI(Scanner sc, File dictionaryFile) {
        showRules();

        System.out.print("Your name: ");
        String human = sc.nextLine();

        System.out.print("AI difficulty (Easy/Medium/Hard): ");
        String diff = sc.nextLine();

        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");
        int timerSeconds = readTimerChoice(sc);

        ArrayList<Player> players = new ArrayList<Player>();
        players.add(new Player(human));
        players.add(BoggleAI.createAIPlayer("AI", diff));

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
                    session.saveIfPvAIQuit(new File(human + "Save.txt"));
                } else if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                } else if (input.equalsIgnoreCase("HINT")) {
                    if (session.isHintUsed()) {
                        System.out.println("Hint already used.");
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
                        System.out.println("Already used. 0 points.");
                    } else {
                        System.out.println("Invalid. 0 points.");
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
                System.out.print("All passed. Shake Up? (Y/N): ");
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
                    System.out.println("Hint already used.");
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
                    System.out.println("Already used. 0 points.");
                } else {
                    System.out.println("Invalid. 0 points.");
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
                    "All active players passed. Shake Up? (Y/N): "
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
                        System.out.println("Hint already used.");
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
                        System.out.println("Already used. 0 points.");
                    } else {
                        System.out.println("Invalid. 0 points.");
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
                    "All active players passed. Shake Up? (Y/N): "
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
                        System.out.println("Rejected: already used.");
                    } else {
                        System.out.println("Rejected: invalid.");
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

    public static char[][] readBoardFile(File file) {
        try {
            if (file == null || !file.exists()) return null;

            Scanner scanner = new Scanner(file);
            String letters = "";
            while (scanner.hasNext()) {
                letters = letters + scanner.next();
            }
            scanner.close();

            letters = letters.toUpperCase();
            int need = GameSession.BOARD_SIZE * GameSession.BOARD_SIZE;
            if (letters.length() < need) return null;

            char[][] board =
                new char[GameSession.BOARD_SIZE][GameSession.BOARD_SIZE];
            int index = 0;
            for (int r = 0; r < GameSession.BOARD_SIZE; r++) {
                for (int c = 0; c < GameSession.BOARD_SIZE; c++) {
                    char ch = letters.charAt(index);
                    if (ch < 'A' || ch > 'Z') return null;
                    board[r][c] = ch;
                    index++;
                }
            }
            return board;
        } catch (Exception e) {
            return null;
        }
    }

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

    public static File findDictionaryFile() {
        String[] candidates = new String[] {
            "src/wordlist.txt",
            "wordlist.txt",
            "BoggleAssignment/redesigned-octo-spork/src/wordlist.txt",
            "BoggleAssignment/wordlist.txt",
            "redesigned-octo-spork/src/wordlist.txt",
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
