package Twins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import usefullAbstract.GenericGraphHandler;
import usefullAbstract.InVector;

public class SincronizzaCommon extends GenericGraphHandler{
	
	protected static boolean nuovaTransizione(TransizioneDoppia nuovo, HashMap<String,TransizioneDoppia> pagliaio)
	{
		String eventoNuovo = nuovo.getEvento();
		String fromNuovo = nuovo.getSorgente();
		String toNuovo = nuovo.getDestinazione();
		String id = fromNuovo + "-" + toNuovo + "-" + eventoNuovo;
		if(pagliaio.get(id)!=null)
		{
			return false;
		}
		return true;
	}
		
	protected static boolean nuovoStato(String nuovo, Vector<String> Sdue)
	{
		String primoNuovo = nuovo.split("-")[0];
		String secondoNuovo = nuovo.split("-")[1];
		//System.out.println("primoN: " + primoNuovo + ";   secondoN : " + secondoNuovo);
		for(int i=0; i<Sdue.size(); i++)
		{
			String attuale = Sdue.get(i);
			String primo = attuale.split("-")[0];
			String secondo = attuale.split("-")[1];
			//System.out.println("primo: " + primo + ";   secondo : " + secondo);
			if(primo.equalsIgnoreCase(primoNuovo) && secondo.equalsIgnoreCase(secondoNuovo))
			{
				return false;
			}
			if(primo.equalsIgnoreCase(secondoNuovo) && secondo.equalsIgnoreCase(primoNuovo))
			{
				return false;
			}
		}
		return true;
	}

	protected static boolean checkPrima(Vector<Vector<TransizioneDoppia>> Ta,int level)
	{	
		//PROVA 1 2 2 3 1

		if(Ta.get(level).size()==0)
		{
			System.out.println("vale C1: "
					+ "non ci sono transizioni ambigue nell'automa sincronizzato");
			//System.out.println("finisco la sincronizzazione di livello: " + level);
			return true;
		}
		else
		{
			System.out.println("non vale C1");
			return false;
		}
	}
	
	protected static boolean checkSeconda(HashMap<String, Relationship> T, int level)
	{
		if(deterministic(T))
		{
			System.out.println("vale C2: il bad twin è deterministico");
			//System.out.println("finisco la sincronizzazione di livello: " + level);
			return true;
		}
		else
		{
			System.out.println("non vale c2: il bad twin non è deterministico");
		}
		return false;
	}
	
	protected static boolean checkTerza(HashMap<String, Relationship> T, int level)
	{
		if(thirdCondition(T))
		{
			System.out.println("vale la C3");
			//System.out.println("finisco la sincronizzazione di livello: " + level);
			return true;
		}
		else
		{
			System.out.println("non vale C3");
		}
		return false;
	}
		
	protected static boolean checkQuarta(Vector<String> in, int level, 
			HashMap<String, TransizioneDoppia> ta, HashMap<String, TransizioneDoppia> Tdue, String who)
	{
		boolean ris = false;
		
		if(ta.size()==0)
		{
			//System.err.println("facile, esco subito");
			return false;
		}
		searchCycle(in, who);
		Iterator<String> ks = Globals.inCycleNodes.keySet().iterator();
		//System.err.println("dim in cycle nodes : " + Globals.inCycleNodes.size());
		while(ks.hasNext())
		{ 
			String a = ks.next();
			System.err.println(" nodi in cicli : " + Globals.inCycleNodes.get(a));
		}
		searchFirstAmbiguous(ta, Tdue, in, who);
	/*	
		Iterator<String> ks = Globals.primeTransizioniAmbigue.keySet().iterator();
		while(ks.hasNext())
		{ 
			String a = ks.next();
			System.err.println("prime ambigue: " +
					Globals.primeTransizioniAmbigue.get(a).getSorgente());
		}*/
		ris = checkIfFromAmbiguousGoToCycle(who);
		
		return ris;
	}
	
		
	protected static boolean checkIfFromAmbiguousGoToCycle(String who)
	{
		//prima trovo tutte le destinazioni delle transizioni ambigue
		Vector<String> destinazioniAmbigue = new Vector<String>();
		Iterator<String> ks = Globals.primeTransizioniAmbigue.keySet().iterator();
		while(ks.hasNext())
		{ 
			String a = ks.next();
			TransizioneDoppia attuale = Globals.primeTransizioniAmbigue.get(a);
			String dest = attuale.getDestinazione();
		//	System.err.println("destinazione ambigua: " + dest);
			destinazioniAmbigue.addElement(dest);
		}

		
		for(int a=0; a<destinazioniAmbigue.size(); a++)
		{
			String altra = destinazioniAmbigue.get(a);
			String sa = altra.split("-")[0];
			String sb = altra.split("-")[1];
			altra = sb + "-" + sa;
			if(Globals.inCycleNodes.get(destinazioniAmbigue.get(a))!=null)
			{
				return true;
			}
			
			if(Globals.inCycleNodes.get(altra)!=null)
			{
				return true;
			}
		}
		
		if(who.equalsIgnoreCase("f"))
		{
			for(int a=0; a<destinazioniAmbigue.size(); a++)
			{
				String sa = destinazioniAmbigue.get(a).split("-")[0];
				String sb = destinazioniAmbigue.get(a).split("-")[1];
				String altra = sb + "-" + sa;
				//SISTEMA LA COSA DEL NODO CHE PUÒAVERE IL NOME INVERTITO
				Node primo = findNodeByNameSyncro(destinazioniAmbigue.get(a));
				Node primo2 = findNodeByNameSyncro(altra);
				Iterator<String> ksz = Globals.inCycleNodes.keySet().iterator();
				while(ksz.hasNext())
				{ 
					String az = ksz.next();
					Node secondo = findNodeByNameSyncro(Globals.inCycleNodes.get(az));
					//System.err.println("da qui!");
					HashMap<String, Relationship> appoggio = findPathRels(primo, secondo);
					if(appoggio!=null)
					{
						if(appoggio.size()>0)
						{
							//System.out.println("FATTO2");
							return true;
						}
					}
					

					HashMap<String, Relationship> appoggio2 = findPathRels(primo2, secondo);
					if(appoggio2!=null)
					{
						if(appoggio2.size()>0)
						{
							//System.out.println("FATTO2");
							return true;
						}
					}
				}

				
			}
		}
		return false;
	}
	
	protected static void searchFirstAmbiguous(HashMap<String, TransizioneDoppia> ta, 
			HashMap<String, TransizioneDoppia> Tdue, Vector<String> nodi, String who)
	{
		Iterator<String> ksta = ta.keySet().iterator();
		while(ksta.hasNext())
		{ 
			String i = ksta.next();
			String sorgenteAmbigua = ta.get(i).getSorgente();
			boolean prima = true;
			HashMap<String, Relationship> path = new HashMap<String, Relationship>();
			if(who.equalsIgnoreCase("f"))
			{
				Node from = findNodeByNameSyncro(nodi.get(0));
				Node to = findNodeByNameSyncro(sorgenteAmbigua);
				path = findPathRels(from,to);
			}
			/*else
			{
				Node from = findNodeByNameSyncroSecond(nodi.get(0));
				Node to = findNodeByNameSyncroSecond(sorgenteAmbigua);
				path = findPathRelsSecond(from,to);
			}*/
			Iterator<String> ks = path.keySet().iterator();
			while(ks.hasNext())
			{ 
				String a = ks.next();
				if(InVector.inVettoreSyncroHash(path.get(a), ta, who))
				{
					prima = false;
				}
			}
			if(prima)
			{
				Globals.primeTransizioniAmbigue.put(i, ta.get(i));
			}
		}
	}
	
	protected static void searchCycle(Vector<String> sdue, String who)
	{
		Globals.inCycleNodes.clear();
		for(int i=0; i<sdue.size(); i++)
		{
		   if(!InVector.inVettoreHash(sdue.get(i), Globals.inCycleNodes))
		   {
			   if(who.equalsIgnoreCase("f"))
			   {
				   Node n = findNodeByNameSyncro(sdue.get(i));
				   findPathNodes(n,n);
			   }
			   else
			   {
				   Node n = findNodeByNameSyncroSecond(sdue.get(i));
				   findPathNodesSecond(n,n);
			   }

		   }
		}
	}
	
	
	public static HashMap<String, Relationship> findPathRelsSecond(Node s, Node e)
	{
		HashMap<String, Relationship> ris = new HashMap<String, Relationship>();
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
			//System.err.println("n1: " + s.getProperties("name").values().toString() +
			//		";  n2: " + e.getProperties("name").values().toString());
			Iterator<Path> iteratore = findPathSecond(s,e);
			//System.err.println(iteratore.hasNext() + "--");
			Iterator<Relationship> result = null;
			if(iteratore.hasNext())
			{
				do
				{
					Path path = iteratore.next();
					result = path.relationships().iterator();
					if(result.hasNext()) //se è vuoto non mi interessa..
					{
						ris.clear();
		                do
		                {
		                	Relationship add = result.next();
		                	String key = pulisci(add.getProperties("type").values().toString());
							ris.put(key ,add);
		                }while(result.hasNext());
		                if(ris.size()!=0)
		                {
		                	return ris;
		                }
					}    
				}while(iteratore.hasNext());
			}
			tx.success();
		}
		return ris;
	}
	
	public static HashMap<String, Relationship> findPathRels(Node s, Node e)
	{
		HashMap<String, Relationship> ris = new HashMap<String, Relationship>();
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			//System.err.println("n1: " + s.getProperties("name").values().toString() +
			//		";  n2: " + e.getProperties("name").values().toString());
			Iterator<Path> iteratore = findPath(s,e);
			//System.err.println(iteratore.hasNext() + "--");
			Iterator<Relationship> result = null;
			if(iteratore.hasNext())
			{
				do
				{
					Path path = iteratore.next();
					result = path.relationships().iterator();
					if(result.hasNext()) //se è vuoto non mi interessa..
					{
						ris.clear();
		                do
		                {
		                	Relationship add = result.next();
		                	String key = pulisci(add.getProperties("type").values().toString());
							ris.put(key ,add);
		                }while(result.hasNext());
		                if(ris.size()!=0)
		                {
		                	return ris;
		                }
					}    
				}while(iteratore.hasNext());
			}
			tx.success();
		}
		return ris;
	}


	public static void findPathNodes(Node s, Node e)
	{
		HashMap<String, Relationship> path = findPathRels(s,e);
		//System.out.println("ecco un percorso lungo: " + path.size());
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			Iterator<String> ks = path.keySet().iterator();
			while(ks.hasNext())
			{ 
				String a = ks.next();
				Relationship attuale = path.get(a);
				String sorgente = attuale.getProperties("from").values().toString();
				sorgente = pulisci(sorgente);
				String destinazione = attuale.getProperties("to").values().toString();
				destinazione = pulisci(destinazione);
				Globals.inCycleNodes.put(sorgente, sorgente);
				Globals.inCycleNodes.put(destinazione, destinazione);
			}
			tx.success();
		}

	}
	
	public static void findPathNodesSecond(Node s, Node e)
	{
		HashMap<String, Relationship> path = findPathRelsSecond(s,e);
		//System.out.println("ecco un percorso lungo: " + path.size());
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
			Iterator<String> ks = path.keySet().iterator();
			while(ks.hasNext())
			{ 
				String a = ks.next();
				Relationship attuale = path.get(a);
				String sorgente = attuale.getProperties("from").values().toString();
				sorgente = pulisci(sorgente);
				String destinazione = attuale.getProperties("to").values().toString();
				destinazione = pulisci(destinazione);
				Globals.inCycleNodes.put(sorgente, sorgente);
				Globals.inCycleNodes.put(destinazione, destinazione);
			}
			tx.success();
		}
	}
	
	
		
}
