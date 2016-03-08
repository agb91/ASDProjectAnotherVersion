package main;

import java.util.Vector;

import letturaXML.Graficatore;
import letturaXML.readXmlGraph;
import talkToDb.CheckRequirements;
import talkToDb.ORM;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import Twins.FirstBadTwin;
import Twins.GoodTwin;
import Twins.GeneralBadTwin;
import Twins.SincronizzaFirst;
import Twins.SincronizzaSecond;
import global.Globals;

public class Main {
	
	private static String DB_PATH = "/home/andrea/Scrivania/springExample/ZanellaProjectLast/dbBad";
	
	public static void main(String[] args) {
		
		System.out.println("comincio a cancellare i db "
				+ "precedenti ed a inizializzare le variabili...");
		Globals.initialize();
		System.out.println("costruisco i database e la struttura...");
    	ORM db = new ORM(DB_PATH); //inizializzo il database neo4j
    	System.out.println("leggo da xml...");
    	db.readXml();// legge l'xml e lo scrive nel db di neo4j
    	//db.readTest(8,11,10);
    	System.out.println("controllo se rispetta i requisiti");
    	CheckRequirements.check();//valuta se il grafo rispetta i requisiti
    	System.out.println("comincio il programma principale");
		
    	/*FirstBadTwin.createBadTwinLevel1();
    	GoodTwin.createGoodTwin(1);
    	SincronizzaFirst.syncro(1);
    	GeneralBadTwin.createBadTwinGeneral(2);
    	GoodTwin.createGoodTwin(2);
    	/*SincronizzaFirst.syncro(2);
    	SincronizzaSecond.syncroSecond(2);
    	/*GeneralBadTwin.createBadTwinGeneral(3);
    	GoodTwin.createGoodTwin(3);
    	SincronizzaFirst.syncro(3);
    	SincronizzaSecond.syncroSecond(3);*/
    	
    	Menu.ask();
    	Menu.ask();
    	Menu.ask();
    	Menu.ask();
    	Menu.ask();
    	Menu.ask();
    	Menu.ask(); 
    
    	
    	System.out.println("ho finito");
    
    }

}
