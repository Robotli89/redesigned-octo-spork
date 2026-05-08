package game;

import model.AI;
import model.BoggleBoard;
import model.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Phase2PlayerVsAI {
    // Called when human becomes "passed" (manual PASS or auto-pass after 2 wrong).
    // return true if game should end immediately.
    public static boolean handleHumanPassed(Scanner sc, GameSession session) {
        // Offer shake right away (only if shake not used yet)
        if (!session.isShakeUpUsed()) {
            System.out.print("Shake Up? (Y/N): ");
            String ch = sc.nextLine();
            if (ch == null) ch = "";
            ch = ch.trim();
            if (ch.equalsIgnoreCase("Y")) {
                session.performShake();
                return false;
            }
        }

        // target score = 0: if someone passes and shake already used (or user said no shake),
        // the remaining player continues until score > passer. If no words and same score => tie.
        if (session.targetScore == 0) {
            Player passer = session.players.get(0); // human is always index 0 here
            Player other = session.players.get(1);  // AI is always index 1 here
            int passScore = passer.totalScore;

            int toOther = session.nextTurn();
            if (toOther == 2) return true;

            for (;;) {
                if (other.totalScore > passScore) break;

                if (session.getCurrentPlayer().isAI) {
                    GameSession.AIResult rr = session.runAITurnIfNeeded();
                    if (rr.passed) break;
                } else {
                    break;
                }

                int adv = session.nextTurn();
                if (adv == 2) break;

                // skip the passer (already passed) so other can keep trying
                if (!session.getCurrentPlayer().isAI) {
                    int skip = session.nextTurn();
                    if (skip == 2) break;
                }
            }

            return true;
        }

        return true;
    }

    public static void run(Scanner sc, File dictionaryFile) {
        Phase1PlayerVsPlayer.showRules();

        System.out.print("Your name: ");
        String human = sc.nextLine();

        System.out.print("AI difficulty (Easy/Medium/Hard): ");
        String diff = sc.nextLine();

        int minLen = Phase1PlayerVsPlayer.readInt(sc, "Minimum word length (>=3): ", 3);
        int target = Phase1PlayerVsPlayer.readIntAllowZero(sc, "Target score (0 = no target): ");

        List<Player> players = new ArrayList<Player>();
        players.add(new Player(human));
        players.add(AI.createAIPlayer("AI", diff));

        GameSession session = new GameSession(players, minLen, target, dictionaryFile);

        for (;;) {
            Player cur = session.getCurrentPlayer();
            System.out.println();
            BoggleBoard.printBoard(session.getBoard());
            System.out.println("Round: " + session.getCurrentRound());
            System.out.println("Turn: " + cur.name + "  Score=" + cur.totalScore);

            if (cur.isAI) {
                GameSession.AIResult r = session.runAITurnIfNeeded();
                if (r.passed) {
                    System.out.println("AI PASSED");
                } else {
                    System.out.println("AI played: " + r.word + " (+" + r.points + ")");
                }
            } else {
                System.out.print("Enter word (or PASS / QUIT): ");
                String input = sc.nextLine();
                if (input == null) input = "";
                input = input.trim();
                if (input.equalsIgnoreCase("QUIT")) {
                    session.quit();
                    session.saveIfPvAIQuit(new File(human + "_save.txt"));
                } else if (input.equalsIgnoreCase("PASS")) {
                    session.pass();
                    boolean endNow = handleHumanPassed(sc, session);
                    if (endNow) {
                        Phase1PlayerVsPlayer.announceWinner(session);
                        break;
                    }
                    continue;
                } else {
                    int sr = session.submitWord(input);
                    if (sr == 1) {
                        System.out.println("Valid! +" + input.trim().length() + " points");
                    } else if (sr == 2) {
                        System.out.println("Already used. 0 points.");
                    } else {
                        System.out.println("Invalid. 0 points.");
                    }

                    // If this invalid guess triggered auto-pass (2 wrong), offer shake right away.
                    if (session.players.get(0).passed) {
                        boolean endNow = handleHumanPassed(sc, session);
                        if (endNow) {
                            Phase1PlayerVsPlayer.announceWinner(session);
                            break;
                        }
                        continue;
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
                System.out.print("All passed. Shake Up? (Y/N): ");
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
