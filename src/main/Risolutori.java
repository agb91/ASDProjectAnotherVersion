package main;

import Twins.FirstBadTwin;
import Twins.GeneralBadTwin;
import Twins.GoodTwin;
import Twins.SincronizzaFirst;
import Twins.SincronizzaSecond;

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
		long startTime = System.currentTimeMillis();
		int i=2;
		boolean diagnosable = true;
		FirstBadTwin.createBadTwinLevel1(); 
		GoodTwin.createGoodTwin(1);
		while(diagnosable)
		{
			GeneralBadTwin.createBadTwinGeneral(i);
			if(SincronizzaSecond.diagnosableC2C3(i))
			{
				i++;
			}
			else
			{
				GoodTwin.createGoodTwin(i);
				diagnosable = SincronizzaSecond.diagnosableC1(i);
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
		System.out.println("Questo metodo ha rischiesto " + seconds + ";  \n\n\n\n");
	}
	
	public static void third()
	{
		long startTime = System.currentTimeMillis();
		int i=1;
		boolean diagnosable = true;
		while(diagnosable)
		{			
			if(i>1)
			{
				GeneralBadTwin.createBadTwinGeneral(i);
			}
			else
			{
				FirstBadTwin.createBadTwinLevel1();
			}
			if(SincronizzaSecond.diagnosableC2C3(i))
			{
				GoodTwin.createGoodTwin(i);
				SincronizzaFirst.syncro(i);
				SincronizzaSecond.syncroSecond(i);
				i++;
			}
			else
			{
				boolean c1 = false;
				boolean c4 = false;
				if(i>1)
				{
					GoodTwin.createGoodTwin(i);
					SincronizzaSecond.syncroSecond(i);
					if(SincronizzaSecond.diagnosableC1(i))
					{
						c1=true;
					}
					if(!SincronizzaSecond.diagnosableC4(i))
					{
						c4=true;
					}
				}
				else
				{
					GoodTwin.createGoodTwin(1);
					SincronizzaFirst.syncro(1);
					if(SincronizzaFirst.diagnosableC1(1))
					{
						c1=true;
					}
					if(!SincronizzaFirst.diagnosableC4(1))
					{
						c4=true;
					}
				}
				if(c1)
				{
					GoodTwin.createGoodTwin(i);
					SincronizzaFirst.syncro(i);
					SincronizzaSecond.syncroSecond(i);
					i++;
				}
				else
				{
					if(c4)
					{
						System.out.println("non posso diagnosticare il livello " + i);
						System.out.println("********************************************************");
						System.out.println("||    il livello generale di diagnosticabilità è "+(i-1) + "    ||" );
						System.out.println("********************************************************");
						diagnosable = false;
					}
				}
			}

		}
		
		
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		System.out.println("Questo metodo ha rischiesto " + seconds + ";  \n\n\n\n");
	
	}

}
