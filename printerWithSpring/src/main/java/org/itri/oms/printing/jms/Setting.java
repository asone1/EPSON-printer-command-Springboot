package org.itri.oms.printing.jms;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;

@Component("setting")
public class Setting {

	public static String _KEY_SERVER_URL = "serverUrl";
	public static String _KEY_QUEUE_IP = "queueIp";
	public static String _KEY_QUEUE_USER = "queueUser";
	public static String _KEY_QUEUE_PW = "queuePw";
	public static String _KEY_QUEUE_NAME = "queueName";
	public static String _KEY_PRINTER_NAME = "printerName";
	public static String _KEY_VHOST = "hostName";

	private static String _FOLDER = System.getProperty("user.home") + File.separator + "oms";
	private static String _FILE_NAME = "setting.json";
	private String settingString;
	private Map<String, Object> settingMap;
	private Gson gson = new Gson();

	public boolean initSetting(String serverUrl,String queueIp,String queueUser,String queuePw,String queueName, String vhostName) throws IOException {
		settingMap = new HashMap<String, Object>();
		settingMap.put(_KEY_SERVER_URL, serverUrl);
		settingMap.put(_KEY_QUEUE_NAME, queueName);
		settingMap.put(_KEY_QUEUE_IP, queueIp);
		settingMap.put(_KEY_QUEUE_USER, queueUser);
		settingMap.put(_KEY_QUEUE_PW, queuePw);
		settingMap.put(_KEY_VHOST, vhostName);
		return true;
	}

	public void checkFolder() throws IOException {
		Path path = Paths.get(_FOLDER);
		if (!Files.exists(path)) {
			Files.createDirectory(path);
		}
	}

	public boolean loadSetting() throws IOException {
		boolean isSuccess = false;
		checkFolder();
		Path path = Paths.get(_FOLDER + File.separator + _FILE_NAME);
		if (Files.exists(path)) {
			BufferedReader reader;
			reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
			settingMap = gson.fromJson(reader, Map.class);
			settingString = gson.toJson(settingMap);
			isSuccess = true;
			reader.close();
		}
		return isSuccess;
	}

	public boolean saveSetting() throws IOException {
		boolean isSuccess = false;
		checkFolder();
		Path path = Paths.get(_FOLDER + File.separator + _FILE_NAME);
		JsonWriter writer = new JsonWriter(Files.newBufferedWriter(path));
		writer.beginObject();
		settingMap.keySet().stream().forEach(key -> {
			try {
				writer.name(key).value(settingMap.get(key).toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		writer.endObject();
		writer.close();
		return isSuccess;
	}

	public String getSettingString() {
		return settingString;
	}

	public void setSettingString(String settingString) {
		this.settingString = settingString;
	}

	public Map<String, Object> getSettingMap() {
		return settingMap;
	}

	public void setSettingMap(Map<String, Object> settingMap) {
		this.settingMap = settingMap;
	}

}
