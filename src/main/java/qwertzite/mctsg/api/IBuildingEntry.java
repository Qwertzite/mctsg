package qwertzite.mctsg.api;

import com.google.gson.JsonObject;

/**
 * 	建物の種類を表す．
 * @author Qwertzite
 * 
 * 2019/09/08
 */
public interface IBuildingEntry {
	
	/**
	 * load settings for this building.
	 * @param obj
	 * @return true on success.
	 */
	public boolean loadSettings(JsonObject obj);
	
	public boolean isEnabled();
	
	public boolean hasLimit();
	/** The maximum number of this type of building. */
	public int getLimit();
	/** Get the weight for randomly selection buildings. Defaults to 100. */
	public int getWeight();
	/** 	建物の道に平行な向きの大きさ */
	public int getWidth();
	/** 	建物の道に直角な向きの大きさ */
	public int getLength();
	/** 	建物のy方向の大きさ */
	public int getHeight();
	/** 	建物の深さ，マイナスが地下，プラスが宙に浮いた状態 */
	public int getDepth();
	
	/**
	 * 	実際に建物を建てる時の
	 * @param context
	 */
	public void generateBuilding(BuildingContext context);

}
