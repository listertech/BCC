package com.listerdigital.bcc.service;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

@DynamoDBTable(tableName="Test_BCC")
public class BCC {
	
	private String EmailAddress;
    private String SubjectLine;
    private String CampaignName;
    private String S3FileName;
    private String SentDate;

    @DynamoDBHashKey(attributeName="EmailAddress")
    public String getMail() { return EmailAddress; }
    public void setMail(String mail) { this.EmailAddress = mail; }

    @DynamoDBRangeKey(attributeName = "SubjectLine")
    public String getSubject() { return SubjectLine; }
    public void setSubject(String SubjectLine) { this.SubjectLine = SubjectLine; }

    @DynamoDBAttribute(attributeName = "S3FileName")
    public String getS3FileName() { return S3FileName;}
    public void setS3FileName(String S3FileName) { this.S3FileName = S3FileName; }
    
    @DynamoDBAttribute(attributeName = "CampaignName")
    public String getCampaignName() { return CampaignName;}
    public void setCampaignName(String name) { this.CampaignName = name; }
    
    @DynamoDBAttribute(attributeName = "SentDate")
    public String getSentDate() { return SentDate;}
    public void setSentDate(String dt) { this.SentDate = dt; }
    
}
