package main;

import utility.Reader;

public class Menu {
	
	private static String question = "Quale metodo di risoluzione vuoi? (1,2,3)";
	private static String errorMessage = "1,2 o 3 non sono ammessi altri valori";
	
	public static void ask()
	{
		String answer = Reader.read(question);
		switch (answer) {
        case "1":  Risolutori.first();
                 break;
        case "2":  Risolutori.second();
                 break;
        case "3":  Risolutori.third();
                 break;
        default: errorInput();
		}
	}
	
	private static void errorInput()
	{
		System.out.println(errorMessage);
		ask();
	}

}
