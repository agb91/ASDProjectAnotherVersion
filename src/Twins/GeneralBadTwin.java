package Twins;

import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import talkToDb.ORM;
import talkToDb.Tripletta;
import usefullAbstract.GenericGraphHandler;

public class GeneralBadTwin extends GenericGraphHandler{
	
	static Vector<String> Tprimo = new Vector<String>();
	
	public static void createBadTwinGeneral(int livello)
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			Vector<String> tPrimo = new Vector<String>();
			tPrimo = riempiTPrimo();
			/*for(int s=0; s<tPrimo.size(); s++)
			{
				System.out.println("tPrimo prima: " + tPrimo.get(s));
			}*/
			for(int i=1; i<Globals.allNodes.size(); i++)
			{
				Node nodoAttuale = Globals.allNodes.get(i);
				String nomeNodo = pulisci(nodoAttuale.getProperties("name").values().toString());
				for(int a=1; a<Globals.allRelations.size(); a++)
				{
					Relationship transazioneAttuale = Globals.allRelations.get(a);
					String from = pulisci(transazioneAttuale.getProperties("from").values().toString());
					String osservabile = pulisci(transazioneAttuale.getProperties("oss").values().toString());
					Node destinazione = transazioneAttuale.getEndNode();
					boolean bool = from.equalsIgnoreCase(nomeNodo) && osservabile.equalsIgnoreCase("y");
					//System.out.println("nome nodo: " + nomeNodo + ";   from : " + from + ";  bool : "+ bool);
					if(bool)
					{
						String nomeTransizione = transazioneAttuale.getProperties("type").values().toString();
						String eventoTransizione = transazioneAttuale.getProperties("event").values().toString();
						int cardinalita = getCardinalita(eventoTransizione);
						boolean fault;
						String guasto = pulisci(transazioneAttuale.getProperties("guasto").values().toString());
						if(guasto.equalsIgnoreCase("y"))
						{
							//System.out.println("ho trovato guasto");
							fault = true;
						}
						else
						{
							fault = false;
						}
						//System.out.println("fauls:  " + fault + ";     guasto: " + guasto);
						// il grafo A è già definito globalmente
						Vector<Tripletta> insiemeTriplette = Find.find(destinazione,(livello-cardinalita),fault,eventoTransizione); 
						//System.out.println("uscite dal find: " + insiemeTriplette.size());
						for(int k = 0; k<insiemeTriplette.size(); k++)
						{
							Tripletta triplettaAttuale = insiemeTriplette.get(k);
							String eventoTripletta = triplettaAttuale.getEvento();
							String guastoAttuale = "n";
							if(triplettaAttuale.isFaultPrimo())
							{
								guastoAttuale = "y";
							}							
							if(!identici(eventoTripletta))
							{
								String sorgente = pulisci(nodoAttuale.getProperties("name").values().toString());
								String destination = pulisci(triplettaAttuale.getsDestinazione().getProperties("name").values().toString());
								String id = sorgente + "--" + triplettaAttuale.getEventoOrdered() + "--" + destination + "--" + guastoAttuale;
								//System.out.println("tripletta: " + id);
								addRelationBad(nodoAttuale, triplettaAttuale.getsDestinazione(), 
										id, "y", triplettaAttuale.getEvento() , guastoAttuale);
								tPrimo.add(id);
							}
						}
					}
				}
			}		
			/*for(int d = 0; d<tPrimo.size(); d++)
			{
				System.out.println("t primo: " + tPrimo.get(d));
			}*/
			ORM.updateDb(tPrimo);
			removeIsolatedStatesBad();
			System.out.println("---------------------------------------------");
			System.out.println("created bad twin level" + livello);
			tx.success();
		}
	}
	
	public static boolean checkC2C3()
	{
		//secondo caso se è deterministico allora è diagnosticabile
		if(deterministic(Globals.allRelations))
		{
			System.out.println("vale C2: il bad twin è deterministico");
			return true;
		}
		
		//cerco le transizioni di guasto: prendo il loro evento.
		// se per tutti quegli eventi non esistono transizioni di guasto
		// che abbiano come evento quegli eventi allora è diagnosticabile
		if(thirdCondition(Globals.allRelations))
		{
			System.out.println("vale la C3");
			return true;
		}
		return false;
	}
	
	private static boolean identici( String evento)
	{
		String elenco[] = evento.split("//");
		for(int a=0; a<elenco.length ; a++)
		{
			for(int k=0; k<elenco.length; k++)
			{
				if(a!=k)
				{
					if(!elenco[k].equalsIgnoreCase(elenco[a]))
					{
						//System.out.println("entra in identici: " + evento + "rispondo false;");
						return false;
					}
				}
			}
		}
		//System.out.println("entra in identici: " + evento + "rispondo true;");
		return true;		
	}
	
	private static int getCardinalita(String tr)
	{
		int ris = 0;
		ris = tr.split("//").length;
		return ris;
	}
}
