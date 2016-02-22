package Twins;


import java.util.Arrays;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import usefullAbstract.GenericGraphHandler;

public class Sincronizza extends GenericGraphHandler{
	
	private static Vector<Node> S = new Vector<Node>();
	private static Vector<Node> Sprimo = new Vector<Node>();
	private static Vector<Relationship> T = new Vector<Relationship>();
	private static Vector<Relationship> Tprimo = new Vector<Relationship>();
	private static Vector<String> Sdue = new Vector<String>();
	private static Vector<TransizioneDoppia> Tdue = new Vector<TransizioneDoppia>();
	private static Vector<TransizioneDoppia> Ta = new Vector<TransizioneDoppia>();
	private static Vector<String> Sprev = new Vector<String>();
	private static Vector<String> Sdiff = new Vector<String>();
	
	public static boolean syncro()
	{
		createData();
		algoritmo();
		/*for(int i = 0 ; i<Sdue.size(); i++)
		{
			System.out.println("nodi sincros:  " + Sdue.get(i));
		}*/
		/*for(int i = 0 ; i<Ta.size(); i++)
		{
			System.out.println("ta: sorg:" + Ta.get(i).getSorgente() + 
					";   dest : " + Ta.get(i).getDestinazione()
					+ ";    evento: " + Ta.get(i).getEvento());
		}*/
		return diagnosticable();
	}
	
	private static boolean diagnosticable()
	{
		//primo caso, se non ha transizioni ambigue allora è diagnosticabile
		if(Ta.size()==0)
		{
			System.out.println("vale C1: "
					+ "non ci sono transizioni ambigue nell'automa sincronizzato");
			return true;
		}
		
		//secondo caso se è deterministico allora è diagnosticabile
		if(deterministic())
		{
			System.out.println("vale C2: il bad twin è deterministico");
			return true;
		}
		
		//cerco le transizioni di guasto: prendo il loro evento.
		// se per tutti quegli eventi non esistono transizioni di guasto
		// che abbiano come evento quegli eventi allora è diagnosticabile
		if(thirdCondition())
		{
			System.out.println("vale la C3");
			return true;
		}
		System.out.println("non ho nessuna delle condizioni di diagnosticabilità"
				+ "\n, quindi NON DIAGNOSTICABILE");
		return false;
	}
	
	//cerco le transizioni di guasto: prendo il loro evento.
	// se per tutti quegli eventi non esistono transizioni di guasto
	// che abbiano come evento quegli eventi allora è diagnosticabile
	private static boolean thirdCondition()
	{
		boolean risp = true;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int k=0; k<T.size(); k++)
			{
				String guastoK = pulisci(T.get(k).getProperties("guasto").values().toString()); 
				String osservabileK = pulisci(T.get(k).getProperties("oss").values().toString()); 
				String eventoK = pulisci(T.get(k).getProperties("event").values().toString()); 
				if(guastoK.equalsIgnoreCase("y") && osservabileK.equalsIgnoreCase("y"))
				{
					for( int s=0; s<T.size(); s++)
					{
						if(s!=k)
						{
							String guastoS = pulisci(T.get(s).getProperties("guasto").values().toString()); 
							String eventoS = pulisci(T.get(s).getProperties("event").values().toString());
							if(eventoS.equalsIgnoreCase(eventoK) && guastoS.equalsIgnoreCase("n"))
							{
								return false;
							}
						}
					}
				}
				
			}
			tx.success();
		}
		return risp;
	}
	
	/*
	Se in un automa esistono due transizioni, una di
	guasto e l’altra no, aventi lo stesso stato sorgente, lo
	stesso stato destinazione e lo stesso evento
	osservabile, tale automa è non-deterministico*/
	private static boolean deterministic()
	{
		boolean deterministico = true;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<T.size(); i++)
			{
				Relationship t1 = T.get(i);
				String sorgente1 = pulisci(T.get(i).getProperties("from").values().toString()); 
				String evento1 = pulisci(T.get(i).getProperties("event").values().toString()); 
				
				for(int a=0; a<T.size(); a++)
				{
					if(a!=i)
					{
						String sorgente2 = pulisci(T.get(a).getProperties("from").values().toString()); 
						String evento2 = pulisci(T.get(a).getProperties("event").values().toString()); 
					
						boolean nonD = sorgente2.equalsIgnoreCase(sorgente1) 
								&& uguali(evento2, evento1);
						if(nonD)
						{
						//	System.out.println("non determinsitico perchè: ");
						//	System.out.println("prima transizione: " + sorgente1 + " evento : " + evento1);
						//	System.out.println("seconda transizione: " + sorgente2 + " evento : " + evento2);
						//	System.out.println("sto per dire che è NON deterministico");
							deterministico = false;
						}
					}
				}
			}
			tx.success();
		}	
		//System.out.println("sto per dire che è deterministico");
		return deterministico;
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
								if(nuovoStato(nuovo))
								{
									Sdue.addElement(nuovo);
								}
								if(nuovaTransizione(tsecondo))
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
		//System.out.println("entro in primo blocco");
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
			
			//System.out.println("sopra T vale: " + T.size());
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
						
						/*if(stato.equalsIgnoreCase("A") && sorgente1.equalsIgnoreCase("A") && sorgente2.equalsIgnoreCase("A"))
						{
								System.out.println(";  evento1: " + evento1 + " evento2 " +evento2 + ";  uguali?: " + uguali(evento1,evento2)) ;
						}*/
						
						if(bool)
						{
							//System.out.println("entrato");
							TransizioneDoppia tsecondo = new TransizioneDoppia();
							tsecondo.setSorgente(stato+"-"+stato);
							tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
							tsecondo.setEvento(evento1);
							//tsecondo.setGuasto("n");
							String nuovo = destinazione1+"-"+destinazione2;
							if(nuovoStato(nuovo))
							{
								Sdue.addElement(nuovo);
							}
							//System.out.println("stato: " + destinazione1+"-"+destinazione2);
							if(nuovaTransizione(tsecondo))
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
	
	private static boolean nuovaTransizione(TransizioneDoppia nuovo)
	{
		String eventoNuovo = nuovo.getEvento();
		//String guastoNuovo = nuovo.getGuasto();
		//System.out.println("primoN: " + primoNuovo + ";   secondoN : " + secondoNuovo);
		for(int i=0; i<Tdue.size(); i++)
		{
			TransizioneDoppia attuale = Tdue.get(i);
			String evento = attuale.getEvento();
			//String guasto = attuale.getGuasto();
			if(stessoStato(attuale.getSorgente(),nuovo.getSorgente()) 
				&& stessoStato(attuale.getDestinazione(),nuovo.getDestinazione())
				&& uguali(eventoNuovo,evento))
			{
				return false;
			}
		}
		return true;
	}
	
	private static boolean stessoStato(String primo, String secondo)
	{
		String primoA = primo.split("-")[0];
		String primoB = primo.split("-")[1];
		String secondoA = secondo.split("-")[0];
		String secondoB = secondo.split("-")[1];
		if(primoA.equalsIgnoreCase(secondoA) && primoB.equalsIgnoreCase(secondoB))
		{
			return true;
		}
		if(primoA.equalsIgnoreCase(secondoB) && primoB.equalsIgnoreCase(secondoA))
		{
			return true;
		}
		return false;
	}
		
	private static boolean nuovoStato(String nuovo)
	{
		String primoNuovo = nuovo.split("-")[0];
		String secondoNuovo = nuovo.split("-")[1];
		//System.out.println("primoN: " + primoNuovo + ";   secondoN : " + secondoNuovo);
		for(int i=0; i<Sdue.size(); i++)
		{
			String attuale = Sdue.get(i);
			String primo = attuale.split("-")[0];
			String secondo = attuale.split("-")[1];
			//System.out.println("primo: " + primo + ";   secondo : " + secondo);
			if(primo.equalsIgnoreCase(primoNuovo) && secondo.equalsIgnoreCase(secondoNuovo))
			{
				return false;
			}
			if(primo.equalsIgnoreCase(secondoNuovo) && secondo.equalsIgnoreCase(primoNuovo))
			{
				return false;
			}
		}
		return true;
	}
	
	private static boolean uguali(String a , String b)
	{
		String[] va = a.split("//");
		String[] vb = b.split("//");
		if(va.length!=vb.length)
		{
			return false;
		}
		for(int i=0; i<va.length; i++)
		{
			va[i] = va[i].toLowerCase();
			vb[i] = vb[i].toLowerCase();
		}
		Arrays.sort( va );
		Arrays.sort(vb);
		for(int i=0; i<va.length; i++)
		{
			if(!va[i].equalsIgnoreCase(vb[i]))
			{
				return false;
			}
		}
		return true;
	}
	
	private static boolean checkEqual(Vector<String> primo, Vector<String> secondo)
	{
	/*	System.out.println("------------------------------------------");
		System.out.println(primo.toString());
		System.out.println(secondo.toString());*/

		if(primo.size() != secondo.size())
		{
			/*System.out.println("prima dimensione: " + primo.size());
			System.out.println("seconda dimensione: " + secondo.size());
			System.out.println("non è uguale la dimensione!!");*/
			return false;
		}
		
		for(int i=0; i<primo.size(); i++)
		{
			if(!stessoStato(primo.get(i),secondo.get(i)))
			{
				//System.out.println("diversi:  " + primo.get(i) + "---" + secondo.get(i));
				return false;
			}
		}
		//System.out.println("rispondo che sono uguali");
		return true;	
	}
	
	private static void createData()
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
		for(int i=0; i<Globals.allRelations.size(); i++)
		{
			T.addElement(Globals.allRelations.get(i));
		}
		
		//SCELGO DI NON INCLUDERE IL NODO FITTIZIO INIZIALE
		Sprimo.clear();
		for(int i=1; i<Globals.allNodesGood.size(); i++)
		{
			Sprimo.addElement(Globals.allNodesGood.get(i));
		}
	
		//SCELGO DI NON INCLUDERE LA TRANSIZIONE INIZIALE INIZIALE
		Tprimo.clear();
		for(int i=1; i<Globals.allRelationsGood.size(); i++)
		{
			Tprimo.addElement(Globals.allRelationsGood.get(i));
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
