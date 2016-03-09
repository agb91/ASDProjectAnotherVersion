package usefullAbstract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import Twins.TransizioneDoppia;
import global.Globals;
import talkToDb.Cycle;
import talkToDb.ORM.RelTypes;

public class GenericGraphHandler extends Adder{
	
	
	/*
	 * se 2 transizioni diverse hanno stessa sorgente e stesso evento allora
	 *  NON DETERMISTICO
	 */
	protected static boolean deterministic(Vector<Relationship> T)
	{
		boolean deterministico = true;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<T.size(); i++)
			{
				Relationship t1 = T.get(i);
				String sorgente1 = pulisci(T.get(i).getProperties("from").values().toString()); 
				String evento1 = pulisci(T.get(i).getProperties("event").values().toString()); 
				
				for(int a=0; a<T.size(); a++)
				{
					if(a!=i)
					{
						String sorgente2 = pulisci(T.get(a).getProperties("from").values().toString()); 
						String evento2 = pulisci(T.get(a).getProperties("event").values().toString()); 
					
						boolean nonD = sorgente2.equalsIgnoreCase(sorgente1) 
								&& InVector.stessoEvento(evento2, evento1);
						if(nonD)
						{
						//	System.out.println("non determinsitico perchè: ");
						//	System.out.println("prima transizione: " + sorgente1 + " evento : " + evento1);
						//	System.out.println("seconda transizione: " + sorgente2 + " evento : " + evento2);
						//	System.out.println("sto per dire che è NON deterministico");
							deterministico = false;
						}
					}
				}
			}
			tx.success();
		}	
		//System.out.println("sto per dire che è deterministico");
		return deterministico;
	}
		
	protected static boolean inInteger(Integer ago, Vector<Integer> pagliaio)
	{
		for(int i=0; i<pagliaio.size(); i++)
		{
			if(ago==pagliaio.get(i))
			{
				return true;
			}
		}
		return false;
	}
	
	/*protected static boolean hasOnePath(Node s, Node e)
	{
		Path path = null;
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			PathFinder<Path> finder =
					GraphAlgoFactory.allPaths(PathExpanders.forDirection(
							Direction.OUTGOING ), 15 );
			path = finder.findSinglePath( s, e );
			tx.success();
		}	
		if(path.length()>1)
		{
			return true;
		}
		return false;
	}*/
	
	//classe grezza: trova tutti i path mettendo sia nodi sia relazioni in raw
	protected static Iterator<Path> findPath(Node s, Node e)
	{
		Iterator<Path> iteratore = null;
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
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
	
	//classe grezza: trova tutti i path mettendo sia nodi sia relazioni in raw
	protected static Iterator<Path> findPathSecond(Node s, Node e)
	{
		Iterator<Path> iteratore = null;
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
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
	
	
	//cerco le transizioni di guasto: prendo il loro evento.
	// se per tutti quegli eventi non esistono transizioni di guasto
	// che abbiano come evento quegli eventi allora è diagnosticabile
	protected static boolean thirdCondition(Vector<Relationship> T)
	{
		boolean risp = true;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int k=0; k<T.size(); k++)
			{
				String guastoK = pulisci(T.get(k).getProperties("guasto").values().toString()); 
				String osservabileK = pulisci(T.get(k).getProperties("oss").values().toString()); 
				String eventoK = pulisci(T.get(k).getProperties("event").values().toString()); 
				if(guastoK.equalsIgnoreCase("y") && osservabileK.equalsIgnoreCase("y"))
				{
					for( int s=0; s<T.size(); s++)
					{
						if(s!=k)
						{
							String guastoS = pulisci(T.get(s).getProperties("guasto").values().toString()); 
							String eventoS = pulisci(T.get(s).getProperties("event").values().toString());
							if(eventoS.equalsIgnoreCase(eventoK) && guastoS.equalsIgnoreCase("n"))
							{
								return false;
							}
						}
					}
				}
				
			}
			tx.success();
		}
		return risp;
	}
		
	
	protected static boolean checkEqual(Vector<String> primo, Vector<String> secondo)
	{
		//System.out.println("------------------------------------------");
		//System.out.println(primo.toString());
		//System.out.println(secondo.toString());

		if(primo.size() != secondo.size())
		{
			/*System.out.println("prima dimensione: " + primo.size());
			System.out.println("seconda dimensione: " + secondo.size());
			System.out.println("non è uguale la dimensione!!");*/
			return false;
		}
		
		for(int i=0; i<primo.size(); i++)
		{
			if(!stessoStato(primo.get(i),secondo.get(i)))
			{
				//System.out.println("diversi:  " + primo.get(i) + "---" + secondo.get(i));
				return false;
			}
		}
		//System.out.println("rispondo che sono uguali");
		return true;	
	}

	
	
	
	protected static void removeIsolatedStatesBad(int level)
	{
		boolean raggiungibile = false;
		for(int i=1; i<Globals.allNodes.size(); i++)
		{
			//System.out.println("check del nodo: " + Globals.allNodes.get(i));
			raggiungibile = checkPathFromRootBad(Globals.allNodes.get(i));
			if(!raggiungibile)
			{
				//System.err.println("prima: "+ Globals.allNodes.size());
				killNode(Globals.allNodes.get(i), i, level);
				//System.err.println("dopo: "+ Globals.allNodes.size());
				i--;
			}
		}
	}
		
	//prima elimino tutte le relazioni che partono da n
	//poi elimino n
	protected static void killNode(Node n, int index, int level)
	{
		Globals.allNodes.remove(index);
		String nomeNode = n.getProperties("name").values().toString();
		for(int l=0; l<Globals.allRelationsGeneral.size(); l++)
		{
			for(int a=0; a<Globals.allRelationsGeneral.get(l).size(); a++)
			{
				Relationship r = Globals.allRelationsGeneral.get(l).get(a);
				String fromr = r.getProperties("from").values().toString();
				if(stessoStato(fromr,nomeNode))
				{
					Globals.allRelationsGeneral.get(l).get(a).delete();
					Globals.allRelationsGeneral.get(l).remove(a);
					a--;		
				}
			}
		}
		for(int l=0; l<Globals.allRelationsGoodGeneral.size(); l++)
		{
			for(int a=0; a<Globals.allRelationsGoodGeneral.get(l).size(); a++)
			{
				Relationship r = Globals.allRelationsGoodGeneral.get(l).get(a);
				String fromr = r.getProperties("from").values().toString();
				if(stessoStato(fromr,nomeNode))
				{
					Globals.allRelationsGoodGeneral.get(l).get(a).delete();
					Globals.allRelationsGoodGeneral.get(l).remove(a);
					a--;		
				}
			}
		}
		n.delete();
	}
	
	
	protected static boolean checkPathFromRootBad(Node n)
	{
		boolean raggiungibile = false;
		//System.out.println("analizzo il nodo : " + n.getProperties("name").values().toString());
		
		Node root = Globals.allNodes.get(0);
		Iterator<Path> tuttiIPath = findPath(root,n);
		while(tuttiIPath.hasNext() && !raggiungibile)
		{
			Path path = tuttiIPath.next();
			if(path.relationships().iterator().hasNext()) 
			{
				raggiungibile = true;
			}
		}
		return raggiungibile;
	}
	
	protected static Vector<Node> getAllNodesUntil(int level, Vector<Vector<Node>> who)
	{
		Vector<Node> ris = new Vector<Node>();
		for(int l=0; l<=level; l++ )
		{
			for(int i=0; i< who.get(l).size(); i++)
			{
				Node nuova = who.get(l).get(i);
				ris.add(nuova);
			}
		}
		return ris;
	}
	
	protected static Vector<Relationship> getAllRelationsUntil(int level, Vector<HashMap<String, Relationship>> who)
	{
		Vector<Relationship> ris = new Vector<Relationship>();
		for(int l=0; l<=level; l++ )
		{
			Iterator<String> keyset = who.get(l).keySet().iterator();
			while(keyset.hasNext())
			{ 
				String key = keyset.next();
				ris.addElement(who.get(l).get(key));
			}
		}
		return ris;
	}
	
	protected static void killRelation(String id,  
			Vector<HashMap<String, Relationship>> hash)
	{
		for(int a=0; a<hash.size(); a++)
		{
			if(hash.get(a).get(id)!=null)
			{
				hash.get(a).get(id).delete();
			}
			hash.get(a).remove(id);
		}	
	}
	
	protected static Vector<String> riempiTPrimo(int level)
	{
		Vector<String> ris = new Vector<String>();
		for(int l=0; l<level; l++)
		{
			HashMap<String, Relationship> hash = new HashMap<String, Relationship>();
			hash = Globals.allRelationsGeneralHash.get(l);
			
			Iterator<String> keyset = hash.keySet().iterator();
			while(keyset.hasNext())
			{ 
				String key = keyset.next();
				Relationship appoggio = hash.get(key);
				String osservabilita = pulisci(appoggio.getProperties("oss").values().toString());
				if(osservabilita.equalsIgnoreCase("y"))
				{
					String nome = appoggio.getProperties("type").values().toString();
					ris.addElement(nome);
				}	
			}
		}
		return ris;
	}
	
			

}
