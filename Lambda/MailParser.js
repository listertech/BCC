var AWS = require('aws-sdk');
//var dynamodb = new AWS.DynamoDB({apiVersion: '2012-10-17'});
var dynamodb = new AWS.DynamoDB();
var docClient = new AWS.DynamoDB.DocumentClient();
const s3 = new AWS.S3({ apiVersion: '2006-03-01' });
var MailParser = require("mailparser").MailParser;
const aws = require('aws-sdk');
const mailparser = new MailParser();
var fs = require("fs");

var http = require('http');
var body;
var mailparser_res;


exports.handler = function(event, context) {
    console.log(JSON.stringify(event, null, '  '));
    var optionspost = {
        host: 'requestb.in',
        path: '/15tkfto1',
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
  
  mailparser.on("end", function(mail_object){
    //console.log("From:", mail_object.from); //[{address:'sender@example.com',name:'Sender Name'}]
    console.log("Subject:", mail_object.subject); // Hello world!
    console.log("To:", mail_object.to); // How are you today?
    console.log("Headers:", mail_object.headers.date);
   //var r=JSON.parse(mail_object.to);
    body="Subject,To,Date,FileName"+"---\n"+mail_object.subject+","+mail_object.to[0].address+","+mail_object.headers.date+","+bucket+"/"+key;
     //req.write('body.!.'+body+'\n object..'+body);
     
    
    var param = {Bucket: 'sr-buk1', Key:'metadata.txt' ,Body:body};
    s3.putObject(param, function(err, data) {
                if (err) {
                    req.write('error in uploading'+ err.stack);
                    req.end();
                    console.log(err, err.stack);} // an error occurred
                else {
                    req.write(' \n uploaded successfully');
                    req.end();
                    console.log(data);
                    
                }           // successful response
        
                console.log('actually done!');
                context.done();
            });
            //req.end();
});
    
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
             req.write('error in s3..'+JSON.stringify(err));
            req.end();
            
           //callback(message);
            
        } else {
           //req.write('No error s3 body..');
           
           mailparser.write(data.Body);
           mailparser.end();
           
        }
    });
    
    
    

    console.log('done?');
    //context.done();
};
