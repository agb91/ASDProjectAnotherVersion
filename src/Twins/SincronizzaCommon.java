package Twins;

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
	
	protected static boolean nuovaTransizione(TransizioneDoppia nuovo, Vector<TransizioneDoppia> pagliaio)
	{
		String eventoNuovo = nuovo.getEvento();
		//String guastoNuovo = nuovo.getGuasto();
		//System.out.println("primoN: " + primoNuovo + ";   secondoN : " + secondoNuovo);
		for(int i=0; i<pagliaio.size(); i++)
		{
			TransizioneDoppia attuale = pagliaio.get(i);
			String evento = attuale.getEvento();
			//String guasto = attuale.getGuasto();
			if(stessoStato(attuale.getSorgente(),nuovo.getSorgente()) 
				&& stessoStato(attuale.getDestinazione(),nuovo.getDestinazione())
				&& InVector.stessoEvento(eventoNuovo,evento))
			{
				return false;
			}
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
	
	protected static boolean checkSeconda(Vector<Relationship> T, int level)
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
	
	protected static boolean checkTerza(Vector<Relationship> T, int level)
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
			Vector<TransizioneDoppia> ta, Vector<TransizioneDoppia> Tdue, String who)
	{
		boolean ris = false;
		
		if(ta.size()==0)
		{
			return false;
		}
		searchCycle(in, who);
		searchFirstAmbiguous(ta, Tdue, in, who);
		ris = checkIfFromAmbiguousGoToCycle(who);
		
		return ris;
	}
	
		
	protected static boolean checkIfFromAmbiguousGoToCycle(String who)
	{
		//prima trovo tutte le destinazioni delle transizioni ambigue
		Vector<String> destinazioniAmbigue = new Vector<String>();
		for(int i=0; i<Globals.primeTransizioniAmbigue.size(); i++)
		{
			TransizioneDoppia attuale = Globals.primeTransizioniAmbigue.get(i);
			String dest = attuale.getDestinazione();
		//	System.err.println("destinazione ambigua: " + dest);
			destinazioniAmbigue.addElement(dest);
		}
		
		//poi verifico se esse sono in un ciclo
		for(int a=0; a<destinazioniAmbigue.size(); a++)
		{
			for(int i=0; i<Globals.inCycleNodes.size(); i++)
			{
				String nome = Globals.inCycleNodes.get(i);
				//System.err.println("confronto: " +nome + "; con " + destinazioniAmbigue.get(a));
				if(stessoStato(nome, destinazioniAmbigue.get(a)))
				{
					return true;
				}
			}
		}

		/*if(who.equalsIgnoreCase("f"))
			{
				try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
				{
		
			// ultimo caso: non è in un ciclo ma può raggiungere un nodo
			// che è in un ciclo
			for(int a=0; a<destinazioniAmbigue.size(); a++)
			{
				Node primo = findNodeByNameSyncro(destinazioniAmbigue.get(a));
				for(int i=0; i<Globals.inCycleNodes.size(); i++)
				{
					Node secondo = findNodeByNameSyncro(Globals.inCycleNodes.get(i));
					Vector<Relationship> path = findPathRels(primo, secondo);
					if(path.size()>0)
					{
						//System.out.println("FATTO2");
						return true;
					}
				}
			}
			}
			tx.success();
		}*/
		return false;
	}
	
	protected static void searchFirstAmbiguous(Vector<TransizioneDoppia> ta, 
			Vector<TransizioneDoppia> Tdue, Vector<String> nodi, String who)
	{
		for(int i=0; i<ta.size(); i++)
		{
			String sorgenteAmbigua = ta.get(i).getSorgente();
			boolean prima = true;
			Vector<Relationship> path = null;
			if(who.equalsIgnoreCase("f"))
			{
				Node from = findNodeByNameSyncro(nodi.get(0));
				Node to = findNodeByNameSyncro(sorgenteAmbigua);
				path = findPathRels(from,to);
			}
			else
			{
				Node from = findNodeByNameSyncroSecond(nodi.get(0));
				Node to = findNodeByNameSyncroSecond(sorgenteAmbigua);
				path = findPathRelsSecond(from,to);
			}
			for(int a=0; a<path.size(); a++)
			{
				if(InVector.inVettoreSyncro(path.get(a), ta, who))
				{
					prima = false;
				}
			}
			if(prima)
			{
				Globals.primeTransizioniAmbigue.addElement(ta.get(i));
			}
		}
	}
	
	protected static void searchCycle(Vector<String> sdue, String who)
	{
		Globals.inCycleNodes.clear();
		for(int i=0; i<sdue.size(); i++)
		{
		   if(!InVector.inVettore(sdue.get(i), Globals.inCycleNodes))
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

	public static Vector<Relationship> findPathRels(Node s, Node e)
	{
		Iterator<Path> iteratore = findPath(s,e);
		Iterator<Relationship> result = null;
		Vector<Relationship> ris = new Vector<Relationship>();
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
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
						ris.addElement(result.next());
	                }while(result.hasNext());
	                if(ris.size()!=0)
	                {
	                	return ris;
	                }
				}    
			}while(iteratore.hasNext());
			tx.success();
		}
		return ris;
	}
	
	public static Vector<Relationship> findPathRelsSecond(Node s, Node e)
	{
		Iterator<Path> iteratore = findPathSecond(s,e);
		Iterator<Relationship> result = null;
		Vector<Relationship> ris = new Vector<Relationship>();
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
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
						ris.addElement(result.next());
	                }while(result.hasNext());
	                if(ris.size()!=0)
	                {
	                	return ris;
	                }
				}    
			}while(iteratore.hasNext());
			tx.success();
		}
		return ris;
	}

	
	public static void findPathNodes(Node s, Node e)
	{
		Vector<Relationship> path = findPathRels(s,e);
		//System.out.println("ecco un percorso lungo: " + path.size());
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			for(int i=0; i<path.size(); i++)
			{
				Relationship attuale = path.get(i);
				String sorgente = attuale.getProperties("from").values().toString();
				sorgente = pulisci(sorgente);
				String destinazione = attuale.getProperties("to").values().toString();
				destinazione = pulisci(destinazione);
				if(!InVector.inVettore(sorgente,Globals.inCycleNodes))
				{
					Globals.inCycleNodes.addElement(sorgente);
				}
				if(!InVector.inVettore(destinazione,Globals.inCycleNodes))
				{
					Globals.inCycleNodes.addElement(destinazione);
				}
					
			}
			tx.success();
		}
	}
	
	public static void findPathNodesSecond(Node s, Node e)
	{
		Vector<Relationship> path = findPathRelsSecond(s,e);
		//System.out.println("ecco un percorso lungo: " + path.size());
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
			for(int i=0; i<path.size(); i++)
			{
				Relationship attuale = path.get(i);
				String sorgente = attuale.getProperties("from").values().toString();
				sorgente = pulisci(sorgente);
				String destinazione = attuale.getProperties("to").values().toString();
				destinazione = pulisci(destinazione);
				if(!InVector.inVettore(sorgente,Globals.inCycleNodes))
				{
					Globals.inCycleNodes.addElement(sorgente);
				}
				if(!InVector.inVettore(destinazione,Globals.inCycleNodes))
				{
					Globals.inCycleNodes.addElement(destinazione);
				}
					
			}
			tx.success();
		}
	}
	
	
		
}
