package config;

import javax.jms.JMSException;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnection;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;

@Configuration
public class Config {
    @Bean
    public BasicAWSCredentials basicAWSCredentials(@Value("${aws.access-key}") String awsAccessKey,
            @Value("${aws.secret-key}") String awsSecretKey) {
        return new BasicAWSCredentials(awsAccessKey, awsSecretKey);
    }

    @Bean
    public AmazonSQS amazonSQS(@Value("${aws.regions}") String regions,
            @Qualifier("basicAWSCredentials") BasicAWSCredentials basicAWSCredentials) {
        return AmazonSQSClientBuilder.standard().withRegion(regions)
                .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials)).build();
    }

    @Bean
    public SQSConnection sqsConnection(@Qualifier("amazonSQS") AmazonSQS amazonSQS) throws JMSException {
        return new SQSConnectionFactory(new ProviderConfiguration(), amazonSQS).createConnection();
    }
}
