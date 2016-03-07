package usefullAbstract;

import java.util.Arrays;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import Twins.TransizioneDoppia;
import global.Globals;

public class InVector extends GenericGraphHandler{
	
	// se se ago è dentro pagliaio
	public static boolean inRel(String id, Vector<Relationship> pagliaio)
	{
		if(pagliaio.size()==0)
		{
			return false;
		}

		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<pagliaio.size(); i++)
			{
				String idPagliaio = pulisci(pagliaio.get(i).getProperties("type").values().toString());
				if(idPagliaio.equalsIgnoreCase(id))
				{					
					return true;
				}
			}
			tx.success();
		}	
		return false;
	}
	
	public static boolean inVettore(String ago, Vector<String> pagliaio)
	{
		for(int i=0; i<pagliaio.size(); i++)
		{
			if(pagliaio.get(i).equalsIgnoreCase(ago))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean notExistGoodRels(String n1, String n2, String ev, int level)
	{
		for(int l=0; l<=level; l++)
		{
			for(int i=0; i<Globals.allRelationsGoodGeneral.get(l).size(); i++)
			{
				Relationship attuale = Globals.allRelationsGoodGeneral.get(l).get(i);
				String pagliaioN1 = pulisci(attuale.getProperties("from").values().toString());
				String pagliaioN2 = pulisci(attuale.getProperties("to").values().toString());
				String pagliaioEv = pulisci(attuale.getProperties("event").values().toString());
				
				if(stessoStato(pagliaioN1,n1) && stessoStato(pagliaioN2,n2)
						&& stessoEvento(ev, pagliaioEv))
				{
					//System.out.println("ho scartato; " + ago);
					return false;
				}
			}	
		}		
		return true;
	}


	public static boolean notExistSyncro(String ago)
	{
		for(int l=0; l<Globals.allRelationsSyncroGeneral.size(); l++)
		{
			for(int i=0; i<Globals.allRelationsSyncroGeneral.get(l).size(); i++)
			{
				Relationship attuale = Globals.allRelationsSyncroGeneral.get(l).get(i);
				String pagliaio = pulisci(attuale.getProperties("type").values().toString());
				if(pagliaio.equalsIgnoreCase(ago))
				{
					//System.out.println("ho scartato; " + ago);
					return false;
				}
			}
		}
		for(int l=0; l<Globals.allNodesSyncroGeneral.size(); l++)
		{
			for(int i=0; i<Globals.allNodesSyncroGeneral.get(l).size(); i++)
			{
				Node attuale = Globals.allNodesSyncroGeneral.get(l).get(i);
				String pagliaio = pulisci(attuale.getProperties("name").values().toString());
				if(pagliaio.equalsIgnoreCase(ago))
				{
					//System.out.println("ho scartato; " + ago);
					return false;
				}
			}
		}
		return true;	
	}
	
	public static boolean notExistSyncroSecondRels(String s, String d, String e)
	{
		s = pulisci(s);
		d = pulisci(d);
		e = pulisci(e);
		for(int l=0; l<Globals.allRelationsSyncroGeneralSecond.size(); l++)
		{
			for(int i=0; i<Globals.allRelationsSyncroGeneralSecond.get(l).size(); i++)
			{
				Relationship attuale = Globals.allRelationsSyncroGeneralSecond.get(l).get(i);
				String pagliaios = pulisci(attuale.getProperties("from").values().toString());
				String pagliaiod = pulisci(attuale.getProperties("to").values().toString());
				String pagliaioe = pulisci(attuale.getProperties("event").values().toString());
				if(stessoStato(pagliaios,s) && stessoStato(pagliaiod,d) && pagliaioe.equalsIgnoreCase(e))
				{
					//System.out.println("ho scartato; " + ago);
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean notExistSyncroSecondNode(String ago)
	{
		for(int l=0; l<Globals.allNodesSyncroGeneralSecond.size(); l++)
		{
			/*if(ago.equalsIgnoreCase("B-E"))
			{
				System.out.println("chiamato: " + Globals.allRelationsSyncroGeneralSecond.get(l).size());
			}*/
			for(int i=0; i<Globals.allNodesSyncroGeneralSecond.get(l).size(); i++)
			{
				/*if(ago.equalsIgnoreCase("B-E"))
				{
					System.out.println("chiamato: " + Globals.allRelationsSyncroGeneralSecond.get(l).size());
				}*/
				Node attuale = Globals.allNodesSyncroGeneralSecond.get(l).get(i);
				String pagliaio = pulisci(attuale.getProperties("name").values().toString());
				if(stessoStato(pagliaio,ago))
				{
					//System.out.println("per la cosa dei nodinscarrto " + ago);
					return false;
				}
			}
		}
		return true;	
	}
	
	
	public static boolean notExistBadRels(String n1, String n2, String guasto, String event, int level )
	{
		for(int l=0; l<=level; l++)
		{
			for(int i=0; i<Globals.allRelationsGeneral.get(l).size(); i++)
			{
				Relationship attuale = Globals.allRelationsGeneral.get(l).get(i);
				String pagliaioN1 = pulisci(attuale.getProperties("from").values().toString());
				String pagliaioN2 = pulisci(attuale.getProperties("to").values().toString());
				String pagliaioGu = pulisci(attuale.getProperties("event").values().toString());
				String pagliaioEv = pulisci(attuale.getProperties("guasto").values().toString());
							
				
				if(stessoStato(pagliaioN1,n1) && stessoStato(pagliaioN2, n2)
						&& stessoEvento(pagliaioEv, event) && pagliaioGu.equalsIgnoreCase(guasto))
				{
					//System.out.println("ho scartato; " + ago);
					return false;
				}
			}	
		}		
		return true;	
	}
	
	
	public static boolean stessoEvento(String a , String b)
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
	
	public static boolean inNodes(Node ago, Vector<Node> pagliaio)
	{
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<pagliaio.size(); i++)
			{
				String idAgo = ago.getProperties("name").values().toString();
				String idPagliaioi = pagliaio.get(i).getProperties("name").values().toString();
				if(idAgo.equalsIgnoreCase(idPagliaioi))
				{
					return true;
				}
			}
			tx.success();
		}	
		return false;
	}


}
