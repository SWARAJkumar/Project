package Code;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import javax.swing.*;
import java.awt.*;

public class Othello_Client {

	public static void main(String[] args) {
		int num_games = 2000;
		int n=num_games;
		double black_wins = 0;
		double white_wins = 0;
		double ties = 0;
		double black_percentage_wins = 0;
		double white_percentage_wins = 0;
		double tie_percentage = 0;
		double moving_black_percentage_wins = 0;
		double moving_white_percentage_wins = 0;
		double moving_tie_percentage = 0;
		FileWriter csvfile= null;

		try{
			
			csvfile = new FileWriter("gameStatus.csv");
			csvfile.append("gameNo,result,blackPercentage,whitePercentage,tiePercentage,numerosity,avg_fitness,avg_prediction_error,no_macro");
			csvfile.append("\n"); 
			
			while (n-- > 0) {
				//System.out.println("Game Number: " + n );
				Othello game = new Othello();
				Main_XCS xcs_agent = new Main_XCS(game);
				Minimax m = new Minimax();
				Environment e = new Environment();
				boolean is_complete = false;

				while (true) {

					if (game.isBlackTurn) {
						xcs_agent.run_experiment();
					} else {

						// try {
						// TimeUnit.SECONDS.sleep(2);
						// } catch (InterruptedException e) {
						// e.printStackTrace();
						// }
						// System.out.println("Hi bro");

						m.minimax(game, 4, false, -9999, 9999);
						//					System.out.println(m.minimax(game, 2, false, -9999, 9999));
						Two_d_array_indices action = m.best_move_stack.pop();
						m.best_score_stack.pop();
					//					System.out.println(action + " " +m.best_score_stack.pop());
						e.take_action(game, action);

					}

					is_complete = game.is_game_complete();
					String winner= "";
					if (is_complete) {
						game.declareWinner();
						if (game.get_winner().equals("Black")) {
							black_wins++;
							winner="black";
						} else if (game.get_winner().equals("White")) {
							white_wins++;
							winner="white";
						  } else {
						ties++;
						winner="tie";
						}
						xcs_agent.give_delayed_reward();
						xcs_agent.appy_GA();
						xcs_agent.write_population_set_to_file();
						
						moving_black_percentage_wins=((black_wins/(num_games-n))*100);
						moving_white_percentage_wins=((white_wins/(num_games-n))*100);
						moving_tie_percentage=((ties/(num_games-n))*100);
						int sum_numerosity=xcs_agent.calculate_total_numerosity();
						int no_of_macro_classifier=xcs_agent.no_of_macro_classifier();
						double[] avg_values= xcs_agent.calculate_values_to_be_stored();
						
						System.out.println(num_games-n+","+winner+","+moving_black_percentage_wins+","+moving_white_percentage_wins+","+moving_tie_percentage);
						System.out.println("Numerosity : "+sum_numerosity);
						System.out.println("No of macro classifier : "+no_of_macro_classifier);
						System.out.println("--------------------------------------------------------------------------------");
						csvfile.append(num_games-n+","+winner+","+moving_black_percentage_wins+","+moving_white_percentage_wins
								+","+moving_tie_percentage+","+sum_numerosity+","+avg_values[0]+","+avg_values[1]+","+
								no_of_macro_classifier);
						csvfile.append("\n");
						
						if(n%100==0){
							Mail.send("UPDATE", "GA ran:"+Run_GA.GA_count+"\nWinning Percentage : "+moving_black_percentage_wins+"\n tie:"+moving_tie_percentage+"\nremoved by hashh count"+InsertionDeletion.removed_because_of_hashes);
						}
						break;
					}
				}
			
			}
		}catch(Exception exception){exception.printStackTrace();}finally {
	        try {
	           csvfile.flush();
	           csvfile.close();
	              } catch (IOException e) {
	            	  e.printStackTrace();
	              }
		}
		
		black_percentage_wins=(black_wins/num_games)*100;
		white_percentage_wins=(white_wins/num_games)*100;
		tie_percentage=(double)(ties/num_games)*100;

		try {
			FileWriter out_actual_time = new FileWriter("stats.txt");
			out_actual_time.write("Black % wins ="+black_percentage_wins+"\n"+"White % wins ="+white_percentage_wins+"\n"+"Tie % ="+tie_percentage+"\n");
			out_actual_time.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Mail.send("XCS: ended","black wins : "+black_percentage_wins+"\nwhite wins : "+white_percentage_wins+"\ntie : "+tie_percentage+"\nCovering"+Main_XCS.covering_count);
	}
}
