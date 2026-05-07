package game;

import model.BoggleBoard;
import model.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Phase1PlayerVsPlayer {
    public static void run(Scanner sc, File dictionaryFile) {
        showRules();

        System.out.print("Player 1 name: ");
        String p1 = sc.nextLine();
        System.out.print("Player 2 name: ");
        String p2 = sc.nextLine();

        int minLen = readInt(sc, "Minimum word length (>=3): ", 3);
        int target = readIntAllowZero(sc, "Target score (0 = no target): ");

        List<Player> players = new ArrayList<Player>();
        players.add(new Player(p1));
        players.add(new Player(p2));

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);
        gameLoop(sc, session);
    }

    public static void showRules() {
        System.out.println("Rules:");
        System.out.println("1. Connect adjacent letters (horizontal, vertical, diagonal).");
        System.out.println("2. A cube can only be used once per word.");
        System.out.println("3. Wrong word gives no points (2 wrong guesses = auto pass).");
        System.out.println("4. If all players pass, you may Shake the Board once.");
        System.out.println();
    }

    public static void gameLoop(Scanner sc, GameSession session) {
        for (;;) {
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);
            System.out.print("Enter word (or PASS / QUIT): ");
            String input = sc.nextLine();
            if (input == null) input = "";
            input = input.trim();

            if (input.equalsIgnoreCase("QUIT")) {
                session.quit();
            } else if (input.equalsIgnoreCase("PASS")) {
                session.pass();
            } else {
                int r = session.submitWord(input);
                if (r == 1) {
                    System.out.println("Valid! +" + input.trim().length() + " points");
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

    public static void announceWinner(GameSession session) {
        Player w = session.determineWinner();
        System.out.println();
        System.out.println("Game ended. Winner: " + (w == null ? "NONE" : w.name));
        for (int i = 0; i < session.players.size(); i++) {
            Player p = session.players.get(i);
            System.out.println(p.name + " score=" + p.totalScore);
        }
    }

    public static int readInt(Scanner sc, String prompt, int minValue) {
        for (;;) {
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
        for (;;) {
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
}
