package at.rkn2016.parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.util.*;

import at.rkn2016.parser.HTTPPayload.PayloadType;

public class HTTPRequest {
	public enum HTTPversion {HTTP1, HTTP1_1, HTTP2}
	public RequestMethod method;
	public	HTTPversion version;
	public	String resource;
	public	String host;
	public 	int port;
	public	HashMap<String, String> fields;
	private HTTPPayload payload;
	
	public HTTPRequest()
	{
		fields = new HashMap<>();
	}
	public boolean checkSSL(BufferedInputStream streamFromClient, BufferedOutputStream streamToClient, BufferedOutputStream streamToServer) throws IOException
	{
		if(isSSL())
		{
			String s = "HTTP/1.1 200 OK\r\n"
					+ "\r\n";
			streamToClient.write(s.getBytes());
			streamToClient.flush();
			int bytes_read;
			byte[] buffer = new byte[1000];
			while ((bytes_read = streamFromClient.read(buffer)) != -1)
			{
				streamToServer.write(buffer, 0, bytes_read);
				streamToServer.flush();
			}
			return true;
		}
		else
		{
			return false;
		}
		
	}
	
	public boolean isSSL()
	{
		return method.getValue() == RequestMethod.method.CONNECT;
	}
	
	public static boolean parse(BufferedInputStream in, HTTPRequest request) throws IOException, IllegalArgumentException
	{
		StreamAlive stream_alive = new StreamAlive();
		StringBuffer buffer = new StringBuffer();
		ReadLine.readLine(in, buffer, stream_alive);
		if(stream_alive.alive == false)
			throw new  IllegalArgumentException("Stream closed while trying to read header");

		String[] request_fields = buffer.toString().split(" ", 3);
		
		
		if(request_fields.length != 3)
			throw new IllegalArgumentException("Header contains to less lines");
		request.method = new RequestMethod(request_fields[0]);
		request.resource = request_fields[1];
		String[] tmp = request.resource.split("//", 2);
		if(tmp.length == 2)
			request.resource = tmp[1];
		if(!request.resource.startsWith("/"))
		{
			tmp = request.resource.split("/",2);
			if(tmp.length == 2)
				request.resource = "/" + tmp[1];
			else
				request.resource = "/";
		}
		String version[] = request_fields[2].split("/");
		if(version.length != 2)
			throw new IllegalArgumentException("Version must contain HTTP/1 or HTTP/1.1 but is: " + request_fields[2]);
		if(! version[0].toUpperCase().equals("HTTP"))
			throw new IllegalArgumentException("Version must contain HTTP");
		if(version[1].equals("1"))
			request.version = HTTPversion.HTTP1;
		else if(version[1].equals("1.1"))
			request.version = HTTPversion.HTTP1_1;
		else
			throw new IllegalArgumentException("Version must contain HTTP/1 or HTTP/1.1 but is " + version[0] + "/" + version[1]);
		
		buffer = new StringBuffer();
		while(ReadLine.readLine(in, buffer, stream_alive) != 0)
		{
			String[] split_line = buffer.toString().split(": ", 2);
			if(split_line.length != 2)
				throw new IllegalArgumentException("The shit is steaming"); // ALEX told us to continue
			request.fields.put(split_line[0], split_line[1]);
			
			buffer = new StringBuffer();
		}
		if((request.host = request.fields.get("Host")) == null)
			throw new IllegalArgumentException("Host: <String>  is a requested field");
		String tmp_[] = request.host.split(":");
		if(tmp_.length == 2)
		{
			request.port = Integer.parseInt(tmp_[1]);
			request.host = tmp_[0];
		}
		else
			request.port = 80;
		request.fields.remove("Host");
		return stream_alive.alive;
	}
	
	public boolean parseData(BufferedInputStream streamFromClient) throws IOException
	{
		if(this.method.getValue() == RequestMethod.method.POST)
		{
			payload = new HTTPPayload(fields);
			System.out.println(toString());
			boolean ret =  payload.parseData(streamFromClient, null);
			System.out.println( new String(payload.get()));
			return ret;
		}
		return true;
	}
	
	public void setPayload(byte[] data) throws IOException
	{
		payload.setPayload(data, 1000);
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
	
	public boolean send(BufferedOutputStream streamToServer) throws IOException
	{
		streamToServer.write(this.toString().getBytes());
		if(payload != null)
		{
			return payload.sendData(streamToServer);
		}
		return true;
	}
	
	public void payloadPut(byte[] data) throws IOException
	{
		payload.put(data);
	}

	
	@Override
	public String toString()
	{
		String return_val = method.toString() + " " + resource + " HTTP/1.1\r\n";
		return_val += "Host: " + host + "\r\n";
		for (Map.Entry<String, String> entry : fields.entrySet())
			return_val += entry.getKey() + ": " +entry.getValue() + "\r\n";
		return_val += "\r\n";
		return return_val;
	}
}

class RequestMethod {
	public enum method {GET, POST, HEAD, PUT, DELETE, CONNECT, OPTIONS, TRACE};
	private static final Map<String, method> RequestMethodMap;
	static {
		RequestMethodMap = new HashMap<String, method>();
		RequestMethodMap.put("GET", method.GET);
		RequestMethodMap.put("POST", method.POST);
		RequestMethodMap.put("HEAD", method.HEAD);
		RequestMethodMap.put("PUT", method.PUT);
		RequestMethodMap.put("DELETE", method.DELETE);
		RequestMethodMap.put("CONNECT", method.CONNECT);
		RequestMethodMap.put("OPTIONS", method.OPTIONS);
		RequestMethodMap.put("TRACE", method.TRACE);
	}
	
	private method value;
	private String value_str;
	
	public RequestMethod(String value)
	{
		setValue(value);
	}

	public method getValue() {
		return value;
	}

	public void setValue(String value) {
		if((this.value = RequestMethodMap.get(value)) == null)
			throw new IllegalArgumentException("Unknown request method " + value);
		value_str = value;
	}
	
	public void setValue(method value) {
		for (Map.Entry<String, method> entry : RequestMethodMap.entrySet())
		{
		    if(entry.getValue() == value)
		    {
		    	this.value_str = entry.getKey();
		    	this.value = value;
		    	return;
		    }
		}
		throw new IllegalArgumentException("Unknown request method " + value);
	}
	
	@Override
	public String toString()
	{
		return value_str;
	}
}
