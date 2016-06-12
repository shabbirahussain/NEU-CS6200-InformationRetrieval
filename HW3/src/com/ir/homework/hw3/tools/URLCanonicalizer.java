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
public final class URLCanonicalizer {

	/**
	 * @param args
	 * @throws URISyntaxException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws URISyntaxException, MalformedURLException {
		String url = "https://play.Spotify.com:80//artis/t/../../53Xhw.htm#fbYqKCa1cC15pYq2q";

		URLCanonicalizer uc = new URLCanonicalizer();
		System.out.println(uc.getCanninizedURL(url));
	}
	
	
	public URL getCanninizedURL(String url) throws URISyntaxException, MalformedURLException{
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
