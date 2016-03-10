package main;

import Twins.FirstBadTwin;
import Twins.GeneralBadTwin;
import Twins.GoodTwin;
import Twins.SincronizzaFirst;
import Twins.SincronizzaSecond;
import global.Globals;

public class Risolutori {
	
	public static void first()
	{
		long startTime = System.currentTimeMillis();
		int level = 1;
		boolean ancora = true;
		while(ancora)
		{
			if(level==1)
			{
				FirstBadTwin.createBadTwinLevel1();
			}
			else
			{
				GeneralBadTwin.createBadTwinGeneral(level);
			}
			GoodTwin.createGoodTwin(level);
			SincronizzaFirst.syncro(level);
			if(SincronizzaFirst.checkC4(level))
			{
				System.out.println("********************************************************");
				System.out.println("||    il livello generale di diagnosticabilità è "+(level-1) + "    ||" );
				System.out.println("********************************************************");
				ancora = false;
			}
			level++;
		}
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime) - Globals.writeTime;
		System.out.println("Questo metodo ha rischiesto " + seconds + " milliseconds ;  \n\n\n\n");
	}
	
	public static void second()
	{
		long startTime = System.currentTimeMillis();
		int i=1;
		boolean ancora = true;
		while(ancora)
		{
			if(i==1)
			{
				FirstBadTwin.createBadTwinLevel1();
			}
			else
			{
				GeneralBadTwin.createBadTwinGeneral(i);
			}
			if(SincronizzaSecond.checkC2C3(i))
			{
				GoodTwin.createGoodTwin(i);
				i++;
			}
			else
			{
				GoodTwin.createGoodTwin(i);
				SincronizzaFirst.syncro(i);;
				
				if(SincronizzaFirst.checkC1(i))
				{
					i++;
				}
				else
				{
					if(SincronizzaFirst.checkC4(i))
					{
						System.out.println("non posso diagnosticare il livello " + i);
						System.out.println("********************************************************");
						System.out.println("||    il livello generale di diagnosticabilità è "+(i-1) + "    ||" );
						System.out.println("********************************************************");
						ancora = false;
					}
					i++;					
				}
			}
		}
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime) - Globals.writeTime;
		System.out.println("Questo metodo ha rischiesto " + seconds + ";  \n\n\n\n");
	}
	
	/*public static void second()
	{
		long startTime = System.currentTimeMillis();
		int i=1;
		boolean ancora = true;
		while(ancora)
		{
			if(i==1)
			{
				FirstBadTwin.createBadTwinLevel1();
			}
			else
			{
				GeneralBadTwin.createBadTwinGeneral(i);
			}
			if(SincronizzaSecond.checkC2C3(i))
			{
				GoodTwin.createGoodTwin(i);
				i++;
			}
			else
			{
				GoodTwin.createGoodTwin(i);
				SincronizzaSecond.syncroSecond(i);
				
				if(SincronizzaSecond.checkC1(i))
				{
					i++;
				}
				else
				{
					if(SincronizzaSecond.checkC4(i))
					{
						System.out.println("non posso diagnosticare il livello " + i);
						System.out.println("********************************************************");
						System.out.println("||    il livello generale di diagnosticabilità è "+(i-1) + "    ||" );
						System.out.println("********************************************************");
						ancora = false;
					}
					i++;					
				}
			}
		}
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		System.out.println("Questo metodo ha rischiesto " + seconds + ";  \n\n\n\n");
	}
	*/
	public static void third()
	{
		long startTime = System.currentTimeMillis();
		int i=1;
		boolean ancora = true;
		while(ancora)
		{		
			//System.err.println("VENGO CHIAMATO");
			if(i>1)
			{
				GeneralBadTwin.createBadTwinGeneral(i);
			}
			else
			{
				FirstBadTwin.createBadTwinLevel1();
				GoodTwin.createGoodTwin(1);
			}
			if(SincronizzaSecond.checkC2C3(i))
			{
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
				}
				else
				{
					GoodTwin.createGoodTwin(1);
					SincronizzaFirst.syncro(1);
				}
				if(i>1)
				{
					if(SincronizzaSecond.checkC1(i))
					{
						c1=true;
					}
				}
				else
				{
					if(SincronizzaFirst.checkC1(1))
					{
						c1=true;
					}
				}
				if(c1)
				{
					i++;
				}
				else
				{
					if(i>1)
					{
						if(SincronizzaSecond.checkC4(i))
						{
							c4=true;
						}
					}
					else{
						if(SincronizzaFirst.checkC4(1))
						{
							c4=true;
						}
					}
					if(c4)
					{
						System.out.println("non posso diagnosticare il livello " + i);
						System.out.println("********************************************************");
						System.out.println("||    il livello generale di diagnosticabilità è "+(i-1) + "    ||" );
						System.out.println("********************************************************");
						ancora = false;
					}
					i++;
				}
			}

		}
		
		
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime) - Globals.writeTime;
		System.out.println("Questo metodo ha rischiesto " + seconds + ";  \n\n\n\n");
	
	}

}
