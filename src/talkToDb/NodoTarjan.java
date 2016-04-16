package talkToDb;

import java.util.Arrays;

public class NodoTarjan {
	
	private String nome = "";
	private int index = -1;
	private int minDist = 999;

	public NodoTarjan(String _nome, int indice, int distanza)
	{
		setNome(_nome);
		setIndex(indice);
		setMinDist(distanza);
	}

	public String getNome() {
		/*String[] vettore = nome.split("-");
		Arrays.sort(vettore);
		nome = vettore[0] + "-" + vettore[1];*/
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getMinDist() {
		return minDist;
	}

	public void setMinDist(int minDist) {
		//System.err.print("proposta: " + minDist + ";  attuale : " + this.minDist);
		if(minDist<this.minDist)
		{
			this.minDist = minDist;
		}
		//System.err.println(";  inserita: " + this.minDist); 
	}

}
