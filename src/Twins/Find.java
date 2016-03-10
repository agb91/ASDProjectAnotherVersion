package Twins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import global.Globals;
import talkToDb.Tripletta;
import usefullAbstract.GenericGraphHandler;

public class Find extends GenericGraphHandler{
	
	private static int getCardinalita(String e)
	{
		int ris;
		ris = e.split("//").length;
		return ris;
	}

	public static Vector<Tripletta> find (Node s, int n, boolean fault, String ot, int level)
	{
		ot = pulisci(ot);
		Vector<Tripletta> risultato = new Vector<Tripletta>();
		Vector<Relationship> allRelationsUntilNow = getAllRelationsUntil(level, Globals.allRelationsGeneralHash); //perchè è compreso questo...
		for(int q=0; q<allRelationsUntilNow.size(); q++)
		{
			Relationship transazioneAttuale = allRelationsUntilNow.get(q);
			String fromRichiesto = pulisci(s.getProperties("name").values().toString());
			String fromTransazioneAttuale = pulisci(transazioneAttuale.getProperties("from").values().toString());
			if(fromTransazioneAttuale.equalsIgnoreCase("inizio"))
			{
				continue;
			}
			/*System.out.println("fromRichiesto:  " + fromRichiesto);
			System.out.println("transazione attuale from : " + fromTransazioneAttuale);
			System.out.println("--------------------------------------------------");
			*/
			if(fromTransazioneAttuale.equalsIgnoreCase(fromRichiesto))
			{
				boolean faultPrimo;
				String guasto = pulisci(transazioneAttuale.getProperties("guasto").values().toString());
				String osservabile = pulisci(transazioneAttuale.getProperties("oss").values().toString());
				String evento = pulisci(transazioneAttuale.getProperties("event").values().toString());
				Node sPrimo = transazioneAttuale.getEndNode();
				int cardinalitaEvento = getCardinalita(evento);
				//System.out.println("trovo cardinalità == : " + cardinalitaEvento);
				//System.out.println("evento letto : " + evento);
				//System.out.println("guasto letto: " + guasto);
				if(guasto.toLowerCase().contains("y"))
				{
					faultPrimo=true;
				}
				else
				{
					faultPrimo = fault;
				}
				//System.out.println("oss: " + osservabile);
				if(osservabile.equalsIgnoreCase("y") && cardinalitaEvento <= n)
				{
					String eventoTripletta = evento + "//" + ot;
					eventoTripletta = pulisciEvento(eventoTripletta);
					if(n==cardinalitaEvento)
					{
						Tripletta aggiungi = new Tripletta(eventoTripletta, sPrimo, faultPrimo );
						risultato.addElement(aggiungi);
					}
					else
					{
						Vector<Tripletta> aggiunta = find(sPrimo, (n-cardinalitaEvento), faultPrimo, eventoTripletta, level );
						risultato.addAll(aggiunta);
					}
				}
				if(osservabile.equalsIgnoreCase("n"))
				{
					Vector<Tripletta> aggiunta = find(sPrimo, n, faultPrimo, ot, level);
					risultato.addAll(aggiunta);
				}
			}
		}
		return risultato;
	}
	
	public static HashMap<String, Tripletta> findHash (Node s, int n, boolean fault, String ot, int level)
	{
		ot = pulisci(ot);
		HashMap<String, Tripletta> risultato = new HashMap<String, Tripletta>();
		HashMap<String, Relationship> allRelationsUntilNow = getAllRelationsUntilHash(level, Globals.allRelationsGeneralHash); //perchè è compreso questo...
		Iterator<String> keyset = allRelationsUntilNow.keySet().iterator();
		while(keyset.hasNext())
		{ 
			String a = keyset.next();
			Relationship transazioneAttuale = allRelationsUntilNow.get(a);
			String fromRichiesto = pulisci(s.getProperties("name").values().toString());
			String fromTransazioneAttuale = pulisci(transazioneAttuale.getProperties("from").values().toString());
			if(fromTransazioneAttuale.equalsIgnoreCase("inizio"))
			{
				continue;
			}
			/*System.out.println("fromRichiesto:  " + fromRichiesto);
			System.out.println("transazione attuale from : " + fromTransazioneAttuale);
			System.out.println("--------------------------------------------------");
			*/
			if(fromTransazioneAttuale.equalsIgnoreCase(fromRichiesto))
			{
				boolean faultPrimo;
				String guasto = pulisci(transazioneAttuale.getProperties("guasto").values().toString());
				String osservabile = pulisci(transazioneAttuale.getProperties("oss").values().toString());
				String evento = pulisci(transazioneAttuale.getProperties("event").values().toString());
				Node sPrimo = transazioneAttuale.getEndNode();
				int cardinalitaEvento = getCardinalita(evento);
				//System.out.println("trovo cardinalità == : " + cardinalitaEvento);
				//System.out.println("evento letto : " + evento);
				//System.out.println("guasto letto: " + guasto);
				if(guasto.equalsIgnoreCase("y"))
				{
					faultPrimo=true;
				}
				else
				{
					faultPrimo = fault;
				}
				//System.out.println("oss: " + osservabile);
				if(osservabile.equalsIgnoreCase("y") && cardinalitaEvento <= n)
				{
					String eventoTripletta = evento + "//" + ot;
					eventoTripletta = pulisciEvento(eventoTripletta);
					if(n==cardinalitaEvento)
					{
						Tripletta aggiungi = new Tripletta(eventoTripletta, sPrimo, faultPrimo );
						risultato.put(eventoTripletta +"-"+ sPrimo+"-"+ faultPrimo,aggiungi);
					}
					else
					{
						HashMap<String,Tripletta> aggiunta = findHash(sPrimo, (n-cardinalitaEvento), faultPrimo, eventoTripletta, level );
						Iterator<String> ks = aggiunta.keySet().iterator();
						while(ks.hasNext())
						{ 
							String an = ks.next();
							risultato.put(an , aggiunta.get(an));
						}
					}
				}
				if(osservabile.equalsIgnoreCase("n"))
				{
					if(osservabile.equalsIgnoreCase("n"))
					{
						HashMap<String, Tripletta> aggiunta = findHash(sPrimo, n, faultPrimo, ot, level);
						Iterator<String> ks = aggiunta.keySet().iterator();
						while(ks.hasNext())
						{ 
							String an = ks.next();
							risultato.put(an , aggiunta.get(an));
						}
					}
				}
			}
		}
		return risultato;
	}
	
	
	private static String pulisciEvento(String evento)
	{
		//System.out.println("evento prima: " + evento);
		int limite = evento.length() -2;
		if(evento.substring(limite).equalsIgnoreCase("//"))
		{
			evento = evento.substring(0, limite);
		}
		//System.out.println("evento dopo: " + evento);
		return evento;
	}
	

}