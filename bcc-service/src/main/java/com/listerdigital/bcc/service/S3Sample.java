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
	 
	 public List<String> getfileNames(){
		 AmazonS3 s3= getS3obj();
		 Properties properties = System.getProperties();
	      properties.setProperty("mail.smtp.host", "localhost");
	      Session session = Session.getDefaultInstance(properties);
	      List<String> campaignsList = new ArrayList<String>();
		 ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName("sr-buk1").withPrefix("Symantec_Archiver"));
		 int count =0;
		 campaignsList.add("FileName,Subject_Line,Date,Send-To");
		 for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
			 count++;
            
             InputStream input =  s3.getObject(new GetObjectRequest(
 					"sr-buk1", objectSummary.getKey())).getObjectContent();
             if(count ==1)
            	 continue;
             try {
				MimeMessage mime  = new MimeMessage(session,input) ;
				//if(mime.getAllRecipients()[0]!=null && mime.getAllRecipients()[0].toString().toLowerCase().equalsIgnoreCase(email)){
				//String str=	objectSummary.getKey()+','+;
				Date d = mime.getSentDate();
				String sent_to=mime.getAllRecipients()[0].toString();
				 SimpleDateFormat Format = new SimpleDateFormat("dd-MMM-YYYY HH:mm");
				 String sent_date=Format.format(d);
				 String subject=mime.getSubject(); 
				 String row=objectSummary.getKey()+','+subject+','+sent_date+','+sent_to+'\n';
				campaignsList.add(row);
				//}
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             
         }
		 System.out.println("count.."+count);
		/* S3Object s3object = s3.getObject(new GetObjectRequest(
					"sr-buk1", "Symantec_Archiver/01p6qjtqn2bt8hvrunnu0v5rp4k46aqf8h6tvo81"));
			System.out.println("Content-Type: "  + 
					s3object.getObjectMetadata());*/
		 System.out.println(campaignsList);
			return campaignsList;
	 }
	 
	 
	 public List<String> getfileNames(String email){
		 AmazonS3 s3= getS3obj();
		 Properties properties = System.getProperties();
	      properties.setProperty("mail.smtp.host", "localhost");
	      Session session = Session.getDefaultInstance(properties);
	      List<String> campaignsList = new ArrayList<String>();
		 ObjectListing objectListing = s3.listObjects(new ListObjectsRequest().withBucketName("sr-buk1").withPrefix("Symantec_Archiver"));
		 int count =0;
		 for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
			 count++;
            
             InputStream input =  s3.getObject(new GetObjectRequest(
 					"sr-buk1", objectSummary.getKey())).getObjectContent();
             if(count ==1)
            	 continue;
             try {
				MimeMessage mime  = new MimeMessage(session,input) ;
				if(mime.getAllRecipients()[0]!=null && mime.getAllRecipients()[0].toString().toLowerCase().equalsIgnoreCase(email)){
					campaignsList.add(objectSummary.getKey());
				}
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
             if(count == 11)
            	 break;
         }
		/* S3Object s3object = s3.getObject(new GetObjectRequest(
					"sr-buk1", "Symantec_Archiver/01p6qjtqn2bt8hvrunnu0v5rp4k46aqf8h6tvo81"));
			System.out.println("Content-Type: "  + 
					s3object.getObjectMetadata());*/
		 System.out.println(campaignsList);
			return campaignsList;
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
	 
	/*public static void main(String[] args) {
		 String str_host="52.9.146.109";
	    	String str_user="gfproduser";
	    	int str_port=22;
	    	String str_dir="/home/gfproduser/Bcc";
	    	String str_file="campaign.csv";
	    	getCampaignDetails(str_host,str_user,str_port,str_dir,str_file);
		 	
		System.out.println("Iam here");
	    	String emailID="respqatest14@gmail.com";
	    	getDynamonew(emailID);
	}*/

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
	 
	 
	 public static String getDynamoDBItem(String mail){
		 //Table table = DB.getTable(tableName);
		 //System.out.println("hello..");
		 /* GetItemSpec spec = new GetItemSpec()
          .withPrimaryKey("EmailAddress", key);*/
		 AWSCredentials creds = new BasicAWSCredentials("AKIAIZXF4XUGIFLIPQGA", "URmKH0mu/6LSUIFvk2tMlOIHk5lNqvD6PU4bAoIm");

		  DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new ProfileCredentialsProvider()));
		  Table table = dynamoDB.getTable("Test_BCC");
	        
		    AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(creds);
		    ddbClient.setRegion(Region.getRegion(Regions.US_EAST_1));
	 
	    	/*Map<List<String,AttributeValue>>=new HashMapList<String,AttributeValue>;*/
	    	Map<String, AttributeValue> Partition_key = new HashMap<String, AttributeValue>();
	    	Map<String, AttributeValue> sort_key = new HashMap<String, AttributeValue>();
	    	
	    	HashMap<String, KeysAndAttributes> requestItems = new HashMap<String, KeysAndAttributes>();
	    	
	    	Partition_key.put("EmailAddress",new AttributeValue().withS(mail));
	    	//Partition_key.put("SubjectLine",new AttributeValue().withS(subject_line));
	    	
/*Map<String, AttributeValue> key = new HashMap("hashKey", new AttributeValue("hashVal"), "rangeKey", new AttributeValue("rangeVal"));
	    	
	    	ArrayList<KeySchemaElement> keySchema = new ArrayList<KeySchemaElement>();
            keySchema.add(new KeySchemaElement()
                .withAttributeName("mailID")
                .withKeyType(KeyType.HASH));
	    	*/
	    	GetItemRequest request = new GetItemRequest().withTableName("Test_BCC").withKey(Partition_key);
	    	
	    	/* BatchGetItemRequest batchGetItemRequest=new BatchGetItemRequest();
	    	 batchGetItemRequest.setRequestItems(requestItems);*/
	    	 
	    	 
	    	  System.out.println("Request: "+request);
	    	  JSONObject json = new JSONObject();
	    	    Gson gson = new Gson();
	    	    String json_string;
	    	
	    	    	
	        try {
	    	 
	            GetItemResult result = ddbClient.getItem(request);
	            System.out.println("GetItem succeeded: "+result.toString());
	            Map<String, AttributeValue> itemAttributes = result.getItem();
		        System.out.println("SubjLine: "+itemAttributes.get("SubjectLine"));
		            json.putAll(itemAttributes);
		            json_string=gson.toJson(json);
		            System.out.println("json "+json_string);
		            return json_string;
	        } catch (Exception e) {
	            System.err.println("GetItem failed.");
	            System.err.println(e.getMessage());
	        }  

		 return null;
	 }
	 
	 public static String getCampaignDetails(String str_Host, String str_Username, int int_Port, String str_FileDirectory, String str_FileName)
	  {
		 JSch jsch = new JSch();
		 StringBuilder obj_StringBuilder = new StringBuilder();
	    try
	    {
	           
	            String privateKey = "C://Users//sravanajyothi.n//Downloads//private_key.ppk";

	            jsch.addIdentity(privateKey);
	            System.out.println("identity added ");

	            com.jcraft.jsch.Session session = jsch.getSession(str_Username, str_Host, int_Port);
	            System.out.println("session created.");
	            java.util.Properties config = new java.util.Properties(); 
	            config.put("StrictHostKeyChecking", "no");
	            session.setConfig(config);

	            // disabling StrictHostKeyChecking may help to make connection but makes it insecure
	            // see http://stackoverflow.com/questions/30178936/jsch-sftp-security-with-session-setconfigstricthostkeychecking-no
	            // 
	            // java.util.Properties config = new java.util.Properties();
	            // config.put("StrictHostKeyChecking", "no");
	            // session.setConfig(config);

	            session.connect();
	            System.out.println("session connected.....");

	            Channel channel = session.openChannel("sftp");
	            channel.setInputStream(System.in);
	            channel.setOutputStream(System.out);
	            channel.connect();
	            System.out.println("shell channel connected....");

	            ChannelSftp c = (ChannelSftp) channel;
	            c.cd(str_FileDirectory);
	            System.out.println("Entered Location");
	            InputStream obj_InputStream = c.get(str_FileName);
	           
	            System.out.println("Got File");
	            char[] ch_Buffer = new char[0x10000];
	            Reader obj_Reader = new InputStreamReader(obj_InputStream, "UTF-8");
	            int int_Line = 0;
	            do
	            {
	              int_Line = obj_Reader.read(ch_Buffer, 0, ch_Buffer.length);
	              if (int_Line > 0)
	              { obj_StringBuilder.append(ch_Buffer, 0, int_Line);}
	            }
	            while (int_Line >= 0);
	            obj_Reader.close();
	            obj_InputStream.close();
	            c.exit();
	            channel.disconnect();
	            session.disconnect();
	            System.out.println("done");

	    }
	    catch (Exception ex)
	    {
	      ex.printStackTrace();
	    }
	    System.out.println("reading file....."+obj_StringBuilder.toString());
	    try{
	    AmazonS3 s3= getS3obj();
	   PutObjectResult pres= s3.putObject("sr-buk1","CampaignDetails/"+(new Date()).toString(),obj_StringBuilder.toString() );
	   System.out.println(pres.getETag());
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    /*
	    String[] columns=obj_StringBuilder.toString().split("\n");
	    String[] fields=columns[0].split(",");
	    String[] values=columns[1].split(",");
	    JSONObject object = new JSONObject();
	    for(int i=0;i<fields.length;i++){
	    	object.put(fields[i], values[i]);
	    }
	    System.out.println("JSON..."+object.toString());*/
	    return obj_StringBuilder.toString();
	  }
	 
	/* private static void displayTextInputStream(InputStream input)
			    throws IOException {
			    	// Read one text line at a time and display.
		 Properties properties = System.getProperties();

	      // Setup mail server
	      properties.setProperty("mail.smtp.host", "localhost");

	      // Get the default Session object.
	      Session session = Session.getDefaultInstance(properties);
	      try {
	    	  MimeMessage mime  = new MimeMessage(session,input) ;
	    	System.out.println("To -- "+mime.getAllRecipients()[0]);
	    	System.out.println("Subject -- "+mime.getSubject());
	    	System.out.println("Body -- "+mime.getContent().toString());
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			        BufferedReader reader = new BufferedReader(new 
			        		InputStreamReader(input));
			        while (true) {
			            String line = reader.readLine();
			            if (line == null) break;

			            System.out.println("    " + line);
			        }
			        System.out.println();
			    }*/
}
