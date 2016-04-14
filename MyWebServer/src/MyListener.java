import java.io.*;
import java.net.*;

public class MyListener {
	
	//public static PrintStream toTextFile; 
	
	public static void main (String a[]) throws IOException {
		int q_len = 6; // requested maximum length of the Queue
		int port = 1565; // A TCP server that runs on port 1565
		Socket sock;
		
		ServerSocket servsock = new ServerSocket(port, q_len); // creates a server socket, bound to the specified port
		System.out.println ("Listener server starting up, listening at port 80.\n");
		while(true) {
			sock = servsock.accept(); // Listens for a connection to be established to this socket and accepts it
			new WorkerListener(sock).start(); // Invoke Worker class to handle it			
		}
	}		
}


class WorkerListener extends Thread{
	Socket sock;
	
	WorkerListener(Socket s){ // constructor for Worker 
		sock = s;
	}
	
	public void run(){
		PrintStream out = null;
		BufferedReader in = null;
		String name = "";
		try{
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			try{				
				name = in.readLine();
				System.out.println("Looking up " +name);
				out.println("Looking up " +name);
			}catch (IOException x){
				System.out.println("Server read error");
				out.println("Failed in attempt to look up " +name);
			}
			sock.close();
		}catch (IOException ioe){
			System.out.println(ioe);
			
		}
	}	
}