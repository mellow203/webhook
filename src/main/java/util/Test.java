package util;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import service.CampaignActionServiceImpl;

@Slf4j
public class Test {

    public void main() {
        ObjectMapper objectMapper = new ObjectMapper();
        CampaignActionServiceImpl campaignActionService = new CampaignActionServiceImpl(objectMapper);
        /*
         * The ProfileCredentialsProvider will return your [default]
         * credential profile by reading from the credentials file located at
         * (~/.aws/credentials).
         */
        ProfileCredentialsProvider credentialsProvider = new ProfileCredentialsProvider();
        try {
            credentialsProvider.getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }

        AmazonSQS sqs = AmazonSQSClientBuilder.standard()
                .withCredentials(credentialsProvider)
                .withRegion(Regions.AP_NORTHEAST_2)
                .build();

        System.out.println("===========================================");

        try {
            // Create a queue
            //            System.out.println("Creating a new SQS queue called MyQueue.\n");
            //            CreateQueueRequest createQueueRequest = new CreateQueueRequest("MyQueue");
            //            String myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();
            //
            //            // List queues
            //            System.out.println("Listing all queues in your account.\n");
            //            for (String queueUrl : sqs.listQueues().getQueueUrls()) {
            //                System.out.println("  QueueUrl: " + queueUrl);
            //            }
            //            System.out.println();

            //            // Send a message
            //            System.out.println("Sending a message to MyQueue.\n");
            //            sqs.sendMessage(new SendMessageRequest(myQueueUrl, "This is my message text."));

            // Receive messages
            String myQueueUrl = "https://sqs.ap-northeast-2.amazonaws.com/595763884011/webhooking";
            ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(myQueueUrl);
            List<Message> messages = sqs.receiveMessage(receiveMessageRequest).getMessages();
/*
            for (Message message : messages) {
                System.out.println("  Message");
                System.out.println("    MessageId:     " + message.getMessageId());
                System.out.println("    ReceiptHandle: " + message.getReceiptHandle());
                System.out.println("    MD5OfBody:     " + message.getMD5OfBody());
                System.out.println("    Body:          " + message.getBody());
                for (Map.Entry<String, String> entry : message.getAttributes().entrySet()) {
                    System.out.println("  Attribute");
                    System.out.println("    Name:  " + entry.getKey());
                    System.out.println("    Value: " + entry.getValue());
                }
            }*/
////////////////////////////////////////////////////////////////////////////////////////////////////////////////




            messages.stream().forEach(msg -> {
                try {
                    Map<String, Object> msgInfo = objectMapper.readValue(msg.getBody(), new TypeReference<Map<String, Object>>() {});

                        //Action 정보가 있을 때 -> 다른 캠페인6143984302975  활성화하기
                        campaignActionService.campaignStatus();

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            });


            // Delete a message
            System.out.println("Deleting a message.\n");
            String messageReceiptHandle = messages.get(0).getReceiptHandle();
            sqs.deleteMessage(new DeleteMessageRequest(myQueueUrl, messageReceiptHandle));

            //            // Delete a queue
            //            System.out.println("Deleting the test queue.\n");
            //            sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it " +
                    "to Amazon SQS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
        } catch (AmazonClientException ace) {
            System.out.println("Error Message: " + ace.getMessage());
        }
    }
}
