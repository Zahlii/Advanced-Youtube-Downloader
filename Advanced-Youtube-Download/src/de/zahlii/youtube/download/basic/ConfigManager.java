package de.zahlii.youtube.download.basic;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;



public class ConfigManager {
	public static final String DS = File.separator;
	public static final File YOUTUBE_DL = new File("youtube-dl.exe");
	public static final File FFMPEG = new File("ffmpeg.exe");

	public static final File TEMP_DIR = new File("temp");

	private static ConfigManager instance;

	private static File configFile = new File("ytload.config");
	private static String encoding = "UTF-8";

	private Map<ConfigKey, String> config;

	private ConfigManager() {
		this.config = new HashMap<ConfigKey, String>();
		loadFile();
	}

	private void loadFile() {

		try {
			if (!configFile.exists())
				configFile.createNewFile();

			byte[] encoded = Files.readAllBytes(Paths.get(configFile.toURI()));
			String[] lines = new String(encoded, encoding).split("\n");
			for (String line : lines) {
				String[] parts = line.split("=");
				if (parts.length < 2)
					continue;
				this.config.put(ConfigKey.valueOf(parts[0]), parts[1]);
			}
		} catch (IOException e) {
			Logging.log("failed loading config file", e);
		}
	}

	private void writeFile() {

		try {
			if (!configFile.exists())
				configFile.createNewFile();

			StringBuilder sb = new StringBuilder();
			for (Entry<ConfigKey, String> e : this.config.entrySet()) {
				sb.append(e.getKey());
				sb.append("=");
				sb.append(e.getValue());
				sb.append("\n");
			}

			FileWriter f2 = new FileWriter(configFile, false);
			f2.write(sb.toString());
			f2.close();

		} catch (IOException e) {
			Logging.log("failed writing config file", e);
		}
	}

	public void setConfig(ConfigKey key, String value) {
		this.config.put(key, value);
		writeFile();
	}

	public static ConfigManager getInstance() {
		if (instance == null)
			instance = new ConfigManager();

		return instance;
	}

	public String getConfig(ConfigKey key, String def) {
		if(!this.config.containsKey(key)) {
			setConfig(key,def);
			return def;
		} else {
			return this.config.get(key);
		}
	}

	public enum ConfigKey {
		AUDIO_BITRATE, DIR_TARGET, FILENAME_CONVENTION, DIR_IMAGES
	}

}
