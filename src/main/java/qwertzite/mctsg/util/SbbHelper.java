package qwertzite.mctsg.util;

import java.util.Random;

import net.minecraft.block.BlockColored;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import qwertzite.mctsg.api.BuildingContext;

public class SbbHelper {
	public static StructureBoundingBox create(int x, int y, int z, int offX, int offY, int offZ, int width, int height, int length, EnumFacing facing) {
		switch(facing) {
		case SOUTH: return new StructureBoundingBox(x+offX         , y+offY, z+offZ         , x+offX+width -1, y+offY+height-1, z+offZ+length-1);
		case NORTH: return new StructureBoundingBox(x-offX-width +1, y+offY, z-offZ-length+1, x-offX         , y+offY+height-1, z-offZ         );
		case EAST:  return new StructureBoundingBox(x+offZ         , y+offY, z-offX-width +1, x+offZ+length-1, y+offY+height-1, z-offX         );
		case WEST:  return new StructureBoundingBox(x-offZ-length+1, y+offY, z+offX         , x-offZ         , y+offY+height-1, z+offX+width -1);
		default:    return new StructureBoundingBox(x+offX         , y+offY, z+offZ         , x+offX+width -1, y+offY+height-1, z+offZ+length-1);
		}
	}
	
	public static void markSbbRegion(BuildingContext context, StructureBoundingBox sbb, int y, Random rand) {
		EnumDyeColor colour = EnumDyeColor.byMetadata(rand.nextInt(16));
		context.fill(sbb.minX, y, sbb.minZ, sbb.maxX, y, sbb.maxZ, Blocks.WOOL.getDefaultState().withProperty(BlockColored.COLOR, colour));
	}
}
