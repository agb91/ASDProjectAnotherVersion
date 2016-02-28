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
import Twins.Sincronizza;
import global.Globals;

public class Main {
	
	private static String DB_PATH = "/home/andrea/Scrivania/springExample/ZanellaProjectLast/dbBad";
	
	public static void main(String[] args) {
		
		System.out.println("comincio a creare il db ed a leggere l'xml...");
		Globals.initialize();
    	ORM db = new ORM(DB_PATH); //inizializzo il database neo4j
    	db.readXml();// legge l'xml e lo scrive nel db di neo4j
    	CheckRequirements.check();//valuta se il grafo rispetta i requisiti
    	System.out.println("comincio il programma principale");
		
    	Menu.ask();
    	    	

    }

}
