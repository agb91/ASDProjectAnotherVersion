package usefullAbstract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import Twins.TransizioneDoppia;
import global.Globals;
import talkToDb.ORM.RelTypes;

public class Adder {
	
	protected static String pulisci(String s)
	{
		String ris = s;
		for(int i=0; i<2; i++)
		{
			if(ris.startsWith("["))
			{
			   ris = ris.substring(1,ris.length()-1);
			}
		}
		return ris;
	}
	
	protected static boolean inVettore(String ago, Vector<String> pagliaio)
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
	
	protected static boolean inVettoreSyncro(Relationship ago,
			Vector<TransizioneDoppia> pagliaio, String who)
	{
		if(who.equalsIgnoreCase("f"))
		{
			try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
			{
				String fromAgo = ago.getProperties("from").values().toString();
				fromAgo = pulisci(fromAgo);
				String toAgo = ago.getProperties("to").values().toString();
				toAgo = pulisci(toAgo);
				String evAgo = ago.getProperties("event").values().toString();
				evAgo = pulisci(evAgo);
				for(int i=0; i<pagliaio.size(); i++)
				{
					String fromPa = pagliaio.get(i).getSorgente();
					String toPa = pagliaio.get(i).getDestinazione();
					String evPa = pagliaio.get(i).getEvento();
		
					if(fromAgo.equalsIgnoreCase(fromPa)
							&& toAgo.equalsIgnoreCase(toPa)
							&& evAgo.equalsIgnoreCase(evPa))
					{
						return true;
					}
				}
				tx.success();
			}	
		}
		else
		{
			try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
			{
				String fromAgo = ago.getProperties("from").values().toString();
				fromAgo = pulisci(fromAgo);
				String toAgo = ago.getProperties("to").values().toString();
				toAgo = pulisci(toAgo);
				String evAgo = ago.getProperties("event").values().toString();
				evAgo = pulisci(evAgo);
				for(int i=0; i<pagliaio.size(); i++)
				{
					String fromPa = pagliaio.get(i).getSorgente();
					String toPa = pagliaio.get(i).getDestinazione();
					String evPa = pagliaio.get(i).getEvento();
		
					if(fromAgo.equalsIgnoreCase(fromPa)
							&& toAgo.equalsIgnoreCase(toPa)
							&& evAgo.equalsIgnoreCase(evPa))
					{
						return true;
					}
				}
				tx.success();
			}	
		}
		return false;
	}
	
	protected static boolean inVettoreSyncroHash(Relationship ago,
			HashMap<String, TransizioneDoppia> pagliaio, String who)
	{
		if(who.equalsIgnoreCase("f"))
		{
			try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
			{
				String idAgo = ago.getProperties("type").values().toString();
				idAgo = pulisci(idAgo);
				if(pagliaio.get(idAgo)!=null)
				{
					return true;
				}
				tx.success();
			}	
		}
		else
		{
			try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
			{
				String idAgo = ago.getProperties("type").values().toString();
				idAgo = pulisci(idAgo);
				if(pagliaio.get(idAgo)!=null)
				{
					return true;
				}
				tx.success();
			}	
		}
		return false;
	}
	
	
	protected static boolean inVettore(Relationship ago, Vector<Relationship> pagliaio)
	{
		for(int i=0; i<pagliaio.size(); i++)
		{
			String attuale = pagliaio.get(i).getProperties("type").values().toString();
			attuale = pulisci(attuale);
			String nomeAgo = ago.getProperties("type").values().toString();
			if(attuale.equalsIgnoreCase(nomeAgo))
			{
				return true;
			}
		}
		return false;
	}
	
	
	protected static void addNodeSyncro( String n, int level)
	{
		long startTime = System.currentTimeMillis();
		Node userNode = null;
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			if(InVector.notExistSyncro(n))
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = Globals.graphDbSyncro.createNode( label );
		        userNode.setProperty( "name", n);
		        Globals.allNodesSyncroGeneral.get(level).addElement(userNode);
			    tx.success();
			}   
		}    	
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		Globals.writeTime += seconds;

	}
	
	
	protected static boolean stessoStato(String primo, String secondo)
	{
		if(primo.equalsIgnoreCase(secondo))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
		
	protected static void addNodeSyncroSecond( String n, int level)
	{
		long startTime = System.currentTimeMillis();
		Node userNode = null;
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
			if(InVector.notExistSyncroSecondNode(n))
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = Globals.graphDbSyncroSecond.createNode( label );
		        userNode.setProperty( "name", n);
		        Globals.allNodesSyncroGeneralSecond.get(level).addElement(userNode);
			    tx.success();
			    //Globals.lastSyncroNodes.add(userNode);
			}   
		}   
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		Globals.writeTime += seconds;
	}
	
	protected static void addNodeGood(String n) {
		long startTime = System.currentTimeMillis();
		Node userNode = null;
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
			if(!InVector.inNodes(n, Globals.allNodesGood))
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = Globals.graphDbGood.createNode( label );
		        userNode.setProperty( "name", n);
		        Globals.allNodesGood.addElement(userNode);
			}
		    tx.success();
		}    
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		Globals.writeTime += seconds;
	}
	

	/*protected static boolean notExist(String ago, int level )
	{
		for(int l=0; l<=level; l++)
		{
			for(int i=0; i<Globals.allRelationsGeneral.get(l).size(); i++)
			{
				Relationship attuale = Globals.allRelationsGeneral.get(l).get(i);
				String pagliaio = pulisci(attuale.getProperties("type").values().toString());
				if(pagliaio.equalsIgnoreCase(ago))
				{
					//System.out.println("ho scartato; " + ago);
					return false;
				}
			}	
		}		
		return true;	
	}*/
	

	
	protected static Relationship addRelationBad(Node n1, Node n2, String nome, String oss, String ev, String gu, int level)
	{
		long startTime = System.currentTimeMillis();
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			if(InVector.notExistBadRels(pulisci(nome), level))
			{
				//System.err.println("level: " + level);
				relationship = n1.createRelationshipTo( n2, RelTypes.STD );
				relationship.setProperty( "type", pulisci(nome) );
				relationship.setProperty( "oss", pulisci(oss) );
				ev = pulisci(ev);
				relationship.setProperty("event", pulisci(ev));
				relationship.setProperty("guasto", pulisci(gu));
				String nomeN1 = n1.getProperties("name").values().toString();
				String nomeN2 = n2.getProperties("name").values().toString();	
				relationship.setProperty("from", pulisci(nomeN1));
				relationship.setProperty("to", pulisci(nomeN2));
				//System.err.println("chiave: " + nome);
				//System.err.println("prima: " +  Globals.allRelationsGeneralHash.get(level).size());
				Globals.allRelationsGeneralHash.get(level).put(pulisci(nome), relationship);
				//System.err.println("dopo: " +  Globals.allRelationsGeneralHash.get(level).size());
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			}
			tx.success();
		}	
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		Globals.writeTime += seconds;
		return relationship;
	}
	
	protected static Relationship addRelationSyncro(String n1s, String n2s, String nome, String oss, String ev, int level)
	{
		long startTime = System.currentTimeMillis();
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			if(InVector.notExistSyncro(nome))
			{
				Node n1 = findNodeByNameSyncro(n1s);
				Node n2 = findNodeByNameSyncro(n2s);
				if(n1!=null && n2!=null)
				{	
					relationship = n1.createRelationshipTo( n2, RelTypes.STD );
					relationship.setProperty( "type", pulisci(nome) );
					relationship.setProperty( "oss", pulisci(oss) );
					ev = pulisci(ev);
					relationship.setProperty("event", pulisci(ev));
					String nomeN1 = n1.getProperties("name").values().toString();
					String nomeN2 = n2.getProperties("name").values().toString();	
					relationship.setProperty("from", pulisci(nomeN1));
					relationship.setProperty("to", pulisci(nomeN2));
					Globals.allRelationsSyncroGeneralHash.get(level).put(pulisci(nome), relationship);
					//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
				}
				else
				{
					System.err.println("ho omesso delle transizioni");
				}
			}
			tx.success();
		}	
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		Globals.writeTime += seconds;
		return relationship;
	}
	
	protected static Relationship addRelationSyncroSecond(String n1s, String n2s, String nome, String oss, String ev, int level)
	{
		long startTime = System.currentTimeMillis();
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
			//System.err.println("ecco la transizione  " + pulisci(nome));
			//if(InVector.notExistSyncroSecondRels(n1s, n2s, ev))
			if(InVector.notExistSyncroSecondRels(pulisci(nome)))
			{
				//System.out.println("nodo: " + n1s);
				Node n1 = findNodeByNameSyncroSecond(n1s);
				Node n2 = findNodeByNameSyncroSecond(n2s);		
				relationship = n1.createRelationshipTo( n2, RelTypes.STD );
				relationship.setProperty( "type", pulisci(nome) );
				relationship.setProperty( "oss", pulisci(oss) );
				ev = pulisci(ev);
				relationship.setProperty("event", pulisci(ev));
				String nomeN1 = n1.getProperties("name").values().toString();
				String nomeN2 = n2.getProperties("name").values().toString();	
				relationship.setProperty("from", pulisci(nomeN1));
				relationship.setProperty("to", pulisci(nomeN2));
	
				Globals.allRelationsSyncroGeneralSecondHash.get(level).put(pulisci(nome), relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			
			}
			tx.success();
		}	
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		Globals.writeTime += seconds;
		return relationship;
	}
	
	protected static Node findNodeByNameSyncro(String n1s)
	{
		String a = n1s.split("-")[0];
		String b = n1s.split("-")[1];
		String n2s = b+"-"+a;
		ArrayList<Node> userNodes = new ArrayList<>();
		Label label = DynamicLabel.label( "Nome" );
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		    		Globals.graphDbSyncro.findNodes( label, "name", n1s ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }
		    }
		    tx.success();
		}
		
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		    		Globals.graphDbSyncro.findNodes( label, "name", n2s ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }
		    }
		    tx.success();
		}
		if(userNodes.size()==0)
		{
			System.out.println("non trovo il nodo: " + n1s+ "||--fine nome");
			return null;
		}
		return userNodes.get(0);

	}
	
	protected static Node findNodeByNameSyncroSecond(String n1s)
	{
		ArrayList<Node> userNodes = new ArrayList<>();
		Label label = DynamicLabel.label( "Nome" );
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		    		Globals.graphDbSyncroSecond.findNodes( label, "name", n1s ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }
		    }
		    String A = n1s.split("-")[0];
			String B = n1s.split("-")[1];
			String n2s = B+"-"+A;
		    try ( ResourceIterator<Node> users =
		    		Globals.graphDbSyncroSecond.findNodes( label, "name", n2s ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }
		    }
		    tx.success();
		}
		return userNodes.get(0);

	}
	
	protected static Node findNodeByNameBadStd(String nameToFind)
	{
		ArrayList<Node> userNodes = new ArrayList<>();
		Label label = DynamicLabel.label( "Nome" );
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
		    try ( ResourceIterator<Node> users =
		    		Globals.graphDb.findNodes( label, "name", nameToFind ) )
		    {
		        while ( users.hasNext() )
		        {
		            userNodes.add( users.next() );
		        }
		    }
		}
		if(userNodes.size()==0)
		{
			System.out.println("non trovo il nodo: " + nameToFind);
		}
		
		return userNodes.get(0);

	}
	
	protected static Node findNodeByNameGood(String n2) {
		n2 = pulisci(n2);
		Node returned = null;
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
			for(int i=0; i<Globals.allNodesGood.size(); i++)
			{
				Node attuale = Globals.allNodesGood.get(i);
				String nomeAttuale = attuale.getProperties("name").values().toString();
				nomeAttuale = pulisci(nomeAttuale);
				/*System.out.println("attuale: " + nomeAttuale);
				System.out.println("n2 : " + n2);
				System.out.println("------------------------------------------------------");
				*/
				if(n2.equalsIgnoreCase(nomeAttuale))
				{
					returned = attuale;
				}
			}
			tx.success();
		}
		if(returned==null)
		{
			System.out.println("non trovo il nodo: " + n2);
		}
		return returned;
	}
	

	/*protected static boolean notExistGood(String ago, int level)
	{
		for(int l=0; l<=level; l++)
		{
			for(int i=0; i<Globals.allRelationsGoodGeneral.get(l).size(); i++)
			{
				Relationship attuale = Globals.allRelationsGoodGeneral.get(l).get(i);
				String pagliaio = pulisci(attuale.getProperties("type").values().toString());
				if(pagliaio.equalsIgnoreCase(ago))
				{
					//System.out.println("ho scartato; " + ago);
					return false;
				}
			}	
		}		
		return true;
	}*/

	
	protected static void addRelationGood(String n1, String n2, String nome, 
			String oss, String ev, String gu, int level) {
		long startTime = System.currentTimeMillis();
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
			if(InVector.notExistGoodRels(nome, level))
			{
				Node n1Node = findNodeByNameGood(pulisci(n1));
				Node n2Node = findNodeByNameGood(pulisci(n2));
				relationship = n1Node.createRelationshipTo( n2Node, RelTypes.STD );
				relationship.setProperty( "type", pulisci(nome) );
				relationship.setProperty( "oss", pulisci(oss) );
				ev = pulisci(ev);
				relationship.setProperty("event", pulisci(ev));
				relationship.setProperty("guasto", pulisci(gu));
				relationship.setProperty("from", pulisci(n1));
				relationship.setProperty("to", pulisci(n2));
				tx.success();
				Globals.allRelationsGoodGeneralHash.get(level).put(pulisci(nome), relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			}	
		}
		long endTime = System.currentTimeMillis();
		long seconds = (endTime - startTime);
		Globals.writeTime += seconds;
	}
	


}
