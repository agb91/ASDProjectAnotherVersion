package letturaXML;

import java.util.Vector;


public class Graficatore {  //si occupa di dare un grafico pronto al main

	static readXmlGraph lg;
	private static String path="";

	public Graficatore()  //costruttore in caso generale
	{
		path="xmls/dilemma2Xml.xml";
		lg = new readXmlGraph(path);
	}
	
	public static Vector<Transizione> getRelationsList()
	{
		Vector<Transizione> t = lg.leggiTransizioni();
		return t;
	}
	
	public static Vector<Nodo> getNodesList() 
	{
		Vector <Nodo> g = lg.leggiNodi();		
		return g;
	}


}