package com.listerdigital.bcc.service;


import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.json.simple.JSONObject;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.applicationdiscovery.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.PaginatedQueryList;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.gson.Gson;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;


public class S3Sample {
	

	 private static DateFormat JSON;

	public  static AmazonS3 getS3obj() {
		AWSCredentials creds = new BasicAWSCredentials("AKIAIZXF4XUGIFLIPQGA", "URmKH0mu/6LSUIFvk2tMlOIHk5lNqvD6PU4bAoIm");
		AmazonS3 s3= new AmazonS3Client(creds);
        

		Region usWest2 = Region.getRegion(Regions.US_EAST_1);
		s3.setRegion(usWest2);
		return s3;
	}
	 
	 public String getHtmlContent(String objectKey) throws IOException, MessagingException{
		 AmazonS3 s3= getS3obj();
		 Properties properties = System.getProperties();
	      properties.setProperty("mail.smtp.host", "localhost");
	      Session session = Session.getDefaultInstance(properties);
	      InputStream input = s3.getObject(new GetObjectRequest(
					"sr-buk1",objectKey )).getObjectContent();
	      MimeMessage mime=null;
	      JSONObject obj = new JSONObject();
	      try {
	    	  
			 mime  = new MimeMessage(session,input) ;
			 Date d = mime.getSentDate();
			 SimpleDateFormat Format = new SimpleDateFormat("dd-MMM-YYYY HH:mm");
			 String dateString=Format.format(d);
			 String subject=mime.getSubject(); 
			 String body=mime.getContent().toString();
			 obj.put("subject",subject);
		     obj.put("date",dateString);
		     obj.put("body",body);
		     
			 System.out.println("Object.."+obj.toString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	      return obj.toString();
	 }

	public String getDynamonew(String email){
		
		 
		 AWSCredentials creds = new BasicAWSCredentials("AKIAIZXF4XUGIFLIPQGA", "URmKH0mu/6LSUIFvk2tMlOIHk5lNqvD6PU4bAoIm");

		  DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
		  Table table = dynamoDB.getTable("Test_BCC");
	        
		    AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(creds);
		    ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
		    

            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);
    
            Map<String, AttributeValue> eav = new HashMap<String, AttributeValue>();
            eav.put(":val1", new AttributeValue().withS(email));
          
            BCC bcc_obj=new BCC();
            bcc_obj.setMail(email);
            DynamoDBQueryExpression<BCC> queryExpression = new DynamoDBQueryExpression<BCC>()
                .withKeyConditionExpression("EmailAddress = :val1")
                .withExpressionAttributeValues(eav);

            List<BCC> paginatedList = mapper.query(BCC.class, queryExpression);
            System.out.println("size..."+paginatedList.size());
            ArrayList<ArrayList<String>> final_list= new ArrayList<ArrayList<String>>();
            
            JSONObject obj = new JSONObject();
           List<String> filenames=new ArrayList<String>();
           List<String> Sub=new ArrayList<String>();
           List<String> camp=new ArrayList<String>();
           List<String> dt=new ArrayList<String>();
           String filename = "";
           int count=0;
            for(BCC b : paginatedList ){
           
            	filenames.add(b.getS3FileName());
            	Sub.add(b.getSubject());
            	camp.add(b.getCampaignName());
            	dt.add(b.getSentDate());
            	
            }
            Gson gson = new Gson();
            obj.put("Filenames",filenames);
            obj.put("Subject",Sub);
            obj.put("CampaignDetails",camp);
            obj.put("Date",dt);
            
            System.out.println("json string..."+obj.get("Filenames"));
            
            final_list.add((ArrayList<String>) filenames);
            final_list.add((ArrayList<String>) camp);
            final_list.add((ArrayList<String>) Sub);
            final_list.add((ArrayList<String>) dt);
            
            String res=gson.toJson(obj);
            System.out.println("array..."+final_list);
            
           
        
            return res;
		 
	}
	 
	 
	 
	
	
}
