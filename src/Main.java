import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        File dict = new File("src/wordlist.txt");

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
            else System.out.println("Invalid choice.");
        }

        sc.close();
    }
}
