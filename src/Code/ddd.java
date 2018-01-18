package Code;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ddd {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		double x=1;
		double n = (2 * x - 40000) / 20000;
		System.out.println(n);
		double g = 1 / (1 + Math.exp(-n));

		System.out.println(g);
	}

}
