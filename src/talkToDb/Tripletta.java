package talkToDb;

import java.lang.reflect.Array;
import java.util.Arrays;

import org.neo4j.graphdb.Node;

public class Tripletta {

	private String evento;
	private Node sDestinazione;
	private boolean faultPrimo;
	
	public Tripletta(String _evento, Node _sDestinazione, boolean _faultPrimo)
	{
		setEvento(_evento);
		setsDestinazione(_sDestinazione);
		setFaultPrimo(_faultPrimo);
	}

	public String getEvento() {
		return evento;
	}
	
	public String getEventoOrdered()
	{
		//System.out.println("prima= "+evento);
		String appoggio = evento;
		String[] vettore = appoggio.split("//");
		Arrays.sort(vettore);
		appoggio = "";
		appoggio = vettore[0];
		for(int i=1; i<vettore.length; i++)
		{
			appoggio+="//" + vettore[i];
		}
		//System.out.println("dopo = " + appoggio);
		
		//System.out.println("-----------------------------");
		return appoggio;
	}

	public void setEvento(String evento) {
		this.evento = evento;
	}

	public Node getsDestinazione() {
		return sDestinazione;
	}

	public void setsDestinazione(Node sDestinazione) {
		this.sDestinazione = sDestinazione;
	}

	public boolean isFaultPrimo() {
		return faultPrimo;
	}

	public void setFaultPrimo(boolean faultPrimo) {
		this.faultPrimo = faultPrimo;
	}
}
