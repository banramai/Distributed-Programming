import java.io.*;  // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.util.*;

class myDataArray {
	  int num_lines = 0;
	  String[] lines = new String[8];
	}

public class BCHandler {
	
	  private static String XMLfileName = "C:\\temp\\mimer.output";
	  private static PrintWriter toXmlOutputFile;
	  private static File xmlFile;
	  private static BufferedReader fromMimeDataFile;
	  
	  public static void main(String args[]) {
		  int i = 0;
		  String serverName;
		  if (args.length < 1) 
			  serverName = "localhost";
		  else 
			  serverName = args[0];
		  
		  BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		  XStream xstream = new XStream();
		  myDataArray da = new myDataArray();
		  myDataArray daTest = new myDataArray();
		  
		  try {
		      System.out.println("Executing the java application.");
		      System.out.flush();
		      Properties p = new Properties(System.getProperties());
		      
		      String argOne = p.getProperty("firstarg");
		      System.out.println("First var is: " + argOne);
		      
		      fromMimeDataFile = new BufferedReader(new FileReader(argOne));
		      // Only allows for five lines of data in input file plus safety:
		      while(((da.lines[i++] = fromMimeDataFile.readLine())!= null) && i < 8){
		    	  System.out.println("Data is: " + da.lines[i-1]);
		      }
		      
		      da.num_lines = i-1;
		      System.out.println("i is: " + i);
		      
		      String xml = xstream.toXML(da);
		      sendToBC(xml, serverName);
		      
		      System.out.println("\n\nHere is the XML version:");
			  System.out.print(xml);
			  
			  daTest = (myDataArray) xstream.fromXML(xml); // deserialize data
			  System.out.println("\n\nHere is the deserialized data: ");
			  for(i=0; i < daTest.num_lines; i++){
				  System.out.println(daTest.lines[i]);
			  }				 
			  System.out.println("\n");
			  
			  xmlFile = new File(XMLfileName);
			  if (xmlFile.exists() == true && xmlFile.delete() == false){
			    throw (IOException) new IOException("XML file delete failed.");
			  }
			  xmlFile = new File(XMLfileName);
			  if (xmlFile.createNewFile() == false){
			    throw (IOException) new IOException("XML file creation failed.");
			  }
			  else{
				    toXmlOutputFile = 
				      new PrintWriter(new BufferedWriter(new FileWriter(XMLfileName)));
				    toXmlOutputFile.println("First arg to Handler is: " + argOne + "\n");
				    toXmlOutputFile.println(xml);
				    toXmlOutputFile.close();
			  }
		  }catch (Throwable e) {
		      e.printStackTrace();
		  }
	  }
	  
	  static void sendToBC (String sendData, String serverName){
		    Socket sock;
		    BufferedReader fromServer;
		    PrintStream toServer;
		    String textFromServer;
		    try{
		      // Open our connection Back Channel on server:
		      sock = new Socket(serverName, 2570);
		      toServer   = new PrintStream(sock.getOutputStream());
		      // Will be blocking until we get ACK from server that data sent
		      fromServer = 
			new  BufferedReader(new InputStreamReader(sock.getInputStream()));
		      
		      toServer.println(sendData);
		      toServer.println("end_of_xml");
		      toServer.flush(); 
		      // Read two or three lines of response from the server,
		      // and block while synchronously waiting:
		      System.out.println("Blocking on acknowledgment from Server... ");
		      textFromServer = fromServer.readLine();
		      if (textFromServer != null){System.out.println(textFromServer);}
		      sock.close();
		    } catch (IOException x) {
		      System.out.println ("Socket error.");
		      x.printStackTrace ();
		    }
		  }


}
