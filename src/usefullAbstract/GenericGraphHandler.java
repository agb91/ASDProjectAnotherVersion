package usefullAbstract;

import java.util.Iterator;

import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.PathExpanders;

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

}
