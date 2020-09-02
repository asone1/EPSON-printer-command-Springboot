package org.itri.oms.printing.jms;

import com.rabbitmq.client.QueueingConsumer.Delivery;

public interface RabbitMqListener {
	
	public void onMessage(Delivery delivery);
	
}
