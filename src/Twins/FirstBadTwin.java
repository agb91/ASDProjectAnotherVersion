package Twins;

import java.util.HashMap;
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
			long bdTime = 0;
			long prima = System.currentTimeMillis();
			if(!inInteger(1,Globals.badTwinDid))
			{
				try ( Transaction tx = Globals.graphDb.beginTx() )
				{
					Vector<String> tPrimo = new Vector<String>();
					tPrimo = riempiTPrimo(1);
					for(int i=1; i<Globals.allNodes.size(); i++)
					{
						Node nodoAttuale = Globals.allNodes.get(i);
						String nomeNodo = pulisci(nodoAttuale.getProperties("name").values().toString());
						HashMap<String,Relationship> rels =getAllRelationsUntilHash(0, Globals.allRelationsGeneralHash);
						//System.err.println("contenuti: " + rels.size());
						Iterator<String> keyset = rels.keySet().iterator();
						while(keyset.hasNext())
						{ 
							String a = keyset.next();
							Relationship transazioneAttuale = rels.get(a);
							String from = pulisci(transazioneAttuale.getProperties("from").values().toString());
							if(from.equalsIgnoreCase("inizio"))
							{
								continue;
							}
							Node destinazione = transazioneAttuale.getEndNode();
							String osservabilita = pulisci(transazioneAttuale.getProperties("oss").values().toString());
							
	
							//System.out.println("oss:  " + osservabilita);
							//System.out.println("--------------------------------");
							if(from.equalsIgnoreCase(nomeNodo) 
									&& osservabilita.equalsIgnoreCase("n"))
							{
								String nomeTransizione = pulisci(transazioneAttuale.getProperties("type").values().toString());
								//System.out.println("sono la trans9zione : " + nomeTransizione);
								boolean fault;
								String guasto = pulisci(transazioneAttuale.getProperties("guasto").values().toString());
								//System.out.println("guasto: " + guasto);
								if(guasto.equalsIgnoreCase("y"))
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
								HashMap<String,Tripletta> insiemeTriplette = Find.findHash(destinazione,1,fault,eventoNullo,0); 
								// l'ultimo parametro è 0 perchè il find lavora suo dati del livello prima... 1-1 = 0
								Iterator<String> ks = insiemeTriplette.keySet().iterator();
								while(ks.hasNext())
								{ 
									String an = ks.next();
									Tripletta triplettaAttuale = insiemeTriplette.get(an);
									String guastoAttuale = "n";
									//System.out.println("ho letto fault: " + triplettaAttuale.isFaultPrimo());
									if(triplettaAttuale.isFaultPrimo())
									{
										guastoAttuale = "y";
									}
									String sorgente = pulisci(nodoAttuale.getProperties("name").values().toString());
									String destination = pulisci(triplettaAttuale.getsDestinazione().getProperties("name").values().toString());
									//System.out.println("sto addando con evento:  " + triplettaAttuale.getEvento());
									String id = sorgente + "--" + triplettaAttuale.getEvento() +"--" + destination + "--" + guastoAttuale;
									long before = System.currentTimeMillis();
									addRelationBad(nodoAttuale, triplettaAttuale.getsDestinazione(), 
											id, "y", triplettaAttuale.getEvento() 
											, guastoAttuale, 1);
									long after = System.currentTimeMillis();
									bdTime += (after-before);
									tPrimo.add(id);
								}
							}
						}
					}	
					long seconda = System.currentTimeMillis();
					System.err.println("tempo per il first bad twin:" + 
									( (seconda-prima)-bdTime) );
					ORM.updateDb(1);
					removeIsolatedStatesBad(1);
					System.out.println("---------------------------------------------");
					System.out.println("created bad twin level 1");
					tx.success();
					Globals.badTwinDid.addElement(1);
				}
			}	
		}
		
		
			
		
		

}
