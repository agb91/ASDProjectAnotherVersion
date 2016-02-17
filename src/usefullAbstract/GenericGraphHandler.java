package usefullAbstract;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.ResourceIterator;
import org.neo4j.graphdb.Transaction;

import global.Globals;
import talkToDb.ORM.RelTypes;

public class GenericGraphHandler {
	
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
	
	protected static void removeIsolatedStatesBad()
	{
		boolean raggiungibile = false;
		for(int i=1; i<Globals.allNodes.size(); i++)
		{
			//System.out.println("check del nodo: " + Globals.allNodes.get(i));
			raggiungibile = checkPathFromRootBad(Globals.allNodes.get(i));
			if(!raggiungibile)
			{
				killNode(Globals.allNodes.get(i), i);
				i--;
			}
		}
	}
		
	//prima elimino tutte le relazioni che partono da n
	//poi elimino n
	protected static void killNode(Node n, int index)
	{
		Globals.allNodes.remove(index);
		String nomeNode = n.getProperties("name").values().toString();
		for(int a=0; a<Globals.allRelations.size(); a++)
		{
			Relationship r = Globals.allRelations.get(a);
			String fromr = r.getProperties("from").values().toString();
			if(fromr.contains(nomeNode))
			{
				Globals.allRelations.get(a).delete();
				Globals.allRelations.remove(a);
				a--;		
			}
		}
		n.delete();
	}
	
	
	protected static boolean checkPathFromRootBad(Node n)
	{
		boolean raggiungibile = false;
		//System.out.println("analizzo il nodo : " + n.getProperties("name").values().toString());
		
		Node root = Globals.allNodes.get(0);
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
	

	
	protected static Vector<String> riempiTPrimo()
	{
		Vector<String> ris = new Vector<String>();
		for(int i=0; i<Globals.allRelations.size(); i++)
		{
			Relationship attuale = Globals.allRelations.get(i);
			String osservabilita = attuale.getProperties("oss").values().toString();
			if(osservabilita.contains("y"))
			{
				String nome = attuale.getProperties("type").values().toString();
				ris.add(nome);
			}
		}
		return ris;
	}
	
	protected static Iterator<Path> findPath(Node s, Node e)
	{
		Iterator<Path> iteratore = null;
		PathFinder<Path> finder =
				GraphAlgoFactory.allPaths(PathExpanders.forDirection(
						Direction.OUTGOING ), 15 );
		Iterable<Path> paths = finder.findAllPaths( s, e );
		
		iteratore = paths.iterator();
		return iteratore;
	}
	
	protected static Relationship addRelationBad(Node n1, Node n2, String nome, String oss, String ev, String gu)
	{
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDb.beginTx() )
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
			tx.success();
			Globals.allRelations.addElement(relationship);
			//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
		}	
		return relationship;
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

	
	protected static void addRelationGood(String n1, String n2, String nome, String oss, String ev, String gu) {
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDbGood.beginTx() )
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
			Globals.allRelationsGood.addElement(relationship);
			//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
		
		}	
	}
	
	protected static Relationship addRelation(Node n1, Node n2, String nome, String oss, String ev, String gu)
	{
		Relationship relationship = null;
		try ( Transaction tx = Globals.graphDb.beginTx() )
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
			tx.success();
			Globals.allRelations.addElement(relationship);
			//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
		}	
		return relationship;
	}

}
