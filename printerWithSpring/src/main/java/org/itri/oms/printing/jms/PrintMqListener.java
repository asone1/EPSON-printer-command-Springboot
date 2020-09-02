package org.itri.oms.printing.jms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.itri.oms.printing.PrinterCombo;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.rabbitmq.client.BasicProperties;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import localPrint.PrintUsingDotMatrix;
import java.lang.reflect.Type;
import com.google.gson.reflect.TypeToken;

@Component("printMqListener")
public class PrintMqListener implements RabbitMqListener {

	private PrinterCombo printerCombo;

	@Override
	public void onMessage(Delivery delivery) {
		try {
			BasicProperties props = delivery.getProperties();
			if (delivery.getBody().length > 0) {
				/// printerCombo.printDoc(null,delivery.getBody());
				String messageInString = new String(delivery.getBody(), "UTF-8");
				HashMap<String, String> map = (HashMap<String, String>) new Gson().fromJson(messageInString,
						new TypeToken<HashMap<String, String>>() {
						}.getType());
				System.out.println(messageInString);
				new PrintUsingDotMatrix().print((HashMap) map, printerCombo.returnSelectedPrinter());
				// receiveAndPrint(printerCb.getSelectedItem().toString(),queueCb.getSelectedItem().toString());

			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public PrinterCombo getPrinterCombo() {
		return printerCombo;
	}

	public void setPrinterCombo(PrinterCombo printerCombo) {
		this.printerCombo = printerCombo;
	}

}
