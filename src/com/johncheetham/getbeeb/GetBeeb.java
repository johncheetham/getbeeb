/*
    GetBeeb 0.1.4    October 2008

    Copyright (C) 2008 John Cheetham    
    
    web   : http://www.johncheetham.com/projects/getbeeb
    email : developer@johncheetham.com
    
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.
 
    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

   
 */

package com.johncheetham.getbeeb;

import java.io.*;
import org.w3c.dom.Document;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 

import java.util.*;
import java.text.SimpleDateFormat;
import java.net.*;

public class GetBeeb {    
	
	// Default values get overridden by properties file 
	private boolean getAllLinks=true;
	private int sleep=10;
	
	private DocumentBuilderFactory docBuilderFactory;
	private DocumentBuilder docBuilder;
    
	private void parse(String xmlfeed, String outfile) {
	    System.out.println("Processing "+outfile);
	    HashMap hm= new HashMap();

	    Calendar cal = Calendar.getInstance();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    String date=sdf.format(cal.getTime());	     
	    date = date.substring(0,4) + date.substring(5,7) + date.substring(8,10) + date.substring(11,13) + date.substring(14,16) + date.substring(17,19);	  
	    double iDate=Double.parseDouble(date); // 2008-10-11 09:13:13	  
	       
	    try {		   
		    Document doc = getXMLDoc(xmlfeed);		    
		    doc.getDocumentElement().normalize();		    
		    NodeList listOfEntries = doc.getElementsByTagName("entry");
		    int totalEntries = listOfEntries.getLength();
		   	
		    for(int s=0; s<listOfEntries.getLength() ; s++) {
			    Node entryNode = listOfEntries.item(s);			   	    
			    if(entryNode.getNodeType() != Node.ELEMENT_NODE){				   			    
				    continue;
			    }
			    Element entryElement = (Element)entryNode; 
			    NodeList avList = entryElement.getElementsByTagName("availability");
			    Element avElement = (Element)avList.item(0);
			    String avStart=avElement.getAttribute("start"); // 2008-10-08T23:50:00Z
			    String avEnd=avElement.getAttribute("end");     // 2008-10-15T23:50:00Z		
			    
			    String sdate = avStart.substring(0,4) + avStart.substring(5,7) + avStart.substring(8,10) + avStart.substring(11,13) + avStart.substring(14,16) + avStart.substring(17,19);				  
			    double isdate=Double.parseDouble(sdate); // 2008-10-11 09:13:13				    
			     
			    String edate = avEnd.substring(0,4) + avEnd.substring(5,7) + avEnd.substring(8,10) + avEnd.substring(11,13) + avEnd.substring(14,16) + avEnd.substring(17,19);				   
			    double iedate=Double.parseDouble(edate); // 2008-10-11 09:13:13	      
      
			    if ((isdate > iDate) || (iedate < iDate)) {				    			   
				    continue;
			    }				     				    
				    
			    String title=getXMLVal("title",entryNode);			   
			    title=title.replace("&","&amp;");
			    String synopsis=getXMLVal("synopsis",entryNode);
			    synopsis=synopsis.replace("&","&amp;");
			    String link=getXMLVal("link",entryNode);   
			    
			    // 'link' now contains the URL of the media selector
			    // If 'getAllLinks' then open the URL andextract the link to the realaudio file.
			    // (requires connecting for every listen again program)
			    // If not 'getAllLinks' just put the Media Selector link into the web page.
			    // The link to the realaudio file is then extracted on demand by a perl cgi script
							    
			    if (getAllLinks) {
				    String realLink=getRealLink(link);
				    if (realLink.equals("nfd")) {					    
					    continue;
				    }
				    String [] t = {realLink,synopsis};						     
				    hm.put(title, t);					    
			    } else {
				    String [] t = {link,synopsis};						     
				    hm.put(title, t);
			    }			   
		    }		    
		    
		    BufferedWriter outputStream = new BufferedWriter(new FileWriter(outfile));					    
		    outputStream.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n");		    
		    outputStream.write("<html>\n");
		    outputStream.write("<head>\n");
		    outputStream.write("<title>"+outfile+"</title>\n");		    
		    outputStream.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\" >\n");
		    
		    outputStream.write("</head>\n");	
		    outputStream.write("<body>\n");	
		    outputStream.write("<a href=\"/projects/getbeeb/pdahome.html\">PDA Home</a>\n");			    
		    outputStream.write("<h2>"+outfile+"</h2>\n");	
		    outputStream.write("<table CELLPADDING=6 CELLSPACING=0>\n");	
		    
		    Map tm = new TreeMap(hm);
		    Iterator iterator = tm.entrySet().iterator();
		    String [] bgCols = {"Beige","LightBlue"};
		    int tog=0;
		    String prevSyn="";
		    while (iterator.hasNext()) {
			    Map.Entry entry = (Map.Entry) iterator.next();			   
			    String title=entry.getKey().toString();
			    String [] a = (String[])entry.getValue();			   
			    String link=a[0];
			    String synopsis=a[1];
			    if (tog==0) {
				    tog=1;
			    } else {
				    tog=0;
			    }
			    String tSyn=synopsis;
			    if (prevSyn.equals(synopsis)) {
				    tSyn="";
			    }
			    outputStream.write("<tr bgcolor=\""+bgCols[tog]+"\">\n");
			     if (getAllLinks) {
				     outputStream.write("<td><a href=\""+link+"\">&gt</a>&nbsp;"+title+"</td>\n");				     
			     } else { 
				     outputStream.write("<td><a href=\"/cgi-bin/getbeeb.pl?mslink="+link+"\">&gt</a>&nbsp;"+title+"</td>\n");
			     }
			    outputStream.write("<td>"+tSyn+"</td></tr>\n");				   
			    prevSyn=synopsis;
		    }
		    outputStream.write("</table>\n");		    		    
		    sdf = new SimpleDateFormat("dd MMMMM yyyy HH:mm:ss");
		    String now=sdf.format(cal.getTime());		    
		    outputStream.write("<br><br><font size=\"2\">Page created: "+now+"</font><br>\n");		    
		    outputStream.write("</body>\n");	
		    outputStream.write("</html>\n");		    
		    outputStream.close();	
	    } catch (IOException ioe) {
			ioe.printStackTrace ();
			System.exit(1);
	    }	
	}
	
	private Document getXMLDoc(String xmlURL) {		 	
		Document doc=null;
		try {
			URL url=new URL(xmlURL);
			URLConnection conn = url.openConnection();
			conn.connect();
			InputStream is=conn.getInputStream();			
			doc = docBuilder.parse(is);
			is.close();
		} catch (MalformedURLException mue) {
			mue.printStackTrace();
			System.exit(1);	
		} catch (IOException ioe) {	
			ioe.printStackTrace();
			System.exit(1);	
		} catch (SAXException e) {
			Exception x = e.getException();
			((x == null) ? e : x).printStackTrace();
			System.exit(1);		
		}
		return doc;		
	}
	
	private String getRealLink(String link) {
		
		try {
			//Pause between connections
			Thread.sleep(sleep*1000);
		} catch (InterruptedException ie) {
		}
				
		Document doc = getXMLDoc(link);		
		doc.getDocumentElement().normalize();		    
		NodeList listOfEntries = doc.getElementsByTagName("media");
		int totalEntries = listOfEntries.getLength();			
		for(int s=0; s<listOfEntries.getLength() ; s++) {
		     Node entryNode = listOfEntries.item(s);			     
		     if(entryNode.getNodeType() == Node.ELEMENT_NODE){
			    Element mediaElement = (Element)entryNode;				   
			    String encoding=mediaElement.getAttribute("encoding");
			    if (encoding.equals("real")) {
				     NodeList avList = mediaElement.getElementsByTagName("connection");
				     Element connElement = (Element)avList.item(0);
				     String href=connElement.getAttribute("href");					     
				     return href;
			    }				    		    
		     }
		}		
		return "nfd";
	}
	
	private String getXMLVal(String tag, Node node) {
		Element element = (Element)node;
		NodeList tagList = element.getElementsByTagName(tag);
		Element tagElement = (Element)tagList.item(0);		
		NodeList texttagList = tagElement.getChildNodes();
		
		String xv="";
		try {
			xv=(texttagList.item(0)).getNodeValue().trim();
		} catch (NullPointerException npe) {
			xv="";
		}
		return xv;		
	}
	
	private void init() {		
		System.out.println("initialising");
		Properties defaultProps = null;			
		try {
			// create and load default properties
			defaultProps = new Properties();			
			FileInputStream in = new FileInputStream("getbeeb.properties");
			defaultProps.load(in);
			in.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("properties file 'getbeeb.properties' not found, default values will be used");			
			return;
		} 
		catch (IOException ioe) {
			System.out.println("properties file 'getbeeb.properties' not found, default values will be used");			
			return;
		} 
		
		String prop=defaultProps.getProperty("getAllLinks");
		if (prop==null) {	
		} else if (prop.equalsIgnoreCase("true")) {
			getAllLinks=true;
		} else if (prop.equalsIgnoreCase("false")) {			
			getAllLinks=false;
		}		
		System.out.println("getAllLinks="+getAllLinks);	
		
		try {
			prop=defaultProps.getProperty("sleep");
			int sl=Integer.parseInt(prop);
			sleep=sl;
		} catch (Exception e) {
		}		
		System.out.println("sleep="+sleep);	
		
		docBuilderFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docBuilderFactory.newDocumentBuilder();
		} catch (javax.xml.parsers.ParserConfigurationException pce) {
			pce.printStackTrace();
			System.exit(1);
		}
	}

	public static void main(String[] args) {
		
		GetBeeb gb = new GetBeeb();
		gb.init();
		
		try {
			BufferedReader inputStream = new BufferedReader(new FileReader("filelist"));            
			String line=null;
			while ((line = inputStream.readLine()) != null) {
				if (line.startsWith("#")) {					
					continue;
				}				
				String[] fields = line.split(",");
				if (fields.length != 2) {
					System.out.println("Error reading filelist. Line:"+line);
					System.exit(1);
				};
				String url = fields[0];
				String fileout = fields[1];							
				gb.parse(url,fileout);	
				//Pause between connections
				Thread.sleep(gb.sleep*1000);	
			}
			inputStream.close();
		} catch (java.io.FileNotFoundException fnfe) {
			System.out.println("File Not Found");
		} catch (java.io.IOException ioe) {
			System.out.println("I/O Exception");
		} catch (java.lang.InterruptedException ie) {
		}
		
	}	    

}