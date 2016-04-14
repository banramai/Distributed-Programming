import java.io.*;
import java.net.*;

public class MyTelnet {
	
	public static void main(String args[]){
		String serverName;
		int port = 1565;
		if (args.length <1) serverName = "localhost";
		else serverName = args[0]; 
		
		System.out.println("=====MyTelnet Program======");
		System.out.println("Using server: " + serverName + ", Port:" +port);
		
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		try{
			String name;
			//StringBuilder msg = new StringBuilder();
			
			do{
				System.out.print("Enter the input, (quit) to end: ");
				System.out.flush();
				name = in.readLine();
				
				
				if(name.indexOf("quit") < 0)
					toServer(name, serverName, port);
			}while(name.indexOf("quit") < 0 );
			System.out.println("Cancelled by user request.");
		}catch (IOException x) {
			x.printStackTrace();
		}
	}
			

	static void toServer(String name, String serverName, int port){
		Socket sock;
		BufferedReader fromServer;
		PrintStream toServer;
		String textFromServer;
		
		try{
			sock = new Socket(serverName, port);
			
			fromServer = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			toServer = new PrintStream(sock.getOutputStream());
			toServer.println(name);
			toServer.flush();
			
			for(int i=1; i<4; i++){
				textFromServer = fromServer.readLine();
				if(textFromServer!=null)
					System.out.println(textFromServer);
			}
			sock.close();
		}catch (IOException x){
			System.out.println("Socket error.");
			x.printStackTrace();
		}		
	}
}
