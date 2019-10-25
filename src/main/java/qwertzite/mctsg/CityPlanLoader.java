package qwertzite.mctsg;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonObject;

import qwertzite.mctsg.api.ICityPlan;
import qwertzite.mctsg.api.ICityPlanEntry;
import qwertzite.mctsg.api.ITSGPlugin;

public class CityPlanLoader {
	private static Map<String, ICityPlanEntry<?>> entries = new HashMap<>();
	private static List<ICityPlanEntry<?>> ordered = new LinkedList<>();
	
	private CityPlanLoader() {}
	
	public static void init() {
		for (ITSGPlugin p : MctsgPluginLoader.getPlugins()) {
			p.registerPlans(CityPlanLoader::addPlan);
		}
		
		for (Entry<String, ICityPlanEntry<?>> e : entries.entrySet()) {
			JsonObject json = MctsgResources.getPlanSetting(e.getKey());
			e.getValue().cratePlanSetting(json);
			MctsgResources.savePlanSetting(json, e.getKey());
		}
	}
	
	public static void addPlan(String id, ICityPlanEntry<?> supplier) {
		CityPlanLoader.entries.put(id, supplier);
		CityPlanLoader.ordered.add(supplier);
	}
	
	/**
	 *  returns new CityPlan instance.
	 */
	public static ICityPlan getCityPlan(String id) {
		if (id == null) { return ordered.get(0).get(); }
		if (entries.containsKey(id)) {
			return entries.get(id).get();
		} else {
			return null;
		}
	}
}
