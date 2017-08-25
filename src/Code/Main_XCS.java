package Code;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.JButton;

public class Main_XCS {
	static int covering_count=0;
	// Data Members
	public static final int size_Of_Population = 3000;
	public static final int GA_Threshold = 100;
	public static final int Deletion_Threshold =20;// change it to 20 if not working
	public static final int Subsumption_Threshold = 20;
	public static final double prob_Crossover = 0.5;
	public static final double prob_Mutating = 0.01;
	public static final double fitness_Threshold = 0.01;
	public static final double minimum_num_of_actions_Threshold = 12;
	public static final double dont_care_Threshold = 0.15;
	public static final double aphsylant = 0.5;
	public static final double prediction_error_knot = 0.1; // Initially 0.01
	public static final double alpha = 0.1;
	public static final double power_factor_nu = 5;
	public static final double beta = 0.02; // Initially 0.2
	public static int actual_time = 0;

	ArrayList<Classifier> population_Set = new ArrayList<Classifier>(size_Of_Population);
	ArrayList<Classifier> match_Set;
	ArrayList<Classifier> action_Set = new ArrayList<Classifier>();
	HashMap<Classifier, Boolean> executed_actions = new HashMap<>();
	static HashMap<Classifier, Boolean> executed_actions_for_GA = new HashMap<>();
	static LinkedList<pair> list_action_sets;
	double[][] prediction_array;

	Othello game;
	JButton[][] buttons;

	// Constructor:
	public Main_XCS(Othello game) {
		// actual_time++;
		this.game = game;
		buttons = game.get_buttons();
		list_action_sets = new LinkedList<>();

		//System.out.println("reading serializble");
		// Generate population-set:
		this.generate_Population_Set();
		
	}

	// Input state and actions set pair class (Used in GA):
	private class pair {
		String input;
		ArrayList<Classifier> actions_Set;

		public pair(String input, ArrayList<Classifier> actions_Set) {
			this.input = input;
			this.actions_Set = actions_Set;
		}
	}

	// Member functions:
	public void run_experiment() {
		// Get the incoming state from environment:
		Environment env = new Environment();
		String input_situation = env.get_input_state(game);
//		System.out.println(input_situation);

		// Generate match-set:
		this.generate_Match_Set(input_situation);

		// Generate prediction array:
		this.generate_Prediction_Array();

		// Select Action:
		Two_d_array_indices action_to_execute = this.select_action(env);
		// System.out.println(action_to_execute.i + ", " + action_to_execute.j);

		// Generate action set:
		this.generate_action_set(action_to_execute);

		// Execute action:
		env.take_action(game, action_to_execute);

		// Update parameters:
		double sigma_numerosity = 0;
		for (Classifier cl : action_Set) {
			sigma_numerosity += cl.getNumerosity();
		}

		for (Classifier cl : action_Set) {
			cl.setExperience(cl.getExperience() + 1);

			double AScl = cl.getAction_set_size();

			if (cl.getExperience() < 1 / beta) {
				AScl += (sigma_numerosity - AScl) / cl.getExperience();
			} else {
				AScl += beta * (sigma_numerosity - AScl);
			}
			
			cl.setAction_set_size(AScl);
		}

		// Giving reward to classifiers:
		int row = action_to_execute.i;
		int col = action_to_execute.j;
		int reward = 0;

		// Immediate reward:
		if ((row == 0 && col == 0) || (row == Othello.BOARD_SIZE - 1 && col == 0)
				|| (col == Othello.BOARD_SIZE - 1 && row == 0)
				|| (row == Othello.BOARD_SIZE - 1 && col == Othello.BOARD_SIZE - 1)) {

			reward = 1;
			this.update_set_action_set(reward);
		}

		Subsumption.do_action_set_subsumption(action_Set, population_Set);

		create_action_set_list_for_GA(env);
	}

	// Member Functions
	public void generate_Population_Set() {
		ObjectInputStream os = null;
		int count =1;
		int count_fit=0;
		try {
			FileInputStream file_stream = new FileInputStream("classifier5.ser");
			File time_file = new File("actual_time.txt");
			FileReader file_reader = new FileReader(time_file);
			BufferedReader bf = new BufferedReader(file_reader);

			String num = null;
			while ((num = bf.readLine()) != null) {
				actual_time = Integer.parseInt(num) + 1;
			}
			bf.close();
			os = new ObjectInputStream(file_stream);
			while (true) {
				Classifier cl;
				cl = (Classifier) os.readObject();

				if(cl==null)
					break;
				System.out.println(count+"\t"+cl.getCondition().toString()+"\t"+cl.getFitness()+","+count_fit);
				if(cl.getFitness()==0.001)
					count_fit++;
				count++;
				population_Set.add(cl);
			}
		} catch (FileNotFoundException ex) {
			return;
		} catch (IOException ex) {
			return;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	


	public void generate_Match_Set(String input_condition) {
		match_Set = new ArrayList<>();

		if (population_Set.size() == 0) {
			generate_Covering_Classifer(input_condition, new HashMap<>());
		}

		for (int i = 0; i < population_Set.size(); i++) {
			String population_Condition = population_Set.get(i).getCondition().toString();
			boolean isEqual = true;
			for (int j = 0; j < population_Condition.length(); j++) {
				if ((input_condition.charAt(j) == '1' && population_Condition.charAt(j) == '0')
						|| (input_condition.charAt(j) == '0' && population_Condition.charAt(j) == '1')) {
					isEqual = false;
					break;
				}
			}
			if (isEqual) {
				match_Set.add(population_Set.get(i));
			}
		}

		HashMap<String, Boolean> map = new HashMap<>();
		for (int i = 0; i < match_Set.size(); i++) {
			String str = "" + match_Set.get(i).getAction().i + match_Set.get(i).getAction().j;
			map.put(str, true);
		}

		if (map.size() < minimum_num_of_actions_Threshold) {
			generate_Covering_Classifer(input_condition, map);
			InsertionDeletion.delete_from_population(population_Set);
		}
	}

	public void generate_Covering_Classifer(String input_condition, HashMap<String, Boolean> map) {
		covering_count++;
		StringBuilder new_rule_condition = new StringBuilder();
		for (int i = 0; i < input_condition.length(); i++) {
			double rand_number = Math.random();
			if (rand_number < dont_care_Threshold) {
				new_rule_condition.append("#");
			} else {
				new_rule_condition.append(input_condition.charAt(i));
			}
		}
		for (int i = 0; i < buttons.length; i++) {
			for (int j = 0; j < buttons[i].length; j++) {
				if ((i == (Othello.BOARD_SIZE / 2 - 1) && j == (Othello.BOARD_SIZE / 2 - 1))
						|| (i == (Othello.BOARD_SIZE / 2) && j == (Othello.BOARD_SIZE / 2))
						|| (i == (Othello.BOARD_SIZE / 2 - 1) && j == (Othello.BOARD_SIZE / 2))
						|| (i == (Othello.BOARD_SIZE / 2) && j == (Othello.BOARD_SIZE / 2 - 1))) {
					continue;
				}
				if (map.containsKey("" + i + j)) {
					continue;
				}
				Classifier new_classifier = new Classifier(new_rule_condition, new Two_d_array_indices(i, j));
				population_Set.add(new_classifier);
				//System.out.println("coevering applied");
				match_Set.add(new_classifier);
			}
		}

	}

	public void generate_Prediction_Array() {
		prediction_array = new double[Othello.BOARD_SIZE][Othello.BOARD_SIZE];
		double[][] fitness_sum_array = new double[Othello.BOARD_SIZE][Othello.BOARD_SIZE];

		for (Classifier cl : match_Set) {
			int i = cl.getAction().i;
			int j = cl.getAction().j;
			prediction_array[i][j] += cl.getPrediction() * cl.getFitness();
			fitness_sum_array[i][j] += cl.getFitness();
		}

		for (int i = 0; i < fitness_sum_array.length; i++) {
			for (int j = 0; j < fitness_sum_array.length; j++) {
				if (fitness_sum_array[i][j] != 0) {
					prediction_array[i][j] /= fitness_sum_array[i][j];
				}
			}
		}
	}

	public Two_d_array_indices select_action(Environment env) {
		HashMap<String, Boolean> valid_move = env.get_pink();
		double rand_num = Math.random();
		int retvali = 0, retvalj = 0;
		
		// Exploitation
		if (rand_num < aphsylant) {
			double max = Integer.MIN_VALUE;
			for (int i = 0; i < prediction_array.length; i++) {
				for (int j = 0; j < prediction_array[i].length; j++) {
					if (prediction_array[i][j] >= max && valid_move.containsKey("" + i + j)) {
						max = prediction_array[i][j];
						retvali = i;
						retvalj = j;
					}
				}
			}
		}

		// Exploration
		else {
			Set<Entry<String, Boolean>> entry = valid_move.entrySet();
			ArrayList<String> list_of_actions = new ArrayList<>();
			for (Entry<String, Boolean> ent : entry) {
				list_of_actions.add(ent.getKey());
			}
			do {
				int rand_index = (int) (Math.random() * list_of_actions.size());
				retvali = list_of_actions.get(rand_index).charAt(0) - '0';
				retvalj = list_of_actions.get(rand_index).charAt(1) - '0';
			} while (prediction_array[retvali][retvalj] == 0);
		}

		return new Two_d_array_indices(retvali, retvalj);
	}

	public void generate_action_set(Two_d_array_indices action) {
		action_Set.clear();

		for (Classifier cl : match_Set) {
			if (cl.getAction().i == action.i && cl.getAction().j == action.j) {
				executed_actions.put(cl, true);
				action_Set.add(cl);
			}
		}
	}

	public void update_fitness() {
		Set<Entry<Classifier, Boolean>> allEntries = executed_actions.entrySet();
		ArrayList<Double> accuracy = new ArrayList<>();
		double accuracy_sum = 0;

		for (Entry<Classifier, Boolean> entry : allEntries) {
			double pred_error = entry.getKey().getPrediction_error();
			if (pred_error < prediction_error_knot) {
				accuracy.add(1.0);
			} else {
				accuracy.add(alpha * (Math.pow(pred_error / prediction_error_knot, -power_factor_nu)));
			}
			accuracy_sum += accuracy.get(accuracy.size() - 1) * entry.getKey().getNumerosity();
		}

		int k = 0;
		for (Entry<Classifier, Boolean> entry : allEntries) {
			double fitness = entry.getKey().getFitness();
			fitness = fitness + beta * (accuracy.get(k++) * entry.getKey().getNumerosity() / accuracy_sum - fitness);
			entry.getKey().setFitness(fitness);
		}
	}

	public void update_fitness_action_set() {
		ArrayList<Double> accuracy = new ArrayList<>();
		double accuracy_sum = 0;

		for (Classifier cl : action_Set) {
			double pred_error = cl.getPrediction_error();
			if (pred_error < prediction_error_knot) {
				accuracy.add(1.0);
			} else {
				accuracy.add(alpha * (Math.pow(pred_error / prediction_error_knot, -power_factor_nu)));
			}
			accuracy_sum += accuracy.get(accuracy.size() - 1) * cl.getNumerosity();
		}

		int k = 0;
		for (Classifier cl : action_Set) {
			double fitness = cl.getFitness();
			fitness = fitness + beta * (accuracy.get(k++) * cl.getNumerosity() / accuracy_sum - fitness);
			cl.setFitness(fitness);
		}
	}

	public void give_delayed_reward() {
		int reward = 0;
		// Winning the game
		if (game.get_winner() == "White") {
			reward = -1;
		}
		// Losing the game
		else if (game.get_winner() == "Black") {
			reward = +1;
		}
		this.update_set(reward);
	}

	private void update_set(int reward) {
		//System.out.println("I am here");
		Set<Entry<Classifier, Boolean>> allEntries = executed_actions.entrySet();

		for (Entry<Classifier, Boolean> entry : allEntries) {
			Classifier cl = entry.getKey();

			// Update prediction:
			cl.setPrediction(cl.getPrediction() + reward);

			// Update prediction error:
			if (cl.getExperience() < 1 / beta) {
				double error = Math.abs(cl.getPrediction() - reward) / cl.getExperience();
				//System.out.println("1Error:" + error);
				cl.setPrediction_error(error);
			} else {
				double error = Math.abs(cl.getPrediction() - reward) * beta;
				cl.setPrediction_error(error);
				//System.out.println("2Error:" + error);
			}
		}

		this.update_fitness();
	}

	private void update_set_action_set(int reward) {

		for (Classifier cl : action_Set) {

			// Update prediction:
			cl.setPrediction(cl.getPrediction() + reward);

			// Update prediction error:
			if (cl.getExperience() < 1 / beta) {
				double error = Math.abs(cl.getPrediction() - reward) / cl.getExperience();
				cl.setPrediction_error(error);
				//System.out.println("1Error:" + error);
			} else {
				double error = Math.abs(cl.getPrediction() - reward) * beta;
				cl.setPrediction_error(error);
				//System.out.println("2Error:" + error);
			}
		}

		this.update_fitness_action_set();
	}

	public void write_population_set_to_file() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream("Classifiers.ser", false));
			for (Classifier cl : this.population_Set) {
				os.writeObject(cl);
			}
			
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			FileWriter out_actual_time = new FileWriter("actual_time.txt");
			out_actual_time.write("" + actual_time);
			out_actual_time.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void appy_GA() {
		//System.out.println("GA Run");
		for (pair p : list_action_sets) {
			Run_GA.running_GA(p.actions_Set, population_Set, p.input, actual_time);
		}
	}

	public void create_action_set_list_for_GA(Environment env) {
		ArrayList<Classifier> action_Set_to_store = new ArrayList<>(action_Set);
		list_action_sets.add(new pair(env.get_input_state(game), action_Set_to_store));
	}
	
	public double[] calculate_values_to_be_stored(){
		
		double avg_fitness=0.0;
		double avg_prediction_error=0.0;
		
		int no_of_macro_classifier=0;

		for(Classifier cl:population_Set){
			avg_fitness+= cl.getFitness();
			avg_prediction_error+=cl.getPrediction_error();
			no_of_macro_classifier++;
		}
		avg_fitness= avg_fitness/no_of_macro_classifier;
		avg_prediction_error= avg_prediction_error/no_of_macro_classifier;
		
		double[] values= {avg_fitness,avg_prediction_error};
		return values;
	}
	
	public int calculate_total_numerosity(){
		int sum_numerosity = 0;
		
		for (Classifier ptr : population_Set) {
			sum_numerosity += ptr.getNumerosity();
		}
		return sum_numerosity;
	}
	
	public int no_of_macro_classifier(){
		return population_Set.size();
	}
}
