package Code;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;
import javax.swing.JButton;

public class XCS_test {

	// Data Members
	public static final int size_Of_Population = 2000;
	public static final int GA_Threshold = 150;
	public static final int Deletion_Threshold = 20;
	public static final int Subsumption_Threshold = 20;
	public static final double prob_Crossover = 0.5;
	public static final double prob_Mutating = 0.01;
	public static final double fitness_Threshold = 0.1;
	public static final double minimum_num_of_actions_Threshold = 12;
	public static final double dont_care_Threshold = 0.15;
	public static final double aphsylant = 0.5;
	public static final double prediction_error_knot = 0.1;
	public static final double alpha = 0.1;
	public static final double power_factor_nu = 5;
	public static final double beta = 0.02;
	public static int actual_time = 0;

	ArrayList<Classifier> population_Set = new ArrayList<Classifier>(size_Of_Population);
	ArrayList<Classifier> match_Set;
	ArrayList<Classifier> action_Set = new ArrayList<Classifier>();
	HashMap<Classifier, Boolean> executed_actions = new HashMap<>();
	double[][] prediction_array;

	Othello game;
	JButton[][] buttons;

	// Constructor:
	public XCS_test(Othello game) {
		this.game = game;
		buttons = game.get_buttons();
		this.generate_Population_Set();
	}

	// Member functions:
	public void run_experiment() {
	
		// Get the incoming state from environment:
		Environment env = new Environment();

		String input_situation = env.get_input_state(game);
		System.out.println(input_situation);

		// Generate match-set:
		this.generate_Match_Set(input_situation);

		// Generate prediction array:
		this.generate_Prediction_Array();

		// Select Action:
		Two_d_array_indices action_to_execute = this.select_action(env);

		// Generate action set:
		this.generate_action_set(action_to_execute);

		// Execute action:
		env.take_action(game, action_to_execute);
	}

	// Member Functions
	public void generate_Population_Set() {
		ObjectInputStream os = null;
		int count=0;
		try {
			FileInputStream file_stream = new FileInputStream("Classifiers.ser");
			os = new ObjectInputStream(file_stream);
			while (true) {
				Classifier cl;
				cl = (Classifier) os.readObject();
				population_Set.add(cl);
				count++;
				System.out.println(count + "\t" + cl.getCondition().toString() + "\t" + cl.getAction().toString() + "\t"
						+ cl.getPrediction_error());
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
		if (match_Set.isEmpty()) {
			System.out.println("Match set empty");
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
		
		Set<String> e = valid_move.keySet();
		System.out.println("Valid actions:");
		for (String ent : e) {
			System.out.print(ent+" ");
		}
		System.out.println();

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
			Set<String> entry = valid_move.keySet();
			ArrayList<String> list_of_actions = new ArrayList<>();
			for (String ent : entry) {
				list_of_actions.add(ent);
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
}
