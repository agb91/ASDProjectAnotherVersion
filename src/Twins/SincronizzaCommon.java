package Twins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Stack;
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
import talkToDb.NodoTarjan;
import usefullAbstract.GenericGraphHandler;
import usefullAbstract.InVector;

public class SincronizzaCommon extends GenericGraphHandler{
	
	private static int indexTarjan = 0;
	private static Stack<NodoTarjan> S = new Stack<NodoTarjan>();
	private static Vector<HashMap<String, NodoTarjan>> componentiConnesse= new Vector<HashMap<String, NodoTarjan>>();
	
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
		for(int i=0; i<Sdue.size(); i++)
		{
			String attuale = Sdue.get(i);
			if(nuovo.equalsIgnoreCase(attuale))
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
		searchCycleTarjan(in, Tdue, who);
		//searchCycle(in, who);
		//Iterator<String> ks = Globals.inCycleNodes.keySet().iterator();
		//System.err.println("dim in cycle nodes : " + Globals.inCycleNodes.size());
	/*	while(ks.hasNext())
		{ 
			String a = ks.next();
			System.err.println(" nodi in cicli : " + Globals.inCycleNodes.get(a));
		}*/
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
	
	protected static void searchCycleTarjan(Vector<String> sdue, 
			HashMap<String, TransizioneDoppia> tdue, String who)
	{
		//System.err.println(sdue.size() + "--> " + sdue.get(3));
		//System.err.println(tdue.size() + "--" + tdue.get(tdue.keySet().iterator().next()));
		
		indexTarjan = 0;
		S = new Stack<NodoTarjan>();
		HashMap<String, NodoTarjan> V = new HashMap<String, NodoTarjan>();
		for(int i=0; i<sdue.size(); i++)
		{
			NodoTarjan n = new NodoTarjan(sdue.get(i), 9876, 999); //9876 significa indefinito
			V.put(n.getNome(),n);
		}
		
		Iterator<String> k = V.keySet().iterator();
		while(k.hasNext())
		{ 
			String a = k.next();
			if(V.get(a).getIndex()==(9876))
			{
				tarjan(a, V, tdue);				
			}
		}
	}
	
	protected static void tarjan(String indexCiclo, HashMap<String, NodoTarjan> sdue, 
			HashMap<String, TransizioneDoppia> tdue)
	{
		sdue.get(indexCiclo).setIndex(indexTarjan);
		sdue.get(indexCiclo).setMinDist(indexTarjan);
		indexTarjan++;
		NodoTarjan v = sdue.get(indexCiclo); 
		//System.err.println("addo : " +aggiungi.getNome());
		S.push(v);
		Iterator<String> k = tdue.keySet().iterator();
		while(k.hasNext())
		{ 
			String a = k.next();
			TransizioneDoppia attuale = tdue.get(a);
			if(attuale.getSorgente().equalsIgnoreCase(v.getNome()))
			{
				//System.err.println("sorgente: " + attuale.getSorgente());
				String dest = attuale.getDestinazione();
				NodoTarjan found = sdue.get(dest);
				//System.err.println("dest: " + dest + "; index: " + found.getIndex());
				if(found.getIndex()==(9876))
				{
					//System.err.println("prima : " + sdue.get(dest).getMinDist());
					tarjan(sdue.get(dest).getNome(), sdue, tdue);
					//System.err.println("dopo : " + sdue.get(dest).getMinDist());
					int dm = sdue.get(dest).getMinDist();
					sdue.get(indexCiclo).setMinDist(dm);
				}
				else
				{
					if(inS(sdue.get(dest)))
					{
						int dm = sdue.get(dest).getMinDist();
						sdue.get(indexCiclo).setMinDist(dm);
					}
				}
			}
		}
		if(sdue.get(indexCiclo).getMinDist()==sdue.get(indexCiclo).getIndex())
		{
			//System.err.println("trovato insieme di componenti fortemente connessi");
			//HashMap<String, NodoTarjan> adder = new HashMap<String, NodoTarjan>();
			boolean ancora = true;
			//System.err.println("--------------------------------");
			while(!S.isEmpty() && ancora)
			{
				NodoTarjan nuovo = S.pop();
				//adder.put(nuovo.getNome(), nuovo);
			//	System.err.println("aggiungo: " + nuovo.getNome() + 
			//			", uguaglianza: " + sdue.get(indexCiclo).getMinDist());
				if(!nuovo.getNome().equalsIgnoreCase(sdue.get(indexCiclo).getNome()))
				{
					//System.err.println("sgam: " + nuovo.getNome());
					ancora= false;	
				}
				//System.err.println("addo: " + nuovo.getNome());
				Globals.inCycleNodes.put(nuovo.getNome(), nuovo.getNome());
			}
			//System.err.println("adder : " + adder.size());
			//componentiConnesse.addElement(adder);
		}
		
	}
	
	private static boolean inS(NodoTarjan n)
	{
		for(int i=0; i<S.size(); i++)
		{
			String s = S.get(i).getNome();
			String s1 = n.getNome();
			if (s.equalsIgnoreCase(s1))
			{
				return true;
			}
		}
		return false;
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
			Iterator<Path> iteratore = findPathSyncro(s,e);
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
