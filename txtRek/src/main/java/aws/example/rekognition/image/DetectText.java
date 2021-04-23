package aws.example.rekognition.image;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.Message;



public class DetectText {

	public static List<String> detect(AmazonRekognition rekognitionClient, String name, String s3Bucket) throws FileNotFoundException {
		S3Object s3Obj = new S3Object();
		s3Obj.withBucket(s3Bucket);
		s3Obj.withName(name);
		
		Image img = new Image();
		img.withS3Object(s3Obj);
		
		DetectTextRequest request = new DetectTextRequest();
		request.withImage(img);
		
		List<String> resultList = new ArrayList<String>();
		
		try {
			
			DetectTextResult result = rekognitionClient.detectText(request);
			
			List<TextDetection> textDetections = result.getTextDetections();

				if(!textDetections.equals(null)) {
					resultList.add("========="+name+"==========");
					for(TextDetection text: textDetections) { 
						resultList.add("Detected: " + text.getDetectedText());
						resultList.add("Type:" + text.getType());}
						resultList.add(" ");
				}
					resultList.add("------");
				return resultList;
		}
		catch(AmazonRekognitionException e){
			e.printStackTrace();
		}
		return resultList;

	}
	public static void main(String[] args) throws FileNotFoundException {
		//Prepare a list to save images
		List<String> imageList = new ArrayList<String>();
		//Prepare a status to know whether we should continue to get the message from sqs
		String Astatus = "stillRun"; //when it becomes "-1", the loop will end.
		
		//create a sqs
		AmazonSQS sqs = AmazonSQSClientBuilder.defaultClient();
		
		//get the message from sqs	
		while(!Astatus.equals("-1")) {
			
			List<Message> messages = sqs.receiveMessage("https://sqs.us-east-1.amazonaws.com/333516920379/MyQueue1615875981231").getMessages();
			System.out.println(messages);
			
			for (Message picIndex : messages) {
				String pic = picIndex.getBody();//get all the message body
				System.out.println(pic);
				if(pic.equals("-1")) {
					Astatus = "-1";
					break;
				}else {
				imageList.add(pic);//save the images}
			}
		}
		}
		
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
		String s3Bucket = "njit-cs-643";
		
		//make a txt
		String path = "/home/ec2-user/";
		String filepath = path + "output.txt";
		File file1 = new File(filepath);
		PrintStream ps = new PrintStream(file1); 
		System.setOut(ps);
		
	for(String i : imageList) { 
			try {
				List<String> result = detect(rekognitionClient, i, s3Bucket); //text recognition
				for(String r: result) {
					ps.println(r);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}

	}

}
