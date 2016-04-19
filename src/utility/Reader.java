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
	
	public static int readInt(String question)
	{
		int answerInt = 0;
		try{
			Scanner scanner = new Scanner(System.in);
			System.out.print(question);
			String answer = scanner.next();
			answerInt = Integer.valueOf(answer);
		}catch( NumberFormatException e)
		{
			System.out.println("un numero maggiore di zero...");
			answerInt = readInt(question);
		};
		if(answerInt==0)
		{
			System.out.println("un numero maggiore di zero...");
			answerInt = readInt(question);
		}
		return answerInt;
	}
	
	

}
