package at.rkn2016.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;

import javax.net.ssl.SSLSocket;

import at.rkn2016.parser.HTTPRequest;
import at.rkn2016.plugins.RequestHeaderChanger;
import at.rkn2016.proxy.*;

public class RequestThread extends Thread {
	private boolean cookie_dump = false;
	private boolean is_SSL = false;
	private HashMap<String, Socket> connections;
	Socket client;

	BufferedInputStream streamFromClient;
	BufferedOutputStream streamToClient;

	public RequestThread(Socket client) {
		super();
		this.client = client;
		connections = new HashMap<String, Socket>();
		try {
			streamFromClient = new BufferedInputStream(client.getInputStream());
			streamToClient = new BufferedOutputStream(client.getOutputStream());
		} catch (IOException e) {
			try
			{
				client.close();
			}
			catch (IOException e_io2) {
			}
			e.printStackTrace();
		}

	}

	@Override
	public void run() {
		super.run();
		try {
			try {
				boolean stream_alive = false;							
				do
				{
					BufferedOutputStream streamToServer;
					BufferedInputStream streamFromServer;
					HTTPRequest http_request = new HTTPRequest();
					stream_alive = HTTPRequest.parse(streamFromClient, http_request);
					if(stream_alive)
					{
						stream_alive = http_request.parseData(streamFromClient);
					}
					http_request.fields.remove("Proxy-Connection");
					//http_request.fields.remove("Connection");
					http_request.fields.remove("Accept-Encoding");

					if(cookie_dump && http_request.fields.containsKey("Cookie"))
					{
						File yourFile = new File("../cookie_dump/" + http_request.host);
						if(!yourFile.exists()) 
						{
							yourFile.createNewFile();
						}
						Files.write(Paths.get("../cookie_dump/" + http_request.host), (http_request.fields.get("Cookie") + "\r\n").getBytes(), StandardOpenOption.APPEND);
					}
					
					if(RKNProxy.PLUGIN_HEADER_CHANGER)
					{
						RequestHeaderChanger.modifyHTTPrequest(http_request);
					}
					System.out.println(http_request.toString());

					if(connections.containsKey(http_request.host) == false)
					{
						Socket server = new Socket(http_request.host, http_request.port);
						connections.put(http_request.host, server);
						streamFromServer = new BufferedInputStream(server.getInputStream());
						streamToServer = new BufferedOutputStream(server.getOutputStream());
						new ReplyThread(server, streamFromServer, streamToClient, http_request.isSSL()).start();
					}
					else if(connections.get(http_request.host).isClosed()) /* Java is Copy&Paste */
					{
						Socket server = new Socket(http_request.host, http_request.port);
						connections.put(http_request.host, server);
						streamFromServer = new BufferedInputStream(server.getInputStream());
						streamToServer = new BufferedOutputStream(server.getOutputStream());
						new ReplyThread(server, streamFromServer, streamToClient, http_request.isSSL()).start();
					}
					else
					{
						Socket server = connections.get(http_request.host);
						streamToServer = new BufferedOutputStream(server.getOutputStream());
					}

					

					if(http_request.checkSSL(streamFromClient, streamToClient, streamToServer))
					{
						break;
					}
					
					http_request.send(streamToServer);
					streamToServer.flush();
					
				} while(stream_alive);
			} catch (IllegalArgumentException e) {
				System.out.println("Error: " + e.toString());
			}						

		} 
		catch (IOException e)
		{
			e.printStackTrace();
			System.out.println("WTF: " + e.getMessage());
			return;
		}



		System.out.println("Stream is gone.");

		// The server closed its connection to us, so we close our
		// connection to our client.
		/*try {
			streamToClient.close();
			streamToClient = null;
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		try {
			if (server != null)
				server.close();
			if (client != null)
				client.close();
		} catch (IOException e) {
		}*/
	}

}
