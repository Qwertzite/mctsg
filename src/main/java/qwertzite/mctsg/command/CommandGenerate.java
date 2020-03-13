package qwertzite.mctsg.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import qwertzite.mctsg.BuildingLoader;
import qwertzite.mctsg.CityPlanLoader;
import qwertzite.mctsg.MctsgResources;
import qwertzite.mctsg.ModLog;
import qwertzite.mctsg.api.BuildArea;
import qwertzite.mctsg.api.BuildingContext;
import qwertzite.mctsg.api.EnumFillPolicy;
import qwertzite.mctsg.api.ICityPlan;

public class CommandGenerate extends CommandBase {
	public static final String PLAN = "plan";
	public static final String POS = "pos";
	public static final String RANGE = "range";
	public static final String POLICY = "policy";
	public static final String SEED = "seed";
	public static final String WEIGHT = "weight";
	
	@Override
	public String getName() {
		return "tsgGenerate";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/tsgGenerate [plan=planName] [pos=x,y,z] [range=nx,px,nz,pz] [(key=arg)]... [-option]...\n"
				+ "plan=<plan name> (e.g. plan=mc_village)\n"
				+ "pos=x,y,z  (e.g. pos=1,2,3)\n"
				+ "range=nx,nz,px,pz  (e.g. range=-100,-100,100,100)\n"
				+ "seed=<seed>  (e.g. seed=12345, seed=example_string)";
	}

	/**
	 * Get a list of options for when the user presses the TAB key
	 */
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
		Set<String> set = new HashSet<>();
		set.add(PLAN + "=");
		BlockPos pos = sender.getPosition();
		set.add(POS + "=" + pos.getX() + "," + pos.getY() + "," + pos.getZ());
		set.add(RANGE + "=-100,-100,100,100");
		set.add(POLICY + "=");
		set.add(SEED + "=");
		set.add(WEIGHT + "=");
		// TODO:
//		for (String s : args) {
//			String[] pair = s.split("=", 2);
//			if (pair.length == 2) { set.remove(pair[0]); }
//		}
//		List<String> res = new LinkedList<>();
//		for (String s : set) {
//			res.add(s + "=");
//		}
//		return res;
		return new ArrayList<>(set);
	}

	/**
	 * 	位置	, entry, plan, policy.
	 * 	shape
	 * 	size
	 */
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(new TextComponentString("Generating..."));
		// 	引数の読み込み
		Map<String, String> arguments = new HashMap<>();
		for (String s : args) {
			String[] pair = s.split("=", 2);
			if (pair.length != 2) {
				sender.sendMessage(new TextComponentString("Ignoring argument " + s));
			} else {
				arguments.put(pair[0], pair[1]);
			}
		}
		
		// 	配置選択
		String planName = null;
		planName = arguments.get(PLAN);
		if (planName == null) planName = "mc_village";
		ICityPlan cityPlan = CityPlanLoader.getCityPlan(planName);
		if (cityPlan == null) {
			sender.sendMessage(new TextComponentString("Plan " + planName + " not found. Aborting generation.").setStyle((new Style()).setColor(TextFormatting.RED)));
			return;
		}
		sender.sendMessage(new TextComponentString("Using " + planName));
		
		// json 引数を参照して調整
		ModLog.info("Loading plan options from json file...");
		sender.sendMessage(new TextComponentString("Loading json setting file..."));
		JsonObject obj = MctsgResources.getPlanSetting(planName);
		if (!cityPlan.loadJsonSettings(obj, sender)) { return; } // return if failed.
		MctsgResources.savePlanSetting(obj, planName);
		ModLog.info("saved plan options.");
		
		// その他の引数 default
		BlockPos pos = sender.getPosition();
		if (arguments.containsKey(POS)) {
			pos = this.toBlockPos(arguments.get(POS));
			if (pos == null) {
				sender.sendMessage(new TextComponentString(
						"Failed to parse " + arguments.get(POS) + " to BlockPos.\n" +
						"Format: pos=x,y,z  (e.g. pos=1,2,3)").setStyle((new Style()).setColor(TextFormatting.RED)));
				return;
			}
		}
		
		int[] range = new int[] { -100, -100, 100, 100 };
		if (arguments.containsKey(RANGE)) {
			range = this.toRange(arguments.get(RANGE));
			if (range == null) {
				sender.sendMessage(new TextComponentString(
						"Failed to parse " + arguments.get(RANGE) + " to range.\n" +
						"Format: range=nx,nz,px,pz  (nx: negative x, px: positive x.  e.g. range=-100,-100,100,100)").setStyle((new Style()).setColor(TextFormatting.RED)));
				return;
			}
		}
		
		// TODO: fill policy
		EnumFillPolicy policy = EnumFillPolicy.CONTAIN;
		if (arguments.containsKey(POLICY)) {
			policy = this.toPolicy(arguments.get(POLICY));
			if (policy == null) {
				sender.sendMessage(new TextComponentString(
						"Failed to parse " + arguments.get(POLICY) + " to fill policy.\n" +
						"Format: policy=<CONTAIN/COVER>\n" +
						"CONTAIN: Buildings will not extend beyond build range.\n" +
						"COVER: ").setStyle((new Style()).setColor(TextFormatting.RED)));
				return;
			}
		}
		
		long seed = sender.getEntityWorld().rand.nextLong();
		if (arguments.containsKey(SEED)) {
			seed = this.toSeed(arguments.get(SEED));
		}
		
		if (!cityPlan.processCommandLineOverrides(arguments, sender)) { return; } // return if failed.
		
		Object2IntOpenHashMap<String> buildingWeightOverride = new Object2IntOpenHashMap<>();
		if (arguments.containsKey(WEIGHT)) {
			String override = arguments.get(WEIGHT);
			JsonObject jsonObj = MctsgResources.getBuildingWeightOverride(override);
			if (jsonObj == null) {
				sender.sendMessage(new TextComponentString(
						"Failed to load building weight from " + override + "\n" +
						"Format: weight=<filename>.json (e.g. weight=my_custom_weight.json)\n").setStyle((new Style()).setColor(TextFormatting.RED)));
				return;
			}
			for (Map.Entry<String, JsonElement> e : jsonObj.entrySet()) {
				buildingWeightOverride.put(e.getKey(), e.getValue().getAsInt());
			}
		}
		
		World world = sender.getEntityWorld();
		BuildingContext context = new BuildingContext(world, seed);
		context.translate(pos.getX(), pos.getY(), pos.getZ());
		BuildArea area= new BuildArea(policy, range[0], range[1], range[2], range[3]);
		BuildingLoader.weightOverride = buildingWeightOverride;
		
		sender.sendMessage(new TextComponentString("Plan: ").appendText(planName)
				.appendText("\nRange=").appendText(range[0] + "," + range[1] + "," + range[2] + "," + range[3])
				.appendText("\nFill policy=").appendText(policy.name())
				.appendText("\nSeed=").appendText(Long.toString(seed))
				.appendText("\nWeight=").appendText(buildingWeightOverride.isEmpty() ? "default" : arguments.get(WEIGHT)));
		sender.sendMessage(new TextComponentString("Generating a city. Please wait..."));
		String res = cityPlan.generate(context, area);
		sender.sendMessage(new TextComponentString(res).setStyle(new Style().setColor(TextFormatting.AQUA)));
		
		BuildingLoader.weightOverride = Object2IntMaps.emptyMap();
	}
	
	public BlockPos toBlockPos(String arg) {
		String[] strs = arg.split(",");
		try {
			if (strs.length == 3) {
				int x = Integer.parseInt(strs[0]);
				int y = Integer.parseInt(strs[1]);
				int z = Integer.parseInt(strs[2]);
				return new BlockPos(x, y, z);
			}
		} catch(Exception e) {
			ModLog.info("Failed to parse position.", e);
		}
		return null;
	}
	
	public int[] toRange(String arg) {
		String[] strs = arg.split(",");
		try {
			if (strs.length == 4) {
				int nx = Integer.parseInt(strs[0]);
				int nz = Integer.parseInt(strs[1]);
				int px = Integer.parseInt(strs[2]);
				int pz = Integer.parseInt(strs[3]);
				return new int[] { nx, nz, px, pz };
			}
		} catch(Exception e) {
			ModLog.info("Failed to parse range.", e);
		}
		return null;
	}
	
	public EnumFillPolicy toPolicy(String str) {
		try {
			return EnumFillPolicy.valueOf(str.toUpperCase());
		} catch(Exception e) {
			ModLog.info("Failed to parse fill policy.", e);
		}
		return null;
	}
	
	public long toSeed(String str) {
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			
		}
		return str.hashCode();
	}
}
