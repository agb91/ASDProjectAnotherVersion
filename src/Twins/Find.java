package Twins;

import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import global.Globals;
import talkToDb.Tripletta;

public class Find {
	
	private static int getCardinalita(String e)
	{
		int ris;
		ris = e.split("//").length;
		return ris;
	}
	
	public static Vector<Tripletta> find (Node s, int n, boolean fault, String eventoNullo)
	{
		Vector<Tripletta> risultato = new Vector<Tripletta>();
		for(int q=1; q<Globals.allRelations.size(); q++)
		{
			Relationship transazioneAttuale = Globals.allRelations.get(q);
			String fromRichiesto = s.getProperties("name").values().toString();
			String fromTransazioneAttuale = transazioneAttuale.getProperties("from").values().toString();
			//System.out.println("fromRichiesto:  " + fromRichiesto);
			//System.out.println("transazione attuale from : " + fromTransazioneAttuale);
			if(fromTransazioneAttuale.contains(fromRichiesto))
			{
				boolean faultPrimo;
				String guasto = transazioneAttuale.getProperties("guasto").values().toString();
				String osservabile = transazioneAttuale.getProperties("oss").values().toString();
				String evento = transazioneAttuale.getProperties("event").values().toString();
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
				if(osservabile.toLowerCase().contains("y") && cardinalitaEvento <= n)
				{
					String eventoTripletta = evento;
					if(eventoNullo.length()!=0)
					{
						eventoTripletta = eventoTripletta + "//" + eventoNullo;
					}
					if(n==cardinalitaEvento)
					{
						Tripletta aggiungi = new Tripletta(eventoTripletta, sPrimo, faultPrimo );
						risultato.addElement(aggiungi);
					}
					else
					{
						Vector<Tripletta> aggiunta = find(sPrimo, (n-cardinalitaEvento), faultPrimo, eventoTripletta);
						risultato.addAll(aggiunta);
					}
				}
				if(osservabile.toLowerCase().contains("n"))
				{
					Vector<Tripletta> aggiunta = find(sPrimo, n, faultPrimo, eventoNullo);
					risultato.addAll(aggiunta);
				}
			}
		}
		return risultato;
	}
	

}
