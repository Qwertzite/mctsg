package qwertzite.mctsg.api;

import net.minecraft.world.gen.structure.StructureBoundingBox;

public class BuildArea {
	private final EnumFillPolicy policy;
	private final int minX;
	private final int minZ;
	private final int maxX;
	private final int maxZ;
	
	public BuildArea(EnumFillPolicy policy, int minX, int minZ, int maxX, int maxZ) {
		this.policy = policy;
		this.minX = minX;
		this.minZ = minZ;
		this.maxX = maxX;
		this.maxZ = maxX;
	}
	
	public boolean canFitIn(StructureBoundingBox sbb) {
		switch(this.getPolicy()) {
		case CONTAIN:   return !(sbb.minX < this.getMinX() || sbb.maxX > this.getMaxX() || sbb.minZ < this.getMinZ() || sbb.maxZ > this.getMaxZ());
		case COVER: return !(sbb.minX > this.getMaxX() || sbb.maxX < this.getMinX() || sbb.minZ > this.getMaxZ() || sbb.maxZ < this.getMinZ());
		default: return false;
		}
	}
	
	public EnumFillPolicy getPolicy() { return this.policy; }
	public int getMinX() { return this.minX; }
	public int getMinZ() { return this.minZ; }
	public int getMaxX() { return this.maxX; }
	public int getMaxZ() { return this.maxZ; }
}
