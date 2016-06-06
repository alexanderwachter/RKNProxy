package at.rkn2016.parser;

import java.io.BufferedInputStream;
import java.io.IOException;

public class ReadLine {
	
	public static int readLine(BufferedInputStream stream, StringBuffer output, StreamAlive stream_alive) throws IOException
	{
		int read_char;
		int i = 0;
		stream_alive.alive = true;
		while((read_char = stream.read()) != -1)
		{
			output.append((char)read_char);
			if(i > 0 && output.charAt(i) == '\n' && output.charAt(i-1) == '\r')
			{
				output.delete(i-1, i+1);
				return i -1;
			}
			i++;
		}
		stream_alive.alive = false;
		return i;
	}

}
