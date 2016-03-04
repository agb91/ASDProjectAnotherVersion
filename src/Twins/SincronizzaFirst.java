package Twins;


import java.util.Arrays;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import usefullAbstract.GenericGraphHandler;

public class SincronizzaFirst extends SincronizzaCommon{
	//first method
	protected static Vector<Node> S = new Vector<Node>();
	protected static Vector<Node> Sprimo = new Vector<Node>();
	protected static Vector<Relationship> T = new Vector<Relationship>();
	protected static Vector<Relationship> Tprimo = new Vector<Relationship>();
	protected static Vector<String> Sdue = new Vector<String>();
	protected static Vector<TransizioneDoppia> Tdue = new Vector<TransizioneDoppia>();
	protected static Vector<TransizioneDoppia> Ta = new Vector<TransizioneDoppia>();
	protected static Vector<String> Sprev = new Vector<String>();
	protected static Vector<String> Sdiff = new Vector<String>();
	
	
	public static boolean syncro(int level)
	{
		if(!inInteger(level,Globals.syncroFirstDid))
		{
			System.out.println("inizio la sincronizzazione di tipo 1 di livello: " + level);
			createData(level);
			algoritmo();
			/*for(int i = 0 ; i<Sdue.size(); i++)
			{
				System.out.println("nodi sincros:  " + Sdue.get(i));
			}
			for(int i = 0 ; i<Ta.size(); i++)
			{
				System.out.println("ta: sorg:" + Ta.get(i).getSorgente() + 
						";   dest : " + Ta.get(i).getDestinazione()
						+ ";    evento: " + Ta.get(i).getEvento());
			}*/
			writeInDb(level);
			Globals.syncroFirstDid.addElement(level);
		}
		return checkQuarta(Sdue, level, Ta, Tdue, "f");
	}
	
	public static boolean diagnosableC4(int level)
	{
		return checkQuarta(Sdue, level, Ta, Tdue, "f");
	}
	
	public static void syncroToSecond(int level)
	{
		if(!inInteger(level,Globals.syncroFirstDid))
		{
			System.out.println("inizio la sincronizzazione di tipo 1 di livello: " + level);
			createData(level);
			algoritmo();
			/*for(int i = 0 ; i<Sdue.size(); i++)
			{
				System.out.println("nodi sincros:  " + Sdue.get(i));
			}
			for(int i = 0 ; i<Ta.size(); i++)
			{
				System.out.println("ta: sorg:" + Ta.get(i).getSorgente() + 
						";   dest : " + Ta.get(i).getDestinazione()
						+ ";    evento: " + Ta.get(i).getEvento());
			}*/
			writeInDb(level);
		}
	}
	
	
	
	private static void writeInDb(int level)
	{
		for(int i=0; i<Sdue.size(); i++)
		{
			String nome = Sdue.get(i);
			addNodeSyncro(nome, level);
		}
		for(int i=0; i<Tdue.size(); i++)
		{
			TransizioneDoppia attuale = Tdue.get(i);
			String n1 = attuale.getSorgente();
			String n2 = attuale.getDestinazione();
			String oss = "y";
			String ev = attuale.getEvento();
			String nome = n1+ "-" + n2 + "- " + oss + "- " + ev;
			//Node n1, Node n2, String nome, String oss, String ev, String gu
			addRelationSyncro(n1, n2, nome, oss, ev, level);
		}
		for(int i=0; i<Ta.size(); i++)
		{
			Globals.allTa.get(level).add(Ta.get(i));	
		}
	}
		
		
	private static void algoritmo()
	{
		//creo sprev
		Sprev.clear();
		for(int i=0; i<Sdue.size(); i++)
		{
			Sprev.addElement(Sdue.get(i));
		}
		bloccoSuperioreSincroAlgo();
		bloccoWhileSincroAlgo();
	}
	
	
	private static void bloccoWhileSincroAlgo()
	{
		//System.out.println("entro in while;");
		while(!checkEqual(Sdue, Sprev))
		{
			/*System.out.println("sono diversi:");
			System.out.println("sdue: " + Sdue.toString());
			System.out.println("sprev: " + Sprev.toString());
			System.out.println("-----------------------------------");*/
			Sdiff.clear();
			for(int i=0; i<Sdue.size(); i++)
			{
				String ago = pulisci(Sdue.get(i));
				boolean safe = true;
				for(int a=0; a<Sprev.size(); a++)
				{
					String pagliaio = pulisci(Sprev.get(a));
					if(pagliaio.equalsIgnoreCase(ago))
					{
						safe = false;
					}
				}
				if(safe)
				{
					Sdiff.addElement(Sdue.get(i));
				}
			}	
			//System.out.println("sdiff:   " + Sdiff);
			//System.out.println("dim di sdiff : " +Sdiff.size());
			
			Sprev.clear();
			for(int i=0; i<Sdue.size(); i++)
			{
				Sprev.add(Sdue.get(i));
			}
			
			for(int i=0; i<Sdiff.size(); i++)
			{
				//System.out.println("entro in sdiff");
				String coppia = Sdiff.get(i);
				String sa = coppia.split("-")[0];
				String sb = coppia.split("-")[1];
				//System.out.println("sa: " + sa + ";   sb: " + sb);
				
				/*for(int s=0; s<T.size(); s++)
				{
					try ( Transaction tx = Globals.graphDb.beginTx() )
					{
						System.out.println("ecco T: " + T.get(s).getProperties("type").values().toString());
						tx.success();
					}
				}*/
				
				
				//System.out.println("qui T vale : " + T.size());
				for(int a=0; a<T.size(); a++)
				{
					for(int k=0; k<T.size(); k++)
					{
						try ( Transaction tx = Globals.graphDb.beginTx() )
						{
							Relationship t1 = T.get(a);
							Relationship t2 = T.get(k);
							String guasto1 = pulisci(t1.getProperties("guasto").values().toString());
							String guasto2 = pulisci(t2.getProperties("guasto").values().toString());
							String evento1 = pulisci(t1.getProperties("event").values().toString());
							String evento2 = pulisci(t2.getProperties("event").values().toString());
							String sorgente1 = pulisci(t1.getProperties("from").values().toString());
							String sorgente2 = pulisci(t2.getProperties("from").values().toString());
							String destinazione1 = pulisci(t1.getProperties("to").values().toString());
							String destinazione2 = pulisci(t2.getProperties("to").values().toString());
								
							boolean bool = (a!=k && guasto2.equalsIgnoreCase("n") && 
									uguali(evento1,evento2) &&
									sorgente1.equalsIgnoreCase(sa)&& sorgente2.equalsIgnoreCase(sb));
							
							if(bool)
							{
								//
								//System.out.println("evento1: " + evento1 + ";  evento2: " + evento2 
								//		+ ";  sorgente1: " + sorgente1 + ";   sorgente2 :" + sorgente2
								//		+ "; destinazione1: " + destinazione1 + ";  dest2 : " + destinazione2);
								TransizioneDoppia tsecondo = new TransizioneDoppia();
								tsecondo.setSorgente(sa+"-"+sb);
								tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
								tsecondo.setEvento(evento1);
								//tsecondo.setGuasto("n");
								String nuovo = destinazione1+"-"+destinazione2;
								if(nuovoStato(nuovo, Sdue))
								{
									Sdue.addElement(nuovo);
								}
								if(nuovaTransizione(tsecondo , Tdue))
								{
									if(guasto1.equalsIgnoreCase("y"))
									{
										Ta.addElement(tsecondo);
										//tsecondo.setGuasto("y");
									}
									Tdue.addElement(tsecondo);
	
								}
							}
						}			
					}
				}				
			}
		}
	}
	
		
	private static void bloccoSuperioreSincroAlgo()
	{
		for(int i=0; i<Sprimo.size(); i++)
		{
			String stato = "";
			try ( Transaction tx = Globals.graphDbGood.beginTx() )
			{
				stato = Sprimo.get(i).getProperties("name").values().toString();
				stato = pulisci(stato);
				//System.out.println("lander: " + stato);
				tx.success();
			}
			
			for(int a=0; a<T.size(); a++)
			{
				for(int k=0; k<T.size(); k++)
				{
					try ( Transaction tx = Globals.graphDb.beginTx() )
					{
						Relationship t1 = T.get(a);
						Relationship t2 = T.get(k);
						String guasto1 = pulisci(t1.getProperties("guasto").values().toString());
						String guasto2 = pulisci(t2.getProperties("guasto").values().toString());
						String evento1 = pulisci(t1.getProperties("event").values().toString());
						String evento2 = pulisci(t2.getProperties("event").values().toString());
						String sorgente1 = pulisci(t1.getProperties("from").values().toString());
						String sorgente2 = pulisci(t2.getProperties("from").values().toString());
						String destinazione1 = pulisci(t1.getProperties("to").values().toString());
						String destinazione2 = pulisci(t2.getProperties("to").values().toString());
						String id1 = pulisci(t1.getProperties("type").values().toString());
						String id2 = pulisci(t2.getProperties("type").values().toString());
						
						
						boolean bool = a!=k && guasto2.equalsIgnoreCase("n")
								&& sorgente2.equalsIgnoreCase(stato) 
								&& sorgente1.equalsIgnoreCase(stato)
								&& uguali(evento1,evento2);
						
						//System.out.println("booo:  " + bool);
						/*if(stato.equalsIgnoreCase("A") && sorgente1.equalsIgnoreCase("A") && sorgente2.equalsIgnoreCase("A"))
						{
								System.out.println(";  evento1: " + evento1 + " evento2 " +evento2 + ";  uguali?: " + uguali(evento1,evento2)) ;
						}*/
						
						if(bool)
						{
							TransizioneDoppia tsecondo = new TransizioneDoppia();
							tsecondo.setSorgente(stato+"-"+stato);
							tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
							tsecondo.setEvento(evento1);
							//tsecondo.setGuasto("n");
							String nuovo = destinazione1+"-"+destinazione2;
							if(nuovoStato(nuovo,  Sdue))
							{
								Sdue.addElement(nuovo);
							}
							//System.out.println("stato: " + destinazione1+"-"+destinazione2);
							if(nuovaTransizione(tsecondo, Tdue))
							{
								if(guasto1.equalsIgnoreCase("y"))
								{
									Ta.addElement(tsecondo);
									//Globals.lastTa.addElement(tsecondo);
									//tsecondo.setGuasto("y");
								}
								Tdue.addElement(tsecondo);
							}

						}
					}			
				}
			}
		}
	}
	
	public static boolean diagnosableC1(int level)
	{
		//primo caso, se non ha transizioni ambigue allora è diagnosticabile
		if (checkPrima(Ta, level))
		{
			System.out.println("al livello: " + level + ", la condizione C1 è vera");
			return true;
		}
		else
		{
			System.out.println("al livello: " + level + ", la condizione C1 è FALSA");
		}
		return false;
	}

		
	private static void createData(int level)
	{
		Ta.clear();
		Sprev.clear();
		Sdiff.clear();
		//risolvo problema puntatore
		S.clear();
		for(int i=0; i<Globals.allNodes.size(); i++)
		{
			S.addElement(Globals.allNodes.get(i));
		}
		
		T.clear();
		//risolvo problema puntatore
		Vector<Relationship> appoggio = getAllRelationsUntil(level, Globals.allRelationsGeneral);
		for(int i=0; i<appoggio.size(); i++)
		{
			T.addElement(appoggio.get(i));
		}
		//System.out.println("T,size:   " + T.size());
		
		//SCELGO DI NON INCLUDERE IL NODO FITTIZIO INIZIALE
		Sprimo.clear();
		for(int i=1; i<Globals.allNodesGood.size(); i++)
		{
			Sprimo.addElement(Globals.allNodesGood.get(i));
		}
	
		//SCELGO DI NON INCLUDERE LA TRANSIZIONE INIZIALE INIZIALE
		Tprimo.clear();
		Vector<Relationship> appoggioGood = getAllRelationsUntil(level, Globals.allRelationsGoodGeneral);
		
		for(int i=1; i<appoggioGood.size(); i++)
		{
			Tprimo.addElement(appoggioGood.get(i));
		}
		
		// creo s2
		Sdue.clear();
		for(int i=0; i<Sprimo.size(); i++)
		{
			try ( Transaction tx = Globals.graphDbGood.beginTx() )
			{
				String nome = Sprimo.get(i).getProperties("name").values().toString();
				nome = pulisci(nome);
				nome = nome + "-" +nome;
				Sdue.addElement(nome);
				//System.out.println(nome);
				tx.success();
			}
		}
		
		//creo t2
		Tdue.clear();
		for(int i=0; i<Tprimo.size(); i++)
		{
			try ( Transaction tx = Globals.graphDbGood.beginTx() )
			{
				String nomeFrom = Tprimo.get(i).getProperties("from").values().toString();
				nomeFrom = pulisci(nomeFrom);
				nomeFrom = nomeFrom + "-" +nomeFrom;
				
				String nomeTo = Tprimo.get(i).getProperties("to").values().toString();
				nomeTo = pulisci(nomeTo);
				nomeTo = nomeTo + "-" +nomeTo;
				
				String nomeEv = Tprimo.get(i).getProperties("event").values().toString();
				nomeEv = pulisci(nomeEv);
				//nomeEv = nomeEv + "-" +nomeEv;
				
				TransizioneDoppia nuova = new TransizioneDoppia();
				nuova.setDestinazione(nomeTo);
				nuova.setSorgente(nomeFrom);
				nuova.setEvento(nomeEv);
				
				Tdue.addElement(nuova);
				
				tx.success();
			}
		}
		

	}

}
