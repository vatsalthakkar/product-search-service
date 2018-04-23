package com.kkd.productsearchservice.services;

import com.kkd.productsearchservice.ProductSearchServiceApplication;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

//a service class that retrieves the messages from the rabbitmq server queue
@Service
public class RetrieveMessagesFromGenericQueue {
	// to show the logs
	private static final Logger log = LoggerFactory.getLogger(ProductSearchServiceApplication.class);

	// listing to generic queue
	@RabbitListener(queues = ProductSearchServiceApplication.QUEUE_GENERIC_NAME)
	public void receiveMessage(final Message message) {
		// retrieving the messages and storing them into a log.txt file
		File file = new File("./logs.txt");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter fw = null;
		BufferedWriter bw = null;
		try {
			// writing in file
			fw = new FileWriter(file, true);
			bw = new BufferedWriter(fw);
			bw.write(message.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// closing the opened resources
		finally {
			try {
				if (bw != null) {
					bw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		log.info("Received message as generic: {}", message.toString());
	}
}