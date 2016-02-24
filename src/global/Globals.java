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
	public static Vector <Relationship> allRelations = new Vector<Relationship>();
	public static Vector <Node> allNodesGood = new Vector<Node>();
	public static Vector <Relationship> allRelationsGood = new Vector<Relationship>();
	public static Vector <Node> allNodesSyncro = new Vector<Node>();
	public static Vector <Relationship> allRelationsSyncro = new Vector<Relationship>();

}
