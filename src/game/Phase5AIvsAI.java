package game;

import model.AI;
import model.BoggleBoard;
import model.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Phase5AIvsAI {
    public static void run(Scanner sc, File dictionaryFile) {
        System.out.println("AI vs AI Rules:");
        System.out.println("- Both AIs use the same dictionary and same board.");
        System.out.println("- Your AI is automatic. Opponent AI move is typed manually.");
        System.out.println();

        System.out.print("Your AI difficulty (Easy/Medium/Hard): ");
        String yourDiff = sc.nextLine();
        System.out.print("Opponent AI name: ");
        String oppName = sc.nextLine();

        int minLen = Phase1PlayerVsPlayer.readInt(sc, "Minimum word length (>=3): ", 3);
        int target = Phase1PlayerVsPlayer.readIntAllowZero(sc, "Target score (0 = no target): ");

        List<Player> players = new ArrayList<Player>();
        players.add(AI.createAIPlayer("YourAI", yourDiff));
        players.add(new Player(oppName));

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);

        for (;;) {
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);

            if (cur.isAI) {
                GameSession.AIResult r = session.runAITurnIfNeeded();
                if (r.passed) System.out.println("YourAI PASSED");
                else System.out.println("YourAI played: " + r.word + " (+" + r.points + ")");
            } else {
                System.out.print("Opponent move (word or PASS): ");
                String input = sc.nextLine();
                if (input == null) input = "";
                input = input.trim();
                if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println("Accepted. +" + input.trim().length());
                    } else if (sr == 2) {
                        System.out.println("Rejected: already used.");
                    } else {
                        System.out.println("Rejected: invalid.");
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
            if (ar == 1) {
                // AI vs AI: end on first all-pass
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
        }
    }
}
