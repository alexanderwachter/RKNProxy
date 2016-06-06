package at.rkn2016.proxy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.rkn2016.parser.HTTPPayload.PayloadType;
import at.rkn2016.parser.HTTPPayload;
import at.rkn2016.parser.HTTPResponse;
import at.rkn2016.plugins.ContentReplacePlugin;
import at.rkn2016.plugins.Plugin;

class ReplyThread extends Thread {

	BufferedInputStream streamFromServer;
	BufferedOutputStream streamToClient;
	Socket server;
	boolean is_SSL = false;
	

	public ReplyThread(Socket server, BufferedInputStream streamFromServer, BufferedOutputStream streamToClient, boolean is_SSL) {
		super();
		this.streamFromServer = streamFromServer;
		this.streamToClient = streamToClient;
		this.server = server;
		this.is_SSL = is_SSL;
	}


	@Override
	public void run() {
		super.run();
		boolean stream_alive = false;
		if(is_SSL)
		{
			int bytes_read;
			byte[] buffer = new byte[1000];
			try {
				while ((bytes_read = streamFromServer.read(buffer)) != -1)
				{
					streamToClient.write(buffer, 0, bytes_read);
					streamToClient.flush();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else
		{
			do
			{

				try {
					HTTPResponse http_response = new HTTPResponse();
					
					HTTPResponse.parseHeader(streamFromServer, http_response);
					http_response.fields.put("Access-Control-Allow-Origin", "*");
					http_response.fields.put("Access-Control-Allow-Credentials", "true");
					http_response.fields.put("Access-Control-Allow-Methods", "GET, POST");
					http_response.fields.put("Access-Control-Allow-Headers", "X-Requested-With");
					http_response.fields.put("Access-Control-Max-Age", "86400");
					http_response.fields.remove("Proxy-Authorization");
					stream_alive = http_response.parseData(streamFromServer);

					for(Plugin p : RKNProxy.plugins)
					{
						p.modifyHTTPresponse(http_response);
					}
					
					System.out.println(http_response.toString());
					if(http_response.getPayloadType() == PayloadType.ToEnd)
						http_response.setPayloadType(HTTPPayload.PayloadType.Content_length);
					stream_alive = http_response.send(streamToClient);
					
					if(!stream_alive)
					{			
						streamFromServer.close();
						streamFromServer = null;
						server.close();
					}
								
				} catch (IOException e) {
					// TODO Auto-generated catch block
					try {
						server.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					e.printStackTrace();
					return;
				}
			} while(stream_alive);
		}

		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}