package global;

import java.util.HashMap;
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
	public static Vector <HashMap<String, Relationship>> allRelationsGeneralHash = new Vector<HashMap<String, Relationship>>();
	
	public static Vector<Integer> badTwinDid = new Vector<Integer>();
		
	public static Vector <Node> allNodesGood = new Vector<Node>();
	public static Vector <HashMap<String, Relationship>> allRelationsGoodGeneralHash = new Vector<HashMap<String, Relationship>>();
	public static Vector<Integer> goodTwinDid = new Vector<Integer>();
	
	public static Vector<Vector<TransizioneDoppia>> firstTaPerLevel = new Vector<Vector<TransizioneDoppia>>();
	public static Vector<Vector<TransizioneDoppia>> secondTaPerLevel = new Vector<Vector<TransizioneDoppia>>();
	
	public static Vector<Vector<Node>> allNodesSyncroGeneral = new Vector<Vector<Node>>();
	public static Vector<Vector<Node>> allNodesSyncroGeneralSecond = new Vector<Vector<Node>>();
	public static Vector<Vector<TransizioneDoppia>> allTa = new Vector<Vector<TransizioneDoppia>>();
	public static Vector<HashMap<String, Relationship>> allRelationsSyncroGeneralHash = 
			new Vector<HashMap<String, Relationship>>();
	public static Vector<HashMap<String, Relationship>> allRelationsSyncroGeneralSecondHash = 
			new Vector<HashMap<String, Relationship>>();
	public static Vector<Integer> syncroFirstDid = new Vector<Integer>();
	public static Vector<Integer> syncroSecondDid = new Vector<Integer>();

	public static Vector<String> inCycleNodes = new Vector<String>();
	public static Vector<TransizioneDoppia> primeTransizioniAmbigue = new Vector<TransizioneDoppia>();
	
	public static void initialize()
	{
		for(int i=0; i<10; i++)
		{
			allRelationsGeneralHash.addElement(new HashMap<String, Relationship>());
			allRelationsGoodGeneralHash.addElement(new HashMap<String, Relationship>());
			allNodesSyncroGeneral.addElement(new Vector<Node>());
			allNodesSyncroGeneralSecond.addElement(new Vector<Node>());
			allRelationsSyncroGeneralHash.addElement(new HashMap<String, Relationship>());
			allRelationsSyncroGeneralSecondHash.addElement(new HashMap<String, Relationship>());
			allTa.addElement(new Vector<TransizioneDoppia>());
			firstTaPerLevel.addElement(new Vector<TransizioneDoppia>());
			secondTaPerLevel.addElement(new Vector<TransizioneDoppia>());
		}
		badTwinDid.addElement(Integer.valueOf(0));
		goodTwinDid.addElement(Integer.valueOf(0));
		syncroFirstDid.addElement(Integer.valueOf(0));
		syncroSecondDid.addElement(Integer.valueOf(0));
	}
}
