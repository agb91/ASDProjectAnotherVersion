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

public class GoodTwin extends GenericGraphHandler{
	
	public static void createGoodTwin(int l)
	{
		goodBase();
		removeGuasti();
		removeIsolatedStatesGood();
		System.out.println("created good twin level: "+l);
	}
	
	//crea il grafo good iniziale
	private static void goodBase()
	{
		try ( Transaction tx1 = Globals.graphDb.beginTx() )
		{
			Globals.allNodesGood.clear();
			Globals.allRelationsGood.clear();
			
			for(int i=0; i<Globals.allNodes.size(); i++)
			{
				Node attuale = Globals.allNodes.get(i);
				String nome = pulisci(attuale.getProperty("name").toString());
				//System.out.println("node: " + nome);
				addNodeGood(nome);
			}
			
			for(int i=0; i<Globals.allRelations.size(); i++)
			{
				Relationship attuale = Globals.allRelations.get(i);
				String nome = pulisci(attuale.getProperties("type").values().toString());
				String n1 = pulisci(attuale.getStartNode().getProperties("name").values().toString());
				String n2 = pulisci(attuale.getEndNode().getProperties("name").values().toString());
				String oss = pulisci(attuale.getProperties("oss").values().toString());
				String ev = pulisci(attuale.getProperties("event").values().toString());
				String gu = pulisci(attuale.getProperties("guasto").values().toString());
				addRelationGood(n1, n2, nome, oss, ev, gu);
			}
			
			tx1.success();
		}	
		
	}

	private static void addNodeGood(String n) {
		Node userNode = null;
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
		    Label label = DynamicLabel.label( "Nome" );
	        userNode = Globals.graphDbGood.createNode( label );
	        userNode.setProperty( "name", n);
	        Globals.allNodesGood.addElement(userNode);
		    tx.success();
		}    		
	}

	private static void removeGuasti()
	{
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
			for(int i=0; i<Globals.allRelationsGood.size(); i++)
			{
				Relationship attuale = Globals.allRelationsGood.get(i);
				String guasto = attuale.getProperties("guasto").values().toString();
				//System.out.println("vedo guasto: " + guasto);
				if(guasto.toLowerCase().contains("y"))
				{
					//System.out.println("e me ne libero: " + Globals.allRelationsGood.get(i).getId());
					
					Globals.allRelationsGood.get(i).delete();
					Globals.allRelationsGood.remove(i);
					i--;
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
					killNode(Globals.allNodesGood.get(i), i);
					i--;
				}
			}
			tx1.success();
		}	
	}
	
	//prima elimino tutte le relazioni che partono da n
	//poi elimino n
	public static void killNode(Node n, int index)
	{
		Globals.allNodesGood.remove(index);
		String nomeNode = n.getProperties("name").values().toString();
		for(int a=0; a<Globals.allRelationsGood.size(); a++)
		{
			Relationship r = Globals.allRelationsGood.get(a);
			String fromr = r.getProperties("from").values().toString();
			if(fromr.contains(nomeNode))
			{
				Globals.allRelationsGood.get(a).delete();
				Globals.allRelationsGood.remove(a);
				a--;		
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
