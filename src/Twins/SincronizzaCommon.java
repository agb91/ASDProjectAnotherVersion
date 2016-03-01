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
				&& uguali(eventoNuovo,evento))
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

	protected static boolean checkPrima(Vector<TransizioneDoppia> Ta,int level)
	{
		if(Ta.size()==0)
		{
			System.out.println("vale C1: "
					+ "non ci sono transizioni ambigue nell'automa sincronizzato");
			System.out.println("finisco la sincronizzazione di livello: " + level);
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
			System.out.println("finisco la sincronizzazione di livello: " + level);
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
			System.out.println("finisco la sincronizzazione di livello: " + level);
			return true;
		}
		else
		{
			System.out.println("non vale C3");
		}
		return false;
	}
		
	protected static boolean checkQuarta(Vector<String> in, int level, 
			Vector<TransizioneDoppia> ta, Vector<TransizioneDoppia> Tdue)
	{
		searchCycle(in);
		searchFirstAmbiguous(ta, Tdue, in);
		System.out.println("le transizioni da chekkare sono : "
				+Globals.primeTransizioniAmbigue.size());
		return false;
	}
	
	protected static void searchFirstAmbiguous(Vector<TransizioneDoppia> ta, 
			Vector<TransizioneDoppia> Tdue, Vector<String> nodi)
	{
		Vector<String> sorgentiAmbigue = new Vector<String>();
		for(int i=0; i<ta.size(); i++)
		{
			sorgentiAmbigue.add(ta.get(i).getSorgente());
		}
		for(int i=0; i<sorgentiAmbigue.size(); i++)
		{
			Node from = findNodeByNameSyncro(nodi.get(0));
			Node to = findNodeByNameSyncro(sorgentiAmbigue.get(i));
			Vector<Relationship> path = findPathRels(from,to);
			for(int a=0; a<path.size(); a++)
			{
				Relationship nome = path.get(a);
				if(!inVettoreSyncro(nome,ta));
				{
					Globals.primeTransizioniAmbigue.add(nome);
				}
			}
			
		}		
	}
	
	protected static void searchCycle(Vector<String> sdue)
	{
		Globals.inCycleNodes.clear();
		for(int i=0; i<sdue.size(); i++)
		{
		   if(!inVettore(sdue.get(i), Globals.inCycleNodes))
		   {
			   Node n = findNodeByNameSyncro(sdue.get(i));
			   findPathNodes(n,n);
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
				int a=0;
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
				if(!inVettore(sorgente,Globals.inCycleNodes))
				{
					Globals.inCycleNodes.addElement(sorgente);
				}
				if(!inVettore(destinazione,Globals.inCycleNodes))
				{
					Globals.inCycleNodes.addElement(destinazione);
				}
					
			}
			tx.success();
		}
	}
	
		
}
