package talkToDb;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SerializationUtils;
import org.neo4j.cypher.internal.compiler.v1_9.commands.AllRelationships;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.schema.Schema;
import org.neo4j.io.fs.FileUtils;
import org.neo4j.kernel.GraphDatabaseAPI;

import letturaXML.Graficatore;
import letturaXML.Nodo;
import letturaXML.Transizione;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;


public class ORM {
	
	private static String DB_PATH = "";
	private static GraphDatabaseService graphDb;
	private static GraphDatabaseService graphDbGood;
	private static Vector <Node> allNodes = new Vector<Node>();
	private static Vector <Relationship> allRelations = new Vector<Relationship>();
	private static Vector <Node> allNodesGood = new Vector<Node>();
	private static Vector <Relationship> allRelationsGood = new Vector<Relationship>();
	
	public ORM(String p)
	{
		DB_PATH = p;
		clean(DB_PATH);
	    clean(DB_PATH+"/dbGood");
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		graphDbGood = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH+"/dbGood");
		setLabelSystem();	
	}
	
	// setta tutte le transizione ad "osservabile", nient'altro
	public void createBadTwinLevel1()
	{
		try ( Transaction tx = graphDb.beginTx() )
		{
			for(int i=0; i<allRelations.size(); i++)
			{
				Relationship attuale = allRelations.get(i);
				attuale.setProperty("oss", "y");
			}
			System.out.println("created bad twin level 1");
			tx.success();
		}	
	}
	
	//prima creo un grafo identico al bad twin dello stesso livello (questa parte viene fatta a runtime)
	//rimuovo tutte le transizioni guaste
	//rimuovo tutti gli stati non raggiungibili dallo stato iniziale
	public void createGoodTwinLevel1()
	{
		try ( Transaction tx = graphDbGood.beginTx() )
		{
			removeGuasti();
			//removeIsolatedStates();
			TODOTODOTOD RIGA SOPRA
			tx.success();
		}	
		System.out.println("dimensione good rels: " +allNodesGood.size());
		System.out.println("dimensione bad rels: " +allNodes.size());
	}
	
	private void removeIsolatedStates()
	{
		Vector<Node> appoggio = new Vector<Node>();
		appoggio.add(allNodesGood.get(0));
		boolean raggiungibile = false;
		for(int i=1; i<allNodesGood.size(); i++)
		{
			raggiungibile = checkPathFromRoot(allNodesGood.get(i));
			if(raggiungibile)
			{
				appoggio.add(allNodesGood.get(i));
			}
			else
			{
				Node attuale = allNodesGood.get(i);
				attuale.delete();
			}	
		}
		allNodesGood.clear();
		allNodesGood.addAll(appoggio);
	}
	
	private static boolean checkPathFromRoot(Node n)
	{
		boolean raggiungibile = false;
		System.out.println("analizzo il nodo : " + n.getProperties("name").values().toString());
		
		Node root = allNodesGood.get(0);
		Iterator<Path> iteratore = findPath(root,n);
		while(iteratore.hasNext() && !raggiungibile)
		{
			Path path = iteratore.next();
			if(path.relationships().iterator().hasNext()) 
			{
				raggiungibile = true;
			}
		}
		return raggiungibile;
	}
	
	private static Iterator<Path> findPath(Node s, Node e)
	{
		Iterator<Path> iteratore = null;
		PathFinder<Path> finder =
				GraphAlgoFactory.allPaths(PathExpanders.forDirection(
						Direction.OUTGOING ), 15 );
		Iterable<Path> paths = finder.findAllPaths( s, e );
		
		iteratore = paths.iterator();
		return iteratore;
	}
	
	private void removeGuasti()
	{
		for(int i=0; i<allRelationsGood.size(); i++)
		{
			Relationship attuale = allRelationsGood.get(i);
			String guasto = attuale.getProperties("guasto").values().toString();
			if(guasto.contains("y"))
			{
				attuale.delete();
				allRelationsGood.remove(i);
				i--;
			}
		}
	}
	
	public Vector<Node> getNodes()
	{
		Vector<Node> ris = new Vector<Node>();
		for(int i=0; i<allNodes.size(); i++)
		{
			ris.addElement(allNodes.get(i));
		}
		return ris;
	}
	
	public Vector<Relationship> getRelationships()
	{
		Vector<Relationship> ris = new Vector<Relationship>();
		for(int i=0; i<allRelations.size(); i++)
		{
			ris.addElement(allRelations.get(i));
		}
		return ris;
	}
	
	public void readXml()
	{
		Graficatore ldx = new Graficatore(); //lettura iniziale da xml 
		Vector<Nodo> n = ldx.getNodesList();
		Vector<Transizione> t = ldx.getRelationsList();
		for(int i=0; i<n.size(); i++)
		{
			Nodo appoggio = n.get(i);
            String nNode = appoggio.getNome();
            addNode(nNode, "bad");
            addNode(nNode, "good");
		}	
		for(int i=0; i<t.size(); i++)
		{
			Transizione appoggio = t.get(i);
            String nome = appoggio.getNome();
            String osservabile = appoggio.getOss();
            String evento = appoggio.getEvento();
            String guasto = appoggio.getGuasto();
            String from = appoggio.getFrom();
            String to = appoggio.getTo();
            Node nFrom = findNodeByName(from);
            Node nFromGood = findNodeByNameGood(from);
            Node nTo = findNodeByName(to);
            Node nToGood = findNodeByNameGood(to);
            addRelation(nFrom, nTo,nome,osservabile, evento, guasto, "bad");
            addRelation(nFromGood, nToGood,nome,osservabile, evento, guasto, "good");
		}	
		CheckRequirements.prepare(allNodes,allRelations, graphDb);
	}
	
	public enum RelTypes implements RelationshipType{
		STD;
	}
	
	public static void scriviVettore (Vector<Relationship> oggetto)
	{
		try ( Transaction tx = graphDb.beginTx() )
		{
			for(int i=0; i<oggetto.size(); i++)
			{
				String ris = oggetto.get(i).getProperties("type").values().toString();
				System.out.println(ris);
			}
			tx.success();
		}	
	}
	
	public static boolean inRel(Relationship ago, Vector<Relationship> pagliaio)
	{
		try ( Transaction tx = graphDb.beginTx() )
		{
			for(int i=0; i<pagliaio.size(); i++)
			{
				String idAgo = ago.getProperties("type").values().toString();
				String idPagliaioi = pagliaio.get(i).getProperties("type").values().toString();
				if(idAgo.equalsIgnoreCase(idPagliaioi))
				{
					return true;
				}
			}
			tx.success();
		}	
		return false;
	}
	
	public static boolean inNodes(Node ago, Vector<Node> pagliaio)
	{
		try ( Transaction tx = graphDb.beginTx() )
		{
			for(int i=0; i<pagliaio.size(); i++)
			{
				String idAgo = ago.getProperties("name").values().toString();
				String idPagliaioi = pagliaio.get(i).getProperties("name").values().toString();
				if(idAgo.equalsIgnoreCase(idPagliaioi))
				{
					return true;
				}
			}
			tx.success();
		}	
		return false;
	}
	
	public static Relationship addRelation(Node n1, Node n2, String nome, String oss, String ev, String gu, String who)
	{
		Relationship relationship = null;
		if(who.equalsIgnoreCase("bad"))
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
				relationship = n1.createRelationshipTo( n2, RelTypes.STD );
				relationship.setProperty( "type", nome );
				relationship.setProperty( "oss", oss );
				relationship.setProperty("event", ev);
				relationship.setProperty("guasto", gu);
				String nomeN1 = n1.getProperties("name").values().toString();
				String nomeN2 = n2.getProperties("name").values().toString();	
				relationship.setProperty("from", nomeN1);
				relationship.setProperty("to", nomeN2);
				tx.success();
				allRelations.addElement(relationship);
			}	
		}
		else
		{
			try ( Transaction tx = graphDbGood.beginTx() )
			{
				relationship = n1.createRelationshipTo( n2, RelTypes.STD );
				relationship.setProperty( "type", nome );
				relationship.setProperty( "oss", oss );
				relationship.setProperty("event", ev);
				relationship.setProperty("guasto", gu);
				String nomeN1 = n1.getProperties("name").values().toString();
				String nomeN2 = n2.getProperties("name").values().toString();	
				relationship.setProperty("from", nomeN1);
				relationship.setProperty("to", nomeN2);
				tx.success();
				allRelationsGood.addElement(relationship);
			}
		}
		return relationship;
	}
	
	public static void clean(String path)
	{
		try {
			FileUtils.deleteRecursively(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
	private static Node findNodeByName(String nameToFind)
	{
		ArrayList<Node> userNodes = new ArrayList<>();
		Label label = DynamicLabel.label( "Nome" );
		try ( Transaction tx = graphDb.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		            graphDb.findNodes( label, "name", nameToFind ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }

		        /*for ( Node node : userNodes )
		        {
		            System.out.println( "trovato nodo: " + node.getProperty( "name" ) );
		        }*/
		    }
		}
		return userNodes.get(0);

	}
	
	private static Node findNodeByNameGood(String nameToFind)
	{
		ArrayList<Node> userNodes = new ArrayList<>();
		Label label = DynamicLabel.label( "Nome" );
		try ( Transaction tx = graphDbGood.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		            graphDbGood.findNodes( label, "name", nameToFind ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }
		    }
		}
		return userNodes.get(0);
	}

	
	public static void setLabelSystem()
	{
		IndexDefinition indexDefinition;
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Schema schema = graphDb.schema();
		    indexDefinition = schema.indexFor( DynamicLabel.label( "Nome" ) )
		            .on( "name" )
		            .create();
		    tx.success();
		}
		
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Schema schema = graphDb.schema();
		    schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
		}		
	}
	
	public static Node addNode(String name, String who)
	{
		Node userNode = null;
		if(who.equalsIgnoreCase("bad"))
		{
			try ( Transaction tx = graphDb.beginTx() )
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = graphDb.createNode( label );
		        userNode.setProperty( "name", name);
		        allNodes.addElement(userNode);
			    tx.success();
			}    

		}
		else
		{
			try ( Transaction tx = graphDbGood.beginTx() )
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = graphDbGood.createNode( label );
		        userNode.setProperty( "name", name);
		        allNodesGood.addElement(userNode);
			    tx.success();
	        }
		}
		return userNode;
	}
	
	private static void registerShutdownHook( final GraphDatabaseService graphDb )
	{
	    // Registers a shutdown hook for the Neo4j instance so that it
	    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
	    // running application).
	    Runtime.getRuntime().addShutdownHook( new Thread()
	    {
	        @Override
	        public void run()
	        {
	            graphDb.shutdown();
	        }
	    } );
	}
}
