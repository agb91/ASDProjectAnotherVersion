

import java.util.Vector;

import letturaXML.Graficatore;
import letturaXML.readXmlGraph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import utilita.ORM;

public class Main {
	
	private static String DB_PATH = "/home/andrea/Scrivania/springExample/ZanellaProjectLast/dbtest";
	
	public static void main(String[] args) {
		
		
    	ORM db = new ORM(DB_PATH); //inizializzo il database neo4j
    	db.readXml();
    	
		
		
		/*Node a = ORM.addNode("primo");
		Node b = ORM.addNode("secondo");
		Node c = ORM.addNode("terzo");
		
		Relationship r1 = ORM.addRelation(a,b, "r1");
		Relationship r2 = ORM.addRelation(b,c, "r2");
		Relationship r3 = ORM.addRelation(c,a, "r3");
		
		Vector <Relationship> insieme1 = new Vector<Relationship>();
		insieme1.addElement(r1);
		insieme1.addElement(r2);
		
		Vector <Relationship> insieme2 = new Vector<Relationship>();
		insieme2.addElement(r2);
		insieme2.addElement(r3);	
		
		Vector <Relationship> insiemeUnione = ORM.unione(insieme1,insieme2);
		insiemeUnione = ORM.unione(insiemeUnione,insieme2);
		
		System.out.println("mi aspetto r1 r2 r3");
		ORM.scriviVettore(insiemeUnione);
		
		Vector <Relationship> insiemeIntersezione = ORM.intersezione(insiemeUnione,insieme2);
		insiemeIntersezione = ORM.intersezione(insiemeIntersezione, insieme1);
		insiemeIntersezione = ORM.intersezione(insiemeIntersezione, insieme1);
		
		System.out.println("mi aspetto r2");
		ORM.scriviVettore(insiemeIntersezione);*/

	}

}
