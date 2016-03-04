package global;

import java.util.Vector;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import Twins.TransizioneDoppia;

public class Globals {
	
	public static GraphDatabaseService graphDb;
	public static GraphDatabaseService graphDbGood;
	public static GraphDatabaseService graphDbSyncro;
	public static GraphDatabaseService graphDbSyncroSecond;
	
	public static Vector <Node> allNodes = new Vector<Node>();
	public static Vector <Vector<Relationship>> allRelationsGeneral = new Vector<Vector<Relationship>>();
	public static Vector<Integer> badTwinDid = new Vector<Integer>();
		
	public static Vector <Node> allNodesGood = new Vector<Node>();
	public static Vector <Vector<Relationship>> allRelationsGoodGeneral = new Vector<Vector<Relationship>>();
	public static Vector<Integer> goodTwinDid = new Vector<Integer>();
	
	public static Vector<Vector<Node>> allNodesSyncroGeneral = new Vector<Vector<Node>>();
	public static Vector<Vector<Node>> allNodesSyncroGeneralSecond = new Vector<Vector<Node>>();
	public static Vector<Vector<TransizioneDoppia>> allTa = new Vector<Vector<TransizioneDoppia>>();
	public static Vector<Vector<Relationship>> allRelationsSyncroGeneral = 
			new Vector<Vector<Relationship>>();
	public static Vector<Vector<Relationship>> allRelationsSyncroGeneralSecond = 
			new Vector<Vector<Relationship>>();
	public static Vector<Integer> syncroFirstDid = new Vector<Integer>();
	public static Vector<Integer> syncroSecondDid = new Vector<Integer>();

	public static Vector<String> inCycleNodes = new Vector<String>();
	public static Vector<TransizioneDoppia> primeTransizioniAmbigue = new Vector<TransizioneDoppia>();
	
	public static void initialize()
	{
		for(int i=0; i<10; i++)
		{
			allRelationsGeneral.addElement(new Vector<Relationship>());
			allRelationsGoodGeneral.addElement(new Vector<Relationship>());
			allNodesSyncroGeneral.addElement(new Vector<Node>());
			allNodesSyncroGeneralSecond.addElement(new Vector<Node>());
			allRelationsSyncroGeneral.addElement(new Vector<Relationship>());
			allRelationsSyncroGeneralSecond.addElement(new Vector<Relationship>());
			allTa.addElement(new Vector<TransizioneDoppia>());
		}
		badTwinDid.addElement(Integer.valueOf(0));
		goodTwinDid.addElement(Integer.valueOf(0));
		syncroFirstDid.addElement(Integer.valueOf(0));
		syncroSecondDid.addElement(Integer.valueOf(0));
	}
}
