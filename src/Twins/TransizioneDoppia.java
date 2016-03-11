package Twins;

import java.util.Arrays;

public class TransizioneDoppia {
	
	private String evento;
	private String sorgente;
	private String destinazione;
	private String nome;
	//private String guasto;
	
	
	public String getEvento() {
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
	public String getSorgente() {
		String[] vettore = sorgente.split("-");
		Arrays.sort(vettore);
		sorgente = vettore[0] + "-" + vettore[1];
		return sorgente;
	}
	public void setSorgente(String sorgente) {
		this.sorgente = sorgente;
	}
	public String getDestinazione() {
		String[] vettore = destinazione.split("-");
		Arrays.sort(vettore);
		destinazione = vettore[0] + "-" + vettore[1];
		return destinazione;
	}
	
	public void setDestinazione(String destinazione) {
		this.destinazione = destinazione;
	}
	/*public String getGuasto() {
		return guasto;
	}
	public void setGuasto(String guasto) {
		this.guasto = guasto;
	}*/
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}

}
