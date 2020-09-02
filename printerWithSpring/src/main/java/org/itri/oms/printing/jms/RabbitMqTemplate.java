package org.itri.oms.printing.jms;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;

@Component("rabbitMqTemplate")
public class RabbitMqTemplate {
	Logger logger = LoggerFactory.getLogger(RabbitMqTemplate.class);
	
	@Autowired
	private RabbitMqFactory rabbitMqFactory;
	private String toWorkStationExchange;
	private static RabbitMqTemplate _RabbitMqTemplate;
	
	public RabbitMqTemplate(){
		rabbitMqFactory = RabbitMqFactory.getRabbitMqFactory();
	}
	
	public static RabbitMqTemplate getRabbitMqTemplate(){
		if(_RabbitMqTemplate == null){
			_RabbitMqTemplate = new RabbitMqTemplate();
		}
		return _RabbitMqTemplate;
	}
	
	public void sendTextMessage(String exchangeName,String pattern,String jsonText,Map<String, Object> properties){
		Channel channel;
		try {
			channel = rabbitMqFactory.getConnection().createChannel();
			channel.exchangeDeclare(exchangeName, "topic");
			BasicProperties props = new BasicProperties.Builder().headers(properties).build();
			channel.basicPublish(exchangeName, pattern, props, jsonText.getBytes(StandardCharsets.UTF_8));
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.toString(), e);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	public void sendPrintMessage(String queueName,byte[] data,Map<String, Object> properties){
		Channel channel;
		try {
			channel = rabbitMqFactory.getConnection().createChannel();
			Map<String, Object> args = new HashMap<String, Object>();
			channel.queueDeclare(queueName, false, false, false, args);
			BasicProperties props = new BasicProperties.Builder().headers(properties).build();
			channel.basicPublish("", queueName, props, data);
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.toString(), e);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
	}
	
	public void sendTextMessage(String queueName, final String jsonText, final Map<String, Object> properties) {
		Channel channel;
		try {
			channel = rabbitMqFactory.getConnection().createChannel();
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("x-message-ttl", 3000);
			channel.queueDeclare(queueName, false, false, false, args);
			BasicProperties props = new BasicProperties.Builder().headers(properties).build();
			channel.basicPublish("", queueName, props, jsonText.getBytes());
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error(e.toString(), e);
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
	}

	public RabbitMqFactory getRabbitMqFactory() {
		return rabbitMqFactory;
	}

	public void setRabbitMqFactory(RabbitMqFactory rabbitMqFactory) {
		this.rabbitMqFactory = rabbitMqFactory;
	}

	public String getToWorkStationExchange() {
		return toWorkStationExchange;
	}

	public void setToWorkStationExchange(String toWorkStationExchange) {
		this.toWorkStationExchange = toWorkStationExchange;
	}

	
	
}
