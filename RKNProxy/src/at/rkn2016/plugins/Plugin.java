package at.rkn2016.plugins;

import at.rkn2016.parser.HTTPResponse;

public abstract class Plugin {
	
	abstract public void modifyHTTPresponse(HTTPResponse http_response);
	
	static boolean removeHeaderField(HTTPResponse http_response, String field)
	{
		return (http_response.fields.remove(field) != null);
	}
	
	static boolean manipulateHeaderField(HTTPResponse http_response, String field, String value)
	{
		return (http_response.fields.put(field, value) != null);
	}
}
