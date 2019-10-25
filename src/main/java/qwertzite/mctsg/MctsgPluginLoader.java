package qwertzite.mctsg;

import java.util.LinkedList;
import java.util.List;

import qwertzite.mctsg.api.ITSGPlugin;

public class MctsgPluginLoader {
	/**
	 * true if it is OK to register plugins.
	 */
	private static boolean registing = true;
	private static List<ITSGPlugin> plugins = new LinkedList<>();
	
	private MctsgPluginLoader() {}
	
	public static void registerPlugin(ITSGPlugin plugin) {
		if (registing) {
			plugins.add(plugin);
		} else {
			throw new IllegalStateException("Plugins must be registered in pre-init state.");
		}
	}
	
	static void setRegistingFinished() {
		registing = false;
	}
	
	static List<ITSGPlugin> getPlugins() {
		if (registing) {
			throw new IllegalStateException("Wait till initialisation!");
		}
		return plugins;
	}
}
