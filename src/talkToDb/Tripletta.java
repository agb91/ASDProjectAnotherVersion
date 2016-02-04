package talkToDb;

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
