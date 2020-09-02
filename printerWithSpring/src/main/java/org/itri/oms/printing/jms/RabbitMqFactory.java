package org.itri.oms.printing.jms;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownSignalException;

@Component("rabbitMqFactory")
public class RabbitMqFactory {
	
	Logger logger = LoggerFactory.getLogger(RabbitMqFactory.class);
	private ConnectionFactory _factory;
	private Connection _connection;
	@Value("${rabbitMq.ip}")
	private String _host;
	@Value("${rabbitMq.vhost}")
	private String _vhost;
	@Value("${rabbitMq.userName}")
	private String _userName;
	@Value("${rabbitMq.password}")
	private String _password;
	private static RabbitMqFactory _RabbitMqFactory;
	
	public RabbitMqFactory(){
		
		
	}
	
	public static RabbitMqFactory getRabbitMqFactory(){
		if(_RabbitMqFactory == null){
			_RabbitMqFactory = new RabbitMqFactory();
		}
		return _RabbitMqFactory;
	}
	
//	public  RabbitMqFactory(String host,String userName,String password,int hearbeat){
//		_host = host;
//		_userName = userName;
//		_password = password;
//		
//		_factory = new ConnectionFactory();
//		
//		_factory.setHost(_host);
//		_factory.setUsername(_userName);
//		_factory.setPassword(_password);
//		_factory.setRequestedHeartbeat(hearbeat);
//	}
	
	public Connection getConnection() throws IOException, TimeoutException{
		if(_connection == null || !_connection.isOpen()){
			if(_factory == null) {
				_factory = new ConnectionFactory();
				_factory.setHost(_host);
				_factory.setUsername(_userName);
				_factory.setVirtualHost(_vhost);
				_factory.setPassword(_password);
				_factory.setRequestedHeartbeat(20);
			}
			_connection = _factory.newConnection();
			this._connection.addShutdownListener((ShutdownSignalException cause) -> {
				try {
					_factory.newConnection();
//					RabbitMqListenContainer.createQueueChannels();
				} catch (Exception e) {
					logger.error(e.toString(), e);
					try {
						Thread.sleep(60000);
						getConnection();
					} catch (Exception e1) {
						logger.error(e1.toString(), e1);
					}
				}
			});
		}
		return _connection;
	}
	
	public Connection getConnection(String vhost) throws IOException, TimeoutException{
		if(_connection == null || !_connection.isOpen()){
			if(_factory == null) {
				_factory = new ConnectionFactory();
				_factory.setHost(_host);
				_factory.setUsername(_userName);
				_factory.setVirtualHost(vhost);
				_factory.setPassword(_password);
				_factory.setRequestedHeartbeat(20);
			}
			_connection = _factory.newConnection();
			this._connection.addShutdownListener((ShutdownSignalException cause) -> {
				try {
					_factory.newConnection();
//					RabbitMqListenContainer.createQueueChannels();
				} catch (Exception e) {
					logger.error(e.toString(), e);
					try {
						Thread.sleep(60000);
						getConnection();
					} catch (Exception e1) {
						logger.error(e1.toString(), e1);
					}
				}
			});
		}
		return _connection;
	}

	public String get_host() {
		return _host;
	}

	public void set_host(String _host) {
		this._host = _host;
	}

	
	public String get_vhost() {
		return _vhost;
	}

	public void set_vhost(String _vhost) {
		this._vhost = _vhost;
	}
	public String get_userName() {
		return _userName;
	}

	public void set_userName(String _userName) {
		this._userName = _userName;
	}

	public String get_password() {
		return _password;
	}

	public void set_password(String _password) {
		this._password = _password;
	}
	
	
}
