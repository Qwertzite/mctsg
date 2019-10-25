package qwertzite.mctsg.api;

import com.google.gson.JsonObject;

public interface ICityPlanEntry <P extends ICityPlan> {
	public P get();
	/**
	 * Called on initialisation to create plan setting file.
	 * @param json
	 */
	public void cratePlanSetting(JsonObject json);
}
