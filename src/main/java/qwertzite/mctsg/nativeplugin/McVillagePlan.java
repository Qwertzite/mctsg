package qwertzite.mctsg.nativeplugin;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import qwertzite.mctsg.BuildingLoader;
import qwertzite.mctsg.ModLog;
import qwertzite.mctsg.api.BuildArea;
import qwertzite.mctsg.api.BuildingContext;
import qwertzite.mctsg.api.BuildingSupplier;
import qwertzite.mctsg.api.IBuildingEntry;
import qwertzite.mctsg.api.ICityPlan;
import qwertzite.mctsg.nativeplugin.mcvillage.Road;
import qwertzite.mctsg.util.JsonHelper;
import qwertzite.mctsg.util.SbbHelper;
import qwertzite.mctsg.util.StringParseHelper;

/**
 * 	roadLengthMin は，建物の幅 + 道の幅と同程度以上，あまり小さくすると，道だけで固まってしまう場合がある．
 * 	roadLengthMaxは，建物の奥行の二倍程度+alphaがお勧め．建物の奥行に対し十分に大きくすると，中心に中庭が出来る．
 * なお，roadLengthMaxより大きい建物は生成確率が下がる．
 * 	roadLengthStepは，あまり大きくても不自然になるが，roadLengthMaxに比べ短すぎても，適切な大きさを見つけるまでに時間が掛かる恐れがある．
 * 	roadWidthはお好みで．
 * @author Qwertzite
 * 
 * 2019/09/12
 */
public class McVillagePlan implements ICityPlan {
	public static final String ROAD_LENGTH_MIN = "road_length_min";
	public static final String ROAD_LENGTH_MAX = "road_length_max";
	public static final String ROAD_LENGTH_VAR = "road_length_var";
	public static final String ROAD_LENGTH_STEP = "road_length_step";
	public static final String ROAD_WIDTH = "road_width";
	public static final String ROAD_BLOCK = "road_block";
	public static final String ROAD_BLOCK_NAME = "block";
	public static final String ROAD_BLOCK_META = "meta";
	public static final String CENTRE_OFFSET = "centre_offset";
	public static final String BORDER = "border";
	public static final String SEPARATION_MIN = "separation_min";
	public static final String SEPARATION_MAX = "separation_max";
	
	public int roadLengthMin = 15;
	public int roadLengthMax = 30;
	public int roadLengthVariance = 10;
	public int roadLengthStep = 3;
	public int roadWidth = 3;
	public IBlockState roadBlockState;
	public int centreRoadOffset = 1;
	
	public int buildingBorder = 3; // 建物と道の隙間の最大値
	public int minSeparation = 1; // 建物同士の隙間
	public int maxSeparation = 2;
	
	public BuildArea area;
	
	public List<StructureBoundingBox> bbList = new LinkedList<>();
	public Deque<Road> pending = new LinkedList<>();
 	
	public McVillagePlan() {
		this.roadBlockState = Blocks.GRAVEL.getDefaultState();
	}

	@Override
	public boolean loadJsonSettings(JsonObject obj, ICommandSender sender) {
		int roadLengthMin = this.roadLengthMin;
		int roadLengthMax = this.roadLengthMax;
		int roadLenghtVariance = this.roadLengthVariance;
		int roadLengthStep = this.roadLengthStep;
		int roadWidth = this.roadWidth;
		IBlockState roadBlockState = this.roadBlockState;
		int centreRoadOffset = this.centreRoadOffset;
		int buildingBorder = this.buildingBorder; // 建物と道の隙間の最大値
		int minSeparation = this.minSeparation; // 建物同士の隙間
		int maxSeparation = this.maxSeparation;
		
		try {
			roadLengthMin = JsonHelper.getInt(obj, ROAD_LENGTH_MIN, roadLengthMin, sender);
			roadLengthMax = JsonHelper.getInt(obj, ROAD_LENGTH_MAX, roadLengthMax, sender);
			roadLenghtVariance = JsonHelper.getInt(obj, ROAD_LENGTH_VAR, roadLenghtVariance, sender);
			roadLengthStep = JsonHelper.getInt(obj, ROAD_LENGTH_STEP, roadLengthStep, sender);
			roadWidth = JsonHelper.getInt(obj, ROAD_WIDTH, roadWidth, sender);
			
			if (obj.has(ROAD_BLOCK)) {
				JsonElement block = obj.get(ROAD_BLOCK);
				if (block.isJsonObject()) {
					JsonObject blockObj = block.getAsJsonObject();
					if (!blockObj.has(ROAD_BLOCK_NAME)) {
						blockObj.addProperty(ROAD_BLOCK_NAME, roadBlockState.getBlock().getRegistryName().toString());
					}
					String name = blockObj.get(ROAD_BLOCK_NAME).getAsString();
					Block b = Block.getBlockFromName(name);
					if (b == null) {
						sender.sendMessage(new TextComponentString("Block " + name + " not found. Aborting generation."));
						ModLog.warn("Block {} not found.", name);
						return false;
					}
					IBlockState state;
					if (blockObj.has(ROAD_BLOCK_META)) {
						int meta = blockObj.getAsJsonPrimitive(ROAD_BLOCK_META).getAsInt();
						@SuppressWarnings("deprecation")
						IBlockState annotation =  b.getStateFromMeta(meta);
						state = annotation;
					} else {
						state = b.getDefaultState();
					}
					roadBlockState = state;
				} else {
					Block b = Block.getBlockFromName(block.getAsString());
					if (b == null) {
						sender.sendMessage(new TextComponentString("Block " + block.getAsString() + " not found. Aborting generation."));
						ModLog.warn("Block {} not found.", block.getAsString());
						return false;
					}
					roadBlockState = b.getDefaultState();
				}
			} else {
				obj.addProperty(ROAD_BLOCK, roadBlockState.getBlock().getRegistryName().toString());
			}
			centreRoadOffset = JsonHelper.getInt(obj, CENTRE_OFFSET, centreRoadOffset, sender);
			buildingBorder = JsonHelper.getInt(obj, BORDER, buildingBorder, sender);
			minSeparation = JsonHelper.getInt(obj, SEPARATION_MIN, minSeparation, sender);
			maxSeparation = JsonHelper.getInt(obj, SEPARATION_MAX, maxSeparation, sender);
		} catch(Exception e) {
			sender.sendMessage(new TextComponentString("Caught an exception while loading json settings for plan. Aborting generation."));
			ModLog.warn("Caught an exception while loading json settings for plan.", e);
			return false;
		}
		
		this.roadLengthMin = roadLengthMin;
		this.roadLengthMax = roadLengthMax;
		this.roadLengthVariance = roadLenghtVariance;
		this.roadLengthStep = roadLengthStep;
		this.roadWidth = roadWidth;
		this.roadBlockState = roadBlockState;
		this.centreRoadOffset = centreRoadOffset;
		this.buildingBorder = buildingBorder; // 建物と道の隙間の最大値
		this.minSeparation = minSeparation; // 建物同士の隙間
		this.maxSeparation = maxSeparation;
		sender.sendMessage(new TextComponentString("Successfully loaded settings from json file."));
		return true;
	}
	
	public boolean processCommandLineOverrides(Map<String, String> args, ICommandSender sender) {
		try {
			if (args.containsKey(ROAD_LENGTH_MIN)) { this.roadLengthMin = StringParseHelper.toInt(args.get(ROAD_LENGTH_MIN), sender, ROAD_LENGTH_MIN); }
			if (args.containsKey(ROAD_LENGTH_MAX)) { this.roadLengthMax = StringParseHelper.toInt(args.get(ROAD_LENGTH_MAX), sender, ROAD_LENGTH_MAX); }
			if (args.containsKey(ROAD_LENGTH_VAR)) { this.roadLengthVariance = StringParseHelper.toInt(args.get(ROAD_LENGTH_VAR), sender, ROAD_LENGTH_VAR); }
			if (args.containsKey(ROAD_LENGTH_STEP)) { this.roadLengthStep = StringParseHelper.toInt(args.get(ROAD_LENGTH_STEP), sender, ROAD_LENGTH_STEP); }
			if (args.containsKey(ROAD_WIDTH)) { this.roadWidth = StringParseHelper.toInt(args.get(ROAD_WIDTH), sender, ROAD_WIDTH); }
			if (args.containsKey(CENTRE_OFFSET)) { this.centreRoadOffset = StringParseHelper.toInt(args.get(CENTRE_OFFSET), sender, CENTRE_OFFSET); }
			if (args.containsKey(BORDER)) { this.buildingBorder = StringParseHelper.toInt(args.get(BORDER), sender, BORDER); }
			if (args.containsKey(SEPARATION_MIN)) { this.minSeparation = StringParseHelper.toInt(args.get(SEPARATION_MIN), sender, SEPARATION_MIN); }
			if (args.containsKey(SEPARATION_MAX)) { this.maxSeparation = StringParseHelper.toInt(args.get(SEPARATION_MAX), sender, SEPARATION_MAX); }
		} catch(Exception e) {
			sender.sendMessage(new TextComponentString("Caught an exception while overriding settings with command line args. Aborting generation."));
			ModLog.warn("Caught an exception while overriding settings.", e);
			return false;
		}
		return true;
	}
	
	@Override
	public String generate(BuildingContext context, BuildArea area) {
		this.area = area;
		long time = System.currentTimeMillis();
		System.out.println("generate");// TODO: パラメータ羅列
		Random rand = context.getRand();
		StructureBoundingBox sbbOrigin = this.generateOriginComponent(context);
		this.bbList.add(sbbOrigin);
		
		int off = this.centreRoadOffset;
		this.createRoad(context, sbbOrigin.minX + off, sbbOrigin.maxZ+1, EnumFacing.SOUTH, rand);
		this.createRoad(context, sbbOrigin.maxX - off, sbbOrigin.minZ-1, EnumFacing.NORTH, rand);
		this.createRoad(context, sbbOrigin.maxX+1    , sbbOrigin.maxZ - off, EnumFacing.EAST, rand);
		this.createRoad(context, sbbOrigin.minX-1    , sbbOrigin.minZ + off, EnumFacing.WEST, rand);
		
		BuildingSupplier supplier = BuildingLoader.getNewSupplier(rand);
		
		while(!this.pending.isEmpty()) {
			Road road = this.pending.poll();
			this.addBuildingsForRoad(context, road, rand, supplier);
		}
		
//		for (StructureBoundingBox sbb : this.bbList) {
//			SbbHelper.markSbbRegion(context, sbb, -2, rand);
//		}
		
		// TODO: 結果表示
		System.out.println("finished generation. " + (System.currentTimeMillis() - time) + "ms");
		System.out.println("generated " + this.bbList.size() + " roads and buildings.");
		return "finished generation. " + (System.currentTimeMillis() - time) + "ms\n"
				+ "generated " + this.bbList.size() + " roads and buildings.";
	}
	
	/**
	 * 	両脇にある建物を生成し，道自身のブロックも設置する.
	 * @param context
	 * @param road
	 */
	private void addBuildingsForRoad(BuildingContext context, Road road, Random rand, BuildingSupplier supplier) {
		boolean hasBuildOne = false;
		boolean hasAngledRoad = false;
		
		// leading side
		for (int pos = rand.nextInt(buildingBorder + 1); pos < road.length; pos += MathHelper.getInt(rand, minSeparation, maxSeparation)) {
			if (!supplier.hasNext()) break;
			final int width = supplier.getNextWidth();
			final int length = supplier.getNextLength();
			StructureBoundingBox sbbc = this.getSbbForLeadingSide(road, pos, 0, width, length, 100);
			if (this.canFitIn(sbbc)) { // 大きい時の処理を入れる
				boolean canBuild = false;
				boolean shouldBreak = false;
				if (pos + width <= road.length) {
					canBuild = true;
				} else if (width > road.length) {
					StructureBoundingBox sbbExt = this.getRoadExtentionSbb(road, pos + width - road.length);
					// これ (sbbExt) が収まらない -> 中止
					if (this.canFitIn(sbbExt)) {
						// 収まる -> さらにroadWidth分伸ばせるか確認
						if (this.tryAdditionalExtention(road.facing, sbbExt)) {
							road.resetRoadLength(Math.max(pos + width + roadWidth, road.length));
						} else {
							road.resetRoadLength(Math.max(pos + width, road.length));
						}
						canBuild = true;
						shouldBreak = true;
					}
				}
        		
				if (canBuild) {
					context.pushMatrix();
					this.generateBuildingForLeadingSide(context, supplier.getNext(), road, pos);
					context.popMatrix();
					pos += width;
					hasBuildOne = true;
					this.bbList.add(sbbc);
				}
				if (shouldBreak) {
					supplier.setNext();
					break;
				}
			}
			supplier.setNext();
		}
        
		// lagging side
		for (int pos = rand.nextInt(buildingBorder + 1); pos < road.length; pos += MathHelper.getInt(rand, minSeparation, maxSeparation)) {
			if (!supplier.hasNext()) break;
			final int width = supplier.getNextWidth();
			final int length = supplier.getNextLength();
			StructureBoundingBox sbbc = this.getSbbForLaggingSide(road, pos, 0, width, length, 100);
			if (this.canFitIn(sbbc)) { // 大きい時の処理を入れる
				boolean canBuild = false;
				boolean shouldBreak = false;
				if (pos + width <= road.length) {
					canBuild = true;
				} else if (width > road.length) {
					StructureBoundingBox sbbExt = this.getRoadExtentionSbb(road, pos + width - road.length);
					// これ (sbbExt) が収まらない -> 中止
					if (this.canFitIn(sbbExt)) {
						// 収まる -> さらにroadWidth分伸ばせるか確認
						if (this.tryAdditionalExtention(road.facing, sbbExt)) {
							road.resetRoadLength(Math.max(pos + width + roadWidth, road.length));
						} else {
							road.resetRoadLength(Math.max(pos + width, road.length));
						}
						canBuild = true;
						shouldBreak = true;
					}
				}
				
				if (canBuild) {
					context.pushMatrix();
					this.generateBuildingForLaggingSide(context, supplier.getNext(), road, pos);
					context.popMatrix();
					pos += width;
					hasBuildOne = true;
					this.bbList.add(sbbc);
				}
				if (shouldBreak) {
					supplier.setNext();
					break;
				}
			}
			supplier.setNext();
		}
		
		// 道を追加
		if (hasBuildOne) {
			switch (road.facing) {
			case EAST:
				hasAngledRoad |= this.createRoad(context, road.posX+road.length-roadWidth, road.posZ+1                    , EnumFacing.SOUTH, rand);
				hasAngledRoad |= this.createRoad(context, road.posX+road.length-1        , road.posZ-roadWidth            , EnumFacing.NORTH, rand);
				break;
			case NORTH:
				hasAngledRoad |= this.createRoad(context, road.posX+1                    , road.posZ-road.length+roadWidth, EnumFacing.EAST, rand);
				hasAngledRoad |= this.createRoad(context, road.posX-roadWidth            , road.posZ-road.length+1        , EnumFacing.WEST, rand);
				break;
			case SOUTH:
				hasAngledRoad |= this.createRoad(context, road.posX-1                    , road.posZ+road.length-roadWidth, EnumFacing.WEST, rand);
				hasAngledRoad |= this.createRoad(context, road.posX+roadWidth            , road.posZ+road.length-1        , EnumFacing.EAST, rand);
				break;
			case WEST:
				hasAngledRoad |= this.createRoad(context, road.posX-road.length+roadWidth, road.posZ-1                    , EnumFacing.NORTH, rand);
				hasAngledRoad |= this.createRoad(context, road.posX-road.length+1        , road.posZ+roadWidth            , EnumFacing.SOUTH, rand);
				break;
			default:
				break;
			}
		}
		
		if (hasBuildOne && !hasAngledRoad) {
			switch(road.facing) {
			case EAST:
				this.createRoad(context, road.posX+road.length, road.posZ            , EnumFacing.EAST, rand);
				break;
			case NORTH:
				this.createRoad(context, road.posX            , road.posZ-road.length, EnumFacing.NORTH, rand);
				break;
			case SOUTH:
				this.createRoad(context, road.posX            , road.posZ+road.length, EnumFacing.SOUTH, rand);
				break;
			case WEST:
				this.createRoad(context, road.posX-road.length, road.posZ            , EnumFacing.WEST, rand);
				break;
			default:
			}
		}
		
		road.generate(context);
	}
	
	private StructureBoundingBox getSbbForLeadingSide(Road road, int pos, int depth, int width, int length, int height) {
		switch(road.facing) {
		case SOUTH: return SbbHelper.create(road.posX  -1, 0, road.posZ+pos, 0, depth, 0, width, height, length, EnumFacing.WEST);
		case EAST:  return SbbHelper.create(road.posX+pos, 0, road.posZ  +1, 0, depth, 0, width, height, length, EnumFacing.SOUTH);
		case NORTH: return SbbHelper.create(road.posX  +1, 0, road.posZ-pos, 0, depth, 0, width, height, length, EnumFacing.EAST);
		case WEST:  return SbbHelper.create(road.posX-pos, 0, road.posZ  -1, 0, depth, 0, width, height, length, EnumFacing.NORTH);
		default:    return null;
		}
	}
	
	private StructureBoundingBox getSbbForLaggingSide(Road road, int pos, int depth, int width, int length, int height) {
		switch(road.facing) {
		case SOUTH: return SbbHelper.create(road.posX+roadWidth  , 0, road.posZ+pos+width-1, 0, depth, 0, width, height, length, EnumFacing.EAST);
		case EAST:  return SbbHelper.create(road.posX+pos+width-1, 0, road.posZ-roadWidth  , 0, depth, 0, width, height, length, EnumFacing.NORTH);
		case NORTH: return SbbHelper.create(road.posX-roadWidth  , 0, road.posZ-pos-width+1, 0, depth, 0, width, height, length, EnumFacing.WEST);
		case WEST:  return SbbHelper.create(road.posX-pos-width+1, 0, road.posZ+roadWidth  , 0, depth, 0, width, height, length, EnumFacing.SOUTH);
		default:    return null;
		}
	}
	
	private void generateBuildingForLeadingSide(BuildingContext context, IBuildingEntry building, Road road, int pos) {
		context.translate(road.posX, 0, road.posZ);
		context.rotate(this.facingToRotation(road.facing));
		context.translate(-1, 0, pos);
		context.rotate(Rotation.CLOCKWISE_90);
		building.generateBuilding(context);
	}
	
	private void generateBuildingForLaggingSide(BuildingContext context, IBuildingEntry building, Road road, int pos) {
		context.translate(road.posX, 0, road.posZ);
		context.rotate(this.facingToRotation(road.facing));
		context.translate(roadWidth, 0, pos + building.getWidth() - 1);
		context.rotate(Rotation.COUNTERCLOCKWISE_90);
		building.generateBuilding(context);
	}
	
	private Rotation facingToRotation(EnumFacing facing) {
		switch(facing) {
		case EAST:  return Rotation.COUNTERCLOCKWISE_90;
		case NORTH: return Rotation.CLOCKWISE_180;
		case SOUTH: return Rotation.NONE;
		case WEST:  return Rotation.CLOCKWISE_90;
		default:    return null;
		}
	}
	
	private StructureBoundingBox getRoadExtentionSbb(Road road, int extLen) {
		StructureBoundingBox sbb = new StructureBoundingBox(road.structureBoundingBox);
		switch(road.facing) {
		case SOUTH:
			sbb.minZ = road.structureBoundingBox.maxZ + 1;
			sbb.maxZ = road.structureBoundingBox.maxZ + extLen;
			break;
		case EAST:
			sbb.minX = road.structureBoundingBox.maxX + 1;
			sbb.maxX = road.structureBoundingBox.maxX + extLen;
			break;
		case NORTH:
			sbb.maxZ = road.structureBoundingBox.minZ - 1;
			sbb.minZ = road.structureBoundingBox.minZ - extLen;
			break;
		case WEST:
			sbb.maxX = road.structureBoundingBox.minX - 1;
			sbb.minX = road.structureBoundingBox.minX - extLen;
			break;
		default: return null;
		}
		return  sbb;
	}
	
	/**
	 * extends the extended structure bounding box and
	 * returns true if the extended road can fit in.
	 * @param roadFacing
	 * @param sbbExt
	 * @return
	 */
	private boolean tryAdditionalExtention(EnumFacing roadFacing, StructureBoundingBox sbbExt) {
		switch(roadFacing) {
		case SOUTH: sbbExt.maxZ += roadWidth;
			break;
		case EAST:  sbbExt.maxX += roadWidth;
			break;
		case NORTH: sbbExt.minZ -= roadWidth;
			break;
		case WEST:  sbbExt.minX -= roadWidth;
			break;
		default: return false;
		}
		return this.canFitIn(sbbExt);
	}
	
	protected StructureBoundingBox generateOriginComponent(BuildingContext context) {
		int off = this.centreRoadOffset;
		int width = this.roadWidth;
		int full = width + off*2;
		int minus = -full/2;
		int plus = minus + full -1;
		context.fill(minus, -1, minus, plus, -1, plus, this.roadBlockState);
		return SbbHelper.create(0, 0, 0, minus, -250, minus, full, 512, full, EnumFacing.UP);
	}
	
	/**
	 * 	適切な長さの道をinstantiateし，
	 * 	待機列に入れ，BBを追加する．
	 * @param context
	 * @param x
	 * @param z
	 * @param facing
	 * @param rand
	 * @return true on success.
	 */
	public boolean createRoad(BuildingContext context, int x, int z, EnumFacing facing, Random rand) {
		Road road = this.getRoadPiece(x, z, facing, rand);
		if (road == null) { return false; }
		this.pending.add(road);
		this.bbList.add(road.structureBoundingBox);
		return true;
	}
	
	/**
	 * 	指定された位置と方位で,長さを変えながら適切な長さの道を作る．
	 * @param x
	 * @param z
	 * @param facing
	 * @param rand
	 * @return 新しい道instance. null if failed.
	 */
	protected Road getRoadPiece(int x, int z, EnumFacing facing, Random rand) {
		for (int length = this.roadLengthMax - rand.nextInt(this.roadLengthVariance);
				length >= roadLengthMin; length -= roadLengthStep) {
			StructureBoundingBox sbb = this.getSBBForRoad(x, 0, z, length, facing);
			if (this.canFitIn(sbb)) {
				return new Road(sbb, x, z, length, this.roadWidth, facing, this.roadBlockState);
			}
		}
		return null;
	}
	
	protected StructureBoundingBox getSBBForRoad(int x, int y, int z, int length, EnumFacing facing) {
		return SbbHelper.create(x, y, z, 0, -250, 0, this.roadWidth, 500, length, facing);
	}
	
	/**
	 * returns true only if none of the existing bounding boxes intersect the given one.
	 * @param sbb
	 * @return whether this structure bounding box can fit in
	 */
	protected boolean canFitIn(StructureBoundingBox sbb) {
		return this.area.canFitIn(sbb) &&!this.bbList.parallelStream().anyMatch(e -> e.intersectsWith(sbb));
	}
}
