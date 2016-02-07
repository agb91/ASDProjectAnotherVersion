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
								String id = "t"+k+i+a;
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
		
		/*private static Relationship addRelationBad(Node n1, Node n2, String nome, String oss, String ev, String gu)
		{
			Relationship relationship = null;
			try ( Transaction tx = Globals.graphDb.beginTx() )
			{
				relationship = n1.createRelationshipTo( n2, RelTypes.STD );
				relationship.setProperty( "type", pulisci(nome) );
				relationship.setProperty( "oss", pulisci(oss) );
				ev = pulisci(ev);
				relationship.setProperty("event", pulisci(ev));
				relationship.setProperty("guasto", pulisci(gu));
				String nomeN1 = n1.getProperties("name").values().toString();
				String nomeN2 = n2.getProperties("name").values().toString();	
				relationship.setProperty("from", pulisci(nomeN1));
				relationship.setProperty("to", pulisci(nomeN2));
				tx.success();
				Globals.allRelations.addElement(relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			}	
			return relationship;
		}*/
		
		
		private static Vector<String> riempiTPrimo()
		{
			Vector<String> ris = new Vector<String>();
			for(int i=0; i<Globals.allRelations.size(); i++)
			{
				Relationship attuale = Globals.allRelations.get(i);
				String osservabilita = attuale.getProperties("oss").values().toString();
				if(osservabilita.contains("y"))
				{
					String nome = attuale.getProperties("type").values().toString();
					ris.add(nome);
				}
			}
			return ris;
		}
		
		public static void removeIsolatedStatesBad()
		{
			boolean raggiungibile = false;
			for(int i=1; i<Globals.allNodes.size(); i++)
			{
				//System.out.println("check del nodo: " + Globals.allNodes.get(i));
				raggiungibile = checkPathFromRootBad(Globals.allNodes.get(i));
				if(!raggiungibile)
				{
					killNode(Globals.allNodes.get(i), i);
					i--;
				}
			}
		}
		
		//prima elimino tutte le relazioni che partono da n
		//poi elimino n
		public static void killNode(Node n, int index)
		{
			Globals.allNodes.remove(index);
			String nomeNode = n.getProperties("name").values().toString();
			for(int a=0; a<Globals.allRelations.size(); a++)
			{
				Relationship r = Globals.allRelations.get(a);
				String fromr = r.getProperties("from").values().toString();
				if(fromr.contains(nomeNode))
				{
					Globals.allRelations.get(a).delete();
					Globals.allRelations.remove(a);
					a--;		
				}
			}
			n.delete();
		}
		
		
		public static boolean checkPathFromRootBad(Node n)
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
		
			
		
		

}
