package Code;

import java.io.FileWriter;
import java.util.Arrays;
import java.util.Collections;

public class Data {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Main_XCS xcs = new Main_XCS();
		System.out.println(xcs.population_Set.size());
		double[] fitness = new double[xcs.population_Set.size()];
		double[] pe = new double[xcs.population_Set.size()];
		
		for(int i=0;i<xcs.population_Set.size();i++){
			fitness[i]=xcs.population_Set.get(i).getFitness();
			pe[i]=xcs.population_Set.get(i).getPrediction_error();
			
		}
		Arrays.sort(fitness);
		Arrays.sort(pe);
		double sum_fitness=0.0;
		double sum_pe=0.0;
		
		//System.out.println(fitness[0]+"   "+fitness[xcs.population_Set.size()-1]);
		for(int j=xcs.population_Set.size()-1;j>xcs.population_Set.size()-10;j--){
			sum_fitness+=fitness[j];
			sum_pe+=pe[j];
		}
		
		sum_fitness/=10;
		sum_pe/=10;
		
		System.out.println(sum_fitness+"    "+sum_pe);
		
		// writing in CSV file
		FileWriter csvfile;
		try{
			csvfile = new FileWriter("dataNew.csv", true);
			//csvfile.append("sum_fitness,sum_pe,fittest,peOfBest");
			csvfile.append("\n");
			csvfile.append(sum_fitness+","+sum_pe+","+fitness[xcs.population_Set.size()-1]+","+pe[xcs.population_Set.size()-1]);
			csvfile.flush();
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

}
