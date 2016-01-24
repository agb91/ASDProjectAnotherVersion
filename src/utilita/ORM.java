package utilita;

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
	private static Vector <Node> allNodes = new Vector<Node>();
	private static Vector <Relationship> allRelations = new Vector<Relationship>();
	
	public ORM(String p)
	{
		DB_PATH = p;
	    clean(DB_PATH);
		graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( DB_PATH );
		setLabelSystem();	
	}
	
	public void checkRequirements()
	{
		//ogni stato ha almeno una transizione uscente
		checkOutComing();
		
		//i metodi add gestiscono già eventuali doppioni;
		//più transizioni possono avere lo stesso evento
		
		//grafo è ciclico 
		// ogni ciclo deve avere almeno un evento osservabile
		controlCycleRequirements();
	}
	
	private static void checkOutComing()
	{
		for(int i=0; i<allNodes.size(); i++)
		{
			Node n = allNodes.get(i);
			hasRelationsOut(n,false);
		}
	}

	//	questo nodo ha una relazione uscente?
    private static boolean hasRelationsOut(Node n, boolean verbose)
    {
    	try ( Transaction tx = graphDb.beginTx() )
		{
	    	int a=0;
	    	String nomeNodo = n.getProperties("name").values().toString();
	    	while(a<allRelations.size())
	    	{
	    	   Relationship r = allRelations.get(a);	
	    	   String from = r.getProperties("from").values().toString();
	    	   if(verbose)
	    	   {
	    		   System.out.println("confronto: "+ nomeNodo + "   con: " + from);
	    	   }
	    	   if(nomeNodo.contains(from) || from.contains(nomeNodo))
	    	   {
	    		   return true;
	    	   }
	    	   a++;
	    	}   
		tx.success();
		System.out.println("ci sono nodi senza relazioni uscenti, ad esempio: "+ nomeNodo + ", esco");
		System.exit(2);
		}	
  
	    return false;
    }	
	
	private static void checkAllCycleOsservable(Vector<Relationship> c, String extreme)
	{
		try ( Transaction tx = graphDb.beginTx() )
		{
			boolean safe = false;
			int a=0;
			while(!safe & a<c.size()) //NB per risparmiare se questo ciclo ha una relazione osservabile
				//non è necessario checkare le altre
			{
				if(c.get(a).getProperties("oss").values().toString().toLowerCase().contains("y"))
				{
					safe = true;
				}
				a++;
			}
			if(!safe)
			{
				System.out.println("ci sono cicli senza relazioni osservabili: ad esempio");
				System.out.println("dal nodo al nodo: " + extreme);
				for(int i=0; i<c.size(); i++)
				{
					Relationship thisR = c.get(i);
					System.out.println(thisR.getProperty("type").toString());
				}
				System.exit(2);
				//NB per risparmiare se mi accorgo che anche un solo ciclo è compromesso
				// mi fermo
			}
			tx.success();
		}
	}
		
	public Vector<Cycle> controlCycleRequirements()
	{
		Vector<Cycle> cycles = new Vector<Cycle>();
		Vector<Cycle> rels = new Vector<Cycle>();
		for(int i=0; i<allNodes.size(); i++)
		{
		   Node n = allNodes.get(i);
		   //System.out.println("STO ESAMINANDO IL NODO: " +i);
		   findPathRels(n,n, false);
		}
		return cycles;
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
            String from = appoggio.getFrom();
            String to = appoggio.getTo();
            Node nFrom = findNodeByName(from);
            Node nTo = findNodeByName(to);
            addRelation(nFrom, nTo,nome,osservabile, evento);
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
	
	//classe grezza: trova tutti i path mettendo sia nodi sia relazioni in raw
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
	//RESTITUISCI ogni possiblie percorso BASTA CHE SIA DIVERSO DA PERCORSO VUOTO
	public static void findPathRels(Node s, Node e, Boolean haveToWrite)
	{
		boolean isCyclic = false;
		Iterator<Path> iteratore = findPath(s,e);
		Iterator<Relationship> result = null;
		Vector<Relationship> ris = new Vector<Relationship>();
		try ( Transaction tx = graphDb.beginTx() )
		{
			//LO SO ANCHE IO CHE È UN CAZZO DI WHILE DENTRO UN WHILE MA TENUTO
			//CONTO DEL FATTO CHE PUÒ TROVARE TANTI PERCORSI ALTERNATIVI
			//È MEGLIO COSÌ
			do
			{
				Path path = iteratore.next();
				result = path.relationships().iterator();
				int a=0;
				if(result.hasNext()) //se è vuoto non mi interessa..
				{
					ris.clear();
					isCyclic = true;
	                do
	                {
						ris.addElement(result.next());
	                }while(result.hasNext());
	                if(haveToWrite)
	                {
	                	writeVector(ris,s,e);
	                }
                	checkAllCycleOsservable(ris, e.getProperties("name").toString());
				}    
			}while(iteratore.hasNext());
			
			if(!isCyclic)
			{
				System.out.println("this graph is not cyclic, I stop the program");
				System.exit(2);
			}
			tx.success();
		}
	}
	
	private static void writeVector(Vector<Relationship> v, Node s, Node e)
	{
		System.out.println("ecco un possibile path tra " + s.getProperties("name") + 
				" ed " + e.getProperties("name"));
		try( Transaction tx = graphDb.beginTx() )
		{
			for(int i=0; i<v.size(); i++)
			{
				System.out.println(v.get(i).getProperties("type").values().toString());
			}
			tx.success();
		}
	}
	
	private static void writeVector(Vector<Relationship> v)
	{
		System.out.println("ecco un possibile path");
		try( Transaction tx = graphDb.beginTx() )
		{
			for(int i=0; i<v.size(); i++)
			{
				System.out.println(v.get(i).getProperties("type").values().toString());
			}
			tx.success();
		}
	}
	
	public static Relationship addRelation(Node n1, Node n2, String nome, String oss, String ev)
	{
		Relationship relationship;
		try ( Transaction tx = graphDb.beginTx() )
		{
			relationship = n1.createRelationshipTo( n2, RelTypes.STD );
			relationship.setProperty( "type", nome );
			relationship.setProperty( "oss", oss );
			relationship.setProperty("event", ev);
			String nomeN1 = n1.getProperties("name").values().toString();
			String nomeN2 = n2.getProperties("name").values().toString();	
			relationship.setProperty("from", nomeN1);
			relationship.setProperty("to", nomeN2);
			tx.success();
		}	
		if(!inRel(relationship, allRelations))
		{
			allRelations.addElement(relationship);
			System.out.println("arc created: " + nome + ";");

		}
		else
		{
			System.out.println("rilevo doppioni nelle relazioni, tengo solo le prime");
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
		Node userNode = null;
		try ( Transaction tx = graphDb.beginTx() )
		{
		    Label label = DynamicLabel.label( "Nome" );
	        userNode = graphDb.createNode( label );
	        userNode.setProperty( "name", name);
		    tx.success();
		}    
        if(!inNodes(userNode,allNodes))
        {
	        allNodes.addElement(userNode);
		    System.out.println( "node created: " + name );
        }
        else
        {
        	System.out.println("rilevo doppioni nei nodi: tengo solo il primo");
        	return null;
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
