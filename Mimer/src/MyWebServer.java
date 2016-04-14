import java.io.*;
import java.lang.StringBuilder;
import java.net.*;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

class Worker extends Thread {
	Socket sock;

	Worker(Socket s) {
		sock = s;
	}

	public void run() {

		PrintStream out = null;
		BufferedReader in = null;

		try {
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			out = new PrintStream(sock.getOutputStream());
			String request = in.readLine();
			System.out.println(request);
			String[] requestSplit = request.split(" ");

			if (!request.startsWith("GET")||!request.endsWith("HTTP/1.1")) {
				System.out.println("400" + "Bad Request"+ "Your browser sent a request that "+ "this server could not understand.");
			}

			if (request.indexOf("..") != -1 || request.indexOf("/.ht") != -1
					|| request.endsWith("~")) {

				System.out
						.println("403"
								+ "Forbidden"
								+ "You don't have permission to access the requested URL.");
			}

			
			
			if (requestSplit[0].matches("GET")) {// check for the request if it is get than splits at get
				String dir = requestSplit[1].substring(1);//stores the rest of the request at dir
				if (dir.matches("")) {//if its blank
					dir = "./";//dir parent dir
				}
				File f = new File(dir);
					String Dir_path = guessContentType(dir);//checks for file type
					getFile(Dir_path, dir, out);//gets the file
				
				// Handle the CGI stuff
				
			}
			sock.close();//close the connections
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

    
	/*function to check the type of the file */
	private String guessContentType(String path) {
		StringBuilder Dir_path = new StringBuilder();
		String headerType = null;
		File header = new File(path);
		if (path.endsWith(".txt") || path.endsWith(".java")) {//check if the file is a normal text file
			headerType = "text/plain";

		} else if (path.endsWith(".html") || path.endsWith(".htm")) {// checks if the file is a html 
			headerType = "text/html";
		} else if (path.endsWith(".xyz")) {// checks if the file is an image file
			headerType = "application/xyz";
		} else if (path.endsWith(".class")) {// checks if the file is a class file
			headerType = "application/octet-stream";
		} else {
			headerType = "image/jpeg";
		}

		Dir_path.append("HTTP/1.1 200 OK\n");// gives the content
		Dir_path.append("header_Length: " + header.length() + "\r\n");//prints the content length
		Dir_path.append("header_Type: " + headerType + "\r\n");//prints the content type
		return Dir_path.toString();
	}
    /* Populates the Web Server directory, files, and folders on Firefox and the Client 
    private void getDirectory(String Dir_path, String path, PrintStream out) {
        File files = new File(path);
        System.out.println(Dir_path); // Prints out to console
        out.println(Dir_path); 
        
        System.out.printf("<h1>Index of "+path+"</h1>\n");
        out.printf("<h1>Index of "+path+"</h1>\n");
        
        System.out.println("<pre>\n");     
        out.println("<pre>\n");
        
        System.out.println("<a href=./> Parent Directory </a><br>");
        out.println("<a href=./> Parent Directory </a><br>");
        
               
        File[] listDir = files.listFiles();
        for (int i=0;i<listDir.length;i++) {
        	System.out.println("<a href=\'" + listDir[i] + "\'>" + listDir[i].toString().substring(2) + "</a><br>");
        	out.println("<a href=\'" + listDir[i] + "\'>" + listDir[i].toString().substring(2) + "</a><br>");
        }
        out.println("</pre>"); 
        System.out.println("</pre>");
        
    }
   
    private void addnums(String Dir_path, String path, PrintStream out) {
    		
    		StringBuilder html_form = new StringBuilder();
    		System.out.println(" path: " + path);
    		int result;
    		String val1,val2;
    		int p_num1,p_num2,p_position;
    		
    		p_position = path.indexOf("=") + 1;
    		
    		p_num1 = path.indexOf("&num1=");
    		p_num2 = path.indexOf("&num2=");

    		String person = path.substring(p_position, p_num1);
    		val1 = path.substring(p_num1+6,p_num2);
    		val2 = path.substring(p_num2+6);

    		result = Integer.parseInt(val1) + Integer.parseInt(val2);
    		html_form.append( person + ", the sum of " + val1 + " and " + val2+ " is equal to:\t" + result + "\n");//prints on console
    		
    		System.out.println(html_form.toString());
    		System.out.println(Dir_path);
    		
    		out.println(html_form.toString());//print the html to console
    		out.println(Dir_path);
    		out.flush();
    	} */

    /*function to get the directories from the directries and folders*/

    	private void getFile(String Dir_path, String dir, PrintStream out) {
    		BufferedReader in;
    		String fileName;
    		try {
    			in = new BufferedReader(new FileReader(dir));//stores al the files in the buffer
    			System.out.println(Dir_path);//prints 
    			out.println(Dir_path);//prints on the console
    			while (in.ready()) {//while its still reading fetch all the files and folders
    				fileName = in.readLine();
    				System.out.println(fileName);
    				out.println(fileName);
    			}
    		} catch (IOException ioe) {
    			System.out.println(ioe);
    		}
    	}

    	
    }
public class MyWebServer {
	
	public static void main(String a[]) throws IOException {
		
		int q_len = 6; // number of request
		int port = 2540; //port assigned      
		Socket sock;
		
		ServerSocket servsock = new ServerSocket(port, q_len);
		
		BCLooper AL = new BCLooper(); // create a DIFFERENT thread for Back Door Channel
		Thread t = new Thread(AL);
		t.start();  
		
		System.out.println("Nida MyWebServer is listening at port 2540.\n");
		while (true) {
			sock = servsock.accept(); 
			new Worker(sock).start(); 
		}
	}
}

class BCLooper implements Runnable {
  public static boolean adminControlSwitch = true;
  
  public void run(){ // RUNning the Admin listen loop
    System.out.println("In BC Looper thread, waiting for 2570 connections");
    
    int q_len = 6; /* Number of requests for OpSys to queue */
    int port = 2570;  // Listen here for Back Channel Connections
    Socket sock;
    
    try{
      ServerSocket servsock = new ServerSocket(port, q_len);
      while (adminControlSwitch) {
	// wait for the next ADMIN client connection:
	sock = servsock.accept();
	new BCWorker (sock).start(); 
      }
    }catch (IOException ioe) {System.out.println(ioe);}
  }
}

class myDataArray {
  int num_lines = 0;
  String[] lines = new String[10];
}

class BCWorker extends Thread {
    private Socket sock;
    private int i;
    BCWorker (Socket s){sock = s;}
    PrintStream out = null; BufferedReader in = null;

    String[] xmlLines = new String[15];
    String[] testLines = new String[10];
    String xml;
    String temp;
    XStream xstream = new XStream();
    final String newLine = System.getProperty("line.separator");
    myDataArray da = new myDataArray();
    
    public void run(){
      System.out.println("Called BC worker.");
      try{
	in =  new BufferedReader(new InputStreamReader(sock.getInputStream()));
	out = new PrintStream(sock.getOutputStream()); // to send ack back to client
	i = 0; xml = "";
	while(true){
	  temp = in.readLine();
	  if (temp.indexOf("end_of_xml") > -1) break;
	  else xml = xml + temp + newLine; // Should use StringBuilder in 1.5
	}
	System.out.println("The XML marshaled data:");
	System.out.println(xml);
	out.println("Acknowledging Back Channel Data Receipt"); // send the ack
	out.flush(); sock.close();
	
        da = (myDataArray) xstream.fromXML(xml); // deserialize / unmarshal data
	System.out.println("Here is the restored data: ");
	for(i = 0; i < da.num_lines; i++){
	  System.out.println(da.lines[i]);
	}
      }catch (IOException ioe){
      } // end run
    }
}
