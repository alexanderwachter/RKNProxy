package at.rkn2016.parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class HTTPPayload {
	
	public enum PayloadType {ToEnd, Content_length, chunked};
	private ByteArrayOutputStream payload_buffer;
	private ArrayList<Integer> chunk_list;
	private PayloadType payload_type;
	private HashMap<String, String> fields;
	
	HTTPPayload(HashMap<String, String> fields)
	{
		super();
		payload_buffer = new ByteArrayOutputStream();
		chunk_list = null;
		this.fields = fields;
	}

	public boolean parseData(BufferedInputStream streamFromServer, BufferedOutputStream streamToClient) throws IOException
	{
		if(fields.containsKey("Content-Length"))
			payload_type = PayloadType.Content_length;
		else if(fields.containsKey("Transfer-Encoding"))
			payload_type = PayloadType.chunked;
		else
			payload_type = PayloadType.ToEnd;		
		switch(payload_type)
		{
		case ToEnd:
			return parseDataToEnd(streamFromServer);
		
		case Content_length:
			return parseDataWithLength(streamFromServer, Integer.parseInt(fields.get("Content-Length")));
			
		case chunked:
			return parseDataChunks(streamFromServer, streamToClient);
		}
		return false;
	}
	
	private boolean parseDataToEnd(BufferedInputStream streamFromServer) throws IOException
	{
		int bytes_read;
		byte[] buffer = new byte[1000];
		while ((bytes_read = streamFromServer.read(buffer)) != -1)
		{
			this.payload_buffer.write(buffer, 0, bytes_read);
		}
		
		return false;
	}
	
	private boolean parseDataWithLength(BufferedInputStream streamFromServer, int length) throws IOException
	{
		ByteBuffer buffer = ByteBuffer.allocate(length);
		Boolean ret = parseDataWithLength(streamFromServer, length, buffer);
		this.payload_buffer.write(buffer.array());
		return ret;
	}
	
	private boolean parseDataWithLength(BufferedInputStream streamFromServer, int length, ByteBuffer buffer) throws IOException
	{
		byte[] buffer_arr = new byte[length];
		int read_bytes;
		while( (read_bytes = streamFromServer.read(buffer_arr,0,length)) != -1)
		{
			buffer.put(Arrays.copyOf(buffer_arr, read_bytes));
			if((length -= read_bytes) == 0)
				break;
		}
		if(read_bytes == -1)
			return false;
		
		return true;
	}

	private boolean parseDataChunks(BufferedInputStream streamFromServer, BufferedOutputStream streamToClient) throws IOException
	{
		StreamAlive stream_alive = new StreamAlive();
		chunk_list = new ArrayList<Integer>();
		chunk_list.add(0);
		int chunk_len = 0;
		do
		{
			StringBuffer chunk_len_strb = new StringBuffer();
			ReadLine.readLine(streamFromServer, chunk_len_strb, stream_alive);
			chunk_len = Integer.parseInt(chunk_len_strb.toString(), 16);
			ByteBuffer chunk_buffer = ByteBuffer.allocate(chunk_len);
			if(chunk_len > 0)
			{
				stream_alive.alive = parseDataWithLength(streamFromServer, chunk_len, chunk_buffer);
				this.payload_buffer.write(chunk_buffer.array());
				chunk_list.add(this.payload_buffer.size());
			}
			if((char)streamFromServer.read() != '\r' || (char)streamFromServer.read() != '\n') // check and remove \r\n
				throw new IOException("Expect \\r\\n at end of a chunk");
			if(streamToClient != null)
			{
				String chunk_string = Integer.toHexString(chunk_len) + "\r\n";
				streamToClient.write(chunk_string.getBytes());
				if(chunk_len > 0)
					streamToClient.write(chunk_buffer.array());
				streamToClient.write(new String("\r\n").getBytes());
			}
		}while(chunk_len > 0);
		return stream_alive.alive;
	}
	
	private void createChunks(int chunk_size)
	{
		chunk_list = new ArrayList<Integer>();
		int chunk_begin = 0;
		chunk_list.add(chunk_begin);
		int remaining_data;
		for(remaining_data = this.payload_buffer.size(); remaining_data > chunk_size ; remaining_data -= chunk_size)
		{
			chunk_begin += chunk_size;
			chunk_list.add( chunk_begin);
		}
		if(remaining_data > 0)
			chunk_list.add(chunk_begin + remaining_data);
	}
	
	public boolean sendData(BufferedOutputStream stream) throws IOException
	{
		switch(payload_type)
		{
		case Content_length:
			stream.write(payload_buffer.toByteArray());
			stream.flush();
			break;
			
		case chunked:
			sendChunks(stream);
			break;
		case ToEnd:
			stream.write(payload_buffer.toByteArray());
			stream.flush();
			stream.close();
			return false;
		}
		return true;
	}
	
	public void put(byte[] data) throws IOException
	{
		payload_buffer.write(data);
		if(payload_type == PayloadType.Content_length)
			setContentLength();
		else if (payload_type == PayloadType.chunked)
			chunk_list.add(chunk_list.get(chunk_list.size() - 1) + data.length);
	}
	
	private void setContentLength()
	{
		fields.put("Content-Length", this.payload_buffer.size() + "");
	}
	
	public void setPayload(byte[] data, int chunk_size) throws IOException
	{
		payload_buffer = new ByteArrayOutputStream();
		payload_buffer.write(data);
		setPayloadType(this.payload_type, chunk_size);
	}
	
	public void setPayloadType(PayloadType type, int chunk_size)
	{
		this.payload_type = type;
		
		switch(type)
		{
		case Content_length:
			fields.remove("Transfer-Encoding");
			setContentLength();
			this.chunk_list = null;
			return;
		case chunked:
			fields.put("Transfer-Encoding", "chunked");
			createChunks(chunk_size);
			fields.remove("Content-Length");
			return;
		case ToEnd:
			fields.remove("Transfer-Encoding");
			fields.remove("Content-Length");
			this.chunk_list = null;
			return;
		}
		return;
	}
	
	public void sendChunks(BufferedOutputStream stream) throws IOException
	{
		if(this.chunk_list == null || this.chunk_list.size() <= 1)
			throw new IOException("No chunks to send");
		
		byte payload[] = this.payload_buffer.toByteArray();
		
		Iterator<Integer> chunk_start = chunk_list.iterator();
		int from = chunk_start.next(), to=0;
		while(chunk_start.hasNext())
		{
			to = chunk_start.next();			
			stream.write( new String(Integer.toHexString(to - from) + "\r\n").getBytes());
			if(to > payload.length)
				System.out.println("WTF: to > payload");
			stream.write(Arrays.copyOfRange(payload, from, to));
			stream.write(new String("\r\n").getBytes());
			from = to;
			stream.flush();
		}
		stream.write(new String("0\r\n\r\n").getBytes());
		stream.flush();
	}
	
	public byte[] get()
	{
		return payload_buffer.toByteArray();
	}
	
	public PayloadType getPayloadType()
	{
		return this.payload_type;
	}
}
