package main;

import global.Globals;
import utility.Reader;

public class Menu {
	
	private static String question = "Quale metodo di risoluzione vuoi? (1,2,3)";
	private static String errorMessage = "1,2 o 3 non sono ammessi altri valori";
	
	public static void ask(int levelMax)
	{
		Globals.writeTime = 0;
		String answer = Reader.read(question);
		switch (answer) {
        case "1":  Risolutori.first(levelMax);
                 break;
        case "2":  Risolutori.second(levelMax);
                 break;
        case "3":  Risolutori.third(levelMax);
                 break;
        default: errorInput(levelMax);
		}
	}
	
	private static void errorInput(int levelMax)
	{
		System.out.println(errorMessage);
		ask(levelMax);
	}

}
