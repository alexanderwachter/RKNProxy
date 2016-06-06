package at.rkn2016.plugins;

import java.io.BufferedReader;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import at.rkn2016.parser.HTTPResponse;

public class ResponseHeaderChanger extends Plugin {

	@Override
	public void modifyHTTPresponse(HTTPResponse http_response) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(new File("../config/responseheaderchanger.txt")));
			String line = null;
			while((line = br.readLine()) != null)
			{
				String[] entry = line.split(": ");
				if(entry.length == 1)
				{
					removeHeaderField(http_response, entry[0]);
				}
				else if(entry.length == 2)
				{
					/* also for adding fields (can be checked with a boolean return value) */
					manipulateHeaderField(http_response, entry[0], entry[1]);
				}
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
