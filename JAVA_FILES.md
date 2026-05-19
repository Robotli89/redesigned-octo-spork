# Java File Guide

This project is a Boggle game with both a console version and a Swing GUI version. The Java files are split between game rules, player state, AI behavior, and user interfaces.

## `src/GameSession.java`

`GameSession` is the main game engine. It owns the board, players, scores, dictionary, used words, turn order, pass state, shake-board state, hint state, and end-game reason.

It is responsible for:

- Creating and shuffling the Boggle board.
- Loading and searching the dictionary.
- Validating submitted words against the board and dictionary.
- Calculating word scores.
- Tracking wrong guesses, timeouts, passes, quits, and forced winners.
- Running AI turns through `BoggleAI`.
- Deciding whether the game should continue, offer a shake, or end.
- Saving game summaries and paused game state.

Most gameplay rules should live here so the console and GUI versions behave consistently.

## `src/BoggleGUI.java`

`BoggleGUI` is the Swing graphical interface. It builds the menu, setup screens, board display, score panel, word history, timer, and buttons.

It is responsible for:

- Letting the user choose game phase, players, AI difficulty, timer, dictionary, board color, and save file.
- Displaying the board and current scores.
- Handling button clicks for Submit, Pass, Shake Board, Quit, Hint, and Save Game.
- Showing accepted AI and human words in the word history.
- Showing wrong human words in the word history.
- Moving from human turns to AI turns and ending the game when `GameSession` says the game is over.

This file should mostly handle screen behavior. It calls `GameSession` for actual game decisions.

## `src/BoggleGame.java`

`BoggleGame` is the console version of the game and contains the `main` method. It prints menus and prompts in the terminal, then calls the shared game engine.

It is responsible for:

- Starting the application from the command line.
- Showing the phase menu and rules.
- Reading user input with `Scanner`.
- Running console versions of Player vs Player, Player vs AI, Multiplayer, Multiplayer + AI, and AI vs AI.
- Printing boards, scores, AI moves, hints, pass prompts, shake prompts, and winners.
- Finding the dictionary file.

This file is separate from `BoggleGUI.java` so the same game can run without a graphical window.

## `src/BoggleAI.java`

`BoggleAI` contains the helper logic for AI players.

It is responsible for:

- Searching the board for all valid unused words.
- Using depth-first search to walk adjacent board letters.
- Choosing an AI word based on difficulty.
- Creating AI `Player` objects.

Difficulty behavior:

- Easy chooses only 3- or 4-letter words.
- Medium chooses randomly from the stronger half of available words.
- Hard chooses the longest available word.

## `src/Player.java`

`Player` stores all state for one player.

It tracks:

- Player name.
- Total score.
- Whether the player has passed or quit.
- Wrong-guess count.
- Timeout count.
- Auto-pass counts.
- Words found by the player.
- Whether the player is an AI.
- AI difficulty.

It also has small helper methods for adding score, adding found words, incrementing/resetting counters, and clearing round-only state after a board shake.

## `src/AIResult.java`

`AIResult` is a small result object returned by `GameSession.runAITurnIfNeeded()`.

It tells the caller:

- Whether the current turn was actually an AI turn.
- Whether the AI passed.
- Which word the AI played.
- How many points the word scored.

The static helper methods create the three expected outcomes: not an AI turn, AI passed, or AI played a word.

