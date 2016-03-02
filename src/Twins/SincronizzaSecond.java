package Twins;

import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;

public class SincronizzaSecond extends SincronizzaCommon {
	
	//second method
	protected static Vector<Node> secondSprimo = new Vector<Node>();
	protected static Vector<Relationship> secondTprimo = new Vector<Relationship>();
	protected static Vector<TransizioneDoppia> secondTa = new Vector<TransizioneDoppia>();
	protected static Vector<TransizioneDoppia> secondTaPrimo = new Vector<TransizioneDoppia>();
	protected static Vector<String> secondSdue = new Vector<String>();
	protected static Vector<TransizioneDoppia> secondTdue = new Vector<TransizioneDoppia>();
	protected static Vector<String> secondStemp = new Vector<String>();
	protected static Vector<String> secondSdiff = new Vector<String>();
	

	public static boolean syncroSecond(int level)
	{
		createDataSecond(level );
		algoritmoSecond( level);
		/*System.out.println("ta ha il numero di elementi: " + secondTa.size());
		System.out.println("che va da: " + secondTa.get(0).getSorgente());
		System.out.println("che va to: " + secondTa.get(0).getDestinazione());*/
		writeInDb(level);
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
		for(int i=0; i<secondTdue.size(); i++)
		{
			TransizioneDoppia attuale = secondTdue.get(i);
			String n1 = attuale.getSorgente();
			String n2 = attuale.getDestinazione();
			String oss = "y";
			String ev = attuale.getEvento();
			String nome = n1+ "-" + n2 + "- " + oss + "- " + ev;
			//Node n1, Node n2, String nome, String oss, String ev, String gu
			addRelationSyncroSecond(n1, n2, nome, oss, ev, level);
		}
	}
	
	/*protected static boolean diagnosableC1()
	{
		//primo caso, se non ha transizioni ambigue allora è diagnosticabile
		if(Ta.size()==0)
		{
			System.out.println("vale C1: "
					+ "non ci sono transizioni ambigue nell'automa sincronizzato");
			return true;
		}
		return false;
	}*/
	
	private static void algoritmoSecond(int level)
	{
		bloccoSuperioreSecondo(level);
		bloccoWhileSecondo(level);
	}
	
	private static void bloccoWhileSecondo(int level)
	{
		//System.out.println("entro in while;");
		while(!checkEqual(secondSdue, secondStemp))
		{
			getSecondDiff(); 
			//System.out.println("sono qui");
			secondStemp.clear();
			secondStemp.addAll(secondSdue);
			
			Vector<Relationship> T = getAllRelationsUntil(level, Globals.allRelationsGeneral);
			//System.out.println("questa è la dimensione diT: " + T.size());
			//System.out.println("secondSdiff: " + secondSdiff.size());
			//todo
			for(int i=0; i<secondSdiff.size(); i++)
			{
				//System.out.println("woooooooooooooooooooooooooooooooooooooooooooo");
				String sa = secondSdiff.get(i).split("-")[0];
				String sb = secondSdiff.get(i).split("-")[1];
				try ( Transaction tx = Globals.graphDb.beginTx() )
				{
					for(int a=0; a<T.size(); a++)
					{
						for(int k=0; k<T.size(); k++)
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
					
							boolean bool = (a!=k) && guasto2.equalsIgnoreCase("n")
									&& uguali(evento1,evento2) 
									&& sorgente1.equalsIgnoreCase(sa) 
									&& sorgente2.equalsIgnoreCase(sb);
							//System.out.println("bool: " + bool);
							if(bool)
							{
								//System.out.println("SONO QUI!");
								TransizioneDoppia tsecondo = new TransizioneDoppia();
								tsecondo.setSorgente(secondSdiff.get(i));
								tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
								tsecondo.setEvento(evento1);
								//System.out.println("ecco che aggiungo: " + destinazione1+"-"+destinazione2);
								secondSdue.add(destinazione1+"-"+destinazione2);
								secondTdue.add(tsecondo);
								if(guasto1.equalsIgnoreCase("y"))
								{
									//System.out.println("QUESTA!!!");
									secondTa.addElement(tsecondo);
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
				Vector<Relationship> T = Globals.allRelationsGeneral.get(level);
				for(int a=0; a<T.size(); a++)
				{
					for(int k=0; k<T.size(); k++)
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
						
						
						boolean bool= (a!=k) && guasto2.equalsIgnoreCase("n")
								&& uguali(evento1,evento2) && 
								sorgente1.equalsIgnoreCase(sa) &&
								sorgente2.equalsIgnoreCase(sb);
						if(bool)
						{
							TransizioneDoppia tsecondo = new TransizioneDoppia();
							tsecondo.setSorgente(nomeAttuale);
							tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
							tsecondo.setEvento(evento1);
							//System.out.println("PRIMA ecco Sdue: " + 
							//secondSdue.size() + "----vs----" + secondStemp.size());
							secondSdue.add(destinazione1+"-"+destinazione2);
							secondTdue.add(tsecondo);
							//System.out.println("DOPO ecco Sdue: " + 
							//secondSdue.size() + "----vs----" + secondStemp.size());
							//System.out.println("secondT2 size: " + secondTdue.size() + "----0"+ guasto1);
							if(guasto1.equalsIgnoreCase("y"))
							{
								//System.out.println("QUESTA!!!");
								secondTa.addElement(tsecondo);
							}
						}
					}
				}
			}
		}
	}
	
	private static void getSecondDiff()
	{
		secondSdiff.clear();
		for(int i=0; i<secondSdue.size(); i++)
		{
			String attuale = secondSdue.get(i);
			//System.out.println("sdue: " + attuale);
			boolean present = false;
			for(int a=0; a<secondStemp.size(); a++)
			{
				boolean bool = stessoStato(attuale,secondStemp.get(a));
				/*if(attuale.equalsIgnoreCase("E-B"))
				{
					System.out.println("temp:" + secondStemp.get(a) +
							";   attuale = " + attuale + "; bool = " 
							+ bool);
				}*/
				if(bool)
				{
					/*if(attuale.equalsIgnoreCase("E-B"))
					{
						System.out.println("DROGAAAA");
					}*/
					present = true;
				}
			}
			if(present==false)
			{
				secondSdiff.addElement(attuale);
			}
		}
	}
	
	private static void createDataSecond(int level)
	{
		secondSprimo = getAllNodesUntil(level-1, Globals.allNodesSyncroGeneral);
		secondTprimo = getAllRelationsUntil(level-1, Globals.allRelationsSyncroGeneral);
		secondTaPrimo = Globals.allTa.get(level-1);
		//System.out.println("s eretidati: " + secondSprimo.size());
		
	
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{		
			//System.out.println("HEREEEEEE");
			secondTdue.clear();
			for(int i=0; i<secondTprimo.size(); i++)
			{
				String sorgente = secondTprimo.get(i).getProperties("from").values().toString();
				sorgente = pulisci(sorgente);
				String destinazione = secondTprimo.get(i).getProperties("to").values().toString();
				destinazione = pulisci(destinazione);
				String evento = secondTprimo.get(i).getProperties("event").values().toString();
				evento = pulisci(evento);
				TransizioneDoppia nuova = new TransizioneDoppia();
				nuova.setSorgente(sorgente);
				nuova.setDestinazione(destinazione);
				nuova.setEvento(evento);
				secondTdue.add(nuova);
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
		for(int i=0; i<secondTaPrimo.size(); i++)
		{
			secondTa.add(secondTaPrimo.get(i));	
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
	
	
	/*public static boolean syncroC1(int level)
	{
		createData(level);
		algoritmo();
		return diagnosableC1();
	}*/
	

	
}
