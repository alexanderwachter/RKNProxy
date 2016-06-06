package at.rkn2016.parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import at.rkn2016.parser.HTTPRequest.HTTPversion;

public class HTTPResponse {

	public enum ConnectionState {CLOSE, KEEP_ALIVE};
	private ConnectionState connection_state;
	private HTTPversion version;
	private String HTTPStatuscode_str;
	private int HTTPStatuscode_int;
	public HashMap<String, String> fields;
	
	private HTTPPayload payload;
	
	public HTTPResponse() throws IOException {
		super();
		fields = new HashMap<String, String>();
		payload = new HTTPPayload(fields);
	}
	
	public static boolean parseHeader(BufferedInputStream streamFromServer, HTTPResponse response) throws IOException
	{
		StreamAlive stream_alive = new StreamAlive();
		StringBuffer buffer = new StringBuffer();
		ReadLine.readLine(streamFromServer, buffer, stream_alive);
		if(stream_alive.alive == false)
			throw new  IllegalArgumentException("Stream closed while trying to read header");

		String[] response_fields = buffer.toString().split(" ", 3);
		
		if(response_fields.length != 3)
			throw new IllegalArgumentException("Header contains to less lines: " + buffer.toString());
		
		String version[] = response_fields[0].split("/");
		if(version.length != 2)
			throw new IllegalArgumentException("Version must contain HTTP/1 or HTTP/1.1 but is: " + response_fields[0]);
		if(! version[0].toUpperCase().equals("HTTP"))
			throw new IllegalArgumentException("Version must contain HTTP");
		if(version[1].equals("1.0"))
			response.version = HTTPversion.HTTP1;
		else if(version[1].equals("1.1"))
			response.version = HTTPversion.HTTP1_1;
		else
			throw new IllegalArgumentException("Version must contain HTTP/1.0 or HTTP/1.1 but is " + version[0] + "/" + version[1]);
		
		response.HTTPStatuscode_int = Integer.parseInt(response_fields[1]);
		response.HTTPStatuscode_str = response_fields[2];
		
		buffer = new StringBuffer();
		while(ReadLine.readLine(streamFromServer, buffer, stream_alive) != 0)
		{
			String[] split_line = buffer.toString().split(": ", 2);
			if(split_line.length != 2)
				throw new IllegalArgumentException("The shit is steaming: " + buffer.toString());
			response.fields.put(split_line[0], split_line[1]);
			buffer = new StringBuffer();
		}
		
		
		String connection_state;
		if((connection_state = response.fields.get("Connection")) != null)
		{
			if(connection_state == "close")
				response.connection_state = ConnectionState.CLOSE;
			else
				response.connection_state = ConnectionState.KEEP_ALIVE;
		}
		else {
			response.connection_state = ConnectionState.KEEP_ALIVE;
		}
		
		return stream_alive.alive;
	}
		
	public ConnectionState getConnection_state() {
		return connection_state;
	}
	
	public void setPayload(byte[] data) throws IOException
	{
		payload.setPayload(data, 1000);
	}
	
	public boolean parseData(BufferedInputStream streamFromServer) throws IOException
	{
		return payload.parseData(streamFromServer, null);
	}
		
	public HTTPPayload.PayloadType getPayloadType()
	{
		return payload.getPayloadType();
	}
	
	public void setPayloadType(HTTPPayload.PayloadType type)
	{
		payload.setPayloadType(type, 1000); //standard chunk size = 1000;
	}
	
	public byte[] getPayload()
	{
		return payload.get();
	}

	public boolean send(BufferedOutputStream streamToClient) throws IOException
	{
		streamToClient.write(this.toString().getBytes());
		return payload.sendData(streamToClient);
	}
	
	public void payloadPut(byte[] data) throws IOException
	{
		payload.put(data);
	}

	@Override
	public String toString()
	{
		String return_val;
		
		return_val = "HTTP/1.1 " + this.HTTPStatuscode_int + " " + this.HTTPStatuscode_str + "\r\n";
		for (Map.Entry<String, String> entry : fields.entrySet())
			return_val += entry.getKey() + ": " +entry.getValue() + "\r\n";
		return_val += "\r\n";
		return return_val;
	}
	
	
}
