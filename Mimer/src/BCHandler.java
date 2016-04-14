/*--------------------------------------------------------

1. Banumathi Ramaiah / Date: 10/25/2015

2. Java version used, if not the official version for the class:

1.8

3. Precise command-line compilation examples / instructions:

> javac MyWebServer.java
> javac BCHandler.java


4. Precise examples / instructions to run this program:

Build Instructions:

a. Download Xstream libraries (jar files).
b. Execute jcx.bat file, with the source file, MyWebserver.java

Standard Execution Instructions:

a. MyWebServer emulates a real web server, except it runs on port 2540 and supports the Firefox web browser. > java MyWebServer
b. In command prompt, pointing to the same directory as above, execute the classpath for the source file MyWebServer.java (rx.bat)
c. Open Firefox and type this into the browser line: http://localhost:2540/
d. Returns list of directories and files.
e. Click on the file to be displayed in the browser, specific to its MIME type.

Back Channel Execution Instructions:

a. Web browser makes request to the server.
b. Get a file ending with .xyz
c. Choose shim.bat handler, to handler the MIME type.
d. creates a temp file , view the output c:\temp\mimer.output 
e. The shim.bat running, marshals the data and send it through XML back to the server.
f. The server prints it on the screen.

5. List of files needed for running the program.

 a. MyWebServer.java
 b. Handler.java
 c. BCClient.java
 d. BCHandler.java
 e. serverlog.txt
 f. checklist-mimer.html
 g. mimer-discussion.html
 
6. Notes:

Implemented Secured Socket layer (SSL) in the back channel.

----------------------------------------------------------*/
import java.io.*;  // Get the Input Output libraries
import java.net.*; // Get the Java networking libraries

import java.security.Security;
import java.util.Properties;

//libraries to support SSL sockets
import javax.net.ssl.*;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import com.sun.net.ssl.internal.ssl.Provider;

//libraries to import XStream
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/*
 * This is responsible for serializing to XML & sending to the back channel of custom mime type conversation.
 */

public class BCHandler{
  private static String XMLfileName = "C:\\temp\\mimer.output";
  private static PrintWriter  toXmlOutputFile; 
  private static File xmlFile; 
  private static BufferedReader fromMimeDataFile; 
  
  public static void main (String args[]) {
	  
	// setting up the in stream from the file and a data array to hold the files data.
      
    String serverName;
    if (args.length < 1) serverName = "localhost";
    else serverName = args[0];
   
    try {
      System.out.println("Bhanu Back Channel Client."); 
      System.out.println("Using server: " + serverName + ", Port: 2540 / 2570\n");       
     
      Properties p = new Properties(System.getProperties());
      String argOne = p.getProperty("firstarg"); // Returns a string containing "firstarg"
      System.out.println("First var is: " + argOne); // Prints onto the console
      
      //gets the argument passed in from shim.bat
      fromMimeDataFile = new BufferedReader(new FileReader(argOne)); // Buffer that holds the data
      myDataArray da = new myDataArray();
      int i = 0;
      // Only allows for five lines of data in input file
      while(((da.lines[i++] = fromMimeDataFile.readLine())!= null) && i < 8){
    	  System.out.println("Data is: " + da.lines[i-1]); 
      }
      da.num_lines = i-1; //prints number of lines in the reader
      System.out.println("i is: " + i + "\n"); // Prints the value of i to the console
      
      
      XStream xstream = new XStream();
      String xml = xstream.toXML(da);
      System.out.println("XML output:");
      System.out.println(xml);
	  sendToBC(xml, serverName); // Take the strings from xml and servername and send it to function sendToBC
 
	 // creates a temp file. and checks if it exists / can be deleted.
	  xmlFile = new File(XMLfileName);
	  if (xmlFile.exists() == true && xmlFile.delete() == false){
	    throw (IOException) new IOException("XML file delete failed.");
	  }
	  // creates the temp file
	  xmlFile = new File(XMLfileName);
	  if (xmlFile.createNewFile() == false){
	    throw (IOException) new IOException("XML file creation failed.");
	  }
	  else{
		//outputs the file passed through shim.bat into the temp file on the system.
	    toXmlOutputFile = new PrintWriter(new BufferedWriter(new FileWriter(XMLfileName)));
	    toXmlOutputFile.println("First arg to Handler is: " + argOne + "\n");
	    toXmlOutputFile.println(xml);
	    toXmlOutputFile.close();
	  }
    } catch (IOException x) {x.printStackTrace();}
  }
  
  /*
   * function sends the data to the server in XML format.
   */
  static void sendToBC (String sendData, String serverName){
    BufferedReader fromServer;
    PrintStream toServer;
    String textFromServer;
    try{
    	//SSL Socket implementation 
    	Security.addProvider(new Provider());
    	System.setProperty("javax.net.ssl.trustStore","bchandlerclient.jks"); 
        System.setProperty("javax.net.ssl.trustStorePassword","bchandlerclient");
        
    	SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
		SSLSocket sslSocket = (SSLSocket)sslsocketfactory.createSocket(serverName,2570); // Open our connection Back Channel on server.
      
		//sock = new Socket(serverName, 2570);
		toServer   = new PrintStream(sslSocket.getOutputStream());
		// Will be blocking until we get ACK from server that data sent.
		fromServer = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
       
		toServer.println(sendData);
		final String newLine = System.getProperty("line.separator");
		toServer.println(newLine + "end_of_xml");
		toServer.flush(); 
      
		// Read two or three lines of response from the server,
        // and block while synchronously waiting:
        
		System.out.println("Blocking on acknowledgment from Server... ");
		textFromServer = fromServer.readLine();
		if (textFromServer != null){
			System.out.println(textFromServer);
		}		
		sslSocket.close();
    } catch (IOException x) {
      System.out.println ("Socket error.");
      x.printStackTrace ();
    }
  }
}
