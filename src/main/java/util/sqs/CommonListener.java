package util.sqs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
@Slf4j
public class CommonListener {

	@Value("${thread-pool.service-size}")
	private int servicePS;

	@Autowired
	private SQSPollingService sqsPollingService;

	private boolean active;
	private ExecutorService executorService;


	@PostConstruct
	public void serviceListener() {

		executorService = Executors.newFixedThreadPool(servicePS);
		active = true;

		BlockingQueue<String> queue = sqsPollingService.getMessageBlockingQueue();

		new Thread(() -> {
			while (active) {
				try {
					String message = queue.take();
					log.info("body :" + message);
					//executorService.execute(() -> queueServing.call(message));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			//  종료 되기전에 처리 해야할 로직
			if (!queue.isEmpty()) {

			}

		}).start();
	}


/*	@PreDestroy
	private void destroy() {
		active = false;
		log.info("Listener Service STOP");
	}*/

}
