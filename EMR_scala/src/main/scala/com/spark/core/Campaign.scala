package com.spark.core

import org.apache.spark.SparkConf
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.SparkContext
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.Row;
import org.apache.spark.streaming.{Seconds, StreamingContext, Time}
 import org.apache.spark.sql.types.{StructType,StructField,StringType}
import java.util.Calendar


object Campaign{

		def main(args: Array[String]){
				val conf = new SparkConf().setAppName("Campaign").setMaster("local").set("org.apache.spark.sql.crossJoin.enabled","true")
				val sc = new SparkContext(conf)
				val ssc = new StreamingContext(sc,org.apache.spark.streaming.Seconds(70))
				val sqlContext = new org.apache.spark.sql.SQLContext(sc)
				
				import sqlContext.implicits._
	
			
			val campaignDataFrame = sc.textFile("s3://sr-buk1/input/CampaignDetails/campaignDetails.csv")
			.map(_.split(","))
			.map(record => Campaign(record(0).toString(),record(1).toString(),record(2).toString(),record(3).toString()))
			.toDF()
			campaignDataFrame.registerTempTable("campaignDetails")
			campaignDataFrame.show()
			
			
			/*val emailDataFrame = sc.textFile("s3://sr-buk1/input/EmailDetails/metadata.csv")
			.map(_.split(","))
			.map(record => Email(record(0).toString(),record(1).toString(),record(2).toString(),record(3).toString()))
			.toDF()
			emailDataFrame.registerTempTable("emails")
			emailDataFrame.show()	*/

			val emailLines = ssc.textFileStream("s3://sr-buk1/input/EmailDetails/")
			emailLines.foreachRDD { (rdd : RDD[String], time : Time) =>  
			val emailDataFrame = rdd.map(_.split(",")).map(record => Email(record(0).toString(),record(1).toString(),record(2).toString(),record(3).toString())).toDF()
			emailDataFrame.registerTempTable("emails")
			emailDataFrame.show()

			var name=System.currentTimeMillis	
			val mergedData = sqlContext.sql("SELECT campaignDetails.campaignName,campaignDetails.emailSubject,emails.emailID,campaignDetails.sentTime,emails.S3FileName,emails.receivedTime FROM campaignDetails LEFT OUTER JOIN emails ON LOWER(campaignDetails.emailID)= LOWER(emails.emailID)")
			
			val temp = sqlContext.sql("SELECT * FROM campaignDetails r JOIN emails s ON r.emailID = s.emailID")
			temp.show()
			campaignDataFrame.join(emailDataFrame, Seq("emailID"), "left_outer").show()
			mergedData.show()
			mergedData.repartition(1).write.format("com.databricks.spark.csv"). save("s3://sr-buk1/input/EMR_output/"+name)
			//mergedData.saveAsTextFile("s3://sr-buk1/input/EMR_output/"+name)
			}
			ssc.start()             
			ssc.awaitTermination()
		
		}

	case class Email(emailID: String,Subject: String,S3FileName: String,receivedTime: String)
	case class Campaign(campaignName: String,emailID: String,emailSubject: String,sentTime: String)


}