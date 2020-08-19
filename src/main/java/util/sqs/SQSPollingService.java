package util.sqs;

import java.util.concurrent.*;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import com.amazon.sqs.javamessaging.SQSConnection;

import lombok.Data;
import lombok.extern.log4j.Log4j;


@Log4j
@Data
public class SQSPollingService {

    @Value("${aws.sqs.message-name}")
    private String sqsRequestQueueName;

    @Autowired
    private SQSConnection sqsConnection;

    BlockingQueue<String> messageBlockingQueue = new ArrayBlockingQueue<>(1024);

    @PostConstruct
    private void polling() throws JMSException {
        Session session = sqsConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);

        MessageConsumer consumer = session.createConsumer(session.createQueue(sqsRequestQueueName));
        consumer.setMessageListener(message -> {
            try {
                log.info("Message :: " + ((TextMessage) message).getText());
                messageBlockingQueue.add(((TextMessage) message).getText());
                message.acknowledge();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        sqsConnection.start();

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

   @PreDestroy
    private void destory() {
        try {
            log.info("Collector Service STOP");
            sqsConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

}
