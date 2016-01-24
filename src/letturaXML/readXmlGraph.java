package letturaXML;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.*;
import java.util.Vector;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.*;
import org.xml.sax.SAXException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class readXmlGraph {
		
	  String path;
		
	  public readXmlGraph (String _path)  //costruito con il path di un file xml
	  {
		  path=_path;
	  }
	 
	  public Vector<Nodo> leggiNodi() {  	  
		  Vector<Nodo> names = new Vector<Nodo>();
		  try {	
			  File fXmlFile = new File(path);
			  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			  DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			  Document doc = dBuilder.parse(fXmlFile);
			 
			  //optional, but recommended
			  //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			  doc.getDocumentElement().normalize();	 
			  NodeList nList = doc.getElementsByTagName("grafo");
			  Node nNode = nList.item(0);
			  Element eElement = (Element) nNode;
			  NodeList interni =  eElement.getElementsByTagName("nodo");
			  int l = interni.getLength();
			  for(int i=0; i<l; i++)
			  {
				  Element nome = (Element) interni.item(i);
				  Nodo n = new Nodo(nome.getElementsByTagName("nome").item(0).getTextContent());
				  names.addElement(n);
			  }
	    } catch (Exception e) {
		e.printStackTrace();
	    }

		return names;
	  }

	  public Vector<Transizione> leggiTransizioni() {  	  
		  Vector<Transizione> transizioni = new Vector<Transizione>();
		  try {	
			  File fXmlFile = new File(path);
			  DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			  DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			  Document doc = dBuilder.parse(fXmlFile);
			 
			  //optional, but recommended
			  //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
			  doc.getDocumentElement().normalize();	 
			  NodeList nList = doc.getElementsByTagName("grafo");
			  Node nNode = nList.item(0);
			  Element eElement = (Element) nNode;
			  NodeList interni =  eElement.getElementsByTagName("transizione");
			  int l = interni.getLength();
			  for(int i=0; i<l; i++)
			  {
				  Element transizione = (Element) interni.item(i);
				  String nome = transizione.getElementsByTagName("nome").item(0).getTextContent();
				  String evento = transizione.getElementsByTagName("evento").item(0).getTextContent();
				  String osservabile = transizione.getElementsByTagName("osservabile").item(0).getTextContent();
				  String from = transizione.getElementsByTagName("from").item(0).getTextContent();
				  String to = transizione.getElementsByTagName("to").item(0).getTextContent();
				  Transizione tr = new Transizione(nome, evento, osservabile, from, to);
				  transizioni.addElement(tr);
			  }
	    } catch (Exception e) {
		e.printStackTrace();
	    }

		return transizioni;
	  }

	
}
