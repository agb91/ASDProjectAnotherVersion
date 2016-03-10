package usefullAbstract;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import Twins.TransizioneDoppia;
import global.Globals;

public class InVector extends GenericGraphHandler{
	
	// se se ago è dentro pagliaio
	public static boolean inRel(String id, HashMap<String, Relationship> pagliaio)
	{
		if(pagliaio.size()==0)
		{
			return false;
		}
		if(pagliaio.get(id)!=null)
		{
			return true;
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
	
	public static boolean inVettoreHash(String ago, HashMap<String,String> pagliaio)
	{
		if(pagliaio.get(ago)!=null)
		{
			return true;
		}
		return false;
	}
	
	
	public static boolean InDoppia( TransizioneDoppia ago, Vector<TransizioneDoppia> pagliaio)
	{
		if(pagliaio.size()==0)
		{
			return false;
		}
		String fago = ago.getSorgente();
		String dago = ago.getDestinazione();
		String evago = ago.getEvento();
		for (int i=0; i<pagliaio.size(); i++)
		{
			String fpagliaio = pagliaio.get(i).getSorgente();
			String dpagliaio = pagliaio.get(i).getDestinazione();
			String evpagliaio = pagliaio.get(i).getEvento();
			if(fago.equalsIgnoreCase(fpagliaio) && dago.equalsIgnoreCase(dpagliaio)
					&& evago.equalsIgnoreCase(evpagliaio))
			{
				return true;
			}
		}
		return false;
	}
	
	public static boolean inNodes(String s, Vector<Node> pagliaio)
	{
		if(pagliaio.size()==0)
		{
			return false;
		}
		s = pulisci(s);
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			for(int i=0; i<pagliaio.size(); i++)
			{
				String attuale = pagliaio.get(i).getProperties("name").values().toString();
				attuale = pulisci(attuale);
				if(attuale.equalsIgnoreCase(s))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	public static boolean notExistGoodRels(String id, int level)
	{
		boolean esiste = false;
		for(int l=0; l<=level; l++)
		{
			if(Globals.allRelationsGoodGeneralHash.get(l).get(id)!=null)
			{
				esiste = true;
			}
		}		
		return !esiste;
	}
	

	public static boolean notExistSyncro(String ago)
	{
		boolean esiste = false;
		for(int l=0; l<Globals.allRelationsSyncroGeneralHash.size(); l++)
		{
			if(Globals.allRelationsSyncroGeneralHash.get(l).get(ago)!=null)
			{
				esiste = true;
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
					esiste = true;
				}
			}
		}
		return !esiste;	
	}
	
	public static boolean notExistSyncroSecondRels(String id)
	{
		boolean esiste = false;
		id = pulisci(id);
		for(int l=0; l<Globals.allRelationsSyncroGeneralSecondHash.size(); l++)
		{
			if(Globals.allRelationsGoodGeneralHash.get(l).get(id)!=null)
			{
				esiste = true;
			}
		}
		return !esiste;
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
	
	
	public static boolean notExistBadRels(String id, int level )
	{
		boolean esiste = false;
		id = pulisci(id);
		for(int l=0; l<=level; l++)
		{
			if(Globals.allRelationsGeneralHash.get(l).get(id)!=null)
			{
				esiste = true;
			}
		}
		return !esiste;	
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
