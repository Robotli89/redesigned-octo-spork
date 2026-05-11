import java.io.File;
import java.util.Scanner;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        File dict = findDictionaryFile();
        if (dict == null) {
            System.out.println("Dictionary file not found (wordlist.txt).");
            System.out.println("Result: all words will be INVALID and AI will always pass.");
            dict = new File("wordlist.txt");
        }

        System.out.println("Choose GUI mode or text mode");
        System.out.println("1) GUI mode");
        System.out.println("2) Text mode");
        System.out.print("Choose: ");
        String modeChoice = sc.nextLine();

        if (modeChoice != null && modeChoice.trim().equals("1")) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    new gui.BoggleGUI();
                }
            });
            return;
        } else if (modeChoice == null || !modeChoice.trim().equals("2")) {
            System.out.println("Invalid choice.");
            sc.close();
            return;
        }

        for (;;) {
            System.out.println();
            System.out.println("Boggle Menu");
            System.out.println("1) Player vs Player");
            System.out.println("2) Player vs AI");
            System.out.println("3) Multiplayer");
            System.out.println("4) Multiplayer + AI");
            System.out.println("5) AI vs AI");
            System.out.println("0) Exit");
            System.out.print("Choose: ");
            String ch = sc.nextLine();
            if (ch == null) ch = "";
            ch = ch.trim();

            if (ch.equals("0")) break;
            if (ch.equals("1")) game.Phase1PlayerVsPlayer.run(sc, dict);
            else if (ch.equals("2")) game.Phase2PlayerVsAI.run(sc, dict);
            else if (ch.equals("3")) game.Phase3Multiplayer.run(sc, dict);
            else if (ch.equals("4")) game.Phase4MultiplayerAI.run(sc, dict);
            else if (ch.equals("5")) game.Phase5AIvsAI.run(sc, dict);
            else {
                System.out.println("Invalid choice.");
                continue;
            }

            System.out.print("Another round? (Y/N): ");
            String again = sc.nextLine();
            if (again == null) again = "";
            again = again.trim();
            if (!again.equalsIgnoreCase("Y")) break;
        }

        sc.close();
    }

    public static File findDictionaryFile() {
        String[] candidates = new String[] {
                "src/wordlist.txt",
                "wordlist.txt",
                "BoggleAssignment/redesigned-octo-spork/src/wordlist.txt",
                "BoggleAssignment/wordlist.txt",
                "redesigned-octo-spork/src/wordlist.txt"
        };
        for (int i = 0; i < candidates.length; i++) {
            File f = new File(candidates[i]);
            if (f.exists()) return f;
        }
        return null;
    }
}
