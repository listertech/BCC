package com.listerdigital.bcc.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONObject;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;

/**
 * Root resource (exposed at "myresource" path)
 */
@Path("myresource")
public class MyResource {

    /**
     * Method handling HTTP GET requests. The returned object will be sent
     * to the client as "text/plain" media type.
     *
     * @return String that will be returned as a text/plain response.
     */
    @GET
    @Path("getCampaigns")
    @Produces(MediaType.TEXT_PLAIN)
    public String getIt(@QueryParam("email") String email) {
    	System.out.println("Inside get -- "+email);
    	return new S3Sample().getfileNames(email).toString();
    }
    
    @GET
    @Path("getAllCampaigns")
    @Produces(MediaType.TEXT_PLAIN)
    public String getAll() {
    	System.out.println("Inside get All-- ");
    	return new S3Sample().getfileNames().toString();
    }
    
    @GET
    @Path("getCampaignHtml")
    @Produces(MediaType.TEXT_HTML)
    public String getCampaign(@QueryParam("fileName") String fileName) {
    	System.out.println("Inside Html");
        try {
			return new S3Sample().getHtmlContent(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fileName;
    }
    
    @GET
    @Path("getDynamoDBItem")
    @Produces(MediaType.TEXT_HTML)
    public String getDynamoDBItem(@QueryParam("mail") String mail) {
    	System.out.println("Inside Html");
    	String json;
    	try {
    		 json= new S3Sample().getDynamonew(mail);
    		 System.out.println("returnning"+json);
    		 return json;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
       
		
    }
    
   
    
}
