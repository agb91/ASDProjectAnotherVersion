package talkToDb;

import java.util.Vector;

import org.neo4j.graphdb.Relationship;

public class Cycle implements Cloneable{
	
	private static Vector<Relationship> relazioni;
	
	public Cycle()
	{
		relazioni = new Vector<Relationship>();
	}
	
	public void addRel(Relationship a)
	{
		relazioni.addElement(a);
	}
	
	public void addRels(Vector<Relationship> a)
	{
		relazioni.addAll(a);
	}
	
	public Vector<Relationship> getRels()
	{
		return relazioni;
	}
}
