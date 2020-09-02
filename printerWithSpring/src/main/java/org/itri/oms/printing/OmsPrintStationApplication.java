package org.itri.oms.printing;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.itri.oms.printing.jms.RabbitMqFactory;
import org.itri.oms.printing.jms.RabbitMqListenContainer;
import org.itri.oms.printing.jms.Setting;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

@SpringBootApplication
public class OmsPrintStationApplication {
	
	
	private static ConfigurableApplicationContext ctx;
	
	public static void main(String[] args) {
		
		ctx = new SpringApplicationBuilder(OmsPrintStationApplication.class)
                .headless(false).run(args);
		
		Environment env = ctx.getBean(Environment.class);
		
//		if(args[0] != null){
//			changeQueueName(ctx,args[0]);
//		}

		try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PrinterCombo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PrinterCombo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PrinterCombo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PrinterCombo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
		
		
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	try {
            		RabbitMqFactory rabbitMqFactory = (RabbitMqFactory) ctx.getBean("rabbitMqFactory");
            		RabbitMqListenContainer rabbitMqListenContainer = (RabbitMqListenContainer) ctx.getBean("rabbitMqListenContainer");
            		Setting setting = (Setting)ctx.getBean("setting");
            		if(!setting.loadSetting()){
            			setting.initSetting(
            					env.getProperty("oms.rest.ip"),
            					rabbitMqFactory.get_host(),
            					rabbitMqFactory.get_userName(),
            					rabbitMqFactory.get_password(),
            					rabbitMqListenContainer.get_queue(),
            					rabbitMqFactory.get_vhost());
            		}
            		rabbitMqFactory.set_host(setting.getSettingMap().get(Setting._KEY_QUEUE_IP).toString());
            		rabbitMqFactory.set_vhost(setting.getSettingMap().get(Setting._KEY_VHOST).toString());
            		rabbitMqFactory.set_userName(setting.getSettingMap().get(Setting._KEY_QUEUE_USER).toString());
            		rabbitMqFactory.set_password(setting.getSettingMap().get(Setting._KEY_QUEUE_PW).toString());
            		rabbitMqListenContainer.set_queue(setting.getSettingMap().get(Setting._KEY_QUEUE_NAME).toString());
            		
            		final PrinterCombo printerCombo = new PrinterCombo(setting,rabbitMqListenContainer);
            		//final PrinterCombo printerCombo = new PrinterCombo(setting);
            		printerCombo.setVisible(true);
            		
                
//                	rabbitMqListenContainer.createQueueChannel();
//					rabbitMqListenContainer.getPrintMqListener().setPrinterCombo(printerCombo);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        });
	}
	
	
	
	
	private static void changeQueueName(ConfigurableApplicationContext ctx,String queueName){
		ConfigurableEnvironment configEnv = ctx.getBean(ConfigurableEnvironment.class);
		Environment env = ctx.getBean(Environment.class);
		MutablePropertySources propertySources = configEnv.getPropertySources();
        Map<String, Object> map = Collections.singletonMap("rabbitMq.printQ.name", queueName);
        propertySources.addFirst(new MapPropertySource("new", map));
        String ip = env.getProperty("rabbitMq.ip");
        String newSystemName = env.getProperty("rabbitMq.printQ.name");
        System.out.println(ip+":"+newSystemName);
	}
	
	public static void restart() {
		try {
			Runtime.getRuntime().exec("java -jar printerWithSpring-0.0.1.jar");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
    }

}
