package usefullAbstract;

import java.util.ArrayList;
import java.util.Arrays;
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
	
	
	/*
	 * se 2 transizioni diverse hanno stessa sorgente e stesso evento allora
	 *  NON DETERMISTICO
	 */
	protected static boolean deterministic(Vector<Relationship> T)
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
	
	protected static void addNodeSyncro( String n)
	{
		Node userNode = null;
		try ( Transaction tx = Globals.graphDbSyncro.beginTx() )
		{
			if(notExistSyncro(n))
			{
			    Label label = DynamicLabel.label( "Nome" );
		        userNode = Globals.graphDbSyncro.createNode( label );
		        userNode.setProperty( "name", n);
		        Globals.allNodesSyncro.addElement(userNode);
			    tx.success();
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
	
	protected static boolean uguali(String a , String b)
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
	
	//cerco le transizioni di guasto: prendo il loro evento.
	// se per tutti quegli eventi non esistono transizioni di guasto
	// che abbiano come evento quegli eventi allora è diagnosticabile
	protected static boolean thirdCondition(Vector<Relationship> T)
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
	
	
	protected static boolean checkEqual(Vector<String> primo, Vector<String> secondo)
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
	
	protected static void removeIsolatedStatesBad(int level)
	{
		boolean raggiungibile = false;
		for(int i=1; i<Globals.allNodes.size(); i++)
		{
			//System.out.println("check del nodo: " + Globals.allNodes.get(i));
			raggiungibile = checkPathFromRootBad(Globals.allNodes.get(i));
			if(!raggiungibile)
			{
				killNode(Globals.allNodes.get(i), i, level);
				i--;
			}
		}
	}
		
	//prima elimino tutte le relazioni che partono da n
	//poi elimino n
	protected static void killNode(Node n, int index, int level)
	{
		Globals.allNodes.remove(index);
		String nomeNode = n.getProperties("name").values().toString();
		for(int a=0; a<Globals.allRelationsGeneral.get(level).size(); a++)
		{
			Relationship r = Globals.allRelationsGeneral.get(level).get(a);
			String fromr = r.getProperties("from").values().toString();
			if(fromr.contains(nomeNode))
			{
				Globals.allRelationsGeneral.get(level).get(a).delete();
				Globals.allRelationsGeneral.get(level).remove(a);
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
	
	protected static Vector<Relationship> getAllRelationsUntil(int level, Vector<Vector<Relationship>> who)
	{
		Vector<Relationship> ris = new Vector<Relationship>();
		for(int l=0; l<=level; l++ )
		{
			for(int i=0; i< who.get(l).size(); i++)
			{
				Relationship nuova = who.get(l).get(i);
				ris.add(nuova);
			}
		}
		return ris;
	}
	
	protected static Vector<String> riempiTPrimo(int level)
	{
		Vector<String> ris = new Vector<String>();
		for(int l=1; l<=level; l++)
		{
			for(int i=0; i<Globals.allRelationsGeneral.get(l-1).size(); i++)
			{
				Relationship attuale = Globals.allRelationsGeneral.get(l-1).get(i);
				String osservabilita = attuale.getProperties("oss").values().toString();
				if(osservabilita.contains("y"))
				{
					String nome = attuale.getProperties("type").values().toString();
					ris.add(nome);
				}
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
	
	protected static boolean notExistSyncro(String ago)
	{
		for(int i=0; i<Globals.allRelationsSyncro.size(); i++)
		{
			Relationship attuale = Globals.allRelationsSyncro.get(i);
			String pagliaio = pulisci(attuale.getProperties("type").values().toString());
			if(pagliaio.equalsIgnoreCase(ago))
			{
				//System.out.println("ho scartato; " + ago);
				return false;
			}
		}
		for(int i=0; i<Globals.allNodesSyncro.size(); i++)
		{
			Node attuale = Globals.allNodesSyncro.get(i);
			String pagliaio = pulisci(attuale.getProperties("name").values().toString());
			if(pagliaio.equalsIgnoreCase(ago))
			{
				//System.out.println("ho scartato; " + ago);
				return false;
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
	
	protected static Relationship addRelationSyncro(String n1s, String n2s, String nome, String oss, String ev)
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
	
				Globals.allRelationsSyncro.addElement(relationship);
				//System.out.println("ho aggiunto la relazione: " + nome + "  da: " + nomeN1 + "  a: " + nomeN2);
			}
			tx.success();
		}	
		return relationship;
	}
	
	private static Node findNodeByNameSyncro(String n1s)
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
