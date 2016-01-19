package utilita;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.TimeUnit;

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
	Vector<Node> nodi = new Vector<Node>();
	
	public ORM(String p)
	{
		DB_PATH = p;
	    clean(DB_PATH);
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		setLabelSystem();
		
		
		
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
            String from = appoggio.getFrom();
            String to = appoggio.getTo();
            Node nFrom = findNodeByName(from);
            Node nTo = findNodeByName(to);
            addRelation(nFrom, nTo,nome,osservabile);
		}	
	}
	
	public enum RelTypes implements RelationshipType{
		STD;
	}
	
	public static Vector<Relationship> unione (Vector<Relationship> a, Vector<Relationship> b)
	{
		Vector<Relationship> risultato = new Vector<Relationship>();
		for(int i=0; i<a.size(); i++)
		{
			if(!inRel(a.get(i),risultato))
			{
				risultato.addElement(a.get(i));
			}
		}
		
		for(int i=0; i<b.size(); i++)
		{
			if(!inRel(b.get(i),risultato))
			{
				risultato.addElement(b.get(i));
			}
		}		
		return risultato;
	}
	
	public static Vector<Relationship> unione (Relationship a, Vector<Relationship> b)
	{
			if(!inRel(a,b))
			{
				b.addElement(a);
			}		
		return b;
	}
	
	public static Vector<Relationship> intersezione (Vector<Relationship> a, Vector<Relationship> b)
	{
		Vector<Relationship> risultato = new Vector<Relationship>();
		for(int i=0; i<a.size(); i++)
		{
			if(inRel(a.get(i), b))
			{
				risultato = unione(a.get(i),risultato);
			}
		}
		return risultato;
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
	
	//classe grezza: trova un generico path mettendo sia nodi sia relazioni in raw
	private static Iterator<Path> findPath(Node s, Node e)
	{
		Iterator<Path> iteratore = null;
		try ( Transaction tx = graphDb.beginTx() )
		{
			PathFinder<Path> finder =
					GraphAlgoFactory.allPaths(PathExpanders.forDirection(
							Direction.OUTGOING ), 15 );
			Iterable<Path> paths = finder.findAllPaths( s, e );
			
			iteratore = paths.iterator();
			tx.success();
		}	
		return iteratore;
	}
	
	//restituisci solo le relazioni, scegli se stamparle 
	public static Iterator<Relationship> findPathRels(Node s, Node e, Boolean haveToWrite)
	{
		Iterator<Path> iteratore = findPath(s,e);
		Iterator<Relationship> result = null;
		try ( Transaction tx = graphDb.beginTx() )
		{
			//LO SO ANCHE IO CHE È UN CAZZO DI WHILE DENTRO UN WHILE MA TENUTO
			//CONTO DEL FATTO CHE PUÒ TROVARE TANTI PERCORSI ALTERNATIVI
			//È MEGLIO COSÌ
			if(haveToWrite)
			{
				do
				{
					Path path = iteratore.next();
					result = path.relationships().iterator();
						System.out.println("\n\n\n\n Ecco un possibile path di Relazioni:");
						System.out.println("Relazioni: ");
		                do
		                {
							if(result.hasNext())
							{
								System.out.println(result.next().getProperty("type"));
							}
		                }while(result.hasNext());	
				}while(iteratore.hasNext());
			}	
			tx.success();
		}
		return result;
	}
	
	public static Iterator<Node> findPathNodes(Node s, Node e, Boolean haveToWrite)
	{
		Iterator<Path> iteratore = findPath(s,e);
		Iterator<Node> result = null;
		try ( Transaction tx = graphDb.beginTx() )
		{
			//LO SO ANCHE IO CHE È UN CAZZO DI WHILE DENTRO UN WHILE MA TENUTO
			//CONTO DEL FATTO CHE PUÒ TROVARE TANTI PERCORSI ALTERNATIVI
			//È MEGLIO COSÌ
			if(haveToWrite)
			{
				do
				{
					Path path = iteratore.next();
					result = path.nodes().iterator();
						System.out.println("\n\n\n\n Ecco un possibile path di Nodi:");
						System.out.println("Nodi: ");
		                do
		                {
							if(result.hasNext())
							{
								System.out.println(result.next().getProperty("name"));
							}
		                }while(result.hasNext());
						
				}while(iteratore.hasNext());
			}	
			tx.success();
		}
		return result; 
	}
	
	public static Relationship addRelation(Node n1, Node n2, String nome, String oss)
	{
		Relationship relationship;
		try ( Transaction tx = graphDb.beginTx() )
		{
			relationship = n1.createRelationshipTo( n2, RelTypes.STD );
			relationship.setProperty( "type", nome );
			relationship.setProperty( "oss", oss );
			tx.success();
		}	
		System.out.println("arc created");
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

		        for ( Node node : userNodes )
		        {
		            System.out.println( "trovato nodo: " + node.getProperty( "name" ) );
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
	
	public static Node addNode(String name)
	{
		Node userNode;
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Label label = DynamicLabel.label( "Nome" );
	        userNode = graphDb.createNode( label );
	        userNode.setProperty( "name", name);
		    System.out.println( "node created" );
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
