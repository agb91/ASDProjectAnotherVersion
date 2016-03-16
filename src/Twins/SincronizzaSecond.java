package Twins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import usefullAbstract.InVector;

public class SincronizzaSecond extends SincronizzaCommon {
	
	//second method
	protected static Vector<Node> secondSprimo = new Vector<Node>();
	protected static HashMap<String, Relationship> secondTprimo = new HashMap<String, Relationship>();
	protected static Vector<TransizioneDoppia> secondTa = new Vector<TransizioneDoppia>();
	protected static Vector<TransizioneDoppia> secondTaPrimo = new Vector<TransizioneDoppia>();
	protected static Vector<String> secondSdue = new Vector<String>();
	protected static HashMap<String, TransizioneDoppia> secondTdue = new HashMap<String, TransizioneDoppia>();
	protected static Vector<String> secondStemp = new Vector<String>();
	protected static Vector<String> secondSdiff = new Vector<String>();
	

	public static void syncroSecond(int level)
	{
		//long diffIn = 0;
		//long startTime = System.currentTimeMillis();
		if(!inInteger(level, Globals.syncroSecondDid))
		{
			if(level==1)
			{
				System.out.println("assurdo logico: dovrei fare la syscrosecond "
						+ "di livello 1, usando la syncrofirst di livello 0"
						+ "la quale non esiste (parto a farle da 1)");
				System.exit(2);
			}
			//long startTimeIn = System.currentTimeMillis();
			SincronizzaFirst.syncroToSecond(level-1);
			//long endTimeIn = System.currentTimeMillis();
			//diffIn = endTimeIn - startTimeIn;
			System.out.println("inizio la sincronizzazione di tipo 2 di livello: " + level);
			createDataSecond(level );
			algoritmoSecond( level);
			for(int i = 0 ; i<secondTa.size(); i++)
			{
				System.out.println("second: ta: sorg:" + secondTa.get(i).getSorgente() + 
						";   dest : " + secondTa.get(i).getDestinazione()
						+ ";    evento: " + secondTa.get(i).getEvento());
			}
			if(Globals.secondTaPerLevel.get(level).size()<secondTa.size())
			{
				Globals.secondTaPerLevel.get(level).clear();
				for(int i = 0 ; i<secondTa.size(); i++)
				{
					Globals.secondTaPerLevel.get(level).addElement(secondTa.get(i));
				}
			}
			writeInDb(level);
			Globals.syncroSecondDid.addElement(level);
		}
		//long endTime = System.currentTimeMillis();
		//long diff = endTime - startTime - diffIn;
		//System.err.println("tempo per syncro2:  di livello:  " + level +" : => " +diff); 
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
			for(int i=0; i<Globals.secondTaPerLevel.get(l).size(); i++)
			{
				String id = Globals.secondTaPerLevel.get(l).get(i).getSorgente() + "-"
						+ Globals.secondTaPerLevel.get(l).get(i).getDestinazione() + "-"
						+ Globals.secondTaPerLevel.get(l).get(i).getEvento();
				daCheckare.put(id ,Globals.secondTaPerLevel.get(l).get(i));
			}
		}
		
		boolean ris = checkQuarta(secondSdue, level, daCheckare, secondTdue,  "s");
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
	
	public static boolean checkC2C3(int level)
	{
		if(Globals.c2.get(level) != null)
		{
			if(Globals.c2.get(level).equalsIgnoreCase("y"))
			{
				System.out.println("al livello: " + level + ", la condizione C2 è vera");
				return true;
			}
		}
		if(Globals.c3.get(level) != null)
		{
			if(Globals.c3.get(level).equalsIgnoreCase("y"))
			{
				System.out.println("al livello: " + level + ", la condizione C2 è vera");
				return true;
			}
			else
			{
				System.out.println("al livello: " + level + ", la condizione C2 È FALSA");
				return false;
			}
		}
		 //secondo caso se è deterministico allora è diagnosticabile
		if (checkSeconda(getAllRelationsUntilHash(level, Globals.allRelationsGeneralHash), level))
		{
			Globals.c2.put(level, "y");
			System.out.println("al livello: " + level + ", la condizione C2 è vera");
			return true;
		}
		else
		{
			Globals.c2.put(level, "n");
			System.out.println("al livello: " + level + ", la condizione C2 È FALSA");
		}
		
		//cerco le transizioni di guasto: prendo il loro evento.
		// se per tutti quegli eventi non esistono transizioni di guasto
		// che abbiano come evento quegli eventi allora è diagnosticabile
		if(checkTerza(getAllRelationsUntilHash(level, Globals.allRelationsGeneralHash), level))
		{
			Globals.c3.put(level, "y");
			System.out.println("al livello: " + level + ", la condizione C3 è vera");
			return true;
		}
		else
		{
			Globals.c3.put(level, "n");
			System.out.println("al livello: " + level + ", la condizione C3 È FALSA");
		}
		return false;
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
		//primo caso, se non ha transizioni ambigue allora è vera
		if (checkPrima(Globals.secondTaPerLevel, level))
		{
			System.out.println("al livello: " + level + ", la condizione C1 è vera");
			Globals.c1.put(level, "y");
			return true;
		}
		else
		{
			Globals.c1.put(level, "n");
			System.out.println("al livello: " + level + ", la condizione C1 è FALSA");
		}
		return false;
	}

	

	private static void writeInDb(int level)
	{
		for(int i=0; i<secondSdue.size(); i++)
		{
			String nome = secondSdue.get(i);
			//System.out.println("scrivo il nodo: " + nome);
			addNodeSyncroSecond(nome, level);
		}
		Iterator<String> ks = secondTdue.keySet().iterator();
		while(ks.hasNext())
		{ 
			String a = ks.next();
			TransizioneDoppia attuale = secondTdue.get(a);
			String n1 = attuale.getSorgente();
			String n2 = attuale.getDestinazione();
			String oss = "y";
			String ev = attuale.getEvento();
			String nome = n1+ "-" + n2 + "- " + oss + "- " + ev;
			//Node n1, Node n2, String nome, String oss, String ev, String gu
			addRelationSyncroSecond(n1, n2, nome, oss, ev, level);
		}
	}
	
	
	private static void algoritmoSecond(int level)
	{
		bloccoSuperioreSecondo(level);
		bloccoWhileSecondo(level);
	}
	
	private static void bloccoWhileSecondo(int level)
	{
		while(!checkEqual(secondSdue, secondStemp))
		{
		
			getSecondDiff(); 
			//System.out.println("sono qui");
			secondStemp.clear();
			secondStemp.addAll(secondSdue);
			
			HashMap<String,Relationship> T = getAllRelationsUntilHash(level, Globals.allRelationsGeneralHash);
			for(int i=0; i<secondSdiff.size(); i++)
			{
				//System.out.println("woooooooooooooooooooooooooooooooooooooooooooo");
				String sa = secondSdiff.get(i).split("-")[0];
				String sb = secondSdiff.get(i).split("-")[1];
				//System.err.println("second: " + sa+"-------"+sb);
				try ( Transaction tx = Globals.graphDb.beginTx() )
				{
					Vector<String> sks1 = new Vector<String>();
					Vector<String> sks2 = new Vector<String>();
					Iterator<String> ks1 = T.keySet().iterator();
					Iterator<String> ks2 = T.keySet().iterator();
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
					
							boolean bool = (p!=s) && guasto2.equalsIgnoreCase("n")
									&& InVector.stessoEvento(evento1,evento2) 
									&& sorgente1.equalsIgnoreCase(sa) 
									&& sorgente2.equalsIgnoreCase(sb);
							if(bool)
							{
								//System.out.println("SONO QUI!");
								TransizioneDoppia tsecondo = new TransizioneDoppia();
								tsecondo.setSorgente(secondSdiff.get(i));
								tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
								tsecondo.setEvento(evento1);
								//System.err.println("dest: " + destinazione1+"-"+destinazione2);
								//System.out.println("ecco che aggiungo: " + destinazione1+"-"+destinazione2);
								String newName = destinazione1+"-"+destinazione2;
								if(!inVettore(newName, secondSdue))
								{
									secondSdue.add(destinazione1+"-"+destinazione2);
								}
								//System.err.println("dest new: " + destinazione1+"-"+destinazione2);
								
								secondTdue.put(tsecondo.getSorgente() + "-" + 
										tsecondo.getDestinazione() + "-" 
										+ tsecondo.getEvento() , tsecondo);
								/*System.err.println("candidata: form" + tsecondo.getSorgente() + "; to: "
										+ destinazione1+"-"+destinazione2 + "ev: "
												+ evento1);*/
								if(guasto1.equalsIgnoreCase("y"))
								{
									//System.out.println("QUESTA!!!");
									if(!InVector.InDoppia(tsecondo, secondTa))
									{
										secondTa.addElement(tsecondo);
									}
								}
							}
						
						}
					}
					tx.success();
				}
			}
		}
	}
	
	private static void bloccoSuperioreSecondo(int level)
	{
		//System.out.println("superiore:  " + secondSprimo.size());
		for(int i=0; i<secondSprimo.size(); i++)
		{
			String sa = "";
			String sb = "";
			String nomeAttuale = "";
			try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
			{
				Node attuale = secondSprimo.get(i);
				nomeAttuale = pulisci(attuale.getProperties("name").values().toString());
				sa = nomeAttuale.split("-")[0];
				sb = nomeAttuale.split("-")[1];
				tx.success();
			}			
			
			try ( Transaction tx = Globals.graphDb.beginTx() )
			{
				//per definizione ti = transizioni con evento composto di livello level
				//nel bad twin
				HashMap<String, Relationship> T = Globals.allRelationsGeneralHash.get(level);
				
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
						
						boolean bool= (p!=s) && guasto2.equalsIgnoreCase("n")
								&& InVector.stessoEvento(evento1,evento2) && 
								sorgente1.equalsIgnoreCase(sa) &&
								sorgente2.equalsIgnoreCase(sb);
						if(bool)
						{
							TransizioneDoppia tsecondo = new TransizioneDoppia();
							tsecondo.setSorgente(nomeAttuale);
							/*if(nomeAttuale.equalsIgnoreCase("n2-n2"))
							{
								System.err.println("bingo superiore second");
							}*/
							tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
							tsecondo.setEvento(evento1);
							//System.out.println("PRIMA ecco Sdue: " + 
							//secondSdue.size() + "----vs----" + secondStemp.size());
							String newName = destinazione1+"-"+destinazione2;
							if(!inVettore(newName, secondSdue))
							{
								secondSdue.add(newName);
							}
							secondTdue.put(tsecondo.getSorgente() + "-" + 
									tsecondo.getDestinazione() + "-" 
									+ tsecondo.getEvento() , tsecondo);
							if(guasto1.equalsIgnoreCase("y"))
							{
								//System.out.println("QUESTA!!!");
								if(!InVector.InDoppia(tsecondo, secondTa))
								{
									secondTa.addElement(tsecondo);
								}
							}
						}
					}
				}
			}
		}
	}
	
	private static void getSecondDiff()
	{
		//System.err.println("second sdue: " + secondSdue.toString());
		//System.err.println("second sstemp: " + secondStemp.toString());
		secondSdiff.clear();
		for(int i=0; i<secondSdue.size(); i++)
		{
			String attuale = secondSdue.get(i);
			//System.out.println("sdue: " + attuale);
			boolean present = false;
			for(int a=0; a<secondStemp.size(); a++)
			{
				boolean bool = stessoStato(attuale,secondStemp.get(a));
				if(bool)
				{
					present = true;
				}
			}
			if(present==false)
			{
				secondSdiff.addElement(attuale);
			}
		}
		//System.err.println("diff:: " + secondSdiff.toString());
		//System.err.println("\n\n");
	}
	
	private static void createDataSecond(int level)
	{
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{	
			secondSprimo = getAllNodesUntil(level-1, Globals.allNodesSyncroGeneral);
	/*		for(int i=0; i<secondSprimo.size(); i++)
			{
				System.err.println(secondSprimo.get(i).getProperties("name").values().toString());
			}*/
			secondTprimo = getAllRelationsUntilHash(level-1, Globals.allRelationsSyncroGeneralHash);
		
			//System.out.println("HEREEEEEE");
			secondTdue.clear();
			Iterator<String> ks = secondTprimo.keySet().iterator();
			while(ks.hasNext())
			{ 
				String an = ks.next();
				Relationship attuale = secondTprimo.get(an);
				String sorgente = attuale.getProperties("from").values().toString();
				sorgente = pulisci(sorgente);
				String destinazione = attuale.getProperties("to").values().toString();
				destinazione = pulisci(destinazione);
				String evento = attuale.getProperties("event").values().toString();
				evento = pulisci(evento);
				TransizioneDoppia nuova = new TransizioneDoppia();
				nuova.setSorgente(sorgente);
				nuova.setDestinazione(destinazione);
				nuova.setEvento(evento);
				secondTdue.put(sorgente + "-" + destinazione + "-" + evento, nuova);
			}
			
			secondSdue.clear();

			for(int i=0; i<secondSprimo.size(); i++)
			{
				String nome = secondSprimo.get(i).getProperties("name").values().toString();
				nome = pulisci(nome);
				//System.out.println("aggiungo:  " + nome);
				
				secondSdue.add(nome);
			}
			tx.success();
		}
		
		secondTa.clear();
		for(int l=1; l<=level; l++)
		{
			for(int i=0; i<Globals.firstTaPerLevel.get(l-1).size(); i++)
			{
				secondTa.add(Globals.firstTaPerLevel.get(l-1).get(i));	
			}
			//System.out.println("ne trovo: " + secondTa.size());
		}

		
		//System.out.println("secondTa size: "+secondTa.size());
		secondStemp.clear();
		for(int i=0; i<secondSdue.size(); i++)
		{
			String appoggio = secondSdue.get(i);
			//System.out.println("entra: " + appoggio);
			appoggio=pulisci(appoggio);
			secondStemp.add(appoggio);
		}
		
		//System.out.println("secondtemp size: "+secondSdue.size());
		//System.out.println("ho ereditato il seguente numero di ta: " + secondTaPrimo.size());
	}
	

	
}
