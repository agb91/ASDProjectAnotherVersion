package Twins;


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
	
	public static void syncro()
	{
		createData();
		boolean isDiagnosable = false;
		if(nonDeterministic())
		{
			algoritmo();
			isDiagnosable = diagnosticabile();
		}
		else
		{
			isDiagnosable = true;
		}
		if(isDiagnosable)
		{
			System.out.println("ok, è diagnosticabile a questo livello");
		}
		for(int i = 0 ; i<Sdue.size(); i++)
		{
			System.out.println("nodi sincros:  " + Sdue.get(i));
		}
		for(int i = 0 ; i<Tdue.size(); i++)
		{
			System.out.println("transazioni sincros:  " + 	Tdue.get(i).getEvento() 
					+ ";   partente da: " + Tdue.get(i).getSorgente()
					+ ";   destinazione to: " + Tdue.get(i).getDestinazione());
		}
	}
	
	private static boolean diagnosticabile()
	{
		if(Ta.size()==0)
		{
			return true;
		}
		for(int k=0; k<T.size(); k++)
		{
			// se sono collegati ad altre transizioni di guasto è ok, sono diagnosticabiliu
			// se sono collegati a transabioni non di guasto no, non sono diagnosticabili
			// se sono non sono collegati, ok, sono diagnosticabili
			String guastoK = pulisci(T.get(k).getProperties("guasto").values().toString()); 
			String osservabileK = pulisci(T.get(k).getProperties("oss").values().toString()); 
			String eventoK = pulisci(T.get(k).getProperties("event").values().toString()); 
			if(guastoK.toLowerCase().contains("y") && osservabileK.toLowerCase().contains("y"))
			{
				for( int s=0; s<T.size(); s++)
				{
					if(s!=k)
					{
						String guastoS = pulisci(T.get(s).getProperties("guasto").values().toString()); 
						String eventoS = pulisci(T.get(s).getProperties("event").values().toString());
						if(eventoS.equalsIgnoreCase(eventoK) && guastoS.toLowerCase().contains("n"))
						{
							return false;
						}
					}
				}
			}
		}
		return true;
	}
	
	/*
	Se in un automa esistono due transizioni, una di
	guasto e l’altra no, aventi lo stesso stato sorgente, lo
	stesso stato destinazione e lo stesso evento
	osservabile, tale automa è non-deterministico*/
	private static boolean nonDeterministic()
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<T.size(); i++)
			{
				Relationship t1 = T.get(i);
				String guastot = t1.getProperties("guasto").values().toString(); 
				String sorgente1 = pulisci(T.get(i).getProperties("from").values().toString()); 
				String evento1 = pulisci(T.get(i).getProperties("event").values().toString()); 
				String destinazione1 = pulisci(T.get(i).getProperties("to").values().toString()); 
	
				guastot = pulisci(guastot);
				if(guastot.toLowerCase().contains("y"))
				{
					for(int a=0; a<T.size(); a++)
					{
						if(a!=i)
						{
							String sorgente2 = pulisci(T.get(a).getProperties("from").values().toString()); 
							String evento2 = pulisci(T.get(a).getProperties("event").values().toString()); 
							String destinazione2 = pulisci(T.get(a).getProperties("to").values().toString()); 
							String guasto2 = pulisci(T.get(a).getProperties("guasto").values().toString());
							boolean nonD = sorgente2.equalsIgnoreCase(sorgente1) 
									&& evento2.equalsIgnoreCase(evento1)
									&& destinazione2.equalsIgnoreCase(destinazione1)
									&& guasto2.toLowerCase().contains("n");
							if(nonD)
							{
								return true;
							}
						}
					}
				}
			}
			tx.success();
		}	
		return false;
	}
	
	private static void algoritmo()
	{
		//creo sprev
		for(int i=0; i<Sdue.size(); i++)
		{
			Sprev.addElement(Sdue.get(i));
		}
		bloccoSuperioreSincroAlgo();
		bloccoWhileSincroAlgo();
		/*for(int i=0; i<Sdue.size(); i++)
		{
			System.out.println("check s2 : " + Sdue.get(i));
		}
		*/
	}
	
	private static void bloccoWhileSincroAlgo()
	{
		while(!checkEqual(Sdue, Sprev))
		{
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
			
			Sprev.clear();
			for(int i=0; i<Sdue.size(); i++)
			{
				Sprev.add(Sdue.get(i));
			}
			
			for(int i=0; i<Sdiff.size(); i++)
			{
				String coppia = Sdiff.get(i);
				String sa = coppia.split("-",2)[0];
				String sb = coppia.split("-",2)[1];				
				
				for(int a=0; a<T.size(); a++)
				{
					for(int k=0; k<T.size(); k++)
					{
						try ( Transaction tx = Globals.graphDb.beginTx() )
						{
							Relationship t1 = T.get(a);
							Relationship t2 = T.get(k);
							String guasto1 = t1.getProperties("guasto").values().toString();
							String guasto2 = t2.getProperties("guasto").values().toString();
							String evento1 = t1.getProperties("event").values().toString();
							String evento2 = t2.getProperties("event").values().toString();
							String sorgente1 = t1.getProperties("from").values().toString();
							String sorgente2 = t2.getProperties("from").values().toString();
							String destinazione1 = t1.getProperties("to").values().toString();
							String destinazione2 = t2.getProperties("to").values().toString();
								
							boolean bool = (a!=k && !guasto2.contains("y") && 
									sorgente2.contains(sa)&& !guasto2.contains("y")&&
									sorgente1.contains(sb));
							
							if(bool)
							{
								TransizioneDoppia tsecondo = new TransizioneDoppia();
								tsecondo.setSorgente(sa+"-"+sb);
								tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
								tsecondo.setEvento(evento1);
								Sdue.addElement(destinazione1+"-"+destinazione2);
								//System.out.println("sono nel while: stato: " + destinazione1+"-"+destinazione2);
								Tdue.addElement(tsecondo);
								if(guasto1.contains("y"))
								{
									Ta.addElement(tsecondo);
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
						String guasto1 = t1.getProperties("guasto").values().toString();
						String guasto2 = t2.getProperties("guasto").values().toString();
						String evento1 = t1.getProperties("event").values().toString();
						String evento2 = t2.getProperties("event").values().toString();
						String sorgente1 = t1.getProperties("from").values().toString();
						String sorgente2 = t2.getProperties("from").values().toString();
						String destinazione1 = t1.getProperties("to").values().toString();
						String destinazione2 = t2.getProperties("to").values().toString();
						
						boolean bool = (a!=k && !guasto2.contains("y")
								&& sorgente2.contains(stato) && 
								sorgente1.contains(stato)
								&& evento1.equalsIgnoreCase(evento2));
						
						if(bool)
						{
							//System.out.println("entrato");
							TransizioneDoppia tsecondo = new TransizioneDoppia();
							tsecondo.setSorgente(stato+"-"+stato);
							tsecondo.setDestinazione(destinazione1+"-"+destinazione2);
							tsecondo.setEvento(evento1);
							Sdue.addElement(destinazione1+"-"+destinazione2);
							//System.out.println("stato: " + destinazione1+"-"+destinazione2);
							
							Tdue.addElement(tsecondo);
							if(guasto1.contains("y"))
							{
								Ta.addElement(tsecondo);
							}
						}
					}			
				}
			}
		}
	}
	
	private static boolean checkEqual(Vector<String> primo, Vector<String> secondo)
	{
		if(primo.size() != secondo.size())
		{
			System.out.println("prima dimensione: " + primo.size());
			System.out.println("seconda dimensione: " + secondo.size());
			System.out.println("non è uguale la dimensione!!");
			return false;
		}
		
		for(int i=0; i<primo.size(); i++)
		{
			if(!primo.get(i).equalsIgnoreCase(secondo.get(i)))
			{
				System.out.println("diversi:  " + primo.get(i) + "---" + secondo.get(i));
				return false;
			}
		}
		
		return true;	
	}
	
	private static void createData()
	{
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
