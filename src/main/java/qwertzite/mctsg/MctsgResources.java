package qwertzite.mctsg;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public class MctsgResources {
	public static final String JSON_SUFFIX = ".json";

	public static final String MAIN_DIR_NAME = "mctsg";
	public static final String BUILDINGS_DIR_NAME = "buildings";
	public static final String BUILDING_SETTING_NAME = "building_settings" + JSON_SUFFIX;
	public static final String DEFAULT_WEIGHT = "default_weight";
	public static final File MAIN_DIR = new File(".", MAIN_DIR_NAME);
	public static final File BUILDINGS = new File(MAIN_DIR, BUILDINGS_DIR_NAME);
	public static final File BUIDING_SETTINGS = new File(MAIN_DIR, BUILDING_SETTING_NAME);
	
	/** static utility class */
	protected MctsgResources() {}
	
	public static File getBuildingFileLocation() {
		if (!BUILDINGS.exists()) { BUILDINGS.mkdirs(); }
		return BUILDINGS;
	}
	
	/**
	 * 
	 * @param folder the folder that the building file is in.
	 * @return
	 */
	public static JsonObject getBuildingSetting(File folder) {
		return loadJsonObject(getBuildingSettingFile(folder), "building setting file");
	}
	
	public static void saveBuildingSetting(JsonObject jsonObj, File folder, boolean indent) {
		saveJsonObject(getBuildingSettingFile(folder), jsonObj, "building settings");
	}
	
	public static File getBuildingSettingFile(File folder) {
		String str = buildingFolderToString(folder);
		return new File(MAIN_DIR, "building" + str + "_settings" + JSON_SUFFIX);
	}
	
	public static JsonObject getPlanSetting(String name) {
		File f = getPlanSettingFile(name);
		return loadJsonObject(f, "plan setting for " + name);
	}
	
	public static void savePlanSetting(JsonObject obj, String name) {
		File file = getPlanSettingFile(name);
		saveJsonObject(file, obj, "plan settings for " + name);
	}
	
	private static File getPlanSettingFile(String name) {
		return new File(MAIN_DIR, name + JSON_SUFFIX);
	}
	
	public static JsonObject getBuildingWeightOverride(String name) {
		File f = getBuildingWeightFile(name);
		return loadJsonObject(f, "Buiding weight override: " + name, false);
	}
	
	public static boolean defaultFWeightFileExists() {
		return getDefaultWeightFile().exists();
	}
	
	public static void saveDefaultWeight(JsonObject obj) {
		File file = getDefaultWeightFile();
		saveJsonObject(file, obj, "Generating building weight template");
	}
	
	private static File getDefaultWeightFile() {
		return getBuildingWeightFile(DEFAULT_WEIGHT);
	}
	
	private static File getBuildingWeightFile(String name) {
		return new File(MAIN_DIR, name + JSON_SUFFIX);
	}
	
	private static JsonObject loadJsonObject(File file, String name) {
		return loadJsonObject(file, name, true);
	}
	
	private static JsonObject loadJsonObject(File file, String name, boolean createIfNotExtist) {
		if (!file.exists()) {
			if (!createIfNotExtist) return null;
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				ModLog.warn("Failed to create " + name, e);
				return null;
			}
		}
		try (FileInputStream fis = new FileInputStream(file)) {
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			JsonReader jr = new JsonReader(isr);
			jr.setLenient(true);
			JsonElement value = Streams.parse(jr);
			jr.close();
			if (value.isJsonObject()) return value.getAsJsonObject();
			else return new JsonObject();
		} catch (IOException e) {
			ModLog.warn("Failed to load a Json object from " + name, e);
			return null;
		}
	}
	
	/**
	 * 
	 * @param file
	 * @param jsonObj
	 * @param name used for error message.
	 */
	private static void saveJsonObject(File file, JsonObject jsonObj, String name) {
		try {
	  		FileOutputStream fos = new FileOutputStream(file);
	  		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos, "UTF-8"));
	  		JsonWriter jw = new JsonWriter(bw);
	  		if (true) { jw.setIndent("  "); }
		  	Streams.write(jsonObj, jw);
			jw.close();
		} catch (IOException e) {
			ModLog.warn("Could not save " + name, e);
		}
	}
	
	/**
	 * 建物の json obj のkey
	 * @param file
	 * @return
	 */
	public static String buildingFileToString(File file) {
		return file.getPath()
				.substring(2 + MctsgResources.MAIN_DIR_NAME.length() + 1 + MctsgResources.BUILDINGS_DIR_NAME.length() + 1)
				.replace("\\", "/");
	}
	
	public static String buildingFolderToString(File folder) {
		return folder.getPath()
				.substring(2 + MctsgResources.MAIN_DIR_NAME.length() + 1 + MctsgResources.BUILDINGS_DIR_NAME.length())
				.replace("\\", ".");
	}
}
