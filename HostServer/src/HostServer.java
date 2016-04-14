/*--------------------------------------------------------

1. Banumathi Ramaiah / Date: 10/11/2015

2. Java version used, if not the official version for the class:

1.8

3. Precise command-line compilation examples / instructions:

> javac HostServer.java

4. Precise examples / instructions to run this program:

In separate shell windows:

> java MyWebServer
> Open a browser and input http://localhost:1565
> Displays host server status page
> Hit the submit button. Note the server status increments by 1 each time the button is clicked.
> Enter migrate in the text field to move the agent to be hosted on new port.
> Opens a new browser to start a new agent and see them synchronize server state.

5. List of files needed for running the program.

 a. HostServer.java

6. Notes:


----------------------------------------------------------*/

// libraries to import I/O streams and Sockets
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * AgentWorker
 * 
 * The program process the request made at each port, 
 * Looks if the input is migrate, finds the next available port 
 * and transfers the client to it.
 */
class AgentWorker extends Thread {
	
	Socket sock; 
	agentHolder parentAgentHolder; 
	int localPort; 
	
	//constructor for Agent Worker
	AgentWorker (Socket s, int prt, agentHolder ah) {
		sock = s;
		localPort = prt;
		parentAgentHolder = ah; // maintains the state and holds the socket.
	}
	
	public void run() {
		PrintStream out = null;
		BufferedReader in = null;
		
		String NewHost = "localhost";
		
		int NewHostMainPort = 1565;		
		String buf = "";
		int newPort;
		Socket clientSock;
		BufferedReader fromHostServer;
		PrintStream toHostServer;
		
		try {
			// handles I/O stream
			out = new PrintStream(sock.getOutputStream());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			
			String inLine = in.readLine(); // read input from the client
			StringBuilder htmlString = new StringBuilder();
			
			
			System.out.println();
			System.out.println("Request line: " + inLine);
			
			// Check if the input string is migrate, then switch to next available port
			
			if(inLine.indexOf("migrate") > -1) {
				
				
				clientSock = new Socket(NewHost, NewHostMainPort); // creates a new socket
				fromHostServer = new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
				
				toHostServer = new PrintStream(clientSock.getOutputStream()); // request the server to fetch the next available port.
				toHostServer.println("Please host me. Send my port! [State=" + parentAgentHolder.agentState + "]");
				toHostServer.flush();
				
				// Handles the port according to the response.
				for(;;) {
					buf = fromHostServer.readLine();
					if(buf.indexOf("[Port=") > -1) {
						break;
					}
				}
				
				//extract the port.
				String tempbuf = buf.substring( buf.indexOf("[Port=")+6, buf.indexOf("]", buf.indexOf("[Port=")) );
				
				newPort = Integer.parseInt(tempbuf); // fetch the new port				
				System.out.println("newPort is: " + newPort);
				
				htmlString.append(AgentListener.sendHTMLheader(newPort, NewHost, inLine)); // HTML response to the client.
				
				htmlString.append("<h3>We are migrating to host " + newPort + "</h3> \n");
				htmlString.append("<h3>View the source of this page to see how the client is informed of the new location.</h3> \n");
				
				htmlString.append(AgentListener.sendHTMLsubmit()); // process the submit button functionality.
				
				System.out.println("Killing parent listening loop.");
				
				ServerSocket ss = parentAgentHolder.sock; // get the socket				
				ss.close(); // terminate the port
				
				
			} else if(inLine.indexOf("person") > -1) {
				parentAgentHolder.agentState++; // increment the server state.
				
				// display the state and the form.
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
				htmlString.append("<h3>We are having a conversation with state   " + parentAgentHolder.agentState + "</h3>\n");
				htmlString.append(AgentListener.sendHTMLsubmit()); // process the submit button functionality.

			} else {
				// display on invalid input.
				htmlString.append(AgentListener.sendHTMLheader(localPort, NewHost, inLine));
				htmlString.append("You have not entered a valid request!\n");
				htmlString.append(AgentListener.sendHTMLsubmit());		
			}
			
			AgentListener.sendHTMLtoStream(htmlString.toString(), out); // output the HTML.
			
			sock.close(); //terminate the socket.			
			
		} catch (IOException ioe) {
			System.out.println(ioe);
		}
	}
	
}
/**
 * The program holds the server state information, 
 * such to retain the state and pass it between the ports. 
 */

class agentHolder {
	
	ServerSocket sock;
	
	int agentState; // variable to maintain the agentState.	
	
	agentHolder(ServerSocket s) { // constructor class.
		sock = s;}
}

/**
 * Implementation for processing the client request
 * when the request is made on the server at port 1565.
 */
class AgentListener extends Thread {
	Socket sock;
	int localPort;
	
	AgentListener(Socket As, int prt) { // constructor 
		sock = As;
		localPort = prt;
	}
	
	int agentState = 0; // set agentState to 0.
	
	public void run() {
		BufferedReader in = null;
		PrintStream out = null;
		String NewHost = "localhost";
		System.out.println("In AgentListener Thread");		
		try {
			String buf; 
			// I/O streams.
			out = new PrintStream(sock.getOutputStream());
			in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));
			
			
			buf = in.readLine(); // read the input line
			
			
			if(buf != null && buf.indexOf("[State=") > -1) {
				
				String tempbuf = buf.substring(buf.indexOf("[State=")+7, buf.indexOf("]", buf.indexOf("[State="))); // get the state from the input.
				agentState = Integer.parseInt(tempbuf); // parse to get the port
				System.out.println("agentState is: " + agentState); // send it to Server.					
			}
			
			System.out.println(buf);
			
			StringBuilder htmlResponse = new StringBuilder();
			//output the HTML response to the browser.
			htmlResponse.append(sendHTMLheader(localPort, NewHost, buf));
			htmlResponse.append("Now in Agent Looper starting Agent Listening Loop\n<br />\n");
			htmlResponse.append("[Port="+localPort+"]<br/>\n");
			htmlResponse.append(sendHTMLsubmit());
			
			sendHTMLtoStream(htmlResponse.toString(), out); //display.
			
			ServerSocket servsock = new ServerSocket(localPort,2); // create a new connection.
			agentHolder agenthold = new agentHolder(servsock); // crate a new agentHolder
			agenthold.agentState = agentState; // assign the state to the agentHolder.
			
			
			while(true) {
				sock = servsock.accept(); //wait for connections.
				
				System.out.println("Got a connection to agent at port " + localPort);
				
				new AgentWorker(sock, localPort, agenthold).start(); //spawn a new agentWorker object.
			}
		
		} catch(IOException ioe) {
			System.out.println("Either connection failed, or just killed listener loop for agent at port " + localPort); // handle the exception.
			System.out.println(ioe);
		}
	}
	
	/**
	 * Program to display host server UI on browser.
	**/
	static String sendHTMLheader(int localPort, String NewHost, String inLine) {
		// stringBuilder to hold the HTML response.
		StringBuilder htmlString = new StringBuilder();

		htmlString.append("<html><head> </head><body>\n");
		htmlString.append("<h2>This is for submission to PORT " + localPort + " on " + NewHost + "</h2>\n");
		htmlString.append("<h3>You sent: "+ inLine + "</h3>");
		htmlString.append("\n<form method=\"GET\" action=\"http://" + NewHost +":" + localPort + "\">\n");
		htmlString.append("Enter text or <i>migrate</i>:");
		htmlString.append("\n<input type=\"text\" name=\"person\" size=\"20\" value=\"YourTextInput\" /> <p>\n"); // text box to accept the input.
		
		return htmlString.toString();
	}
	
	/**
	 * Process the submit button function.
	 */	
	static String sendHTMLsubmit() {
		return "<input type=\"submit\" value=\"Submit\"" + "</p>\n</form></body></html>\n";
	}
	
	/**
	 * Writes OK and other output headers for successful response.
	 */
	static void sendHTMLtoStream(String html, PrintStream out) {
		
		out.println("HTTP/1.1 200 OK");
		out.println("Content-Length: " + html.length());
		out.println("Content-Type: text/html");
		out.println("");		
		out.println(html);
	}
	
}

/**
 * The server simulates hosting agents on different ports.(>3000)
 * This server listens for processes connections from clients to interact with agents.
 */
public class HostServer {
	
	public static int NextPort = 3000; //initialize the port 
	
	public static void main(String[] a) throws IOException {
		
		int q_len = 6; // number of clients can connect to server
		int port = 1565; // assignment requires use of port 1565
		Socket sock;
		
		System.out.println("======= HostServer =======");
		System.out.println("Server starting up, listening at port: " +port);
		System.out.println("Connect from 1 to 3 browsers using \"http:\\\\localhost:1565\"\n");
		
		ServerSocket servsock = new ServerSocket(port, q_len); // create a new server socket
		
		while(true) {			 
			NextPort = NextPort + 1; // increment the port for new request or on migrate request.			
			sock = servsock.accept();	//wait for the next client connection.
			System.out.println("Starting AgentListener at port " + NextPort);			
			new AgentListener(sock, NextPort).start(); //spawn a new listener thread for the client.
		}		
	}
}