package files;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.List;
import model.Player;

public class GameLogManager {
    public GameLogManager() {}

    public static void writeGameLog(
            File file,
            String winnerName,
            String endReason,
            int roundCount,
            List<Player> players,
            boolean shakeUpUsed
    ) {
        if (file == null) return;
        FileWriter fw;
        try {
            fw = new FileWriter(file, true);
        } catch (Exception e) {
            return;
        }
        try {
            fw.write("--- GAME SUMMARY ---\n");
            fw.write("Time: " + LocalDateTime.now() + "\n");
            fw.write("Winner: " + winnerName + "\n");
            fw.write("Reason: " + (endReason == null ? "UNKNOWN" : endReason) + "\n");
            fw.write("Total Rounds: " + roundCount + "\n");
            fw.write("Shake-ups used: " + (shakeUpUsed ? "YES" : "NO") + "\n");

            if (players != null) {
                for (int i = 0; i < players.size(); i++) {
                    Player p = players.get(i);
                    fw.write("Name: " + p.name + " | Score: " + p.totalScore + "\n");
                    fw.write("Words Found: " + p.wordsFound + "\n");
                    fw.write("Auto-passes (Wrong Words): " + p.autoPassWrongCount + "\n");
                    fw.write("Auto-passes (Timer): " + p.autoPassTimerCount + "\n");
                }
            }
            fw.write("\n");
        } catch (Exception e) {
            // ignore
        }
        try { fw.close(); } catch (Exception e) {}
    }
}
