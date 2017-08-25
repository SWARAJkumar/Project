package reader;
import java.io.*;

class ClassifierReader{

	public static void main(String[] args) {
		ObjectInputStream os = null;
		int count =1;
		try {
			FileInputStream file_stream = new FileInputStream("Classifiers.ser");

			os = new ObjectInputStream(file_stream);
			while (true) {
				Classifier cl;
				cl = (Classifier) os.readObject();

				if(cl==null)
					break;
				System.out.println(count+"\t"+cl.getCondition().toString()+"\t"+cl.getAction().toString());
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
}