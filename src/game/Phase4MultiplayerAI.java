package game;

import model.AI;
import model.BoggleBoard;
import model.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Phase4MultiplayerAI {
    public static void run(Scanner sc, File dictionaryFile) {
        Phase1PlayerVsPlayer.showRules();

        int humanCount = Phase1PlayerVsPlayer.readInt(sc, "How many human players (>=1): ", 1);
        int aiCount = Phase1PlayerVsPlayer.readInt(sc, "How many AI players (>=1): ", 1);
        int minLen = Phase1PlayerVsPlayer.readInt(sc, "Minimum word length (>=3): ", 3);
        int target = Phase1PlayerVsPlayer.readIntAllowZero(sc, "Target score (0 = no target): ");

        List<Player> players = new ArrayList<Player>();
        for (int i = 0; i < humanCount; i++) {
            System.out.print("Human Player " + (i + 1) + " name: ");
            String name = sc.nextLine();
            players.add(new Player(name));
        }
        for (int i = 0; i < aiCount; i++) {
            System.out.print("AI #" + (i + 1) + " difficulty (Easy/Medium/Hard): ");
            String diff = sc.nextLine();
            players.add(AI.createAIPlayer("AI" + (i + 1), diff));
        }

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);

        for (;;) {
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            Player cur = session.getCurrentPlayer();
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);

            if (cur.isAI) {
                GameSession.AIResult r = session.runAITurnIfNeeded();
                if (r.passed) System.out.println(cur.name + " PASSED");
                else System.out.println(cur.name + " played: " + r.word + " (+" + r.points + ")");
            } else {
                System.out.print("Enter word (or PASS / QUIT): ");
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
                        System.out.println("Valid! +" + input.trim().length() + " points");
                    } else if (sr == 2) {
                        System.out.println("Already used. 0 points.");
                    } else {
                        System.out.println("Invalid. 0 points.");
                    }
                }
            }

            int ar = session.nextTurn();
            if (ar == 2) {
                Phase1PlayerVsPlayer.announceWinner(session);
                break;
            }
            if (ar == 1) {
                if (session.isShakeUpUsed()) {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
                System.out.print("All active players passed. Shake Up? (Y/N): ");
                String ch = sc.nextLine();
                if (ch != null && ch.trim().equalsIgnoreCase("Y")) {
                    session.performShake();
                } else {
                    Phase1PlayerVsPlayer.announceWinner(session);
                    break;
                }
            }
        }
    }
}
