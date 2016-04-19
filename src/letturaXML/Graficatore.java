package letturaXML;

import java.util.Vector;


public class Graficatore {  //si occupa di dare un grafico pronto al main

	static readXmlGraph lg;
	private static String path="";

	public Graficatore(String _path)  //costruttore in caso generale
	{
		path = _path;
		//path="xmls/originalXml.xml";
		//path="xmls/dilemmaXml.xml";
		//path="xmls/dilemma2Xml.xml";
		//path="xmls/dilemma3Xml.xml";
		//path="xmls/falseOriginalXml.xml";
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