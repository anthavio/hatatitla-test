package com.anthavio.hatatitla.test.web;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/")
public class HelloWorldResource {

	@GET
	@Produces("text/html")
	public String getXml() {
		return "<html><body><h1>Hello World!</body></h1></html>";
	}

	/**
	 * PUT method for updating or creating an instance of HelloWorldResource
	 * @param content representation for the resource
	 * @return an HTTP response with content of the updated or created resource.
	 */
	@PUT
	@Consumes("application/xml")
	@Produces("application/xml")
	public String putXml(String content) {
		return content;
	}

	@PUT
	@Consumes("application/json")
	@Produces("application/json")
	public String putJson(String content) {
		return content;
	}
}
