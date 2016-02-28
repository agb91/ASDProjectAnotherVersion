package main;

import Twins.FirstBadTwin;
import Twins.GeneralBadTwin;
import Twins.GoodTwin;
import Twins.Sincronizza;

public class Risolutori {
	
	public static void first()
	{
		int level = 2;
		boolean diagnosable = true;
		FirstBadTwin.createBadTwinLevel1(); 
		GoodTwin.createGoodTwin(1);
		diagnosable = Sincronizza.syncro(1);
		while(diagnosable)
		{
			GeneralBadTwin.createBadTwinGeneral(level);
			GoodTwin.createGoodTwin(level);
			diagnosable = Sincronizza.syncro(level);
			if(diagnosable == false)
			{
				System.out.println("********************************************************");
				System.out.println("||    il livello generale di diagnosticabilità è "+(level-1) + "    ||" );
				System.out.println("********************************************************");
			}
			level++;
		}

	}
	
	public static void second()
	{
		int i=2;
		boolean diagnosable = true;
		FirstBadTwin.createBadTwinLevel1(); 
		while(diagnosable)
		{
			GeneralBadTwin.createBadTwinGeneral(i);
			if(GeneralBadTwin.checkC2C3(i))
			{
				i++;
			}
			else
			{
				GoodTwin.createGoodTwin(i);
				diagnosable = Sincronizza.syncroC1(i);
				if(diagnosable)
				{
					i++;
				}
				else
				{
					System.out.println("non posso diagnosticare il livello " + i);
					System.out.println("********************************************************");
					System.out.println("||    il livello generale di diagnosticabilità è "+(i-1) + "    ||" );
					System.out.println("********************************************************");
				}
			}
		}
	}
	
	public static void third()
	{
		
	}

}
