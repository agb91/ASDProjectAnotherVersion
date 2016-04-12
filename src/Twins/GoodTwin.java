package Twins;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import talkToDb.ORM;
import talkToDb.ORM.RelTypes;
import usefullAbstract.GenericGraphHandler;
import usefullAbstract.InVector;

public class GoodTwin extends GenericGraphHandler{
	
	public static void createGoodTwin(int l)
	{
		if(!inInteger(l,Globals.goodTwinDid))
		{
			goodBase(l);
			if(l==1)
			{
				removeIsolatedStatesGood();
			}
			System.out.println("created good twin level: "+l);
			Globals.goodTwinDid.addElement(Integer.valueOf(l));
		}		
	}
	
	//crea il grafo good iniziale
	private static void goodBase(int level)
	{
		//System.err.println("size: " + Globals.allNodes.size());

		for(int i=0; i<Globals.allNodes.size(); i++) 
		{
			String nome = "";
			try ( Transaction tx = Globals.graphDb.beginTx() )
			{
				Node attuale = Globals.allNodes.get(i);
				nome = pulisci(attuale.getProperty("name").toString());
				tx.success();
			}
			//System.out.println("node: " + nome);
			//System.err.println("addo ora: " + nome);
			addNodeGood(nome);	
		}
		
		for(int l=0; l<=level; l++)
		{
			HashMap <String, Relationship> hash = new HashMap <String, Relationship>();
			
			Iterator<String> keyset = Globals.allRelationsGeneralHash.get(l).keySet().iterator();
			while(keyset.hasNext())
			{ 
				String key = keyset.next();
				Relationship attuale = Globals.allRelationsGeneralHash.get(l).get(key);
				String nome="";
				String n1 ="";
				String n2 ="";
				String oss ="";
				String ev ="";
				String gu ="";
				try ( Transaction tx = Globals.graphDb.beginTx() )
				{
					nome = pulisci(attuale.getProperties("type").values().toString());
					n1 = pulisci(attuale.getStartNode().getProperties("name").values().toString());
					n2 = pulisci(attuale.getEndNode().getProperties("name").values().toString());
					oss = pulisci(attuale.getProperties("oss").values().toString());
					ev = pulisci(attuale.getProperties("event").values().toString());
					gu = pulisci(attuale.getProperties("guasto").values().toString());
					tx.success();
				}		
				if(gu.equalsIgnoreCase("n"))
				{
					//System.err.println("addo a level: " + level);
					addRelationGood(n1, n2, nome, oss, ev, gu, level);
				}
			}
		}
	}
	
	private static void removeGuasti(int level)
	{
		Vector<String> daCancellare = new Vector<String>();
		for(int l=0; l<=level; l++)
		{
			Iterator<String> keyset = Globals.allRelationsGoodGeneralHash.get(l).keySet().iterator();
			while(keyset.hasNext())
			{ 
				try ( Transaction tx1 = Globals.graphDbGood.beginTx() )
				{
					String key = keyset.next();
					Relationship attuale = Globals.allRelationsGoodGeneralHash.get(l).get(key);
					String guasto = attuale.getProperties("guasto").values().toString();
					//System.out.println("vedo guasto: " + guasto);
	
					if(guasto.toLowerCase().contains("y"))
					{
						daCancellare.addElement(key);
					}
					tx1.success();
				}
			}
		}
		//System.err.println(Globals.allRelationsGoodGeneralHash.get(1).get("E--C--F--y"));
		for(int i=0; i<daCancellare.size(); i++)
		{
			for(int l=0; l<Globals.allRelationsGoodGeneralHash.size(); l++)
			{
				try ( Transaction tx1 = Globals.graphDbGood.beginTx() )
				{
					//System.err.println("cancello : " + daCancellare.get(i));
					//killRelation(daCancellare.get(i), Globals.allRelationsGoodGeneralHash);
					if(Globals.allRelationsGoodGeneralHash.get(l).get(daCancellare.get(i))!=null)
					{
						Globals.allRelationsGoodGeneralHash.get(l).get(daCancellare.get(i)).delete();
					}
					Globals.allRelationsGoodGeneralHash.get(l).remove(daCancellare.get(i));
					tx1.success();
				}
			}
		}
		//System.err.println(Globals.allRelationsGoodGeneralHash.get(1).get("E--C--F--y"));
	}
	
	public static void removeIsolatedStatesGood()
	{
		try ( Transaction tx1 = Globals.graphDbGood.beginTx() )
		{
			boolean raggiungibile = false;
			for(int i=1; i<Globals.allNodesGood.size(); i++)
			{
				raggiungibile = checkPathFromRootGood(Globals.allNodesGood.get(i));
				if(!raggiungibile)
				{
					killNodeGood(Globals.allNodesGood.get(i), i);
					i--;
				}
			}
			tx1.success();
		}	
	}
	
	//prima elimino tutte le relazioni che partono da n
	//poi elimino n
	public static void killNodeGood(Node n, int index)
	{
		String nomeNode = pulisci(n.getProperties("name").values().toString());
		System.err.println("sto killando il nodo: " + nomeNode);
		
		Vector<String> toKill = new Vector<String>();
	
		Iterator<String> ks = Globals.allRelationsGoodGeneralHash.get(1).keySet().iterator();
		while(ks.hasNext())
		{
			String k = ks.next();
			Relationship r = Globals.allRelationsGoodGeneralHash.get(1).get(k);
			String from = r.getProperties("from").values().toString();
			from = pulisci(from);
			String nome = r.getProperties("type").values().toString();
			nome = pulisci(nome);
			if(from.equalsIgnoreCase(nomeNode))
			{
				System.err.println("tolgo la relazione: " + nome);
				Globals.allRelationsGoodGeneralHash.get(1).get(k).delete();
				toKill.addElement(k);
			}			
		}
		
		for(int i=0; i<toKill.size(); i++)
		{
			Globals.allRelationsGoodGeneralHash.get(1).remove(toKill.get(i));
		}
		
		Globals.allNodesGood.get(index).delete();
		Globals.allNodesGood.remove(index);
		System.err.println("rimosso nodo: " + nomeNode);
	}
	

	public static boolean checkPathFromRootGood(Node n)
	{
		boolean raggiungibile = false;
		
		Node root = Globals.allNodesGood.get(0);
		Iterator<Path> tuttiIPath = findPathGood(root,n);
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
