package Twins;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import usefullAbstract.GenericGraphHandler;
import usefullAbstract.InVector;

public class SincronizzaFirst extends SincronizzaCommon{
	//first method
	protected static Vector<Node> S = new Vector<Node>();
	protected static Vector<Node> Sprimo = new Vector<Node>();
	protected static HashMap<String,Relationship> T = new HashMap<String,Relationship>();
	protected static HashMap<String,Relationship> Tprimo = new HashMap<String,Relationship>();
	protected static Vector<String> Sdue = new Vector<String>();
	protected static HashMap<String,TransizioneDoppia> Tdue = new HashMap<String,TransizioneDoppia>();
	protected static Vector<TransizioneDoppia> Ta = new Vector<TransizioneDoppia>();
	protected static Vector<String> Sprev = new Vector<String>();
	protected static Vector<String> Sdiff = new Vector<String>();
	
	
	public static void syncro(int level)
	{
		//long startTime = System.currentTimeMillis();
		if(!inInteger(level,Globals.syncroFirstDid))
		{
			System.out.println("inizio la sincronizzazione di tipo 1 di livello: " + level);
			createData(level);
			algoritmo(level);
			/*for(int i = 0 ; i<Sdue.size(); i++)
			{
				System.out.println("nodi sincros:  " + Sdue.get(i));
			}*/
			for(int i = 0 ; i<Ta.size(); i++)
			{
				System.out.println("first: ta: sorg:" + Ta.get(i).getSorgente() + 
						";   dest : " + Ta.get(i).getDestinazione()
						+ ";    evento: " + Ta.get(i).getEvento());
			}
			if(Globals.firstTaPerLevel.get(level).size()<Ta.size())
			{
				Globals.firstTaPerLevel.get(level).clear();		
				for(int i = 0 ; i<Ta.size(); i++)
				{				
					Globals.firstTaPerLevel.get(level).addElement(Ta.get(i));
				}
			}
			writeInDb(level); 
			Globals.syncroFirstDid.addElement(level);
		}
		//long endTime = System.currentTimeMillis();
		//long diff = endTime - startTime;
		//System.err.println("tempo per syncro1 di livello:  " + level +" : => " + diff); 
	}
	
	public static boolean checkC4(int level)
	{
		if(Globals.c4.get(level)!=null)
		{
			if(Globals.c4.get(level).equalsIgnoreCase("y"))
			{
				System.out.println("al livello " + level + " c4 è vera");
				return true;
			}
			else
			{
				System.out.println("al livello " + level + " c4 è falsa");
				return false;
			}
		}
		HashMap<String,TransizioneDoppia> daCheckare = new HashMap<String,TransizioneDoppia>();
		for(int l=0; l<=level; l++)
		{
			for(int i=0; i<Globals.firstTaPerLevel.get(l).size(); i++)
			{
				String id = Globals.firstTaPerLevel.get(l).get(i).getSorgente() + "-"
						+ Globals.firstTaPerLevel.get(l).get(i).getDestinazione() + "-"
						+ Globals.firstTaPerLevel.get(l).get(i).getEvento();
				daCheckare.put(id ,Globals.firstTaPerLevel.get(l).get(i));
			}
		}
		long startTime4 = System.currentTimeMillis();
		//System.err.println("tocheck: " + daCheckare.size());
		boolean ris = checkQuarta(Sdue, level, daCheckare, Tdue, "f");
		long endTime4 = System.currentTimeMillis();
		long seconds = (endTime4 - startTime4);
		System.err.println("il c4 ha rischiesto " + seconds + " milliseconds ;  \n\n\n\n");

		if(ris)
		{
			Globals.c4.put(level, "y");
			System.out.println("a livello " + level + " c4 è vera");
		}
		else
		{
			Globals.c4.put(level, "n");
			System.out.println("a livello " + level + " c4 è falsa" );
		}
		return ris;
	}
	
	public static void syncroToSecond(int level)
	{
		if(!inInteger(level,Globals.syncroFirstDid))
		{
			System.out.println("inizio la sincronizzazione di tipo 1 di livello: " + level);
			createData(level);
			algoritmo(level);
			/*for(int i = 0 ; i<Sdue.size(); i++)
			{
				System.out.println("nodi sincros:  " + Sdue.get(i));
			}*/
			for(int i = 0 ; i<Ta.size(); i++)
			{
				System.out.println("ta: sorg:" + Ta.get(i).getSorgente() + 
						";   dest : " + Ta.get(i).getDestinazione()
						+ ";    evento: " + Ta.get(i).getEvento());
			}
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
		Iterator<String> ks = Tdue.keySet().iterator();
		while(ks.hasNext())
		{ 
			String a = ks.next();
			TransizioneDoppia attuale = Tdue.get(a);
			String n1 = attuale.getSorgente();
			String n2 = attuale.getDestinazione();
			String oss = "y";
			String ev = attuale.getEvento();
			String nome = n1+ "-" + n2 + "- " + oss + "- " + ev;
			//Node n1, Node n2, String nome, String oss, String ev, String gu
			if(!n1.equalsIgnoreCase("inizio-inizio"))
			{
				addRelationSyncro(n1, n2, nome, oss, ev, level);
			}
		}
		for(int i=0; i<Ta.size(); i++)
		{
			Globals.allTa.get(level).add(Ta.get(i));	
		}
	}
		
		
	private static void algoritmo(int level)
	{
		//creo sprev
		Sprev.clear();
		for(int i=0; i<Sdue.size(); i++)
		{
			Sprev.addElement(Sdue.get(i));
		}
		bloccoSuperioreSincroAlgo(level);
		bloccoWhileSincroAlgo(level);
	}
	
	
	private static void bloccoWhileSincroAlgo(int level)
	{
		//System.out.println("entro in while;");
		while(!checkEqual(Sdue, Sprev))
		{
			Sdiff.clear();
			getSdiff();
			
			Sprev.clear();
			for(int i=0; i<Sdue.size(); i++)
			{
				Sprev.add(Sdue.get(i));
			}
			
			for(int i=0; i<Sdiff.size(); i++)
			{
				//System.err.println("entro in sdiff");
				String coppia = Sdiff.get(i);
				//System.err.println("primo: runno su:  sa: " + coppia.split("-")[0] + ";  sb " + coppia.split("-")[1]);
				String sa = coppia.split("-")[0];
				String sb = coppia.split("-")[1];
				//System.err.println("primo: sa:" + sa + ";   sb:" + sb);
				//System.out.println("qui T vale : " + T.size());
				//Vector<Relationship> T = getAllRelationsUntil(level, Globals.allRelationsGeneralHash);
				
				Iterator<String> ks1 = T.keySet().iterator();
				Iterator<String> ks2 = T.keySet().iterator();
				Vector<String> sks1 = new Vector<String>();
				Vector<String> sks2 = new Vector<String>();
				while(ks1.hasNext())
				{ 
					String a1 = ks1.next();
					sks1.addElement(a1);
				}
				while(ks2.hasNext())
				{
					String a2 = ks2.next();
					sks2.addElement(a2);
				}
				
				for(int s=0; s<sks1.size(); s++ )
				{ 
					for(int p =0; p<sks2.size(); p++)
					{
						try ( Transaction tx = Globals.graphDb.beginTx() )
						{
							Relationship t1 = T.get(sks1.get(s));
							Relationship t2 = T.get(sks2.get(p));
							String guasto1 = pulisci(t1.getProperties("guasto").values().toString());
							String guasto2 = pulisci(t2.getProperties("guasto").values().toString());
							String evento1 = pulisci(t1.getProperties("event").values().toString());
							String evento2 = pulisci(t2.getProperties("event").values().toString());
							String sorgente1 = pulisci(t1.getProperties("from").values().toString());
							String sorgente2 = pulisci(t2.getProperties("from").values().toString());
							String destinazione1 = pulisci(t1.getProperties("to").values().toString());
							String destinazione2 = pulisci(t2.getProperties("to").values().toString());
					
							boolean bool = (s!=p && guasto2.equalsIgnoreCase("n") && 
									InVector.stessoEvento(evento1,evento2) &&
									sorgente1.equalsIgnoreCase(sa)&& sorgente2.equalsIgnoreCase(sb));
						
							if(bool)
							{
								//String destn = destinazione1+"-"+destinazione2;
							/*	if(destn.equalsIgnoreCase("D-F"))
								{
									System.err.println("prima: da: " + sa + ";  a D" + 
												";  ev:  " + evento1);
									System.err.println("seconda: da: " + sb + ";  a F" + 
											";  ev:  " + evento1);
									System.err.println("---------- \n\n");
								}*/
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
										if(!InVector.InDoppia(tsecondo, Ta))
										{
											Ta.addElement(tsecondo);
										}
										//tsecondo.setGuasto("y");
									}
									Tdue.put(tsecondo.getSorgente() + "-" + tsecondo.getDestinazione()
									+ "-" +tsecondo.getEvento(),tsecondo);
	
								}
							}
						}			
					}
				}				
			}
		}
	}
	
		
	private static void bloccoSuperioreSincroAlgo(int level)
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
			
			//System.err.println("T size: " + T.size());
			Iterator<String> ks1 = T.keySet().iterator();
			Iterator<String> ks2 = T.keySet().iterator();
			Vector<String> sks1 = new Vector<String>();
			Vector<String> sks2 = new Vector<String>();
			while(ks1.hasNext())
			{ 
				String a1 = ks1.next();
				sks1.addElement(a1);
			}
			while(ks2.hasNext())
			{
				String a2 = ks2.next();
				sks2.addElement(a2);
			}
			
			for(int s=0; s<sks1.size(); s++ )
			{ 
				for(int p =0; p<sks2.size(); p++)
				{
					try ( Transaction tx = Globals.graphDb.beginTx() )
					{
						//System.err.println(a1 + "----" + a2);
						Relationship t1 = T.get(sks1.get(s));
						Relationship t2 = T.get(sks2.get(p));
						String guasto1 = pulisci(t1.getProperties("guasto").values().toString());
						String guasto2 = pulisci(t2.getProperties("guasto").values().toString());
						String evento1 = pulisci(t1.getProperties("event").values().toString());
						String evento2 = pulisci(t2.getProperties("event").values().toString());
						String sorgente1 = pulisci(t1.getProperties("from").values().toString());
						String sorgente2 = pulisci(t2.getProperties("from").values().toString());
						String destinazione1 = pulisci(t1.getProperties("to").values().toString());
						String destinazione2 = pulisci(t2.getProperties("to").values().toString());
												
						boolean bool = s!=p 
								&& guasto2.equalsIgnoreCase("n")
								&& sorgente2.equalsIgnoreCase(stato) 
								&& sorgente1.equalsIgnoreCase(stato)
								&& InVector.stessoEvento(evento1,evento2);
							
						/*if(stato.equalsIgnoreCase("A") && sorgente1.equalsIgnoreCase("A") && sorgente2.equalsIgnoreCase("A"))
						{
								System.out.println(";  evento1: " + evento1 + " evento2 " +evento2 + ";  uguali?: " + uguali(evento1,evento2)) ;
						}*/
						
						if(bool)
						{
							/*if(destinazione1.equalsIgnoreCase("E") 
									|| destinazione2.equalsIgnoreCase("B"))
							{
								System.err.println("from: " + stato+"-"+stato +";    "
										+ "to: " +destinazione1+"-"+destinazione2 +
										";    ev: " + evento1);
							}
							if(destinazione1.equalsIgnoreCase("B") 
									|| destinazione2.equalsIgnoreCase("E"))
							{
								System.err.println("from: " + stato+"-"+stato +";    "
										+ "to: " +destinazione1+"-"+destinazione2 +
										";    ev: " + evento1);
							}*/
							//System.err.println("here");
							TransizioneDoppia tsecondo = new TransizioneDoppia();
							tsecondo.setSorgente(stato+"-"+stato);
							tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
							tsecondo.setEvento(evento1);
							//tsecondo.setGuasto("n");
							String nuovo = destinazione1+"-"+destinazione2;
							if(nuovoStato(nuovo,  Sdue))
							{
								//System.err.println("add: " +nuovo);
								Sdue.addElement(nuovo);
							}
							/*System.err.println("candidata: form" + tsecondo.getSorgente() + "; to: "
									+ destinazione1+"-"+destinazione2 + "ev: "
											+ evento1);*/
							if(guasto1.equalsIgnoreCase("y"))
							{
								if(!InVector.InDoppia(tsecondo, Ta))
								{
									Ta.addElement(tsecondo);
								}
								//Globals.lastTa.addElement(tsecondo);
								//tsecondo.setGuasto("y");
							}
							//if(!InVector.InDoppia(tsecondo, Tdue))
							//{
							Tdue.put(tsecondo.getSorgente() + "-" + tsecondo.getDestinazione()
							+ "-" +tsecondo.getEvento(), tsecondo);
							//}
						}
					}			
				}
			}
		}
	}
	
	public static boolean checkC1(int level)
	{
		if(Globals.c1.get(level)!=null)
		{
			if(Globals.c1.get(level).equalsIgnoreCase("y"))
			{
				System.out.println("al livello: " + level + ", la condizione C1 è vera");
				return true;
			}
			else
			{
				System.out.println("al livello: " + level + ", la condizione C1 è FALSA");
				return false;
			}
		}
		//primo caso, se non ha transizioni ambigue allora è diagnosticabile
		if (checkPrima(Globals.firstTaPerLevel, level))
		{
			Globals.c1.put(level, "y");
			System.out.println("al livello: " + level + ", la condizione C1 è vera");
			return true;
		}
		else
		{
			Globals.c1.put(level, "n");
			System.out.println("al livello: " + level + ", la condizione C1 è FALSA");
		}
		return false;
	}
	
	private static void getSdiff()
	{
	//	System.err.println("prima sdue: " + Sdue.toString());
	//	System.err.println("prima sstemp: " + Sprev.toString());
		Sdiff.clear();
		for(int i=0; i<Sdue.size(); i++)
		{
			String attuale = Sdue.get(i);
			//System.out.println("sdue: " + attuale);
			boolean present = false;
			for(int a=0; a<Sprev.size(); a++)
			{
				boolean bool = stessoStato(attuale,Sprev.get(a));
				if(bool)
				{
					present = true;
				}
			}
			if(present==false)
			{
				Sdiff.addElement(attuale);
			}
		}
	//	System.err.println("diff:: " + Sdiff.toString());
	//	System.err.println("\n\n");
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
		T = getAllRelationsUntilHash(level, Globals.allRelationsGeneralHash);
		//System.err.println("dim: " + Globals.allRelationsGeneralHash.get(level).size());
		//System.err.println("T,size:   " + T.size());
		
		//SCELGO DI NON INCLUDERE IL NODO FITTIZIO INIZIALE
		Sprimo.clear();
		for(int i=1; i<Globals.allNodesGood.size(); i++)
		{
			Sprimo.addElement(Globals.allNodesGood.get(i));
		}
	
		//SCELGO DI NON INCLUDERE LA TRANSIZIONE INIZIALE INIZIALE
		Tprimo.clear();
		HashMap<String,Relationship> Tprimo = getAllRelationsUntilHash(level, Globals.allRelationsGoodGeneralHash);
				
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
		Iterator<String> k = Tprimo.keySet().iterator();
		while(k.hasNext())
		{ 
			String a = k.next();
			try ( Transaction tx = Globals.graphDbGood.beginTx() )
			{
				String nomeFrom = Tprimo.get(a).getProperties("from").values().toString();
				nomeFrom = pulisci(nomeFrom);
				nomeFrom = nomeFrom + "-" +nomeFrom;
				
				String nomeTo = Tprimo.get(a).getProperties("to").values().toString();
				nomeTo = pulisci(nomeTo);
				nomeTo = nomeTo + "-" +nomeTo;
				
				String nomeEv = Tprimo.get(a).getProperties("event").values().toString();
				nomeEv = pulisci(nomeEv);
				//nomeEv = nomeEv + "-" +nomeEv;
				
				TransizioneDoppia nuova = new TransizioneDoppia();
				nuova.setDestinazione(nomeTo);
				nuova.setSorgente(nomeFrom);
				nuova.setEvento(nomeEv);
				
				Tdue.put(nomeFrom + "-" + nomeTo+ "-" +nomeEv,nuova);
				
				tx.success();
			}
		}
		

	}

}
