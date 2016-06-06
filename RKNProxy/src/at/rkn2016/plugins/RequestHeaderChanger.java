package at.rkn2016.plugins;

import java.io.BufferedReader;


import java.io.File;
import java.io.FileReader;

import at.rkn2016.parser.HTTPRequest;

public class RequestHeaderChanger{

	public static void modifyHTTPrequest(HTTPRequest htttp_request) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("../config/requestheaderchanger.txt")));
			String line = null;
			while((line = br.readLine()) != null)
			{
				String[] entry = line.split(": ");
				if(entry.length == 1)
				{
					removeHeaderField(htttp_request, entry[0]);
				}
				else if(entry.length == 2)
				{
					/* also for adding fields (can be checked with a boolean return value) */
					manipulateHeaderField(htttp_request, entry[0], entry[1]);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static boolean removeHeaderField(HTTPRequest http_request, String field)
	{
		return (http_request.fields.remove(field) != null);
	}
	
	static boolean manipulateHeaderField(HTTPRequest http_request, String field, String value)
	{
		return (http_request.fields.put(field, value) != null);
	}

}
