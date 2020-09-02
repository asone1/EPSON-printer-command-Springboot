package org.itri.oms.printing.jms;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.QueueingConsumer;

@Component("rabbitMqListenContainer")
public class RabbitMqListenContainer {
	static Logger logger = LoggerFactory.getLogger(RabbitMqListenContainer.class);

	@Autowired
	private RabbitMqFactory rabbitMqFactory;
	@Value("${rabbitMq.printQ.name}")
	private String _queue;
	private String _vhost;
	private static Connection _connection;
	@Autowired
	private PrintMqListener printMqListener;


//	public static void createQueueChannels() throws IOException, TimeoutException {
//		_connection = _rabbitMqFactory.getConnection();
//		if (CollectionUtils.isNotEmpty(_queues) && CollectionUtils.isNotEmpty(_listeners)) {
//			int index = 0;
//			for (String queue : _queues) {
//				if (_listeners.get(index) instanceof ContControllerListener || 
//					  _listeners.get(index) instanceof OrderMessageRbListener || 
//					  _listeners.get(index) instanceof AdmMessageRbListener) {
//					createPublishChannel(queue, _listeners.get(index));
//				}
//				index++;
//			}
//		}
//	}
	
	private void createPublishChannel(String exchangeName, RabbitMqListener listener) throws IOException {
		ExecutorService amqpListenerService = null;
		final Channel channel = _connection.createChannel();
		
		channel.exchangeDeclare(exchangeName, "fanout");
		String queueName = channel.queueDeclare(exchangeName, true, false, false, null).getQueue();
//	    String queueName = channel.queueDeclare().getQueue();
	    
	    channel.queueBind(queueName, exchangeName, "");

		final QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, false, consumer);

		amqpListenerService = Executors.newSingleThreadExecutor();
		amqpListenerService.execute(() -> {
			try {
				while (true) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					listener.onMessage(delivery);
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
//				Delay.milliseconds(1000 * 60 * 3);
				try {
					Thread.sleep(1000 * 60 * 3);
					createPublishChannel(exchangeName, listener);
				} catch (Exception e1) {
					logger.error(e.getMessage(), e);
				}
			}
		});
	}

	public void createQueueChannel() throws IOException, TimeoutException {
		if(_connection == null)
			_connection = rabbitMqFactory.getConnection();
		ExecutorService amqpListenerService = null;
		final Channel channel = _connection.createChannel();
		Map<String, Object> args = new HashMap<String, Object>();
//		args.put("x-message-ttl", 3000);
		channel.queueDeclare(this.get_queue(), true, false, false, args);

		final QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(this.get_queue(), false, consumer);

		amqpListenerService = Executors.newSingleThreadExecutor();
		amqpListenerService.execute(() -> {
			try {
				while (true) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					this.printMqListener.onMessage(delivery);
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);

			}
		});
	}
	
	public void createQueueChannel(String vhost,String queue) throws IOException, TimeoutException {
		this._vhost=vhost;
		this._queue=queue;
		if(_connection == null)
			_connection = rabbitMqFactory.getConnection(vhost);
		ExecutorService amqpListenerService = null;
		final Channel channel = _connection.createChannel();
		Map<String, Object> args = new HashMap<String, Object>();
//		args.put("x-message-ttl", 3000);
		channel.queueDeclare(this._queue, true, false, false, args);

		final QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(this._queue, false, consumer);

		amqpListenerService = Executors.newSingleThreadExecutor();
		amqpListenerService.execute(() -> {
			try {
				while (true) {
					QueueingConsumer.Delivery delivery = consumer.nextDelivery();
					this.printMqListener.onMessage(delivery);
					channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);

			}
		});
	}

	public PrintMqListener getPrintMqListener() {
		return printMqListener;
	}

	public void setPrintMqListener(PrintMqListener printMqListener) {
		this.printMqListener = printMqListener;
	}

	public String get_queue() {
		return _queue;
	}

	public void set_queue(String _queue) {
		this._queue = _queue;
	}
	
	public String get_vhost() {
		return _vhost;
	}


}
