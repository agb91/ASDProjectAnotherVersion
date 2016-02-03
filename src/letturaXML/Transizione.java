package letturaXML;

public class Transizione {
	
	private String nome;
	private String oss;
	private String evento;
	private String guasto;
	private String from;
	private String to;

	public Transizione(String _nome, String _evento, String _oss, String _guasto,String _from, String _to)
	{
		setNome(_nome);
		setEvento(_evento);
		setOss(_oss);
		setGuasto(_guasto);
		setFrom(_from);
		setTo(_to);
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getOss() {
		return oss;
	}

	public void setOss(String oss) {
		this.oss = oss;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getEvento() {
		return evento;
	}

	public void setEvento(String evento) {
		this.evento = evento;
	}

	public String getGuasto() {
		return guasto;
	}

	public void setGuasto(String guasto) {
		this.guasto = guasto;
	}

}
