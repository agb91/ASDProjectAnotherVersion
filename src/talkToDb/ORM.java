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

import global.Globals;
import letturaXML.Graficatore;
import letturaXML.Nodo;
import letturaXML.Transizione;
import talkToDb.ORM.RelTypes;
import usefullAbstract.GenericGraphHandler;

import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.ResourceIterator;


public class ORM extends GenericGraphHandler {
	
	private static String DB_PATH = "";
		
	public ORM(String p)
	{
		DB_PATH = p;
		clean(DB_PATH);
	    clean(DB_PATH+"/dbGood");
		Globals.graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		Globals.graphDbGood = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH+"/dbGood");
		Globals.graphDbSyncro = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH+"/dbSyncro");
		Globals.graphDbSyncroSecond = new GraphDatabaseFactory().newEmbeddedDatabase(DB_PATH+"/dbSyncroSecond");
		
		setLabelSystem();	
	}
	
	public static void updateDb(int level)
	{
		for (int l=0; l<=level; l++)
		{
			for(int i=1; i<Globals.allRelationsGeneral.get(l).size(); i++)
			{
				Relationship attuale = Globals.allRelationsGeneral.get(l).get(i);
				String osservabilita = attuale.getProperties("oss").values().toString();
				if(osservabilita.toLowerCase().contains("n"))
				{
					//System.out.println("cancello la relazione: " + Globals.allRelations.get(i).getStartNode() + "--->" + Globals.allRelations.get(i).getEndNode());
					Globals.allRelationsGeneral.get(l).get(i).delete();
					Globals.allRelationsGeneral.get(l).remove(i);
					i--;
				}
			}
		}
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
            addNode(nNode);
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
            Node nFrom = findNodeByNameBadStd(from);
            Node nTo = findNodeByNameBadStd(to);
            addRelation(nFrom, nTo,nome,osservabile, evento, guasto);
		}	
		//CheckRequirements.prepare();
	}
	
	protected static Relationship addRelation(Node n1, Node n2, String nome, String oss, String ev, String gu)
	{
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			relationship = n1.createRelationshipTo( n2, RelTypes.STD );
			relationship.setProperty( "type", pulisci(nome) );
			relationship.setProperty( "oss", pulisci(oss) );
			ev = pulisci(ev);
			relationship.setProperty("event", pulisci(ev));
			relationship.setProperty("guasto", pulisci(gu));
			String nomeN1 = n1.getProperties("name").values().toString();
			String nomeN2 = n2.getProperties("name").values().toString();	
			relationship.setProperty("from", pulisci(nomeN1));
			relationship.setProperty("to", pulisci(nomeN2));
			tx.success();
			Globals.allRelationsGeneral.get(0).addElement(relationship);
			//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
		}	
		return relationship;
	}
	
	public enum RelTypes implements RelationshipType{
		STD;
	}
	
	public static void scriviVettore (Vector<Relationship> oggetto)
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
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
		try ( Transaction tx = Globals.graphDb.beginTx() )
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
		try ( Transaction tx = Globals.graphDb.beginTx() )
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
	
	public static void clean(String path)
	{
		try {
			FileUtils.deleteRecursively(new File(path));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	/*private static Node findNodeByName(String nameToFind)
	{
		ArrayList<Node> userNodes = new ArrayList<>();
		Label label = DynamicLabel.label( "Nome" );
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		    		Globals.graphDb.findNodes( label, "name", nameToFind ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }
		    }
		}
		return userNodes.get(0);

	}*/
		
	public static void setLabelSystem()
	{
		IndexDefinition indexDefinition;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
		    Schema schema = Globals.graphDb.schema();
		    indexDefinition = schema.indexFor( DynamicLabel.label( "Nome" ) )
		            .on( "name" )
		            .create();
		    tx.success();
		}
		
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
		    Schema schema = Globals.graphDb.schema();
		    schema.awaitIndexOnline( indexDefinition, 10, TimeUnit.SECONDS );
		}	
		
		IndexDefinition indexDefinition1;
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
		    Schema schema = Globals.graphDbGood.schema();
		    indexDefinition1 = schema.indexFor( DynamicLabel.label( "Nome" ) )
		            .on( "name" )
		            .create();
		    tx.success();
		}
		
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
		    Schema schema = Globals.graphDbGood.schema();
		    schema.awaitIndexOnline( indexDefinition1, 10, TimeUnit.SECONDS );
		}	
	}
	
	public static Node addNode(String name)
	{
		Node userNode = null;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
		    Label label = DynamicLabel.label( "Nome" );
	        userNode = Globals.graphDb.createNode( label );
	        userNode.setProperty( "name", name);
	        Globals.allNodes.addElement(userNode);
		    tx.success();
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
