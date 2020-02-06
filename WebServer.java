import java.io.*;
import java.net.*;
import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public final class WebServer
{
	public static void main(String argv[]) throws Exception
	{
		//Set port number
		int port = 8888;
		int port2 = 5555;
		
		//Establish the listen socket
		SocketAddress port_1 = new InetSocketAddress(port);
		SocketAddress port_2 = new InetSocketAddress(port2);
		
		ServerSocketChannel tcpserver = ServerSocketChannel.open();
		tcpserver.socket().bind(port_1);
		
		ServerSocketChannel server_2 = ServerSocketChannel.open();
		server_2.socket().bind(port_2);
		tcpserver.configureBlocking(false);
		server_2.configureBlocking(false);
		
		Selector select = Selector.open();
		tcpserver.register(select, SelectionKey.OP_ACCEPT);
		server_2.register(select, SelectionKey.OP_ACCEPT);
		
		//Process HTTP service requests in an infinite loop
		while(true)
		{
			//Listen for a TCP connection request
			select.select();  
			Set<SelectionKey> keys = select.selectedKeys();
			
			for(Iterator<SelectionKey> i = keys.iterator(); i.hasNext();)
			{
				SelectionKey key = i.next();
				i.remove();
				Channel c = key.channel();
				
				if(c == tcpserver)
				{
					SocketChannel socket = tcpserver.accept();
					HttpRequest request = new HttpRequest(socket.socket());
					Thread thread = new Thread(request);
					thread.start();
				}
				else if(c == server_2)
				{
					SocketChannel socket = server_2.accept();
					MovedRequest request = new MovedRequest(socket.socket());
					Thread thread = new Thread(request);
					thread.start();
				}
			}
			//Construct an object to process the HTTP request message
			
			
			//Create a new thread to process the request
			
		}
			
	}
}

final class HttpRequest implements Runnable
{
	final static String CRLF = "\r\n";
	Socket socket;
	
	//Constructor
	public HttpRequest(Socket socket) throws Exception
	{
		this.socket = socket;
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		try {
				processRequest();
		} catch (Exception e) {
				System.out.println(e);
		}
		
	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
	{
		//Construct 1k buffer to hold bytes on their way to socket
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		//Copy requested file into socket's output stream
		while((bytes = fis.read(buffer)) != -1)
		{
			os.write(buffer, 0, bytes);
		}
	}
	
	private static String contentType(String filename)
	{
		if(filename.endsWith(".htm") || filename.endsWith(".html"))
		{
			return "text/html";
		}
		if(filename.endsWith(".jpeg") || filename.endsWith(".jpg"))
		{
			return "image/jpeg";
		}
		if(filename.endsWith(".gif"))
		{
			return "image/gif";
		}
		
		return "application/octet-stream";
	}
	
	private void processRequest() throws Exception
	{
		//Get a reference to the socket's input and output streams
		InputStream is = (socket.getInputStream());
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		//Set up input stream filters
		InputStreamReader in = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(in);
		
		//Get the request line of the HTTP request message
		String requestLine = br.readLine();
		
		//Display the request line
		System.out.println();
		System.out.println(requestLine);
		
		//Get and display the header lines
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0)
		{
			System.out.println(headerLine);
		}
		
		//Extract filename from request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); //Skip over method, which should be "GET"
		String filename = tokens.nextToken();
		
		//Prepend a "." so that file request is within the current directory
		filename = "." + filename;
		System.out.println(filename);
		
		//Open the requested file
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
				fis = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
				fileExists = false;
		}
		
		//Construct the response message
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if(fileExists) 
		{
			statusLine = "HTTP/1.1 200 OK";
			contentTypeLine = "Content-type: " + contentType(filename) + CRLF;
		} 
		else
		{
			statusLine = "HTTP/1.1 404 Not Found";
			contentTypeLine = "Content-type: " + contentType(filename) + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
		}
		
		//Send status and content type line
		os.writeBytes(statusLine);
		os.writeBytes(contentTypeLine);
		//Send blank line to indicate the end of the header
		os.writeBytes(CRLF);
		
		//Send the entity body
		if(fileExists)
		{
			sendBytes(fis, os);
			fis.close();
		}
		else
		{
			os.writeBytes(entityBody);
		}
	
		os.close();
		br.close();
		socket.close();
	}
}

//PORT 5555 301 moved permanently
final class MovedRequest implements Runnable
{
	final static String CRLF = "\r\n";
	Socket socket;
	
	//Constructor
	public MovedRequest(Socket socket) throws Exception
	{
		this.socket = socket;
	}

	@Override
	public void run() 
	{
		// TODO Auto-generated method stub
		try {
				processRequest();
		} catch (Exception e) {
				System.out.println(e);
		}
		
	}
	
	private void processRequest() throws Exception
	{
		//Get a reference to the socket's input and output streams
		InputStream is = (socket.getInputStream());
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		//Set up input stream filters
		InputStreamReader in = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(in);
		
		//Get the request line of the HTTP request message
		String requestLine = br.readLine();
		
		//Display the request line
		System.out.println();
		System.out.println(requestLine);
		
		//Get and display the header lines
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0)
		{
			System.out.println(headerLine);
		}
		
		//Extract filename from request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); //Skip over method, which should be "GET"
		String filename = tokens.nextToken();
		
		//Prepend a "." so that file request is within the current directory
		filename = "." + filename;
		System.out.println(filename);
		
		//Construct the response message
		String statusLine = null;
		String contentTypeLine = null;
	
		statusLine = "HTTP/1.1 301 Moved Permanently" + CRLF;
		contentTypeLine = "Location: https://www.google.com" + CRLF;
			
		//Send status and content type line
		os.writeBytes(statusLine);
		os.writeBytes(contentTypeLine);
		//Send blank line to indicate the end of the header
		os.writeBytes(CRLF);
		
		os.close();
		br.close();
		socket.close();
	}
}