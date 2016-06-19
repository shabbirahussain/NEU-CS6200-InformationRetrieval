/**
 * 
 */
package com.ir.homework.hw3.tools;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author shabbirhussain
 *
 */
public final class URLCanonizer {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws URISyntaxException, MalformedURLException {
		String url = "#";

		System.out.println(getCanninizedURL(url));
	}
	
	
	public static URL getCanninizedURL(String url) throws URISyntaxException, MalformedURLException{
		// Remove Fragment : everything after #
		url = url.replaceAll("#.*", "");
		
		URI compiledURI = new URI(url);
		String protocol = compiledURI.getScheme().toLowerCase(); // Convert scheme to lowercase
		String host     = compiledURI.getHost().toLowerCase();
		String path     = compiledURI.getPath();
		
		compiledURI = new URI(protocol, host, path, null);
		compiledURI = compiledURI.normalize();
		
		return compiledURI.toURL();
		
		// shorten: /a/../b => /b => [^/]+\/[^\/]*../
		//String backFilter     = "[^/]+/[^/]*(../)";
	} 
}
