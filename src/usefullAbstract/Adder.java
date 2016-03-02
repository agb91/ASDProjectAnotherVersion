package usefullAbstract;

import java.util.ArrayList;
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
			Vector<TransizioneDoppia> pagliaio)
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
		Node userNode = null;
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			if(notExistSyncro(n))
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = Globals.graphDbSyncro.createNode( label );
		        userNode.setProperty( "name", n);
		        Globals.allNodesSyncroGeneral.get(level).addElement(userNode);
			    tx.success();
			}   
		}    		
	}
	
	protected static boolean notExistSyncro(String ago)
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
	
	protected static boolean stessoStato(String primo, String secondo)
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
	
	protected static boolean notExistSyncroSecondRels(String s, String d, String e)
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
	
	protected static boolean notExistSyncroSecondNode(String ago)
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
		
	protected static void addNodeSyncroSecond( String n, int level)
	{
		Node userNode = null;
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
			if(notExistSyncroSecondNode(n))
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = Globals.graphDbSyncroSecond.createNode( label );
		        userNode.setProperty( "name", n);
		        Globals.allNodesSyncroGeneralSecond.get(level).addElement(userNode);
			    tx.success();
			    //Globals.lastSyncroNodes.add(userNode);
			}   
		}    		
	}
	
	protected static void addNodeGood(String n) {
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
	

	protected static boolean notExist(String ago, int level )
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
	}
	

	
	protected static Relationship addRelationBad(Node n1, Node n2, String nome, String oss, String ev, String gu, int level)
	{
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDb.beginTx() )
		{
			if(notExist(nome, level))
			{
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
	
				Globals.allRelationsGeneral.get(level).addElement(relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			}
			tx.success();
		}	
		return relationship;
	}
	
	protected static Relationship addRelationSyncro(String n1s, String n2s, String nome, String oss, String ev, int level)
	{
		
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			if(notExistSyncro(nome))
			{
				Node n1 = findNodeByNameSyncro(n1s);
				Node n2 = findNodeByNameSyncro(n2s);
				relationship = n1.createRelationshipTo( n2, RelTypes.STD );
				relationship.setProperty( "type", pulisci(nome) );
				relationship.setProperty( "oss", pulisci(oss) );
				ev = pulisci(ev);
				relationship.setProperty("event", pulisci(ev));
				String nomeN1 = n1.getProperties("name").values().toString();
				String nomeN2 = n2.getProperties("name").values().toString();	
				relationship.setProperty("from", pulisci(nomeN1));
				relationship.setProperty("to", pulisci(nomeN2));
	
				Globals.allRelationsSyncroGeneral.get(level).addElement(relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			}
			tx.success();
		}	
		return relationship;
	}
	
	protected static Relationship addRelationSyncroSecond(String n1s, String n2s, String nome, String oss, String ev, int level)
	{
		
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDbSyncroSecond.beginTx() )
		{
			if(notExistSyncroSecondRels(n1s, n2s, ev))
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
	
				Globals.allRelationsSyncroGeneralSecond.get(level).addElement(relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			
			}
			tx.success();
		}	
		return relationship;
	}
	
	protected static Node findNodeByNameSyncro(String n1s)
	{
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
		return returned;
	}
	

	protected static boolean notExistGood(String ago, int level)
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
	}

	
	protected static void addRelationGood(String n1, String n2, String nome, 
			String oss, String ev, String gu, int level) {
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
		{
			if(notExistGood(nome, level))
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
				Globals.allRelationsGoodGeneral.get(level).addElement(relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			}	
		}
	}
	


}
