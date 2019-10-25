package qwertzite.mctsg.nativeplugin.mcvillage;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import qwertzite.mctsg.api.BuildingContext;

public class Road {
	public int posX;
	public int posZ;
	public StructureBoundingBox structureBoundingBox;
	
	public int length;
	public int width;
	public EnumFacing facing;
	public IBlockState state;
	
	/**
	 * 
	 * @param posX
	 * @param posZ
	 * @param length
	 * @param width
	 * @param facing 親の道がつながる方向．つまり，伸びるのは逆方向
	 * @param state
	 */
	public Road(StructureBoundingBox structureBoundingBox, int posX, int posZ, int length, int width, EnumFacing facing, IBlockState state) {
		this.posX = posX;
		this.posZ = posZ;
		this.structureBoundingBox = structureBoundingBox;
		this.length = length;
		this.width = width;
		this.facing = facing;
		this.state = state;
	}
	
	/**
	 * call this method after addBuildings as this method may change the length.
	 * @param context
	 */
	public void generate(BuildingContext context) {
		context.pushMatrix();
		context.translate(this.posX, 0, this.posZ);
		context.rotate(this.getRotation());
//		context.setBlockState(new BlockPos(0, -1, 0), state);
//		context.setBlockState(new BlockPos(1, -1, 0), state);
//		context.setBlockState(new BlockPos(0, -1, 1), state);
//		context.setBlockState(new BlockPos(0, -1, 2), state);
		context.fill(0, -1, 0, this.width-1, -1, this.length-1, this.state);
		context.popMatrix();
	}
	
	protected Rotation getRotation() {
		switch(this.facing) {
		case SOUTH: return Rotation.NONE;
		case WEST:  return Rotation.CLOCKWISE_90;
		case NORTH: return Rotation.CLOCKWISE_180;
		case EAST:  return Rotation.COUNTERCLOCKWISE_90;
		default: throw new IllegalArgumentException(this.facing + " is not a valid facing for component.");
		}
	}
	
	/**
	 * sets the length and modifies structure bouding box.
	 * @param len
	 */
	public void resetRoadLength(int len) {
		if (this.length == len) { return; }
		this.length = len;
		switch(this.facing) {
		case EAST:
			this.structureBoundingBox.maxX = this.posX + len - 1;
			break;
		case NORTH:
			this.structureBoundingBox.minZ = this.posZ - len + 1;
			break;
		case SOUTH:
			this.structureBoundingBox.maxZ = this.posZ + len - 1;
			break;
		case WEST:
			this.structureBoundingBox.minX = this.posX - len + 1;
			break;
		default:
		}
	}
}
