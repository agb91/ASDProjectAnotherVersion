package Twins;

import java.util.Iterator;

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
			removeGuasti(l);
			removeIsolatedStatesGood();
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
			for(int i=0; i<Globals.allRelationsGeneral.get(l).size(); i++)
			{
				String nome="";
				String n1 ="";
				String n2 ="";
				String oss ="";
				String ev ="";
				String gu ="";
				try ( Transaction tx = Globals.graphDb.beginTx() )
				{
					Relationship attuale = Globals.allRelationsGeneral.get(l).get(i);
					nome = pulisci(attuale.getProperties("type").values().toString());
					n1 = pulisci(attuale.getStartNode().getProperties("name").values().toString());
					n2 = pulisci(attuale.getEndNode().getProperties("name").values().toString());
					oss = pulisci(attuale.getProperties("oss").values().toString());
					ev = pulisci(attuale.getProperties("event").values().toString());
					gu = pulisci(attuale.getProperties("guasto").values().toString());
					tx.success();
				}				
				addRelationGood(n1, n2, nome, oss, ev, gu, level);
			}
		}
		
	}

	
	private static void removeGuasti(int level)
	{
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
			for(int l=0; l<=level; l++)
			{
				for(int i=0; i<Globals.allRelationsGoodGeneral.get(l).size(); i++)
				{
					Relationship attuale = Globals.allRelationsGoodGeneral.get(l).get(i);
					String guasto = attuale.getProperties("guasto").values().toString();
					//System.out.println("vedo guasto: " + guasto);
					if(guasto.toLowerCase().contains("y"))
					{
						//System.out.println("e me ne libero: " + Globals.allRelationsGood.get(i).getId());
						
						Globals.allRelationsGoodGeneral.get(l).get(i).delete();
						Globals.allRelationsGoodGeneral.get(l).remove(i);
						i--;
					}
				}
			}			
			tx.success();
		}	
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
		Globals.allNodesGood.remove(index);
		String nomeNode = n.getProperties("name").values().toString();
		for(int l=0; l<Globals.allRelationsGoodGeneral.size(); l++)
		{
			for(int a=0; a<Globals.allRelationsGoodGeneral.get(l).size(); a++)
			{
				Relationship r = Globals.allRelationsGoodGeneral.get(l).get(a);
				String fromr = r.getProperties("from").values().toString();
				if(fromr.contains(nomeNode))
				{
					Globals.allRelationsGoodGeneral.get(l).get(a).delete();
					Globals.allRelationsGoodGeneral.get(l).remove(a);
					a--;		
				}
			}
		}		
		n.delete();
	}
	

	public static boolean checkPathFromRootGood(Node n)
	{
		boolean raggiungibile = false;
		
		Node root = Globals.allNodesGood.get(0);
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
