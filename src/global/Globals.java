package global;

import java.util.Vector;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

public class Globals {
	
	public static GraphDatabaseService graphDb;
	public static GraphDatabaseService graphDbGood;
	public static GraphDatabaseService graphDbSyncro;
	
	public static Vector <Node> allNodes = new Vector<Node>();
	public static Vector <Vector<Relationship>> allRelationsGeneral = new Vector<Vector<Relationship>>();
	public static Vector<Integer> badTwinDid = new Vector<Integer>();
		
	public static Vector <Node> allNodesGood = new Vector<Node>();
	public static Vector <Vector<Relationship>> allRelationsGoodGeneral = new Vector<Vector<Relationship>>();
	public static Vector<Integer> goodTwinDid = new Vector<Integer>();
	
	public static Vector <Node> allNodesSyncro = new Vector<Node>();
	public static Vector <Relationship> allRelationsSyncro = new Vector<Relationship>();

	public static void initialize()
	{
		for(int i=0; i<10; i++)
		{
			allRelationsGeneral.addElement(new Vector<Relationship>());
			allRelationsGoodGeneral.addElement(new Vector<Relationship>());
		}
		badTwinDid.addElement(Integer.valueOf(0));
		goodTwinDid.addElement(Integer.valueOf(0));
	}
}
