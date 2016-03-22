package talkToDb;

import java.util.Iterator;
import java.util.Vector;

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
import usefullAbstract.GenericGraphHandler;
import utility.PrintTime;


public class CheckRequirements extends GenericGraphHandler{
	
	private static Vector<Relationship> rels = new Vector<Relationship>();
	private static boolean isCyclic = false;

	
	
	public static void check()
	{
		rels = getAllRelationsUntil(0, Globals.allRelationsGeneralHash);
		//ogni stato ha almeno una transizione uscente
		long before = System.currentTimeMillis();
		checkOutComing();
		long after = System.currentTimeMillis();
		PrintTime.stampTime("check transizioni uscenti", before, after);
		
		//i metodi add gestiscono già eventuali doppioni;
		//più transizioni possono avere lo stesso evento
		
		//grafo è ciclico (ha almeno un ciclo), inoltre
		// ogni ciclo deve avere almeno un evento osservabile
		//todo SISTEMA COSI: 
		//	fai un grafo senza le transizioni osservabili, solo non osservabili,
		//	se ha cicli allora il controllo è violato
		before = System.currentTimeMillis();
		controlCycleRequirements();
		after = System.currentTimeMillis();
		PrintTime.stampTime("check no cicli non osservabili", before, after);
		
		//ogni nodi raggiungibile da radice
		before = System.currentTimeMillis();
		rootCanArrive();
		after = System.currentTimeMillis();
		PrintTime.stampTime("check linket to root", before, after);
		
		//ogni relazione uscente da ogni nodo deve prima o poi 
		//raggiungere relazioni osservabili, che è come dire che da ogni nodo si può
		// arrivare a relazionni osservabili, che è come dire che da ogni nodo si può
		// arrivare ad un ciclo. se ogni nodo ha un'uscita, per forza prima o poi si 
		//crea un ciclo, che per i controlli già fatti per forza è osservabile...
		
		/*//I don't wanna twin relations with the same "guasto" value
		before = System.currentTimeMillis();
		checkTwin();
		after = System.currentTimeMillis();
		PrintTime.stampTime("check no twin transictions", before, after);*/
		
		//I expect that all "guasted" relations are not observable
		before = System.currentTimeMillis();
		checkGuastoNonOsservabile();
		after = System.currentTimeMillis();
		PrintTime.stampTime("check fault not observable", before, after);
		
		//I expect that all non obs haven't event
		before = System.currentTimeMillis();
		checkUnOsservable();
		after = System.currentTimeMillis();
		PrintTime.stampTime("check not observable with events", before, after);
	
	}
	
	private static void checkUnOsservable()
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<rels.size(); i++)
			{
				Relationship attuale = rels.get(i);
				String osservabile = attuale.getProperties("oss").values().toString();
				osservabile = pulisci(osservabile);
				if(osservabile.equalsIgnoreCase("n"))
				{
					String evento = attuale.getProperties("event").values().toString();
					evento = pulisci(evento);
					if(!evento.equalsIgnoreCase(""))
					{
						String nome = attuale.getProperties("type").values().toString();
						System.out.println("evento: " + evento);
						System.out.println("la relazione " + nome + "è non osservabile ma ha un evento: non ha senso, esco");
						System.exit(2);
					}
				}
			}
			tx.success();
		}
	}
	
	private static void checkGuastoNonOsservabile()
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<rels.size(); i++)
			{
				Relationship attuale = rels.get(i);
				String guasto = attuale.getProperties("guasto").values().toString();
				guasto = pulisci(guasto);
				String osservabile = attuale.getProperties("oss").values().toString();
				osservabile = pulisci(osservabile);
				String nome = attuale.getProperties("type").values().toString();
				nome = pulisci(nome);
				//System.out.println("guasto: "+guasto + ";   oss: " + osservabile + ";  nome: "+nome);
				if(guasto.equalsIgnoreCase("y") && osservabile.equalsIgnoreCase("y"))
				{
					System.out.println(" ogni transizione di guasto DEVE essere non osservabile, la transazione: " + nome + " viola questa regola");
					System.exit(2);
				}
			}
			tx.success();
		}	
	}
	
	private static void checkTwin()
	{
		int max = rels.size();
		for(int i=0; i<max; i++)
		{
			Relationship attuale = rels.get(i);
			for(int a=0; a<max; a++)
			{
				if(a!=i)
				{
					twin(attuale, rels.get(a));
				}			
			}
		}
	}
	
	private static void twin(Relationship a, Relationship b)
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			String nomea = a.getProperty("type").toString();
			String froma =  a.getProperty("from").toString();
			String toa  = a.getProperty("to").toString();
			String eventa = a.getProperty("event").toString();
			String guastoa = a.getProperty("guasto").toString();
			
			String nomeb = b.getProperty("type").toString();
			String fromb = b.getProperty("from").toString();
			String tob  = b.getProperty("to").toString();
			String eventb = b.getProperty("event").toString();
			String guastob = b.getProperty("guasto").toString();
			
			if((froma.equalsIgnoreCase(fromb)) && (toa.equalsIgnoreCase(tob))
				&& eventa.equalsIgnoreCase(eventb))
			{
				if(guastoa.equalsIgnoreCase(guastob))
				{
					System.out.println("errore: transizioni " + nomea + "--" + nomeb + "  gemelle non ammesse");
					System.exit(2);
				}
			}
			
			tx.success();		
		}			
	}
	
	private static void rootCanArrive()
	{
		for(int i=1; i<Globals.allNodes.size(); i++)
		{
			checkPathFromRoot(Globals.allNodes.get(i));
		}
	}
	
	private static void checkPathFromRoot(Node n)
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			boolean safe = false;
			Node root = Globals.allNodes.get(0);
			Iterator<Path> iteratore = findPathL(root,n);
			while(iteratore.hasNext() && !safe)
			{
				Path path = iteratore.next();
				if(path.relationships().iterator().hasNext()) 
				{
					safe = true;
				}
			}
			if(!safe)
			{
				System.out.println("il nodo " + n.getProperties("name").values().toString()
						+ "non è raggiungibile dalla radice, esco");
				System.exit(2);	

			}
			tx.success();
		}
	}
	
	//classe grezza: trova tutti i path mettendo sia nodi sia relazioni in raw
	private static Iterator<Path> findPathL(Node s, Node e)
	{
		Iterator<Path> iteratore = null;
		try ( Transaction tx = Globals.graphDb.beginTx() )
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
	public static void findPathRels(Node s, Node e)
	{
		Iterator<Path> iteratore = findPathL(s,e);
		Iterator<Relationship> result = null;
		Vector<Relationship> ris = new Vector<Relationship>();
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
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
                	checkAllCycleOsservable(ris, e.getProperties("name").toString());
				}    
			}while(iteratore.hasNext());
			tx.success();
		}
	}
	
	private static void writeVector(Vector<Relationship> v, Node s, Node e)
	{
		System.out.println("ecco un possibile path tra " + s.getProperties("name") + 
				" ed " + e.getProperties("name"));
		try( Transaction tx = Globals.graphDb.beginTx() )
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
		try( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<v.size(); i++)
			{
				System.out.println(v.get(i).getProperties("type").values().toString());
			}
			tx.success();
		}
	}
		
	
	private static void checkOutComing()
	{
		for(int i=0; i<Globals.allNodes.size(); i++)
		{
			Node n = Globals.allNodes.get(i);
			hasRelationsOut(n,false);
		}
	}

	//	questo nodo ha una relazione uscente?
    private static boolean hasRelationsOut(Node n, boolean verbose)
    {
    	try ( Transaction tx = Globals.graphDb.beginTx() )
		{
	    	int a=0;
	    	String nomeNodo = n.getProperties("name").values().toString();
	    	while(a<rels.size())
	    	{
	    	   Relationship r = rels.get(a);	
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
		try ( Transaction tx = Globals.graphDb.beginTx() )
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
		
	public static void controlCycleRequirements()
	{		
		/*Iterator<String> keyset = 
				Globals.allRelationsGeneralHash.get(0).keySet().iterator();
		
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<Globals.allNodes.size(); i++)
			{
				addNodeCheck(Globals.allNodes.get(i));
			}
			while(keyset.hasNext())
			{ 
				String a = keyset.next();
				Relationship transizioneAttuale =
						Globals.allRelationsGeneralHash.get(0).get(a);
				String oss = transizioneAttuale.getProperties("oss").values().toString();
				oss = pulisci(oss);
				String from = transizioneAttuale.getProperties("from").values().toString();
				from = pulisci(from);
				String to = transizioneAttuale.getProperties("to").values().toString();
				to = pulisci(to);
				String nome = transizioneAttuale.getProperties("type").values().toString();
				nome = pulisci(nome);
				if(oss.equalsIgnoreCase("n"))
				{
					Globals.allRelationsNotObservable.put(a, transizioneAttuale);
					addRelationCheck(from, to, nome, oss);
				}
			}
			tx.success();
		}
		System.err.println("dimensione: "  + Globals.allRelationsNotObservable.size()); 
		*/
		for(int i=0; i<Globals.allNodes.size(); i++)
		{
		   Node n = Globals.allNodes.get(i);
		   //System.out.println("STO ESAMINANDO IL NODO: " +i);
		   findPathRels(n,n);
		}
		if(!isCyclic)
		{
			System.out.println("questo grafo non è ciclico, esco");
			System.exit(2);
		}
	}


}
