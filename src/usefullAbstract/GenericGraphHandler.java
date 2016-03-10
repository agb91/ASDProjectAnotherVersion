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
	protected static boolean deterministic(HashMap<String, Relationship> T)
	{
		boolean deterministico = true;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			Vector<String> sks1 = new Vector<String>();
			Vector<String> sks2 = new Vector<String>();
			Iterator<String> ks1 = T.keySet().iterator();
			Iterator<String> ks2 = T.keySet().iterator();
			while(ks1.hasNext())
			{ 
				String a1 = ks1.next();
				sks1.addElement(a1);
			}
			while(ks2.hasNext())
			{
				String a2 = ks2.next();
				sks2.addElement(a2);
			}
			
			for(int i=0; i<sks1.size(); i++)
			{
				Relationship t1 = T.get(sks1.get(i));
				String sorgente1 = pulisci(t1.getProperties("from").values().toString()); 
				String evento1 = pulisci(t1.getProperties("event").values().toString()); 
				
				for(int a=0; a<sks2.size(); a++)
				{
					Relationship t2 = T.get(sks2.get(a));
					if(a!=i)
					{
						String sorgente2 = pulisci(t2.getProperties("from").values().toString()); 
						String evento2 = pulisci(t2.getProperties("event").values().toString()); 
					
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
	protected static boolean thirdCondition(HashMap<String, Relationship> T)
	{
		boolean risp = true;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			Vector<String> sks1 = new Vector<String>();
			Vector<String> sks2 = new Vector<String>();
			Iterator<String> ks1 = T.keySet().iterator();
			Iterator<String> ks2 = T.keySet().iterator();
			while(ks1.hasNext())
			{ 
				String a1 = ks1.next();
				sks1.addElement(a1);
			}
			while(ks2.hasNext())
			{
				String a2 = ks2.next();
				sks2.addElement(a2);
			}
			
			for(int k=0; k<sks1.size(); k++)
			{
				Relationship tk = T.get(sks1.get(k));
				String guastoK = pulisci(tk.getProperties("guasto").values().toString()); 
				String osservabileK = pulisci(tk.getProperties("oss").values().toString()); 
				String eventoK = pulisci(tk.getProperties("event").values().toString()); 
				if(guastoK.equalsIgnoreCase("y") && osservabileK.equalsIgnoreCase("y"))
				{
					for( int s=0; s<sks2.size(); s++)
					{
						if(s!=k)
						{
							Relationship ts = T.get(sks2.get(s));
							String guastoS = pulisci(ts.getProperties("guasto").values().toString()); 
							String eventoS = pulisci(ts.getProperties("event").values().toString());
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
				//System.err.println("morte al nodo: "+ i);
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
		String nomeNode = pulisci(n.getProperties("name").values().toString());
		Vector<String> daCancellare = new Vector<String>();
		//System.err.println("vittima: " + nomeNode);
		for(int l=0; l<Globals.allRelationsGeneralHash.size(); l++)
		{
			Iterator<String> keyset = Globals.allRelationsGeneralHash.get(l).keySet().iterator();
			while(keyset.hasNext())
			{ 
				String a = keyset.next();
				Relationship r = Globals.allRelationsGeneralHash.get(l).get(a);
				String fromr = pulisci(r.getProperties("from").values().toString());
				//System.err.println(fromr);
				if(fromr.equalsIgnoreCase(nomeNode))
				{
					//System.out.println("cancellare: " + a);
					daCancellare.add(a);
				}
			}
		}
		
		for(int i=0; i<daCancellare.size(); i++)
		{
			for(int l=0; l<Globals.allRelationsGeneralHash.size(); l++)
			{
				try ( Transaction tx1 = Globals.graphDb.beginTx() )
				{
					//System.err.println("cancello : " + daCancellare.get(i));
					//killRelation(daCancellare.get(i), Globals.allRelationsGoodGeneralHash);
					if(Globals.allRelationsGeneralHash.get(l).get(daCancellare.get(i))!=null)
					{
						Globals.allRelationsGeneralHash.get(l).get(daCancellare.get(i)).delete();
					}
					Globals.allRelationsGeneralHash.get(l).remove(daCancellare.get(i));
					tx1.success();
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
	
	protected static HashMap<String, Relationship> getAllRelationsUntilHash(int level, Vector<HashMap<String, Relationship>> who)
	{
		HashMap<String, Relationship> ris = new HashMap<String, Relationship>();
		for(int l=0; l<=level; l++ )
		{
			Iterator<String> keyset = who.get(l).keySet().iterator();
			while(keyset.hasNext())
			{ 
				String key = keyset.next();
				ris.put(key, who.get(l).get(key));
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
