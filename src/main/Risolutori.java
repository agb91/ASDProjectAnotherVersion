package main;

import Twins.FirstBadTwin;
import Twins.GeneralBadTwin;
import Twins.GoodTwin;
import Twins.SincronizzaFirst;

public class Risolutori {
	
	public static void first()
	{
		long startTime = System.currentTimeMillis();
		int level = 2;
		boolean diagnosable = true;
		FirstBadTwin.createBadTwinLevel1(); 
		GoodTwin.createGoodTwin(1);
		diagnosable = SincronizzaFirst.syncro(1);
		while(diagnosable)
		{
			GeneralBadTwin.createBadTwinGeneral(level);
			GoodTwin.createGoodTwin(level);
			diagnosable = SincronizzaFirst.syncro(level);
			if(diagnosable == false)
			{
				System.out.println("********************************************************");
				System.out.println("||    il livello generale di diagnosticabilità è "+(level-1) + "    ||" );
				System.out.println("********************************************************");
			}
			level++;
		}
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		System.out.println("Questo metodo ha rischiesto " + seconds + " milliseconds ;  \n\n\n\n");
	}
	
	public static void second()
	{
		/*
		long startTime = System.currentTimeMillis();
		int i=2;
		boolean diagnosable = true;
		FirstBadTwin.createBadTwinLevel1(); 
		GoodTwin.createGoodTwin(1);
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
				diagnosable = SincronizzaFirst.syncroC1(i);
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
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		System.out.println("Questo metodo ha rischiesto " + seconds + ";  \n\n\n\n");*/
	}
	
	public static void third()
	{
		
	}

}
