package utility;

import java.util.Vector;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import talkToDb.ORM;

public class Utils {
	
	public static Vector<Relationship> unione (Vector<Relationship> a, Vector<Relationship> b)
	{
		Vector<Relationship> risultato = new Vector<Relationship>();
		for(int i=0; i<a.size(); i++)
		{
			if(!ORM.inRel(a.get(i),risultato))
			{
				risultato.addElement(a.get(i));
			}
		}
		
		for(int i=0; i<b.size(); i++)
		{
			if(!ORM.inRel(b.get(i),risultato))
			{
				risultato.addElement(b.get(i));
			}
		}		
		return risultato;
	}
	
	public static Vector<Relationship> unione (Relationship a, Vector<Relationship> b)
	{
			if(!ORM.inRel(a,b))
			{
				b.addElement(a);
			}		
		return b;
	}
	
	public static Vector<Relationship> intersezione (Vector<Relationship> a, Vector<Relationship> b)
	{
		Vector<Relationship> risultato = new Vector<Relationship>();
		for(int i=0; i<a.size(); i++)
		{
			if(ORM.inRel(a.get(i), b))
			{ 
				risultato = unione(a.get(i),risultato);
			}
		}
		return risultato;
	}
	
	
	
	

}
