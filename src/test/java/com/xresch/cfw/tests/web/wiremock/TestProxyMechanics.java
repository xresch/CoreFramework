package com.xresch.cfw.tests.web.wiremock;

import java.io.IOException;
import java.net.Proxy;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.xresch.cfw._main.CFW;
import com.xresch.cfw.features.query.CFWQueryContext;

public class TestProxyMechanics {
	
	/****************************************************************
	 * 
	 ****************************************************************/
	private static CFWQueryContext context = new CFWQueryContext();
	
	private static String jsonTestData;
	private static final String PACKAGE = "com.xresch.cfw.tests.features.query.testdata";
	
	public static WireMockServer proxy;
	public static int proxyPort = 6789;
	public static String proxyURL = "http://localhost:" + proxyPort;
	
	public static WireMockServer server;
	public static int serverPort = 7890;
	public static String serverURL = "http://localhost:" + serverPort;
	
	public static String urlProxyPac;
	public static String urlReflector;
	public static String urlReflectorProxied;
	
	@BeforeAll
	public static void setup() {
		
		//==================================================
		// Setup Server
		//==================================================
		server = new WireMockServer(
				WireMockConfiguration.wireMockConfig()
					.port(serverPort)
					.dynamicHttpsPort()
			);
		server.start();
			
		//-----------------------------------
		// /reflector
		// Takes any query params, headers and
		// body and returns it as a JSON object.
		urlReflector = serverURL + "/reflector";
		server.stubFor(
				WireMock.any(WireMock.urlPathEqualTo("/reflector"))
			    .willReturn(WireMock.aResponse()
			        .withStatus(200)
			        .withBody("""
			        		{
			        		  "headers": {{{toJson request.headers}}},
			        		  "query": {{{toJson request.query}}},
			        		  "body": {{#if request.body}}{{{toJson request.body}}}{{else}}null{{/if}}
			        		}
			        		""")
			        .withTransformers("response-template")));	
		
		//-----------------------------------
		// /reflectorProxied
		// used only to check if url contains "reflectorProxied"
		urlReflectorProxied = serverURL + "/reflectorProxied";
		server.stubFor(
				WireMock.any(WireMock.urlPathEqualTo("/reflectorProxy"))
			    .willReturn(WireMock.aResponse()
			        .withStatus(200)
			        .withBody("""
			        		{
			        		  "headers": {{{toJson request.headers}}},
			        		  "query": {{{toJson request.query}}},
			        		  "body": {{#if request.body}}{{{toJson request.body}}}{{else}}null{{/if}}
			        		}
			        		""")
			        .withTransformers("response-template")));	
		
		//-----------------------------------
		// /reflector
		// Takes any query params, headers and
		// body and returns it as a JSON object.
		urlProxyPac = serverURL + "/proxy.pac";
		server.stubFor(
				WireMock.any(WireMock.urlPathEqualTo("/proxy.pac"))
			    .willReturn(WireMock.aResponse()
			        .withStatus(200)
			        .withBody("""
			        	function FindProxyForURL(url, host) {
			        	  console.log("Javascript - FindProxyForURL: "+ url + " " + host);
						  // our local URLs from the domains below example.com don't need a proxy:
						  if (shExpMatch(url, "*reflectorProxied*")) return "PROXY localhost:%d";
						
						  // All other requests go through port 8080 of proxy.example.com.
						  // should that fail to respond, go directly to the WWW:
						  return "DIRECT";
						}
			        		""".formatted(proxyPort)
			        )
			        )
			    );	
		
		//==================================================
		// Setup Proxy
		//==================================================
		proxy = new WireMockServer(
				WireMockConfiguration.wireMockConfig()
					.port(proxyPort)
					.dynamicHttpsPort()
			);
		proxy.start();
		
		// Low priority catch-all proxies to otherhost.com by default
		proxy.stubFor(WireMock.get(WireMock.urlMatching(".*"))
						.atPriority(10)
						.willReturn(WireMock.aResponse().proxiedFrom(serverURL)));
		
		
	}
	
	
	/****************************************************************
	 * 
	 ****************************************************************/
	@Test
	public void testProxyPacURL() throws IOException {
		
		CFW.Properties.PROXY_ENABLED = true;
		CFW.Properties.PROXY_PAC = urlProxyPac;
		
		//---------------------------------
		// Check resolves DIRECT
		Proxy resolvedProxy = CFW.HTTP.getProxy(urlReflector);
		System.out.println("resolvedProxy: "+resolvedProxy); 
		
		Assertions.assertEquals(null, resolvedProxy, "No Proxy");
		
		//---------------------------------
		// Check resolves DIRECT
		resolvedProxy = CFW.HTTP.getProxy(urlReflectorProxied);
		System.out.println("resolvedProxy: " + resolvedProxy); 
		
		Assertions.assertEquals("HTTP @ localhost/127.0.0.1:6789", resolvedProxy.toString(), "Use Proxy");
		
	}

		
}
