package qwertzite.mctsg.api;

import java.util.Map;

import com.google.gson.JsonObject;

import net.minecraft.command.ICommandSender;

public interface ICityPlan {
	/**
	 * City generation will be aborted if false if returned.
	 * @param obj
	 * @return False when failed, or caught an exception.
	 */
	public boolean loadJsonSettings(JsonObject obj, ICommandSender sender);
	public boolean processCommandLineOverrides(Map<String, String> args, ICommandSender sender);
	/**
	 * 
	 * @param context
	 * @param area
	 * @return chat message.
	 */
	public String generate(BuildingContext context, BuildArea area);
}
