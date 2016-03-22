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
	//public static GraphDatabaseService graphDbCheck; // just for check
	
	public static long writeTime = 0;
	
	public static HashMap<String, Relationship> allRelationsNotObservable 
						= new HashMap<String, Relationship>(); //just for check
	
	public static Vector <Node> allNodes = new Vector<Node>();
	public static Vector <HashMap<String, Relationship>> allRelationsGeneralHash = new Vector<HashMap<String, Relationship>>();
	
	public static Vector<Integer> badTwinDid = new Vector<Integer>();
		
	public static Vector <Node> allNodesGood = new Vector<Node>();
	public static Vector <HashMap<String, Relationship>> allRelationsGoodGeneralHash = new Vector<HashMap<String, Relationship>>();
	public static Vector<Integer> goodTwinDid = new Vector<Integer>();
	
	public static HashMap<Integer, String> c1= new HashMap<Integer, String>();
	public static HashMap<Integer, String> c2= new HashMap<Integer, String>();
	public static HashMap<Integer, String> c3= new HashMap<Integer, String>();
	public static HashMap<Integer, String> c4= new HashMap<Integer, String>();
	
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

	public static HashMap<String,String> inCycleNodes = new HashMap<String,String>();
	public static HashMap<String, TransizioneDoppia> primeTransizioniAmbigue =
			new HashMap<String, TransizioneDoppia>();
	
	public static void initialize()
	{
		for(int i=0; i<13; i++)
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
