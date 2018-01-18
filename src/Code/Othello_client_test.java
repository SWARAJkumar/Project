package Code;

import java.util.concurrent.TimeUnit;

public class Othello_client_test {

	public static void main(String[] args) {
		int num_games = 20;
		int n = num_games;
		double black_wins = 0;
		double white_wins = 0;
		double ties = 0;
		double moving_black_percentage_wins = 0;
		double moving_white_percentage_wins = 0;
		double moving_tie_percentage = 0;
		

		while (num_games-- > 0) {
			System.out.println("Game Number: " + (n - num_games));
			Othello game = new Othello();
			XCS_test xcs_agent = new XCS_test(game);
			Minimax m = new Minimax();
			Environment e = new Environment();
			boolean is_complete = false;

			while (true) {

				if (game.isBlackTurn) {
					xcs_agent.run_experiment();
				} else {
					try {
						TimeUnit.SECONDS.sleep(2);
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					}
					System.out.println("Hi bro");
				}

				is_complete = game.is_game_complete();

				if (is_complete) {
					String winner = "";
					game.declareWinner();
					if (game.get_winner().equals("Black")) {
						black_wins++;
						winner = "black";
					} else if (game.get_winner().equals("White")) {
						white_wins++;
						winner = "white";
					} else {
						ties++;
						winner = "tie";
					}

					moving_black_percentage_wins = ((black_wins / (n - num_games)) * 100);
					moving_white_percentage_wins = ((white_wins / (n - num_games)) * 100);
					moving_tie_percentage = ((ties / (n - num_games)) * 100);

					System.out.println(n - num_games + "," + winner + "," + moving_black_percentage_wins + ","
							+ moving_white_percentage_wins + "," + moving_tie_percentage);
					break;
				}
			}
		}
	}
}
