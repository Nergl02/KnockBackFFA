package cz.nerkub.NerKubKnockBackFFA.CustomFiles;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class CustomConfig {

		private File file;
		private FileConfiguration customConfig;

		public CustomConfig(String directory,String fileName, Plugin plugin){
			if (directory == null || directory.isEmpty()) {
				file = new File(plugin.getDataFolder(), fileName);
			} else {
				File dir = new File(plugin.getDataFolder(), directory);
				if (!dir.exists()) {
					dir.mkdirs();
				}

				file = new File(dir, fileName);
			}

			if (!file.exists()) {
				try (InputStream in = plugin.getResource(fileName)) {
					if (in != null) {
						Files.copy(in, file.toPath());
					} else {
						file.createNewFile();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			customConfig = YamlConfiguration.loadConfiguration(file);

		}

		public FileConfiguration getConfig() {
			return customConfig;
		}

		public void saveConfig() {
			try {
				customConfig.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void reloadConfig() {
			customConfig = YamlConfiguration.loadConfiguration(file);
		}

	}

