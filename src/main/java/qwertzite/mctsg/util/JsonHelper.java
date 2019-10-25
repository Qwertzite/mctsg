package qwertzite.mctsg.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class JsonHelper {
	private JsonHelper() {}
	
	public static boolean getBoolean(JsonObject json, String key, boolean fallback, ICommandSender sender) {
		try {
			return getBoolean(json, key, fallback);
		} catch (Exception e) {
			sender.sendMessage(new TextComponentString("Illegal value for " + key + ". Has to be boolean.").setStyle((new Style()).setColor(TextFormatting.RED)));
			throw e;
		}
	}
	
	public static boolean getBoolean(JsonObject json, String key, boolean fallback) {
		if (json.has(key)) {
			return json.get(key).getAsBoolean();
		} else {
			json.add(key, new JsonPrimitive(fallback));
			return fallback;
		}
	}
	
	public static int getInt(JsonObject json, String key, int fallback, ICommandSender sender) {
		try {
			return getInt(json, key, fallback);
		} catch (Exception e) {
			sender.sendMessage(new TextComponentString("Illegal value for " + key + ". Has to be int.").setStyle((new Style()).setColor(TextFormatting.RED)));
			throw e;
		}
	}
	
	/**
	 * Gets the value from the JsonObject if it has the key.
	 * If not, add fallback value to the json object and returns the fallback value.
	 * @param json
	 * @param key
	 * @param fallback
	 * @return
	 */
	public static int getInt(JsonObject json, String key, int fallback) {
		if (json.has(key)) {
			return json.get(key).getAsInt();
		} else {
			json.add(key, new JsonPrimitive(fallback));
			return fallback;
		}
	}
	
	public static String getString(JsonObject json, String key, String fallback, ICommandSender sender) {
		try {
			return getString(json, key, fallback);
		} catch (Exception e) {
			sender.sendMessage(new TextComponentString("Illegal value for " + key + ". Must be String.").setStyle((new Style()).setColor(TextFormatting.RED)));
			throw e;
		}
	}
	
	public static String getString(JsonObject json, String key, String fallback) {
		if (json.has(key)) {
			return json.get(key).getAsString();
		} else {
			json.addProperty(key, fallback);
			return fallback;
		}
	}
}
