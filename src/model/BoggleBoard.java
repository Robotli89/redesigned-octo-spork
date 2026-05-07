package model;
import java.util.Random;

public class BoggleBoard {
  public static final Random random = new Random();
  
  public static final String[] dice = {
    "AAAFRS", "AEEGMU", "CEIILT", "DHHNOT", "FIPRSY",
    "AAEEEE", "AEGMNN", "CEILPT", "DHLNOR", "GORRVW",
    "AAFIRS", "AFIRSY", "CEIPST", "EIIITT", "HIPRRY",
    "ADENNN", "BJKQXZ", "DDLNOR", "EMOTTT", "NOOTUW",
    "AEEEEM", "CCNSTW", "DHHLOR", "ENSSSU", "OOOTTU"
  };
  public static final int size = 5;

  public static void main(String[] args) {
    char[][] board = generateBoard();
    printBoard(board);
  }

  public static char[][] generateBoard() {
    char[] letters = new char[dice.length];
    for (int i = 0;i < dice.length;i++) {
      int face = random.nextInt(dice[i].length());
      letters[i] = dice[i].charAt(face);
    }
    shuffleCharArray(letters);
    char[][] board = new char[size][size];
    int k = 0;
    for (int r = 0; r < size; r++) {
      for (int c = 0; c < size; c++) {
        board[r][c] = letters[k++];
      }
    }
    return board;
  }

  public static void shuffleCharArray(char[] arr) {
    for (int i = arr.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      char tmp = arr[i];
      arr[i] = arr[j];
      arr[j] = tmp;
    }
  }

  public static void printBoard(char[][] board) {
    System.out.println("\nBoggle Board:");
    for (char[] row : board) {
      for (char ch : row) {
        System.out.print(ch + " ");
      }
      System.out.println();
    }
    System.out.println();
  }
}
