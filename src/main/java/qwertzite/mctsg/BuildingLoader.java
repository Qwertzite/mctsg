package qwertzite.mctsg;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import qwertzite.mctsg.api.BuildingSupplier;
import qwertzite.mctsg.api.IBuildingEntry;
import qwertzite.mctsg.api.IFormatEntry;
import qwertzite.mctsg.api.ITSGPlugin;
import qwertzite.mctsg.util.ReloadResult;

public class BuildingLoader {
	// 	起動時にのみ
	private static List<IFormatEntry<? extends IBuildingEntry>> formats;

	public static Map<File, IBuildingEntry> buildings = new HashMap<>();
	/** will be removed in version 2.0 */
	public static Object2IntMap<String> weightOverride = Object2IntMaps.emptyMap();

	private BuildingLoader() {}
	
	public static void onInit() {
		formats = new LinkedList<IFormatEntry<? extends IBuildingEntry>>();
		for (ITSGPlugin p : MctsgPluginLoader.getPlugins()) {
			p.registerFormat(BuildingLoader::register);
		}
		reloadResources();
	}
	
	private static void register(IFormatEntry<? extends IBuildingEntry> entry) {
		formats.add(entry);
	}
	
	/**
	 * 	建物を読み込む．コマンド，起動時に呼ばれる．
	 */
	public static ReloadResult reloadResources() {
		buildings.clear();
		ReloadResult result = new ReloadResult();
		File d = MctsgResources.getBuildingFileLocation();
		recursiveLoading(d, result);
		
		/** Folder, JsonObj */
		Map<File, JsonObject> settings = new HashMap<>();
		JsonObject baseJsonObj = MctsgResources.getBuildingSetting(MctsgResources.BUILDINGS);
		if (baseJsonObj != null) {
			for (Map.Entry<File, IBuildingEntry> e : buildings.entrySet()) {
				File folder = e.getKey().getParentFile();
				String buildingName = MctsgResources.buildingFileToString(e.getKey());
				
				if (!settings.containsKey(folder)) { settings.put(folder, MctsgResources.getBuildingSetting(folder)); }
				JsonObject folderSetting = settings.get(folder);
				
				JsonObject specific;
				if (folderSetting.has(buildingName)) { specific = folderSetting.getAsJsonObject(buildingName); }
				else {
					if (baseJsonObj.has(buildingName)) {
						specific = baseJsonObj.getAsJsonObject(buildingName);
						baseJsonObj.remove(buildingName);
					} else {
						specific = new JsonObject();
					}
					folderSetting.add(buildingName, specific);
				}
				e.getValue().loadSettings(specific);
			}
			for (Map.Entry<File, JsonObject> e : settings.entrySet()) {
				MctsgResources.saveBuildingSetting(e.getValue(), e.getKey(), true);
			}
		}
		{
			JsonObject override = new JsonObject();
			for (Map.Entry<File, IBuildingEntry> e : buildings.entrySet()) {
				int weight = e.getValue().getDefaultWeight();
				override.addProperty(MctsgResources.buildingFileToString(e.getKey()), weight);
			}
			MctsgResources.saveDefaultWeight(override);
			result.generatedWeightTemplate = true;
		}
		ModLog.info("Loaded {} buildings.", result.successed);
		if (result.generatedWeightTemplate) ModLog.info("Generated building weight file template.");
		return result;
	}

	private static void recursiveLoading(File dir, ReloadResult result) {
		File[] c = dir.listFiles();
		for (File f : c) {
			if (f.isDirectory()) {
				recursiveLoading(f, result);
			} else if (f.isFile()) {
				processFile(f, result);
			}
		}
	}
	
	private static boolean processFile(File f, ReloadResult result) {
		for (IFormatEntry<?> e : formats) {
			if (e.checkSuffix(f)) {
				IBuildingEntry building = e.loadFromFile(f);
				if (building != null) {
					buildings.put(f, building);
					result.success();
					return true;
				}
			}
		}
		ModLog.info("Failed to load file {}", f.toString());
		result.failed(f.toString());
		return false;
	}
	
	public static BuildingSupplier getNewSupplier(Random rand) {
		Object2IntMap<IBuildingEntry> override = new Object2IntOpenHashMap<>();
		if (!weightOverride.isEmpty()) {
			for (Map.Entry<File, IBuildingEntry> e : buildings.entrySet()) {
				String stringKey = MctsgResources.buildingFileToString(e.getKey());
				if (weightOverride.containsKey(stringKey)) {
					override.put(e.getValue(), weightOverride.getInt(stringKey));
				}
			}
		}
		return new BuildingSupplier(buildings.values(), rand, override);
	}
}
