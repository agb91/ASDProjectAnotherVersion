package utility;

import java.util.Scanner;

public class Reader {
	  
	public static String read(String question)
	{
		Scanner scanner = new Scanner(System.in);
		System.out.print(question);
		String answer = scanner.next();
		return answer;
	}
	
	

}
