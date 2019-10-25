package qwertzite.mctsg;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonObject;

import qwertzite.mctsg.api.BuildingSupplier;
import qwertzite.mctsg.api.IBuildingEntry;
import qwertzite.mctsg.api.IFormatEntry;
import qwertzite.mctsg.api.ITSGPlugin;
import qwertzite.mctsg.util.ReloadResult;

public class BuildingLoader {
	// 	起動時にのみ
	private static List<IFormatEntry<? extends IBuildingEntry>> formats;

	public static Map<File, IBuildingEntry> buildings = new HashMap<>();

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
		JsonObject jsonObj = MctsgResources.getBuildingSettingFile();
		if (jsonObj != null) {
			for (Map.Entry<File, IBuildingEntry> e : buildings.entrySet()) {
				String name = e.getKey().getPath().substring(2 + MctsgResources.MAIN_DIR_NAME.length() + 1 + MctsgResources.BUILDINGS_DIR_NAME.length() + 1);
				if (!jsonObj.has(name)) {
					JsonObject specific = new JsonObject();
					jsonObj.add(name, specific);
				}
				e.getValue().loadSettings(jsonObj.getAsJsonObject(name));				
			}
			MctsgResources.saveBuildingSettingFile(jsonObj, true);
		}
		ModLog.info("Loaded {} buildings.", result.successed);
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
		return new BuildingSupplier(buildings.values(), rand);
	}
}
