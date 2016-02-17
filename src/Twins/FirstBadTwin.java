package Twins;

import java.util.Iterator;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import talkToDb.ORM;
import talkToDb.Tripletta;
import talkToDb.ORM.RelTypes;
import usefullAbstract.GenericGraphHandler;

public class FirstBadTwin extends GenericGraphHandler {
	
	// setta tutte le transizione ad "osservabile", nient'altro
		public static void createBadTwinLevel1()
		{
			try ( Transaction tx = Globals.graphDb.beginTx() )
			{
				Vector<String> tPrimo = new Vector<String>();
				tPrimo = riempiTPrimo();
				for(int i=1; i<Globals.allNodes.size(); i++)
				{
					Node nodoAttuale = Globals.allNodes.get(i);
					String nomeNodo = nodoAttuale.getProperties("name").values().toString();
					//System.out.println("--------------------------\n\n");
					//System.out.println("nodo: " + nomeNodo);
					for(int a=1; a<Globals.allRelations.size(); a++)
					{
						Relationship transazioneAttuale = Globals.allRelations.get(a);
						String from = transazioneAttuale.getProperties("from").values().toString();
						Node destinazione = transazioneAttuale.getEndNode();
						String osservabilita = transazioneAttuale.getProperties("oss").values().toString();
						

						//System.out.println("oss:  " + osservabilita);
						//System.out.println("--------------------------------");
						if(from.contains(nomeNodo) && osservabilita.toLowerCase().contains("n"))
						{
							String nomeTransizione = transazioneAttuale.getProperties("type").values().toString();
							//System.out.println("sono la trans9zione : " + nomeTransizione);
							boolean fault;
							String guasto = transazioneAttuale.getProperties("guasto").values().toString();
							//System.out.println("guasto: " + guasto);
							if(guasto.toLowerCase().contains("y"))
							{
								//System.out.println("ho trovato guasto");
								fault = true;
							}
							else
							{
								fault = false;
							}
							String eventoNullo = "";
							// il grafo A è già definito globalmente
							Vector<Tripletta> insiemeTriplette = Find.find(destinazione,1,fault,eventoNullo); 
							for(int k = 0; k<insiemeTriplette.size(); k++)
							{
								Tripletta triplettaAttuale = insiemeTriplette.get(k);
								String guastoAttuale = "n";
								//System.out.println("ho letto fault: " + triplettaAttuale.isFaultPrimo());
								if(triplettaAttuale.isFaultPrimo())
								{
									guastoAttuale = "y";
								}
								String sorgente = pulisci(nodoAttuale.getProperties("name").values().toString());
								String destination = pulisci(triplettaAttuale.getsDestinazione().getProperties("name").values().toString());
								String id = sorgente + "--" + triplettaAttuale.getEvento() +"--" + destination + "--" + guastoAttuale;
								addRelationBad(nodoAttuale, triplettaAttuale.getsDestinazione(), 
										id, "y", triplettaAttuale.getEvento() 
										, guastoAttuale);
								tPrimo.add(id);
							}
						}
					}
				}			
				ORM.updateDb(tPrimo);
				removeIsolatedStatesBad();
				System.out.println("---------------------------------------------");
				System.out.println("created bad twin level 1");
				tx.success();
			}	
		}
		
		
			
		
		

}
