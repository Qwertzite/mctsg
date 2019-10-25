package qwertzite.mctsg.util.math;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class Matrix {
	public static final Matrix E =
			new Matrix(
					1, 0, 0, 0,
					0, 1, 0, 0,
					0, 0, 1, 0,
					0, 0, 0, 1,
					false, Rotation.NONE);

	public final int xx, xy, xz, xw;
	public final int yx, yy, yz, yw;
	public final int zx, zy, zz, zw;
	public final int wx, wy, wz, ww;
	
//	public final boolean mirror;
	public final Rotation rotation;

	public Matrix(
			int xx, int xy, int xz, int xw,
			int yx, int yy, int yz, int yw,
			int zx, int zy, int zz, int zw,
			int wx, int wy, int wz, int ww,
			Rotation rotation) {
		this(	xx, xy, xz, xw,
				yx, yy, yz, yw,
				zx, zy, zz, zw,
				wx, wy, wz, ww,
				false, rotation);
	}
	
	private Matrix(
			int xx, int xy, int xz, int xw,
			int yx, int yy, int yz, int yw,
			int zx, int zy, int zz, int zw,
			int wx, int wy, int wz, int ww,
			boolean mirror, Rotation rotation) {
		this.xx = xx;
		this.xy = xy;
		this.xz = xz;
		this.xw = xw;

		this.yx = yx;
		this.yy = yy;
		this.yz = yz;
		this.yw = yw;

		this.zx = zx;
		this.zy = zy;
		this.zz = zz;
		this.zw = zw;

		this.wx = wx;
		this.wy = wy;
		this.wz = wz;
		this.ww = ww;
		
//		this.mirror = mirror;
		this.rotation = rotation;
	}

	public static Matrix translate(int x, int y, int z) {
		return new Matrix(
				1, 0, 0, x,
				0, 1, 0, y,
				0, 0, 1, z,
				0, 0, 0, 1,
				false, Rotation.NONE);
	}
	
	public static Matrix rotate(Rotation r) {
		switch(r) {
		default:
		case NONE:
			return E;
		case CLOCKWISE_90:
			return new Matrix(
					0, 0,-1, 0,
					0, 1, 0, 0,
					1, 0, 0, 0,
					0, 0, 0, 1,
					false, r);
		case CLOCKWISE_180:
			return new Matrix(
					-1, 0, 0, 0,
					 0, 1, 0, 0,
					 0, 0,-1, 0,
					 0, 0, 0, 1,
					 false, r);
		case COUNTERCLOCKWISE_90:
			return new Matrix(
					 0, 0, 1, 0,
					 0, 1, 0, 0,
					-1, 0, 0, 0,
					 0, 0, 0, 1,
					 false, r);
		}
	}
	
//	public static Matrix mirror() {
//		return new Matrix(
//				-1, 0, 0, 0,
//				 0, 1, 0, 0,
//				 0, 0, 1, 0,
//				 0, 0, 0, 1,
//				 true, Rotation.NONE);
//	}
	
	public int[] apply(int[] arrayIn) {
		return new int[] {
				this.xx * arrayIn[0] + this.xy * arrayIn[1] + this.xz * arrayIn[2] + this.xw,
				this.yx * arrayIn[0] + this.yy * arrayIn[1] + this.yz * arrayIn[2] + this.yw,
				this.zx * arrayIn[0] + this.zy * arrayIn[1] + this.zz * arrayIn[2] + this.zw
		};
	}
	
	public int[] apply(int x, int y, int z) {
		return new int[] {
				this.xx * x + this.xy * y + this.xz * z + this.xw,
				this.yx * x + this.yy * y + this.yz * z + this.yw,
				this.zx * x + this.zy * y + this.zz * z + this.zw
		};
	}
	
	public BlockPos apply(BlockPos pos) {
		return new BlockPos(
				this.xx * pos.getX() + this.xy * pos.getY() + this.xz * pos.getZ() + this.xw,
				this.yx * pos.getX() + this.yy * pos.getY() + this.yz * pos.getZ() + this.yw,
				this.zx * pos.getX() + this.zy * pos.getY() + this.zz * pos.getZ() + this.zw
				);
	}
	
	public IBlockState apply(IBlockState state) {
		return state.withRotation(this.rotation);
	}
	
	public Matrix mult(Matrix other) {
		return new Matrix(
				this.xx*other.xx + this.xy*other.yx + this.xz*other.zx + this.xw*other.wx,
				this.xx*other.xy + this.xy*other.yy + this.xz*other.zy + this.xw*other.wy,
				this.xx*other.xz + this.xy*other.yz + this.xz*other.zz + this.xw*other.wz,
				this.xx*other.xw + this.xy*other.yw + this.xz*other.zw + this.xw*other.ww,
				
				this.yx*other.xx + this.yy*other.yx + this.yz*other.zx + this.yw*other.wx,
				this.yx*other.xy + this.yy*other.yy + this.yz*other.zy + this.yw*other.wy,
				this.yx*other.xz + this.yy*other.yz + this.yz*other.zz + this.yw*other.wz,
				this.yx*other.xw + this.yy*other.yw + this.yz*other.zw + this.yw*other.ww,

				this.zx*other.xx + this.zy*other.yx + this.zz*other.zx + this.zw*other.wx,
				this.zx*other.xy + this.zy*other.yy + this.zz*other.zy + this.zw*other.wy,
				this.zx*other.xz + this.zy*other.yz + this.zz*other.zz + this.zw*other.wz,
				this.zx*other.xw + this.zy*other.yw + this.zz*other.zw + this.zw*other.ww,

				this.wx*other.xx + this.wy*other.yx + this.wz*other.zx + this.ww*other.wx,
				this.wx*other.xy + this.wy*other.yy + this.wz*other.zy + this.ww*other.wy,
				this.wx*other.xz + this.wy*other.yz + this.wz*other.zz + this.ww*other.wz,
				this.wx*other.xw + this.wy*other.yw + this.wz*other.zw + this.ww*other.ww,
//				this.mirror^other.mirror, this.rotation.add(this.mirror ? other.rotation : getOtherwise(other.rotation)));
				this.rotation.add(other.rotation));
	}
	
	public void print(String note) {
		System.out.println(note);
		this.print();
	}
	
	public void print() {
		System.out.println(String.format("%d,%d,%d,%d", this.xx, this.xy, this.xz, this.xw));
		System.out.println(String.format("%d,%d,%d,%d", this.yx, this.yy, this.yz, this.yw));
		System.out.println(String.format("%d,%d,%d,%d", this.zx, this.zy, this.zz, this.zw));
		System.out.println(String.format("%d,%d,%d,%d", this.wx, this.wy, this.wz, this.ww));
	}
	
//	private static Rotation getOtherwise(Rotation rot) {
//		switch(rot) {
//		case CLOCKWISE_180:
//			return Rotation.CLOCKWISE_180;
//		case CLOCKWISE_90:
//			return Rotation.CLOCKWISE_90;
//		case COUNTERCLOCKWISE_90:
//			return Rotation.CLOCKWISE_90;
//		case NONE:
//			return Rotation.NONE;
//		default:
//			return rot;
//		}
//	}
}