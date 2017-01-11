var AWS = require('aws-sdk');
//var dynamodb = new AWS.DynamoDB({apiVersion: '2012-10-17'});
var dynamodb = new AWS.DynamoDB();
var docClient = new AWS.DynamoDB.DocumentClient();
const s3 = new AWS.S3({ apiVersion: '2006-03-01' });
var http = require('http');
exports.handler = function(event, context) {
    console.log(JSON.stringify(event, null, '  '));
    var optionspost = {
        host: 'requestb.in',
        path: '/15ukp481',
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        }
    };

    var req=  http.request(optionspost,function(res) {
    console.log("Got response: " + res.statusCode);
    context.succeed();
        }).on('error', function(e) {
    console.log("Got error: " + e.message);
    context.done(null, 'FAILURE');
  });
  
    var tableName = "Test_BCC";
    //var datetime = new Date().getTime().toString();
    
    //var s3data="EmailId,CampName,From,S3FileName,SentDate,CampSubj respqatest13@gmail.com,Symanctec_archiever_campaign,promotional,symantec@gmail.com,Symantec_Archiver/01p6qjtqn2bt8hvrunnu0v5rp4k46aqf8h6tvo81,10/21/201620:09,Symanctec_archiever_test respqatest15@gmail.com,archiever_campaign,Transactional,symantec@gmail.com,Symantec_Archiver/01p6qjtqn2bt8hvrunnu0v5rp4k46aqf8h6tvo81,10/21/201620:09,Symanctec_archiever_test";
   
   var batchitems=function(data){
        var final=[];
        var array=data.split('\n');
        //var columns=array[0].split(',');
        for(var i=1;i<array.length-1;i++) {
        console.log(array[i]);
        
        var itemDetails=array[i].split('\t');
    
        var obj={ PutRequest:{
           Item:{
                "CampaignName":{"S":itemDetails[0]},
                "SubjectLine":{"S":itemDetails[1]},
                "EmailAddress":{"S":itemDetails[2]},
                "SentDate":{"S":itemDetails[3]},
                "S3FileName":{"S":itemDetails[4]},
                "receivedTime":{"S":itemDetails[5]},
                "From":{"S":"Synchrony"}
            }
        }};
        console.log(obj);
        final.push(obj);
        req.write('inside batch function...'+JSON.stringify(obj));
      }
      console.log(final);
      
      return final;
    };
    

	
	  	var batchRequest;
	 	const bucket = event.Records[0].s3.bucket.name;
    const key = decodeURIComponent(event.Records[0].s3.object.key.replace(/\+/g, ' '));
    const params = {
        Bucket: bucket,
        Key: key,
    };
	  	
	  	s3.getObject(params, (err, data) => {
        if (err) {
            console.log(err);
            const message = `Error getting object ${key} from bucket ${bucket}. Make sure they exist and your bucket is in the same region as this function.`;
            console.log(message);
            
             req.write('error..'+JSON.stringify(err));
            req.end();
            
           //callback(message);
            
        } else {
         //  req.write('No error s3 body..'+data.Body.toString('utf-8'));
           
            batchRequest = {
			RequestItems: {
				"Test_BCC":batchitems(data.Body.toString('utf-8'))
	      	}
	  	};
	  	    // req.write('No error batch..'+batchRequest.toString());
             
         
             //callback(null, data.ContentType);
               dynamodb.batchWriteItem(batchRequest, function(err, data) {
        if (err) {
            context.done('error dynamo'+err);
            req.write(' error..'+err);
             req.end();
        }
        else {
            console.log('great success: '+JSON.stringify(data, null, '  '));
            context.done('K THX BY');
             req.write('no  error dynamo..'+batchRequest);
             req.end();
        }
    });
         
            
        }
    });
    
      
    
      
	  	
    
    
     
};
