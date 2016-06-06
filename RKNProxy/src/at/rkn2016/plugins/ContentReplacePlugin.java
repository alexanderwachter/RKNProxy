package at.rkn2016.plugins;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import at.rkn2016.parser.HTTPResponse;

public class ContentReplacePlugin extends Plugin {
	String[] words_to_replace = {" and ", " und ", "Innen", "Cloud", "cloud" };
	String[] words_for_replace = {" without ", " ohne ", "", "Butt", "butt" };

	@Override
	public void modifyHTTPresponse(HTTPResponse http_response) {
		// All methods you want for Content replacement
		try {
			replaceText(http_response);
			replaceJpeg(http_response);
			replaceGIF(http_response);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("No content replacement");
		}
	}

	void replaceText(HTTPResponse http_response) throws IOException
	{
		if(http_response.fields.containsKey("Content-Type") && http_response.fields.get("Content-Type").contains("text/html"))
		{
			String to_replace = new String(http_response.getPayload());
			
			for(int i = 0; i < words_for_replace.length; i++)
			{
				to_replace = to_replace.replace(words_to_replace[i], words_for_replace[i]);
			}
			http_response.setPayload(to_replace.getBytes());
		}
	}
	
	void replaceJpeg(HTTPResponse http_response) throws IOException
	{
		if (http_response.fields.containsKey("Content-Type") && http_response.fields.get("Content-Type").equals("image/jpeg"))
		{
			Path path = Paths.get("../content/example.jpg");
			byte[] data = Files.readAllBytes(path);
			http_response.setPayload(data);
		}
	}
	
	void replaceGIF(HTTPResponse http_response) throws IOException
	{
		if (http_response.fields.containsKey("Content-Type") && http_response.fields.get("Content-Type").equals("image/gif"))
		{
			Path path = Paths.get("../content/example.gif");
			byte[] data = Files.readAllBytes(path);
			http_response.setPayload(data);
		}
	}
}
