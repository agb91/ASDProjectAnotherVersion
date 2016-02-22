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
		diagnosable = Sincronizza.syncro();
		while(diagnosable)
		{
			GeneralBadTwin.createBadTwinGeneral(level);
			GoodTwin.createGoodTwin(level);
			diagnosable = Sincronizza.syncro();
			if(diagnosable == false)
			{
				System.out.println("il livello generale di diagnosticabilità è "+(level-1) );
			}
			level++;
		}

	}

}
