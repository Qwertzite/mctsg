package qwertzite.mctsg.api;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import qwertzite.mctsg.util.math.Matrix;

public class BuildingContext {
	private World world;
	private Random rand;
	
	private Deque<Matrix> stack = new LinkedList<>();
	private Matrix current = Matrix.E;
	
	public BuildingContext(World world, long seed) {
		this.world = world;
		this.rand = new Random(seed);
	}
	
	public Random getRand() { return this.rand; }
	
	// world editing
	
	public void setBlockState(BlockPos pos, IBlockState state) {
		this.world.setBlockState(this.applyTransform(pos), this.applyRotation(state));
	}
	
	/**
	 * Fills the area inclusively.
	 * @param minX
	 * @param minY
	 * @param minZ
	 * @param maxX
	 * @param maxY
	 * @param maxZ
	 * @param state The new block state.
	 */
	public void fill(int minX, int minY, int minZ, int maxX, int maxY, int maxZ, IBlockState state) {
		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					pos.setPos(x, y, z);
					this.setBlockState(pos, state);
				}
			}
		}
	}
	
	// transforms
	private BlockPos applyTransform(BlockPos pos) {
		return this.current.apply(pos);
	}
	
	private IBlockState applyRotation(IBlockState state) {
		return this.current.apply(state);
	}
	
	public void pushMatrix() {
		stack.push(current);
	}
	
	public void popMatrix() {
		this.current = stack.pop();
	}
	
	public void translate(int x, int y, int z) {
		this.current = this.current.mult(Matrix.translate(x, y, z));
	}
	
	public void rotate(Rotation rotation) {
		this.current = this.current.mult(Matrix.rotate(rotation));
	}
}
